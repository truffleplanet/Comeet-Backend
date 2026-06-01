package com.backend.domain.bean.service.command.implement;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.bean.entity.Bean;
import com.backend.domain.bean.mapper.command.BeanCommandMapper;
import com.backend.domain.bean.service.command.BeanCommandService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class BeanCommandServiceImpl implements BeanCommandService {
	private final BeanCommandMapper beanCommandMapper;

	@Override
	public int insert(final Bean bean) {
		log.info("[Bean] 원두 생성 - roasteryId={}, 국가={}", bean.getRoasteryId(), bean.getCountry());
		return beanCommandMapper.insert(bean);
	}

	@Override
	public int update(final Bean bean) {
		log.info("[Bean] 원두 수정 - beanId={}", bean.getId());
		return beanCommandMapper.update(bean);
	}

	@Override
	public int softDelete(final Long beanId) {
		log.info("[Bean] 원두 삭제 - beanId={}", beanId);
		return beanCommandMapper.softDelete(beanId);
	}
}
