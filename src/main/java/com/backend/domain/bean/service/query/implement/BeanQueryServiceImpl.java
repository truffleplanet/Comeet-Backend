package com.backend.domain.bean.service.query.implement;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.common.error.ErrorCode;
import com.backend.common.error.exception.BusinessException;
import com.backend.domain.bean.entity.Bean;
import com.backend.domain.bean.mapper.query.BeanQueryMapper;
import com.backend.domain.bean.service.query.BeanQueryService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class BeanQueryServiceImpl implements BeanQueryService {
	private final BeanQueryMapper queryMapper;

	@Override
	public Bean findById(final Long beanId) {
		Bean bean = queryMapper.findById(beanId)
			.orElseThrow(() -> new BusinessException(ErrorCode.BEAN_NOT_FOUND));
		log.info("[Bean] 원두 조회 - ID: {}", bean.getId());
		return bean;
	}

	@Override
	public List<Bean> findAll(final Pageable pageable) {
		List<Bean> list = queryMapper.findAll(pageable);
		log.info("[Bean] 모든 원두 조회 - size: {}", list.size());
		return list;
	}

	@Override
	public int countAll() {
		return queryMapper.countAll();
	}

	@Override
	public List<Bean> findByRoasteryId(final Long roasteryId, final Pageable pageable) {
		List<Bean> list = queryMapper.findByRoasteryId(roasteryId, pageable);
		log.info("[Bean] 로스터리의 원두 조회 - roasteryId: {}, size: {}", roasteryId, list.size());
		return list;
	}

	@Override
	public int countByRoasteryId(final Long roasteryId) {
		return queryMapper.countByRoasteryId(roasteryId);
	}

	@Override
	public List<Bean> findByCountryContaining(final String keyword, final Pageable pageable) {
		List<Bean> list = queryMapper.findByCountryContaining(keyword, pageable);
		log.info("[Bean] 생산 국가로 원두 검색 - keyword: {}, size: {}", keyword, list.size());
		return list;
	}

	@Override
	public int countByCountryContaining(final String keyword) {
		return queryMapper.countByCountryContaining(keyword);
	}

	@Override
	public List<Bean> findByMenuId(final Long menuId) {
		log.info("[Bean] 메뉴별 원두 조회 - menuId: {}", menuId);
		List<Bean> list = queryMapper.findByMenuId(menuId);
		log.info("[Bean] 메뉴별 원두 조회 완료 - size: {}", list.size());
		return list;
	}
}
