package com.backend.domain.bean.service.facade;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.backend.common.error.ErrorCode;
import com.backend.common.error.exception.BusinessException;
import com.backend.common.util.PageUtils;
import com.backend.domain.bean.converter.BeanConverter;
import com.backend.domain.bean.dto.common.BeanFlavorDto;
import com.backend.domain.bean.dto.request.BeanCreateReqDto;
import com.backend.domain.bean.dto.request.BeanUpdateReqDto;
import com.backend.domain.bean.dto.response.BeanResDto;
import com.backend.domain.bean.entity.Bean;
import com.backend.domain.bean.factory.BeanFactory;
import com.backend.domain.bean.service.command.BeanCommandService;
import com.backend.domain.bean.service.command.BeanFlavorCommandService;
import com.backend.domain.bean.service.query.BeanFlavorQueryService;
import com.backend.domain.bean.service.query.BeanQueryService;
import com.backend.domain.flavor.converter.FlavorConverter;
import com.backend.domain.flavor.dto.common.FlavorBadgeDto;
import com.backend.domain.flavor.entity.Flavor;
import com.backend.domain.flavor.service.FlavorQueryService;
import com.backend.domain.roastery.entity.Roastery;
import com.backend.domain.roastery.service.query.RoasteryQueryService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class BeanFacadeService {

	private final BeanQueryService beanQueryService;
	private final BeanCommandService beanCommandService;
	private final RoasteryQueryService roasteryQueryService;
	private final BeanFlavorQueryService beanFlavorQueryService;
	private final BeanFlavorCommandService beanFlavorCommandService;

	private final BeanFactory beanFactory;
	private final FlavorQueryService flavorQueryService;

	@Transactional
	public BeanResDto createBean(final BeanCreateReqDto reqDto) {
		roasteryQueryService.findById(reqDto.roasteryId());

		Bean bean = beanFactory.create(reqDto);
		int affectedRows = beanCommandService.insert(bean);
		if (affectedRows == 0) {
			throw new BusinessException(ErrorCode.DATABASE_ERROR);
		}

		if (!CollectionUtils.isEmpty(reqDto.flavorIds())) {
			beanFlavorCommandService.insertBeanFlavors(bean.getId(), reqDto.flavorIds());
		}

		return buildBeanResDto(bean);
	}

	@Transactional
	public BeanResDto updateBean(final Long beanId, final BeanUpdateReqDto reqDto) {
		Bean existingBean = beanQueryService.findById(beanId);
		roasteryQueryService.findById(existingBean.getRoasteryId());

		Bean updatedBean = beanFactory.createForUpdate(existingBean, reqDto);
		int affectedRows = beanCommandService.update(updatedBean);
		if (affectedRows == 0) {
			throw new BusinessException(ErrorCode.DATABASE_ERROR);
		}

		// Flavor 매핑 업데이트
		if (reqDto.flavorIds() != null) {
			beanFlavorCommandService.deleteBeanFlavors(beanId);
			if (!reqDto.flavorIds().isEmpty()) {
				beanFlavorCommandService.insertBeanFlavors(beanId, reqDto.flavorIds());
			}
		}

		return buildBeanResDto(updatedBean);
	}

	@Transactional
	public void deleteBean(final Long beanId) {
		Bean existingBean = beanQueryService.findById(beanId);
		roasteryQueryService.findById(existingBean.getRoasteryId());

		int affectedRows = beanCommandService.softDelete(beanId);
		if (affectedRows == 0) {
			throw new BusinessException(ErrorCode.DATABASE_ERROR);
		}
	}

	public BeanResDto getBean(final Long beanId) {
		Bean bean = beanQueryService.findById(beanId);
		return buildBeanResDto(bean);
	}

	public Page<BeanResDto> getAllBeans(final int page, final int size) {
		Pageable pageable = PageUtils.getPageable(page, size);
		List<Bean> beans = beanQueryService.findAll(pageable);

		if (beans.isEmpty()) {
			return PageUtils.toPage(List.of(), pageable, 0);
		}

		int total = beanQueryService.countAll();
		List<BeanResDto> beanResDtos = buildBeanPageDtos(beans);

		return PageUtils.toPage(beanResDtos, pageable, total);
	}

	public Page<BeanResDto> getBeansByRoastery(final Long roasteryId, final int page, final int size) {
		Roastery roastery = roasteryQueryService.findById(roasteryId);
		Pageable pageable = PageUtils.getPageable(page, size);
		List<Bean> beans = beanQueryService.findByRoasteryId(roastery.getId(), pageable);

		if (beans.isEmpty()) {
			return PageUtils.toPage(List.of(), pageable, 0);
		}

		int total = beanQueryService.countByRoasteryId(roastery.getId());
		List<BeanResDto> beanResDtos = buildBeanPageDtos(beans);

		return PageUtils.toPage(beanResDtos, pageable, total);
	}

	public Page<BeanResDto> searchBeansByCountry(final String keyword, final int page, final int size) {
		Pageable pageable = PageUtils.getPageable(page, size);
		List<Bean> beans = beanQueryService.findByCountryContaining(keyword, pageable);

		if (beans.isEmpty()) {
			return PageUtils.toPage(List.of(), pageable, 0);
		}

		int total = beanQueryService.countByCountryContaining(keyword);
		List<BeanResDto> beanResDtos = buildBeanPageDtos(beans);

		return PageUtils.toPage(beanResDtos, pageable, total);
	}

	private BeanResDto buildBeanResDto(final Bean bean) {
		List<Flavor> flavors = beanFlavorQueryService.findFlavorsByBeanId(bean.getId());
		List<FlavorBadgeDto> badges = flavors.stream()
			.map(FlavorConverter::toFlavorBadgeDto)
			.toList();
		return BeanConverter.toBeanResDto(bean, badges);
	}

	private List<BeanResDto> buildBeanPageDtos(final List<Bean> beans) {
		// * Bean ID 목록 추출
		List<Long> beanIds = beans.stream()
			.map(Bean::getId)
			.toList();

		log.info("[Bean] buildBeanPageDtos - beanIds: {}", beanIds);

		List<BeanFlavorDto> beanFlavors = beanFlavorQueryService.findFlavorIdsByBeanIds(beanIds);
		log.info("[Bean] buildBeanPageDtos - beanFlavors 조회 결과: {} 건", beanFlavors.size());
		log.info("[Bean] buildBeanPageDtos - beanFlavors 상세: {}", beanFlavors);

		// * Flavor ID를 모아서 한 번에 조회
		List<Long> allFlavorIds = beanFlavors.stream()
			.map(BeanFlavorDto::flavorId)
			.distinct()
			.toList();

		// * Flavor 정보를 한 번에 조회하고 Map으로 변환
		Map<Long, FlavorBadgeDto> flavorMap = flavorQueryService.findAllByIds(allFlavorIds)
			.stream()
			.map(FlavorConverter::toFlavorBadgeDto)
			.collect(Collectors.toMap(FlavorBadgeDto::flavorId, badge -> badge));

		// * BeanId별로 Flavor 그룹화
		Map<Long, List<FlavorBadgeDto>> beanBadgesMap = beanFlavors.stream()
			.collect(Collectors.groupingBy(
				BeanFlavorDto::beanId,
				Collectors.mapping(
					dto -> flavorMap.get(dto.flavorId()),
					Collectors.filtering(Objects::nonNull, Collectors.toList())
				)
			));

		// * Bean별로 Flavor 뱃지 매핑하여 DTO 변환
		return beans.stream()
			.map(bean -> {
				List<FlavorBadgeDto> badges = beanBadgesMap.getOrDefault(bean.getId(), List.of());
				return BeanConverter.toBeanResDto(bean, badges);
			})
			.toList();
	}
}
