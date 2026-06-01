package com.backend.domain.ai.repository;

import java.util.concurrent.TimeUnit;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.backend.common.error.ErrorCode;
import com.backend.common.error.exception.BusinessException;
import com.backend.domain.ai.entity.BatchProgress;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class BatchProgressRepository {

	private static final String KEY_PREFIX = "batch:progress:";
	private static final long TTL_HOURS = 24;

	private final RedisTemplate<String, Object> redisTemplate;

	public void save(final BatchProgress progress) {
		try {
			String key = KEY_PREFIX + progress.getBatchId();
			redisTemplate.opsForValue().set(key, progress, TTL_HOURS, TimeUnit.HOURS);
			log.debug("[Batch Progress] 저장 완료 - batchId: {}, progress: {}/{}",
				progress.getBatchId(), progress.getCompleted().get(), progress.getTotal());
		} catch (DataAccessException e) {
			log.error("[Batch Progress] 저장 실패 - batchId: {}", progress.getBatchId(), e);
			throw new BusinessException(ErrorCode.DATABASE_ERROR);
		}
	}

	public BatchProgress findById(final String batchId) {
		try {
			String key = KEY_PREFIX + batchId;
			Object value = redisTemplate.opsForValue().get(key);

			if (value instanceof BatchProgress batchProgress) {
				return batchProgress;
			}
			throw new BusinessException(ErrorCode.BATCH_NOT_FOUND);
		} catch (DataAccessException e) {
			log.error("[Batch Progress] 조회 실패 - batchId: {}", batchId, e);
			throw new BusinessException(ErrorCode.DATABASE_ERROR);
		}
	}

	public void update(final BatchProgress progress) {
		save(progress);
	}

	public void deleteById(final String batchId) {
		try {
			String key = KEY_PREFIX + batchId;
			redisTemplate.delete(key);
			log.debug("[Batch Progress] 삭제 완료 - batchId: {}", batchId);
		} catch (DataAccessException e) {
			log.error("[Batch Progress] 삭제 실패 - batchId: {}", batchId, e);
		}
	}
}
