package com.backend.common.ai.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.backend.common.ai.dto.MenuRerankRequest;
import com.backend.common.ai.dto.MenuRerankResponse;
import com.backend.common.ai.dto.RerankRequest;
import com.backend.common.ai.dto.RerankResponse;
import com.backend.common.ai.exception.AiServiceException;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * LLM 기반 리랭킹 서비스 (Spring AI + OpenAI)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LlmService {

	private final ChatClient openAiChatClient;

	@Value("classpath:prompts/bean_rerank_prompt.txt")
	private Resource beanRerankPromptResource;

	@Value("classpath:prompts/menu_rerank_prompt.txt")
	private Resource menuRerankPromptResource;

	private String beanRerankPromptTemplate;
	private String menuRerankPromptTemplate;

	@PostConstruct
	void init() throws IOException {
		this.beanRerankPromptTemplate = beanRerankPromptResource.getContentAsString(StandardCharsets.UTF_8);
		this.menuRerankPromptTemplate = menuRerankPromptResource.getContentAsString(StandardCharsets.UTF_8);
		log.info("[LLM] 리랭킹 프롬프트 템플릿 로드 완료");
	}

	/**
	 * 후보 원두들을 리랭킹하여 Top 5 추천
	 *
	 * @param request 사용자 취향 및 후보 원두 정보
	 * @return 리랭킹된 Top 5 추천 결과
	 */
	public RerankResponse rerank(RerankRequest request) {
		log.debug("[LLM] 원두 리랭킹 요청 - 후보: {}건", request.candidates().size());

		String candidatesText = formatCandidates(request);

		var outputConverter = new BeanOutputConverter<>(RerankResponse.class);

		String userPrompt = beanRerankPromptTemplate
			.replace("{prefAcidity}", String.valueOf(request.userPreference().prefAcidity()))
			.replace("{prefBody}", String.valueOf(request.userPreference().prefBody()))
			.replace("{prefSweetness}", String.valueOf(request.userPreference().prefSweetness()))
			.replace("{prefBitterness}", String.valueOf(request.userPreference().prefBitterness()))
			.replace("{preferredRoastLevels}", String.join(", ", request.userPreference().preferredRoastLevels()))
			.replace("{likedTags}", String.join(", ", request.userPreference().likedTags()))
			.replace("{candidates}", candidatesText)
			.replace("{format}", outputConverter.getFormat());

		try {
			String response = openAiChatClient.prompt()
				.user(userPrompt)
				.call()
				.content();

			log.debug("[LLM] 원두 리랭킹 응답 - {}", response);

			return outputConverter.convert(response);
		} catch (RuntimeException e) {
			log.error("[LLM] 원두 리랭킹 실패", e);
			throw new AiServiceException("Bean reranking failed", e);
		}
	}

	private String formatCandidates(RerankRequest request) {
		StringBuilder sb = new StringBuilder();
		int index = 1;
		for (RerankRequest.CandidateInfo candidate : request.candidates()) {
			sb.append(String.format(
				"%d. [ID:%d] %s (%s, %s)\n   - 산미:%d, 바디:%d, 단맛:%d, 쓴맛:%d\n   - 플레이버: %s\n   - 유사도: %.2f\n\n",
				index++,
				candidate.beanId(),
				candidate.beanName(),
				candidate.roasteryName(),
				candidate.roastLevel(),
				candidate.acidity(),
				candidate.body(),
				candidate.sweetness(),
				candidate.bitterness(),
				String.join(", ", candidate.flavorTags()),
				candidate.similarityScore()
			));
		}
		return sb.toString();
	}

	/**
	 * 후보 메뉴들을 리랭킹하여 Top 5 추천
	 *
	 * @param request 사용자 취향 및 후보 메뉴 정보
	 * @return 리랭킹된 Top 5 메뉴 추천 결과
	 */
	public MenuRerankResponse rerankMenus(MenuRerankRequest request) {
		log.debug("[LLM] 메뉴 리랭킹 요청 - 후보: {}건", request.menuCandidates().size());

		String candidatesText = formatMenuCandidates(request);

		var outputConverter = new BeanOutputConverter<>(MenuRerankResponse.class);

		String userPrompt = menuRerankPromptTemplate
			.replace("{prefAcidity}", String.valueOf(request.userPreference().prefAcidity()))
			.replace("{prefBody}", String.valueOf(request.userPreference().prefBody()))
			.replace("{prefSweetness}", String.valueOf(request.userPreference().prefSweetness()))
			.replace("{prefBitterness}", String.valueOf(request.userPreference().prefBitterness()))
			.replace("{preferredRoastLevels}", String.join(", ", request.userPreference().preferredRoastLevels()))
			.replace("{likedTags}", String.join(", ", request.userPreference().likedTags()))
			.replace("{candidates}", candidatesText)
			.replace("{format}", outputConverter.getFormat());

		try {
			String response = openAiChatClient.prompt()
				.user(userPrompt)
				.call()
				.content();

			log.debug("[LLM] 메뉴 리랭킹 응답 - {}", response);

			return outputConverter.convert(response);
		} catch (RuntimeException e) {
			log.error("[LLM] 메뉴 리랭킹 실패", e);
			throw new AiServiceException("Menu reranking failed", e);
		}
	}

	private String formatMenuCandidates(MenuRerankRequest request) {
		StringBuilder sb = new StringBuilder();
		int index = 1;
		for (RerankRequest.MenuCandidateInfo candidate : request.menuCandidates()) {
			sb.append(String.format(
				"%d. [메뉴ID:%d] %s\n" +
					"   - 메뉴 설명: %s\n" +
					"   - 사용 원두: %s (%s, %s)\n" +
					"   - 원두 플레이버: %s\n" +
					"   - 산미:%d, 바디:%d, 단맛:%d, 쓴맛:%d\n" +
					"   - 유사도: %.2f\n\n",
				index++,
				candidate.menuId(),
				candidate.menuName(),
				candidate.menuDescription() != null ? candidate.menuDescription() : "설명 없음",
				candidate.beanName(),
				candidate.roasteryName() != null ? candidate.roasteryName() : "로스터리 미상",
				candidate.roastLevel(),
				String.join(", ", candidate.flavorTags()),
				candidate.acidity(),
				candidate.body(),
				candidate.sweetness(),
				candidate.bitterness(),
				candidate.similarityScore()
			));
		}
		return sb.toString();
	}
}
