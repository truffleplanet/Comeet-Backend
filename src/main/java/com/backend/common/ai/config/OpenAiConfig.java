package com.backend.common.ai.config;

import java.io.IOException;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.io.Resource;

import lombok.extern.slf4j.Slf4j;

/**
 * OpenAI API 설정 (Spring AI)
 */
@Slf4j
@Configuration
public class OpenAiConfig {

	@Value("classpath:/prompts/rerank_system_prompt.txt")
	private Resource rerankSystemPrompt;

	/**
	 * ChatClient 빈 설정
	 */
	@Bean
	ChatClient openAiChatClient(@Qualifier("openAiChatModel") ChatModel chatModel) throws IOException {
		var loggerAdvisor = SimpleLoggerAdvisor.builder()
			.order(Ordered.LOWEST_PRECEDENCE - 1)
			.build();

		return ChatClient.builder(chatModel)
			.defaultSystem(rerankSystemPrompt)
			.defaultOptions(OpenAiChatOptions.builder()
				.maxCompletionTokens(4096)
				.build())
			.defaultAdvisors(loggerAdvisor)
			.build();
	}
}
