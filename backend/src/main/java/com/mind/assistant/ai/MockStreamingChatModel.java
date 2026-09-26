package com.mind.assistant.ai;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.model.StreamingChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 内置 Mock 流式模型：
 * 当未配置真实 AI API Key（或为 sk-xxx 占位符）时使用，
 * 模拟真实的流式回复行为（每 30-80ms 输出一小段文本），
 * 保证本地无 API Key 也能完整跑通前端联调。
 */
public class MockStreamingChatModel implements ChatModel, StreamingChatModel {

    /** 关键词 -> 预置共情回复 */
    private static final List<String[]> KEYWORD_REPLIES = List.of(
            new String[]{"失眠", "睡眠",
                    "听起来睡眠问题一直困扰着你，这真的很辛苦。长期睡不好，白天的精力和情绪都会受到影响，你能觉察到这一点，已经很不容易了。"
                        + "这里有几个小建议供你参考：睡前一小时远离手机等电子屏幕，让大脑逐渐安静下来；可以尝试固定的睡前仪式，比如热水泡脚、听舒缓的音乐或做几组缓慢的深呼吸；"
                        + "白天尽量保持规律作息，避免午睡过久。如果失眠持续两周以上，建议到医院睡眠专科或心理科咨询，专业的帮助会更有效。"},
            new String[]{"焦虑", "紧张", "压力大", "压力",
                    "谢谢你愿意把这些说出来。焦虑其实是身体在提醒我们：有些事情对我们很重要，只是暂时超出了我们能轻松应对的范围，这份感受是完全可以被理解的。"
                        + "当你感到紧张时，可以试试「4-7-8 呼吸法」：吸气 4 秒、屏息 7 秒、缓慢呼气 8 秒，重复几轮，能帮助身体先平静下来；"
                        + "也可以把担心的事情一件件写下来，区分「能控制的」和「不能控制的」，把注意力放在能做的小事上。记得照顾好饮食和睡眠，如果焦虑已经影响生活，寻求心理咨询师的帮助是很勇敢的选择。"},
            new String[]{"难过", "伤心", "低落", "抑郁", "孤独",
                    "听到你正在经历这些，我很心疼。情绪低落的时候，整个世界好像都蒙上了一层灰色，那种疲惫和无助是真实存在的，请先允许自己难过，不必强撑。"
                        + "可以试着做一件小小的、能让自己舒服的事：晒晒太阳、听喜欢的歌、给信任的朋友发条消息；也可以用写日记的方式把情绪倾倒出来。"
                        + "请记住，情绪是会流动的，再漫长的低谷也会有走出去的那天。如果这种状态持续超过两周，或出现了伤害自己的念头，请一定及时联系专业心理援助热线（如全国心理援助热线 12356）或就医，你值得被认真对待。"},
            new String[]{"学习", "考试", "工作", "加班",
                    "能感受到你背负着不小的压力，无论是学习还是工作，长期紧绷的弦都需要适时放松。请先肯定自己：愿意面对压力本身，就说明你在努力生活。"
                        + "建议试着把大任务拆解成一个个小目标，每完成一个就给自己一点正反馈；同时保证规律的运动和睡眠，它们是情绪最好的稳定剂。"
                        + "如果压力让你出现了明显的躯体不适或情绪崩溃，别硬扛，及时向学校心理中心、公司 EAP 或专业咨询师求助。"},
            new String[]{"感情", "失恋", "分手", "家人", "朋友",
                    "关系的失去或冲突带来的痛，往往比我们想象中更深，你的难过说明你曾真心付出过，这并不可耻。"
                        + "给自己一段哀悼的时间吧，不必急着「想开」。可以写下想对对方说的话，也可以和信任的人聊聊，让情绪有出口。"
                        + "同时试着把注意力慢慢拉回自己身上：好好吃饭、散步、做喜欢的事。时间加上自我照顾，会让伤口慢慢愈合。如果情绪长期走不出来，专业的心理咨询会是一个很好的支持。"}
    );

    /** 默认通用共情回复 */
    private static final String DEFAULT_REPLY =
            "谢谢你愿意和我说这些，我会认真听。每一种情绪的出现都有它的原因，无论是烦恼、疲惫还是迷茫，它们都是你内心真实的信号，值得被温柔对待。"
                + "在这里，你可以放心地把感受说出来，不必评判自己。你也可以试着记录最近的心情、做一次心理小测评，或者先从这些小事开始照顾自己："
                + "保证充足的睡眠、每天留 10 分钟安静地散散步、和信任的人聊一聊。如果此刻你感到强烈的痛苦或无助，请务必拨打心理援助热线 12356 寻求专业支持。"
                + "我会一直在这里陪着你，慢慢来，没关系。";

    /**
     * 默认选项：Mock 模型无特殊选项，返回 null 即可
     */
    @Override
    public ChatOptions getDefaultOptions() {
        return null;
    }

    /**
     * 同步调用（ChatClient 非流式场景）：一次性返回完整回复
     */
    @Override
    public ChatResponse call(Prompt prompt) {
        return buildChunk(extractUserText(prompt));
    }

    /**
     * 流式调用：把回复按小块输出，每块延迟 30-80ms，模拟真实打字机效果
     */
    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {
        String text = mockReply(extractUserText(prompt));
        // 中文按 2 个字符一组切分，模拟逐词输出
        List<String> chunks = splitChunks(text, 2);
        return Flux.fromIterable(chunks)
                .concatMap(chunk -> Mono.delay(Duration.ofMillis(
                                ThreadLocalRandom.current().nextLong(30, 80)))
                        .map(t -> buildChunk(chunk)));
    }

    /**
     * 根据用户输入中的关键词挑选预置回复
     */
    private String mockReply(String userText) {
        if (userText != null) {
            for (String[] entry : KEYWORD_REPLIES) {
                for (int i = 0; i < entry.length - 1; i++) {
                    if (userText.contains(entry[i])) {
                        return entry[entry.length - 1];
                    }
                }
            }
        }
        return DEFAULT_REPLY;
    }

    /**
     * 拼接 Prompt 中最后一条用户消息文本（用于关键词匹配）
     */
    private String extractUserText(Prompt prompt) {
        if (prompt == null || prompt.getInstructions() == null || prompt.getInstructions().isEmpty()) {
            return "";
        }
        return prompt.getInstructions().get(prompt.getInstructions().size() - 1).getContent();
    }

    /**
     * 构造单块 ChatResponse
     */
    private ChatResponse buildChunk(String text) {
        return new ChatResponse(List.of(new Generation(new AssistantMessage(text))));
    }

    /**
     * 把文本按指定长度切成小块
     */
    private List<String> splitChunks(String text, int size) {
        StringBuilder sb = new StringBuilder();
        List<String> chunks = new java.util.ArrayList<>();
        for (char c : text.toCharArray()) {
            sb.append(c);
            if (sb.length() >= size) {
                chunks.add(sb.toString());
                sb.setLength(0);
            }
        }
        if (sb.length() > 0) {
            chunks.add(sb.toString());
        }
        return chunks;
    }
}
