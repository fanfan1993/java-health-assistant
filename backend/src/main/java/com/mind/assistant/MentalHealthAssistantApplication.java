package com.mind.assistant;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * AI 心理健康助手后端启动类
 */
@SpringBootApplication
@MapperScan("com.mind.assistant")
public class MentalHealthAssistantApplication {

    public static void main(String[] args) {
        SpringApplication.run(MentalHealthAssistantApplication.class, args);
    }
}
