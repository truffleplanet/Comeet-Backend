package com.backend.domain.visit.service.facade;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.common.error.ErrorCode;
import com.backend.common.error.exception.BusinessException;
import com.backend.common.util.GeoUtils;
import com.backend.common.util.PageUtils;
import com.backend.domain.menu.entity.Menu;
import com.backend.domain.menu.service.query.MenuQueryService;
import com.backend.domain.store.service.command.StoreCommandService;
import com.backend.domain.user.entity.User;
import com.backend.domain.user.validator.UserValidator;
import com.backend.domain.visit.converter.VisitConverter;
import com.backend.domain.visit.dto.common.VisitInfoDto;
import com.backend.domain.visit.dto.common.VisitPageDto;
import com.backend.domain.visit.dto.request.VerifyReqDto;
import com.backend.domain.visit.dto.response.VerifiedResDto;
import com.backend.domain.visit.entity.Visit;
import com.backend.domain.visit.factory.VisitFactory;
import com.backend.domain.visit.service.command.VisitCommandService;
import com.backend.domain.visit.service.query.VisitQueryService;
import com.backend.domain.visit.validator.VisitValidator;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class VisitFacadeService {
	private static final int ALLOWABLE_RANGE = 100;

	private final VisitCommandService visitCommandService;
	private final VisitQueryService visitQueryService;
	private final VisitValidator visitValidator;
	private final VisitFactory visitFactory;

	private final MenuQueryService menuQueryService;
	private final StoreCommandService storeCommandService;

	private final UserValidator userValidator;

	@Transactional(rollbackFor = Exception.class)
	public VerifiedResDto verifyVisit(final User user, final VerifyReqDto reqDto) {
		userValidator.validate(user);
		Boolean isVerified = checkDistance(reqDto);
		Visit visit = visitFactory.create(user, reqDto, isVerified);

		if (visitCommandService.save(visit) == 0) {
			throw new BusinessException(ErrorCode.DATABASE_ERROR);
		}

		if (isVerified) {
			Menu menu = menuQueryService.findById(reqDto.menuId());
			storeCommandService.incrementVisitCount(menu.getStoreId());
		}

		return VisitConverter.toVerifiedResDto(visit);
	}

	private Boolean checkDistance(final VerifyReqDto reqDto) {
		double calculatedDistance = GeoUtils.calculateHaversineDistance(
			reqDto.storeLocationDto().latitude(),
			reqDto.storeLocationDto().longitude(),
			reqDto.userLocationDto().latitude(),
			reqDto.userLocationDto().longitude()
		);

		return GeoUtils.isWithinRadius(calculatedDistance, ALLOWABLE_RANGE);
	}

	public VisitInfoDto findVisitById(final User user, final Long visitId) {
		userValidator.validate(user);
		Visit visit = visitQueryService.findById(visitId);
		visitValidator.validateVisitBelongsToUser(visit.getUserId(), user.getId());
		return VisitConverter.toVisitInfoDto(visit);
	}

	public Page<VisitPageDto> findAllWithPageableUserId(final User user, final int page, final int size) {
		userValidator.validate(user);
		return PageUtils.buildPageResponse(
			page, size,
			pageable -> visitQueryService.findAllByUserId(user.getId(), pageable),
			() -> visitQueryService.countAllByUserId(user.getId()),
			VisitConverter::toVisitPageDto
		);
	}
}
