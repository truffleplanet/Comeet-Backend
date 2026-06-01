package com.backend.common.ai.service;

import java.util.List;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.stereotype.Service;

import com.backend.common.ai.exception.AiServiceException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * OpenAI Embedding 서비스 (Spring AI)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingService {

	private final EmbeddingModel embeddingModel;

	/**
	 * 텍스트를 임베딩 벡터로 변환
	 *
	 * @param text 임베딩할 텍스트
	 * @return 임베딩 벡터
	 */
	public float[] embed(String text) {
		log.debug("[Embedding] 텍스트 임베딩 요청 - {}", text);

		try {
			EmbeddingResponse response = embeddingModel.embedForResponse(List.of(text));

			if (response == null || response.getResults().isEmpty()) {
				log.error("[Embedding] 응답 비어있음");
				return new float[1536];
			}

			return response.getResults().get(0).getOutput();
		} catch (RuntimeException e) {
			log.error("[Embedding] 텍스트 임베딩 실패", e);
			throw new AiServiceException("Embedding failed", e);
		}
	}

	/**
	 * 태그 리스트를 임베딩 벡터로 변환
	 *
	 * @param tags 플레이버 태그 리스트
	 * @return 임베딩 벡터
	 */
	public float[] embedTags(List<String> tags) {
		if (tags == null || tags.isEmpty()) {
			log.warn("[Embedding] 태그 목록 비어있음");
			return new float[1536];
		}
		String text = String.join(", ", tags);
		return embed(text);
	}

	/**
	 * 여러 텍스트를 배치로 임베딩
	 *
	 * @param texts 임베딩할 텍스트 리스트
	 * @return 임베딩 벡터 리스트
	 */
	public List<float[]> embedBatch(List<String> texts) {
		log.debug("[Embedding] 배치 임베딩 요청 - {}건", texts.size());

		try {
			EmbeddingResponse response = embeddingModel.embedForResponse(texts);

			if (response == null || response.getResults() == null) {
				log.error("[Embedding] 배치 응답 비어있음");
				return List.of();
			}

			return response.getResults().stream()
				.map(result -> result.getOutput())
				.toList();
		} catch (RuntimeException e) {
			log.error("[Embedding] 배치 임베딩 실패", e);
			throw new AiServiceException("Batch embedding failed", e);
		}
	}
}
