package com.backend.domain.roastery.service.facade;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.backend.common.error.ErrorCode;
import com.backend.common.error.exception.BusinessException;
import com.backend.common.util.PageUtils;
import com.backend.domain.roastery.converter.RoasteryConverter;
import com.backend.domain.roastery.dto.request.RoasteryCreateReqDto;
import com.backend.domain.roastery.dto.request.RoasteryUpdateReqDto;
import com.backend.domain.roastery.dto.response.RoasteryResDto;
import com.backend.domain.roastery.entity.Roastery;
import com.backend.domain.roastery.factory.RoasteryFactory;
import com.backend.domain.roastery.service.command.RoasteryCommandService;
import com.backend.domain.roastery.service.query.RoasteryQueryService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class RoasteryFacadeService {

	private final RoasteryQueryService roasteryQueryService;
	private final RoasteryCommandService roasteryCommandService;
	private final RoasteryFactory roasteryFactory;

	public RoasteryResDto createRoastery(final RoasteryCreateReqDto reqDto) {
		Roastery roastery = roasteryFactory.create(reqDto);
		int affectedRows = roasteryCommandService.insert(roastery);
		if (affectedRows == 0) {
			throw new BusinessException(ErrorCode.DATABASE_ERROR);
		}
		return RoasteryConverter.toRoasteryResDto(roastery);
	}

	public RoasteryResDto updateRoastery(final Long roasteryId, final RoasteryUpdateReqDto reqDto) {
		Roastery existingRoastery = roasteryQueryService.findById(roasteryId);

		Roastery updatedRoastery = roasteryFactory.createForUpdate(existingRoastery, reqDto);
		int affectedRows = roasteryCommandService.update(updatedRoastery);
		if (affectedRows == 0) {
			throw new BusinessException(ErrorCode.DATABASE_ERROR);
		}
		return RoasteryConverter.toRoasteryResDto(updatedRoastery);
	}

	public RoasteryResDto getRoastery(final Long roasteryId) {
		Roastery roastery = roasteryQueryService.findById(roasteryId);
		return RoasteryConverter.toRoasteryResDto(roastery);
	}

	public Page<RoasteryResDto> getAllRoasteries(final int page, final int size) {
		return PageUtils.buildPageResponse(
			page, size,
			roasteryQueryService::findAll,
			roasteryQueryService::countAll,
			RoasteryConverter::toRoasteryResDto
		);
	}

	public Page<RoasteryResDto> searchRoasteriesByName(final String keyword, final int page, final int size) {
		return PageUtils.buildPageResponse(
			page, size,
			pageable -> roasteryQueryService.findByNameContaining(keyword, pageable),
			() -> roasteryQueryService.countByNameContaining(keyword),
			RoasteryConverter::toRoasteryResDto
		);
	}

}
