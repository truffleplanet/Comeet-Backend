package com.backend.common.redis.service;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.backend.common.redis.config.RedisVectorConfig.RedisVectorProperties;
import com.backend.common.redis.dto.VectorSearchResult;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.jedis.json.Path2;
import redis.clients.jedis.search.Document;
import redis.clients.jedis.search.FTCreateParams;
import redis.clients.jedis.search.IndexDataType;
import redis.clients.jedis.search.Query;
import redis.clients.jedis.search.SearchResult;
import redis.clients.jedis.search.schemafields.NumericField;
import redis.clients.jedis.search.schemafields.VectorField;
import redis.clients.jedis.search.schemafields.VectorField.VectorAlgorithm;

/**
 * Redis Vector Search 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisVectorService {

	private final JedisPooled jedis;
	private final RedisVectorProperties properties;

	private static final String KEY_PREFIX = "bean:";

	@PostConstruct
	public void init() {
		createIndexIfNotExists();
	}

	/**
	 * Vector 인덱스 생성 (없을 경우)
	 */
	private void createIndexIfNotExists() {
		try {
			jedis.ftInfo(properties.indexName());
			log.info("Redis Vector index '{}' already exists", properties.indexName());
		} catch (JedisDataException e) {
			if (e.getMessage().contains("Unknown index name")) {
				createIndex();
			} else {
				throw e;
			}
		}
	}

	private void createIndex() {
		log.info("Creating Redis Vector index '{}'", properties.indexName());

		Map<String, Object> vectorAttrs = new HashMap<>();
		vectorAttrs.put("TYPE", "FLOAT32");
		vectorAttrs.put("DIM", properties.dimension());
		vectorAttrs.put("DISTANCE_METRIC", "COSINE");

		FTCreateParams createParams = FTCreateParams.createParams()
			.on(IndexDataType.JSON)
			.prefix(KEY_PREFIX);

		jedis.ftCreate(
			properties.indexName(),
			createParams,
			NumericField.of("$.beanId").as("beanId"),
			VectorField.builder()
				.fieldName("$.embedding")
				.as("embedding")
				.algorithm(VectorAlgorithm.FLAT)
				.attributes(vectorAttrs)
				.build()
		);
		log.info("Redis Vector index '{}' created successfully", properties.indexName());
	}

	/**
	 * 원두 임베딩 저장
	 *
	 * @param beanId    원두 ID
	 * @param embedding 임베딩 벡터
	 */
	public void saveEmbedding(Long beanId, float[] embedding) {
		String key = KEY_PREFIX + beanId;

		BeanEmbeddingData data = new BeanEmbeddingData(beanId, embedding);

		jedis.jsonSet(key, Path2.ROOT_PATH, data);
		log.debug("Saved embedding for bean {}", beanId);
	}

	/**
	 * 임베딩 데이터 DTO
	 */
	private record BeanEmbeddingData(Long beanId, float[] embedding) {
	}

	/**
	 * 원두 임베딩 삭제
	 *
	 * @param beanId 원두 ID
	 */
	public void deleteEmbedding(Long beanId) {
		String key = KEY_PREFIX + beanId;
		jedis.del(key);
		log.debug("Deleted embedding for bean {}", beanId);
	}

	/**
	 * 모든 원두 임베딩 삭제 (drop)
	 *
	 * @return 삭제된 임베딩 수
	 */
	public int dropAllEmbeddings() {
		log.info("Dropping all bean embeddings...");

		int deletedCount = 0;
		String cursor = "0";

		do {
			var scanResult = jedis.scan(cursor, new redis.clients.jedis.params.ScanParams()
				.match(KEY_PREFIX + "*")
				.count(100));
			cursor = scanResult.getCursor();

			for (String key : scanResult.getResult()) {
				jedis.del(key);
				deletedCount++;
			}
		} while (!cursor.equals("0"));

		log.info("Dropped {} bean embeddings", deletedCount);
		return deletedCount;
	}

	/**
	 * 유사도 검색 (KNN)
	 *
	 * @param queryEmbedding 쿼리 임베딩 벡터
	 * @param topK           상위 K개 반환
	 * @return 유사도 높은 순으로 정렬된 결과
	 */
	public List<VectorSearchResult> searchSimilar(float[] queryEmbedding, int topK) {
		byte[] vectorBytes = floatArrayToBytes(queryEmbedding);

		Query query = new Query("*=>[KNN " + topK + " @embedding $vec AS score]")
			.addParam("vec", vectorBytes)
			.setSortBy("score", true)
			.returnFields("beanId", "score")
			.limit(0, topK)
			.dialect(2);

		SearchResult result = jedis.ftSearch(properties.indexName(), query);

		List<VectorSearchResult> results = new ArrayList<>();
		for (Document doc : result.getDocuments()) {
			Long beanId = Long.parseLong(doc.getString("beanId"));
			Double score = Double.parseDouble(doc.getString("score"));
			// Cosine similarity로 변환 (score가 거리일 경우 1 - score)
			results.add(new VectorSearchResult(beanId, 1 - score));
		}

		log.debug("Found {} similar beans", results.size());
		return results;
	}

	/**
	 * 특정 beanId 목록에서만 유사도 검색
	 *
	 * @param queryEmbedding 쿼리 임베딩 벡터
	 * @param beanIds        검색 대상 beanId 목록
	 * @param topK           상위 K개 반환
	 * @return 유사도 높은 순으로 정렬된 결과
	 */
	public List<VectorSearchResult> searchSimilarInBeans(float[] queryEmbedding, List<Long> beanIds, int topK) {
		if (beanIds == null || beanIds.isEmpty()) {
			return new ArrayList<>();
		}

		byte[] vectorBytes = floatArrayToBytes(queryEmbedding);

		// beanId 필터 조건 생성
		String beanIdFilter = beanIds.stream()
			.map(id -> "@beanId:[" + id + " " + id + "]")
			.reduce((a, b) -> a + " | " + b)
			.orElse("*");

		Query query = new Query("(" + beanIdFilter + ")=>[KNN " + topK + " @embedding $vec AS score]")
			.addParam("vec", vectorBytes)
			.setSortBy("score", true)
			.returnFields("beanId", "score")
			.limit(0, topK)
			.dialect(2);

		SearchResult result = jedis.ftSearch(properties.indexName(), query);

		List<VectorSearchResult> results = new ArrayList<>();
		for (Document doc : result.getDocuments()) {
			Long beanId = Long.parseLong(doc.getString("beanId"));
			Double score = Double.parseDouble(doc.getString("score"));
			results.add(new VectorSearchResult(beanId, 1 - score));
		}

		log.debug("Found {} similar beans from {} candidates", results.size(), beanIds.size());
		return results;
	}

	/**
	 * 임베딩 존재 여부 확인
	 *
	 * @param beanId 원두 ID
	 * @return 존재 여부
	 */
	public boolean existsEmbedding(Long beanId) {
		String key = KEY_PREFIX + beanId;
		return jedis.exists(key);
	}

	/**
	 * float[] to byte[] 변환 (Little Endian)
	 */
	private byte[] floatArrayToBytes(float[] floats) {
		ByteBuffer buffer = ByteBuffer.allocate(floats.length * 4).order(ByteOrder.LITTLE_ENDIAN);
		for (float f : floats) {
			buffer.putFloat(f);
		}
		return buffer.array();
	}
}
