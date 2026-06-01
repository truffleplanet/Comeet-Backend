package com.backend.domain.review.service.facade;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.common.error.ErrorCode;
import com.backend.common.error.exception.BusinessException;
import com.backend.common.util.PageUtils;
import com.backend.domain.flavor.converter.FlavorConverter;
import com.backend.domain.flavor.dto.common.FlavorBadgeDto;
import com.backend.domain.flavor.service.FlavorQueryService;
import com.backend.domain.review.converter.CuppingNoteConverter;
import com.backend.domain.review.converter.ReviewConverter;
import com.backend.domain.review.dto.common.ReviewFlavorDto;
import com.backend.domain.review.dto.common.ReviewPageDto;
import com.backend.domain.review.dto.request.CuppingNoteReqDto;
import com.backend.domain.review.dto.request.ReviewReqDto;
import com.backend.domain.review.dto.response.CuppingResDto;
import com.backend.domain.review.dto.response.ReviewedResDto;
import com.backend.domain.review.entity.CuppingNote;
import com.backend.domain.review.entity.Review;
import com.backend.domain.review.factory.CuppingNoteFactory;
import com.backend.domain.review.factory.ReviewFactory;
import com.backend.domain.review.mapper.query.TastingNoteQueryMapper;
import com.backend.domain.review.service.command.CuppingNoteCommandService;
import com.backend.domain.review.service.command.ReviewCommandService;
import com.backend.domain.review.service.command.TastingNoteCommandService;
import com.backend.domain.review.service.query.CuppingNoteQueryService;
import com.backend.domain.review.service.query.ReviewQueryService;
import com.backend.domain.review.validator.ReviewValidator;
import com.backend.domain.store.service.command.StoreCommandService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewFacadeService {

	private final TastingNoteQueryMapper tastingNoteQueryMapper;
	private final CuppingNoteQueryService cuppingNoteQueryService;
	private final ReviewQueryService reviewQueryService;
	private final FlavorQueryService flavorQueryService;

	private final TastingNoteCommandService tastingNoteCommandService;
	private final CuppingNoteCommandService cuppingNoteCommandService;
	private final ReviewCommandService reviewCommandService;
	private final StoreCommandService storeCommandService;

	private final ReviewValidator reviewValidator;

	private final ReviewFactory reviewFactory;
	private final CuppingNoteFactory cuppingNoteFactory;

	@Transactional(rollbackFor = Exception.class)
	public ReviewedResDto createReview(final Long userId, final ReviewReqDto reqDto) {
		validateVisitIdNotDuplicate(reqDto.visitId());
		try {
			Review review = processCreateReview(userId, reqDto);
			tastingNoteCommandService.appendTastingNotes(review.getId(), reqDto.flavorIdList());
			applyCreatedReviewStats(review);
			return createReviewedResDto(review, reqDto.flavorIdList());
		} catch (DuplicateKeyException e) {
			log.warn("[Review] 중복 리뷰 생성 감지 - visitId: {}", reqDto.visitId(), e);
			throw new BusinessException(ErrorCode.REVIEW_ALREADY_EXISTS_FOR_VISIT);
		}
	}

	private void validateVisitIdNotDuplicate(final Long visitId) {
		if (reviewQueryService.existsByVisitId(visitId)) {
			throw new BusinessException(ErrorCode.REVIEW_ALREADY_EXISTS_FOR_VISIT);
		}
	}

	@Transactional(rollbackFor = Exception.class)
	public ReviewedResDto updateReview(final Long reviewId, final Long userId, final ReviewReqDto reqDto) {
		Review existingReview = getValidatedReview(reviewId, userId);
		Review updatedReview = processUpdateReview(existingReview, reqDto);
		tastingNoteCommandService.overwriteTastingNotes(reviewId, reqDto.flavorIdList());
		applyUpdatedReviewStats(existingReview, updatedReview);
		return createReviewedResDto(updatedReview, reqDto.flavorIdList());
	}

	@Transactional(rollbackFor = Exception.class)
	public void deleteReview(final Long reviewId, final Long userId) {
		Review review = getValidatedReview(reviewId, userId);
		Long storeId = review.getStoreId();
		int affectedRows = reviewCommandService.softDelete(review.getId());
		if (affectedRows == 0) {
			throw new BusinessException(ErrorCode.DATABASE_ERROR);
		}
		applyDeletedReviewStats(storeId, review.getRating());
	}

	public ReviewedResDto getReviewDetails(final Long reviewId) {
		Review review = reviewQueryService.findById(reviewId);
		List<FlavorBadgeDto> badges = flavorQueryService.findFlavorsByReviewId(reviewId).stream()
			.map(FlavorConverter::toFlavorBadgeDto)
			.toList();
		return ReviewConverter.toReviewedResDto(review, badges);
	}

	public Page<ReviewPageDto> findAllWithPageableByUserId(final Long userId, final int page, final int size) {
		Pageable pageable = PageUtils.getPageable(page, size);
		List<Review> reviews = reviewQueryService.findAllByUserId(userId, pageable);

		if (reviews.isEmpty()) {
			return PageUtils.toPage(List.of(), pageable, 0);
		}

		int total = reviewQueryService.countAllByUserId(userId);
		List<ReviewPageDto> reviewPageDtos = buildReviewPageDtos(reviews);

		return PageUtils.toPage(reviewPageDtos, pageable, total);
	}

	public Page<ReviewPageDto> findAllWithPageableByStoreId(final Long storeId, final int page, final int size) {
		Pageable pageable = PageUtils.getPageable(page, size);
		List<Review> reviews = reviewQueryService.findAllByStoreId(storeId, pageable);

		if (reviews.isEmpty()) {
			return PageUtils.toPage(List.of(), pageable, 0);
		}

		int total = reviewQueryService.countAllByStoreId(storeId);
		List<ReviewPageDto> reviewPageDtos = buildReviewPageDtos(reviews);

		return PageUtils.toPage(reviewPageDtos, pageable, total);
	}

	private Review processCreateReview(final Long userId, final ReviewReqDto reqDto) {
		Review review = reviewFactory.create(userId, reqDto);
		reviewCommandService.insert(review);
		return review;
	}

	private ReviewedResDto createReviewedResDto(final Review review, final List<Long> flavorIds) {
		List<FlavorBadgeDto> badges = flavorQueryService.findAllByIds(flavorIds)
			.stream()
			.map(FlavorConverter::toFlavorBadgeDto)
			.toList();

		return ReviewConverter.toReviewedResDto(review, badges);
	}

	private Review getValidatedReview(final Long reviewId, final Long userId) {
		Review review = reviewQueryService.findById(reviewId);
		reviewValidator.validateReviewBelongsToUser(review, userId);
		reviewValidator.validateReviewNotDeleted(review);
		return review;
	}

	private Review processUpdateReview(final Review existingReview, final ReviewReqDto reqDto) {
		Review updatedReview = reviewFactory.createForUpdate(existingReview, reqDto);

		int affectedRows = reviewCommandService.update(updatedReview);
		if (affectedRows == 0) {
			throw new BusinessException(ErrorCode.DATABASE_ERROR);
		}
		return updatedReview;
	}

	private List<ReviewPageDto> buildReviewPageDtos(final List<Review> reviews) {
		// * 리뷰 ID 목록 추출
		List<Long> reviewIds = reviews.stream()
			.map(Review::getId)
			.toList();

		List<ReviewFlavorDto> reviewFlavors = tastingNoteQueryMapper.findFlavorIdsByReviewIds(reviewIds);

		// * Flavor ID를 모아서 한 번에 조회
		List<Long> allFlavorIds = reviewFlavors.stream()
			.map(ReviewFlavorDto::flavorId)
			.distinct()
			.toList();

		// * Flavor 정보를 한 번에 조회하고 Map으로 변환
		Map<Long, FlavorBadgeDto> flavorMap = flavorQueryService.findAllByIds(allFlavorIds)
			.stream()
			.map(FlavorConverter::toFlavorBadgeDto)
			.collect(Collectors.toMap(FlavorBadgeDto::flavorId, badge -> badge));

		// * ReviewId별로 Flavor 그룹화
		Map<Long, List<FlavorBadgeDto>> reviewBadgesMap = reviewFlavors.stream()
			.collect(Collectors.groupingBy(
				ReviewFlavorDto::reviewId,
				Collectors.mapping(
					dto -> flavorMap.get(dto.flavorId()),
					Collectors.filtering(Objects::nonNull, Collectors.toList()))));

		// * 리뷰별로 Flavor 뱃지 매핑하여 DTO 변환
		return reviews.stream()
			.map(review -> {
				List<FlavorBadgeDto> badges = reviewBadgesMap.getOrDefault(review.getId(), List.of());
				return ReviewConverter.toReviewPageDto(review, badges);
			})
			.toList();
	}

	@Transactional(rollbackFor = Exception.class)
	public CuppingResDto createCuppingNote(
		final Long userId,
		final Long reviewId,
		final CuppingNoteReqDto reqDto
	) {
		getValidatedReview(reviewId, userId);
		validateCuppingNoteNotDuplicate(reviewId);
		CuppingNote cuppingNote = processCreateCuppingNote(reviewId, reqDto);
		return createCuppingResDto(cuppingNote);
	}

	@Transactional(rollbackFor = Exception.class)
	public CuppingResDto updateCuppingNote(
		final Long userId,
		final Long reviewId,
		final CuppingNoteReqDto reqDto
	) {
		getValidatedReview(reviewId, userId);
		CuppingNote cuppingNote = processUpdateCuppingNote(reviewId, reqDto);
		return createCuppingResDto(cuppingNote);
	}

	public CuppingResDto getCuppingNote(final Long reviewId) {
		CuppingNote cuppingNote = cuppingNoteQueryService.findByReviewId(reviewId);
		return createCuppingResDto(cuppingNote);
	}

	private void validateCuppingNoteNotDuplicate(final Long reviewId) {
		if (cuppingNoteQueryService.existsByReviewId(reviewId)) {
			throw new BusinessException(ErrorCode.CUPPING_NOTE_ALREADY_EXISTS);
		}
	}

	private CuppingNote processCreateCuppingNote(final Long reviewId, final CuppingNoteReqDto reqDto) {
		CuppingNote cuppingNote = cuppingNoteFactory.create(reviewId, reqDto);
		int affectedRows = cuppingNoteCommandService.insert(cuppingNote);
		if (affectedRows == 0) {
			throw new BusinessException(ErrorCode.DATABASE_ERROR);
		}
		return cuppingNote;
	}

	private CuppingNote processUpdateCuppingNote(final Long reviewId, final CuppingNoteReqDto reqDto) {
		CuppingNote cuppingNote = cuppingNoteQueryService.findByReviewId(reviewId);
		CuppingNote updatedNote = cuppingNoteFactory.createForUpdate(cuppingNote.getId(), reviewId, reqDto);

		int affectedRows = cuppingNoteCommandService.update(updatedNote);
		if (affectedRows == 0) {
			throw new BusinessException(ErrorCode.DATABASE_ERROR);
		}
		return updatedNote;
	}

	private CuppingResDto createCuppingResDto(final CuppingNote cuppingNote) {
		return CuppingNoteConverter.toCuppingResDto(cuppingNote);
	}

	private void applyCreatedReviewStats(final Review review) {
		storeCommandService.applyReviewStatsDelta(
			review.getStoreId(),
			1,
			ratingCountDelta(null, review.getRating()),
			ratingSumDelta(null, review.getRating())
		);
	}

	private void applyUpdatedReviewStats(final Review existingReview, final Review updatedReview) {
		storeCommandService.applyReviewStatsDelta(
			updatedReview.getStoreId(),
			0,
			ratingCountDelta(existingReview.getRating(), updatedReview.getRating()),
			ratingSumDelta(existingReview.getRating(), updatedReview.getRating())
		);
	}

	private void applyDeletedReviewStats(final Long storeId, final BigDecimal rating) {
		storeCommandService.applyReviewStatsDelta(
			storeId,
			-1,
			ratingCountDelta(rating, null),
			ratingSumDelta(rating, null)
		);
	}

	private int ratingCountDelta(final BigDecimal oldRating, final BigDecimal newRating) {
		if (oldRating == null && newRating != null) {
			return 1;
		}
		if (oldRating != null && newRating == null) {
			return -1;
		}
		return 0;
	}

	private BigDecimal ratingSumDelta(final BigDecimal oldRating, final BigDecimal newRating) {
		BigDecimal oldValue = oldRating == null ? BigDecimal.ZERO : oldRating;
		BigDecimal newValue = newRating == null ? BigDecimal.ZERO : newRating;
		return newValue.subtract(oldValue);
	}
}
