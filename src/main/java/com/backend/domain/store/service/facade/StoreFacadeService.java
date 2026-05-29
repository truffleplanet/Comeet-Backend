package com.backend.domain.store.service.facade;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.backend.common.error.ErrorCode;
import com.backend.common.error.exception.BusinessException;
import com.backend.common.util.GeoUtils;
import com.backend.common.util.StringUtils;
import com.backend.domain.menu.dto.request.MenuCreateReqDto;
import com.backend.domain.menu.dto.response.MenuResDto;
import com.backend.domain.menu.service.facade.MenuFacadeService;
import com.backend.domain.review.dto.common.ReviewPageDto;
import com.backend.domain.review.service.facade.ReviewFacadeService;
import com.backend.domain.store.converter.StoreConverter;
import com.backend.domain.store.dto.request.StoreCreateReqDto;
import com.backend.domain.store.dto.request.StoreSearchReqDto;
import com.backend.domain.store.dto.request.StoreUpdateReqDto;
import com.backend.domain.store.dto.response.StoreDetailResDto;
import com.backend.domain.store.dto.response.StoreListResDto;
import com.backend.domain.store.entity.Store;
import com.backend.domain.store.factory.StoreFactory;
import com.backend.domain.store.service.command.StoreCommandService;
import com.backend.domain.store.service.query.StoreQueryService;
import com.backend.domain.store.validator.StoreValidator;
import com.backend.domain.store.vo.StoreSearchBoundsVo;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreFacadeService {
	public static final String BLANK = "";
	private final StoreQueryService storeQueryService;
	private final StoreCommandService storeCommandService;
	private final StoreValidator storeValidator;
	private final StoreFactory storeFactory;

	private final MenuFacadeService menuFacadeService;
	private final ReviewFacadeService reviewFacadeService;

	private static final String INVALID_CHAR_REGEX = "[^a-zA-Z0-9가-힣]";

	public StoreListResDto searchStores(StoreSearchReqDto request) {
		double distanceKm = GeoUtils.getRadiusInKm(request.radius());

		GeoUtils.BoundingBox boundingBox = GeoUtils.calculateBoundingBox(request.latitude(), request.longitude(),
			distanceKm);

		StoreSearchBoundsVo boundsVo = StoreSearchBoundsVo.builder()
			.minLatitude(boundingBox.minLatitude())
			.maxLatitude(boundingBox.maxLatitude())
			.minLongitude(boundingBox.minLongitude())
			.maxLongitude(boundingBox.maxLongitude())
			.categories(StringUtils.parseCategoryList(request.categories()))
			.keyword(request.keyword())
			.build();

		List<Store> candidateStores = storeQueryService.findStoresWithinBounds(boundsVo);

		Map<Long, Double> distanceMap = new HashMap<>();
		List<Store> filteredStores = candidateStores.stream()
			.filter(store -> {
				double storeDistance = GeoUtils.calculateHaversineDistance(
					request.latitude().doubleValue(),
					request.longitude().doubleValue(),
					store.getLatitude().doubleValue(),
					store.getLongitude().doubleValue()
				);
				if (storeDistance <= distanceKm) {
					distanceMap.put(store.getId(), storeDistance);
					return true;
				}
				return false;
			})
			.sorted(Comparator.comparingDouble(store -> distanceMap.get(store.getId())))
			.toList();

		return StoreConverter.toStoreListResponse(filteredStores, distanceMap);
	}

	public StoreDetailResDto getStoreDetail(Long storeId) {
		Store store = storeQueryService.findById(storeId);
		return StoreConverter.toStoreDetailResponse(store);
	}

	public MenuResDto createMenuForStore(Long storeId, Long userId, MenuCreateReqDto reqDto) {
		return menuFacadeService.createMenu(storeId, userId, reqDto);
	}

	public Page<MenuResDto> getMenusByStore(Long storeId, int page, int size) {
		return menuFacadeService.getMenusByStore(storeId, page, size);
	}

	public Page<ReviewPageDto> getReviewsByStore(Long storeId, int page, int size) {
		return reviewFacadeService.findAllWithPageableByStoreId(storeId, page, size);
	}

	public StoreDetailResDto createStore(StoreCreateReqDto reqDto, Long ownerId) {
		Store store = storeFactory.create(reqDto, ownerId);

		storeValidator.validate(store);
		validateNoDuplicate(store);

		Store savedStore = storeCommandService.createStore(store);
		return StoreConverter.toStoreDetailResponse(savedStore);
	}

	private void validateNoDuplicate(Store store) {
		StoreSearchBoundsVo bounds = StoreConverter.toStoreSearchBoundsVo(store);
		List<Store> nearbyStores = storeQueryService.findStoresWithinBounds(bounds);

		boolean isDuplicate = nearbyStores.stream()
			.anyMatch(existing ->
				isSameAddress(existing.getAddress(), store.getAddress()) &&
					isSameName(existing.getName(), store.getName())
			);

		if (isDuplicate) {
			throw new BusinessException(ErrorCode.STORE_ALREADY_EXISTS);
		}
	}

	private boolean isSameAddress(String addr1, String addr2) {
		return normalize(addr1).equals(normalize(addr2));
	}

	private boolean isSameName(String name1, String name2) {
		return normalize(name1).equals(normalize(name2));
	}

	private String normalize(String text) {
		return text.replaceAll(INVALID_CHAR_REGEX, BLANK).toLowerCase();
	}

	public StoreDetailResDto updateStore(Long storeId, StoreUpdateReqDto reqDto, Long userId) {
		Store store = storeQueryService.findById(storeId);
		storeValidator.validateExistingStore(store, userId);

		Store updatedStore = storeFactory.update(store, reqDto);
		Store savedStore = storeCommandService.updateStore(updatedStore);

		return StoreConverter.toStoreDetailResponse(savedStore);
	}

	public void deleteStore(Long storeId, Long userId) {
		Store store = storeQueryService.findById(storeId);
		storeValidator.validateExistingStore(store, userId);
		storeCommandService.deleteStore(storeId);
	}

	public List<StoreDetailResDto> findMyStores(Long ownerId) {
		List<Store> stores = storeQueryService.findByOwnerId(ownerId);
		return StoreConverter.toStoreDetailResponseList(stores);
	}
}
