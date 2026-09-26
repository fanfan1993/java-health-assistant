package com.mind.assistant.controller;
import com.mind.assistant.entity.ChatMessage;
import com.mind.assistant.service.ChatService;
import com.mind.assistant.entity.ChatSession;
import com.mind.assistant.security.JwtInterceptor;


import com.mind.assistant.common.Result;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

/**
 * AI 对话接口：会话管理 + SSE 流式对话
 */
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * 当前用户会话列表：GET /api/chat/sessions
     */
    @GetMapping("/sessions")
    public Result<List<ChatSession>> sessions() {
        return Result.success(chatService.listSessions());
    }

    /**
     * 新建会话：POST /api/chat/session（title 可选，默认「新的对话」）
     */
    @PostMapping("/session")
    public Result<ChatSession> createSession(@RequestBody(required = false) Map<String, String> body) {
        String title = body == null ? null : body.get("title");
        return Result.success(chatService.createSession(title));
    }

    /**
     * 删除会话：DELETE /api/chat/session/{id}
     */
    @DeleteMapping("/session/{id}")
    public Result<Void> deleteSession(@PathVariable Long id) {
        chatService.deleteSession(id);
        return Result.success();
    }

    /**
     * 会话消息列表：GET /api/chat/session/{id}/messages
     */
    @GetMapping("/session/{id}/messages")
    public Result<List<ChatMessage>> messages(@PathVariable Long id) {
        return Result.success(chatService.listMessages(id));
    }

    /**
     * SSE 流式对话：GET /api/chat/stream?sessionId=1&message=你好
     * 说明：EventSource 不支持自定义请求头，token 可通过 ?token= 传递（JwtInterceptor 已兼容）
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestParam Long sessionId, @RequestParam String message) {
        SseEmitter emitter = new SseEmitter(5 * 60 * 1000L); // 超时 5 分钟
        chatService.streamChat(sessionId, message, emitter);
        return emitter;
    }
}
