package com.backend.domain.store.service.command;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.common.error.ErrorCode;
import com.backend.common.error.exception.BusinessException;
import com.backend.domain.store.entity.Store;
import com.backend.domain.store.mapper.command.StoreCommandMapper;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreCommandServiceImpl implements StoreCommandService {

	private final StoreCommandMapper storeCommandMapper;

	@Override
	public Store createStore(final Store store) {
		storeCommandMapper.save(store);
		log.info("[Store] 가맹점 생성 완료 - ID: {}", store.getId());
		return store;
	}

	@Override
	public Store updateStore(final Store store) {
		int affectedRows = storeCommandMapper.update(store);

		if (affectedRows == 0) {
			log.error("[Store] 가맹점 수정 실패 - ID: {}", store.getId());
			throw new BusinessException(ErrorCode.STORE_NOT_FOUND);
		}

		log.info("[Store] 가맹점 수정 완료 - ID: {}", store.getId());
		return store;
	}

	@Override
	public void deleteStore(final Long storeId) {
		int affectedRows = storeCommandMapper.softDelete(storeId);

		if (affectedRows == 0) {
			log.error("[Store] 가맹점 삭제 실패 - ID: {}", storeId);
			throw new BusinessException(ErrorCode.STORE_NOT_FOUND);
		}

		log.info("[Store] 가맹점 삭제 완료 - ID: {}", storeId);
	}

	@Override
	public void updateRatingStats(final Long storeId, final BigDecimal averageRating, final Integer reviewCount) {
		int affectedRows = storeCommandMapper.updateRatingStats(storeId, averageRating, reviewCount);

		if (affectedRows == 0) {
			log.warn("[Store] 평점 업데이트 실패 - storeId: {}", storeId);
			throw new BusinessException(ErrorCode.STORE_NOT_FOUND);
		}

		log.info("[Store] 평점 업데이트 완료 - storeId: {}, averageRating: {}, reviewCount: {}",
			storeId, averageRating, reviewCount);
	}

	@Override
	public void incrementVisitCount(final Long storeId) {
		int affectedRows = storeCommandMapper.incrementVisitCount(storeId);

		if (affectedRows == 0) {
			log.warn("[Store] 방문 카운트 증가 실패 - storeId: {}", storeId);
			throw new BusinessException(ErrorCode.STORE_NOT_FOUND);
		}

		log.info("[Store] 방문 카운트 증가 완료 - storeId: {}", storeId);
	}
}
