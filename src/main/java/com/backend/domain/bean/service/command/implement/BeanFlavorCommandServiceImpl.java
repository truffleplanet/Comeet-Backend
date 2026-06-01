package com.backend.domain.bean.service.command.implement;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.backend.common.error.ErrorCode;
import com.backend.common.error.exception.BusinessException;
import com.backend.domain.bean.dto.request.BeanFlavorCreateReqDto;
import com.backend.domain.bean.dto.response.BeanFlavorResDto;
import com.backend.domain.bean.entity.Bean;
import com.backend.domain.bean.mapper.command.BeanFlavorCommandMapper;
import com.backend.domain.bean.service.command.BeanFlavorCommandService;
import com.backend.domain.bean.service.query.BeanFlavorQueryService;
import com.backend.domain.bean.service.query.BeanQueryService;
import com.backend.domain.flavor.converter.FlavorConverter;
import com.backend.domain.flavor.dto.common.FlavorBadgeDto;
import com.backend.domain.flavor.entity.Flavor;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class BeanFlavorCommandServiceImpl implements BeanFlavorCommandService {
	private final BeanFlavorCommandMapper commandMapper;
	private final BeanQueryService beanQueryService;
	private final BeanFlavorQueryService beanFlavorQueryService;

	@Override
	public int insertBeanFlavors(final Long beanId, final List<Long> flavorIds) {
		log.info("[BeanFlavor] 원두-플레이버 매핑 생성 - beanId={}, flavorIds={}", beanId, flavorIds);
		return commandMapper.insertBeanFlavors(beanId, flavorIds);
	}

	@Override
	public int deleteBeanFlavors(final Long beanId) {
		log.info("[BeanFlavor] 원두-플레이버 매핑 삭제 - beanId={}", beanId);
		return commandMapper.deleteBeanFlavors(beanId);
	}

	@Override
	public BeanFlavorResDto addBeanFlavors(final Long beanId, final BeanFlavorCreateReqDto reqDto) {
		Bean bean = beanQueryService.findById(beanId);

		int affectedRows = insertBeanFlavors(bean.getId(), reqDto.flavorIds());
		if (affectedRows == 0) {
			throw new BusinessException(ErrorCode.DATABASE_ERROR);
		}

		return buildBeanFlavorResDto(bean.getId());
	}

	@Override
	public BeanFlavorResDto updateBeanFlavors(final Long beanId, final BeanFlavorCreateReqDto reqDto) {
		Bean bean = beanQueryService.findById(beanId);

		deleteBeanFlavors(bean.getId());

		if (!CollectionUtils.isEmpty(reqDto.flavorIds())) {
			int affectedRows = insertBeanFlavors(bean.getId(), reqDto.flavorIds());
			if (affectedRows == 0) {
				throw new BusinessException(ErrorCode.DATABASE_ERROR);
			}
		}

		return buildBeanFlavorResDto(bean.getId());
	}

	@Override
	public void deleteBeanFlavorsByBeanId(final Long beanId) {
		Bean bean = beanQueryService.findById(beanId);
		deleteBeanFlavors(bean.getId());
	}

	private BeanFlavorResDto buildBeanFlavorResDto(final Long beanId) {
		List<Flavor> flavors = beanFlavorQueryService.findFlavorsByBeanId(beanId);
		List<FlavorBadgeDto> badges = FlavorConverter.toFlavorBadgeDtoList(flavors);
		return BeanFlavorResDto.builder()
			.beanId(beanId)
			.flavors(badges)
			.build();
	}
}
