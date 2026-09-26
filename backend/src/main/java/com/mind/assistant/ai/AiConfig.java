package com.mind.assistant.ai;

import com.mind.assistant.ai.MockStreamingChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

/**
 * AI 客户端配置（重要兜底逻辑）：
 * - 若 spring.ai.openai.api-key 为空，或仍为 sk-xxx 占位符，
 *   则不创建真实 ChatClient，改用内置 MockStreamingChatModel（同样支持流式），
 *   保证本地无 API Key 也能完整跑通；
 * - 否则基于 OpenAiChatModel 创建真实 ChatClient
 *   （base-url 可配置，兼容 DeepSeek 等 OpenAI 兼容接口）。
 */
@Configuration
public class AiConfig {

    @Bean
    public ChatClient chatClient(Environment env, ObjectProvider<ChatModel> chatModelProvider) {
        String apiKey = env.getProperty("spring.ai.openai.api-key", "");
        boolean mockMode = !StringUtils.hasText(apiKey) || apiKey.startsWith("sk-xxx");

        ChatModel chatModel = mockMode ? null : chatModelProvider.getIfAvailable();
        if (chatModel == null) {
            // Mock 模式：内置流式模型
            return ChatClient.builder(new MockStreamingChatModel()).build();
        }
        // 真实模式：OpenAI 兼容模型
        return ChatClient.builder(chatModel).build();
    }
}
