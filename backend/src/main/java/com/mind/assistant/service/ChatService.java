package com.mind.assistant.service;
import com.mind.assistant.entity.ChatMessage;
import com.mind.assistant.mapper.ChatMessageMapper;
import com.mind.assistant.entity.ChatSession;
import com.mind.assistant.mapper.ChatSessionMapper;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mind.assistant.security.UserContext;
import com.mind.assistant.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.Disposable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * AI 对话服务：会话管理 + SSE 流式对话
 */
@Slf4j
@Service
public class ChatService {

    /** 系统提示词：心理支持助手「小暖」 */
    private static final String SYSTEM_PROMPT =
            "你是「小暖」，一位温暖、专业、耐心的中文心理健康支持助手。"
                    + "请遵守以下原则："
                    + "1) 始终以共情、接纳的语气回应，先肯定和承接对方的感受，再给出温和的建议；"
                    + "2) 你不是医生，不做任何精神疾病诊断，不随意下结论，不推荐药物；"
                    + "3) 遇到自伤、自杀等危机信号时，第一时间表达关心，并建议对方立即拨打心理援助热线 12356 或前往医院精神心理科求助；"
                    + "4) 回复使用简体中文，语气温柔自然，篇幅适中，避免说教。";

    /** 作为对话上下文的历史消息条数 */
    private static final int HISTORY_LIMIT = 10;

    private final ChatSessionMapper sessionMapper;
    private final ChatMessageMapper messageMapper;
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** SSE 推送专用线程池 */
    private final ExecutorService sseExecutor = Executors.newCachedThreadPool();

    public ChatService(ChatSessionMapper sessionMapper, ChatMessageMapper messageMapper, ChatClient chatClient) {
        this.sessionMapper = sessionMapper;
        this.messageMapper = messageMapper;
        this.chatClient = chatClient;
    }

    // ==================== 会话管理 ====================

    /**
     * 当前用户会话列表（按更新时间倒序）
     */
    public List<ChatSession> listSessions() {
        return sessionMapper.selectList(new QueryWrapper<ChatSession>()
                .eq("user_id", UserContext.getUserId())
                .orderByDesc("update_time"));
    }

    /**
     * 新建会话（默认标题「新的对话」）
     */
    public ChatSession createSession(String title) {
        ChatSession session = new ChatSession();
        session.setUserId(UserContext.getUserId());
        session.setTitle(StringUtils.hasText(title) ? title : "新的对话");
        sessionMapper.insert(session);
        return session;
    }

    /**
     * 删除会话（校验归属），同时删除会话内所有消息
     */
    public void deleteSession(Long sessionId) {
        ChatSession session = getOwnedSession(sessionId);
        sessionMapper.deleteById(session.getId());
        messageMapper.delete(new QueryWrapper<ChatMessage>().eq("session_id", sessionId));
    }

    /**
     * 查询会话消息列表（按时间正序）
     */
    public List<ChatMessage> listMessages(Long sessionId) {
        getOwnedSession(sessionId);
        return messageMapper.selectList(new QueryWrapper<ChatMessage>()
                .eq("session_id", sessionId)
                .orderByAsc("create_time"));
    }

    // ==================== SSE 流式对话 ====================

    /**
     * 流式对话核心逻辑：
     * 1. 校验会话归属，保存用户消息；
     * 2. 取最近 10 条历史消息作为上下文；
     * 3. 通过 ChatClient 流式调用，逐 chunk 通过 SSE 推送 {delta: "..."}；
     * 4. 结束后拼接完整回复保存为 assistant 消息，发送 [DONE] 并 complete。
     */
    public void streamChat(Long sessionId, String message, SseEmitter emitter) {
        Long userId = UserContext.getUserId();
        // 校验会话归属
        ChatSession session = getOwnedSession(sessionId);

        // 保存用户消息，并刷新会话更新时间
        ChatMessage userMsg = new ChatMessage();
        userMsg.setSessionId(sessionId);
        userMsg.setRole("user");
        userMsg.setContent(message);
        messageMapper.insert(userMsg);
        session.setUpdateTime(LocalDateTime.now());
        sessionMapper.updateById(session);

        // 最近 10 条历史消息（先取倒序最近 10 条，再反转为正序）
        List<ChatMessage> history = messageMapper.selectList(new QueryWrapper<ChatMessage>()
                .eq("session_id", sessionId)
                .orderByDesc("create_time")
                .last("limit " + HISTORY_LIMIT));
        java.util.Collections.reverse(history);

        // 构建 Spring AI 消息列表（不含当前这条新消息，最后通过 .user() 传入）
        List<Message> contextMessages = new ArrayList<>();
        for (ChatMessage msg : history) {
            if ("user".equals(msg.getRole())) {
                contextMessages.add(new UserMessage(msg.getContent()));
            } else if ("assistant".equals(msg.getRole())) {
                contextMessages.add(new AssistantMessage(msg.getContent()));
            }
        }

        // 在独立线程中执行流式调用，不阻塞 Tomcat 请求线程
        sseExecutor.execute(() -> doStream(userId, sessionId, message, contextMessages, emitter));
    }

    /**
     * 执行流式调用与 SSE 推送
     */
    private void doStream(Long userId, Long sessionId, String message,
                          List<Message> contextMessages, SseEmitter emitter) {
        StringBuilder fullReply = new StringBuilder();
        Disposable disposable = chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .messages(contextMessages)
                .user(message)
                .stream()
                .content() // Flux<String>：AI 回复文本的增量片段
                .doOnNext(delta -> {
                    fullReply.append(delta);
                    sendDelta(emitter, delta);
                })
                .doOnComplete(() -> finishStream(userId, sessionId, fullReply.toString(), emitter))
                .doOnError(e -> {
                    log.error("AI 流式调用失败, sessionId={}", sessionId, e);
                    emitter.completeWithError(e);
                })
                .subscribe();

        // 客户端断开 / 超时时取消订阅，避免资源泄漏
        emitter.onCompletion(disposable::dispose);
        emitter.onTimeout(disposable::dispose);
        emitter.onError(t -> disposable.dispose());
    }

    /**
     * 推送单个增量片段，事件名 message，data 为 JSON：{delta: "..."}
     */
    private void sendDelta(SseEmitter emitter, String delta) {
        try {
            String json = objectMapper.writeValueAsString(
                    java.util.Map.of("delta", delta));
            emitter.send(SseEmitter.event().name("message").data(json));
        } catch (Exception e) {
            log.warn("SSE 推送失败，客户端可能已断开: {}", e.getMessage());
        }
    }

    /**
     * 流结束：保存完整 assistant 回复 -> 推送 [DONE] -> complete
     */
    private void finishStream(Long userId, Long sessionId, String reply, SseEmitter emitter) {
        try {
            if (StringUtils.hasText(reply)) {
                ChatMessage aiMsg = new ChatMessage();
                aiMsg.setSessionId(sessionId);
                aiMsg.setRole("assistant");
                aiMsg.setContent(reply);
                messageMapper.insert(aiMsg);
            }
            emitter.send(SseEmitter.event().data("[DONE]"));
            emitter.complete();
        } catch (Exception e) {
            log.warn("SSE 收尾失败: {}", e.getMessage());
        }
    }

    /**
     * 获取属于当前用户的会话，不存在或无权限时抛业务异常
     */
    private ChatSession getOwnedSession(Long sessionId) {
        ChatSession session = sessionMapper.selectById(sessionId);
        if (session == null || !session.getUserId().equals(UserContext.getUserId())) {
            throw new BizException("会话不存在或无权访问");
        }
        return session;
    }
}
