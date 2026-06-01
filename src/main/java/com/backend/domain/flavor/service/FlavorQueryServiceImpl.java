package com.backend.domain.flavor.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.common.error.ErrorCode;
import com.backend.common.error.exception.BusinessException;
import com.backend.domain.flavor.entity.Flavor;
import com.backend.domain.flavor.mapper.query.FlavorQueryMapper;
import org.springframework.util.CollectionUtils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class FlavorQueryServiceImpl implements FlavorQueryService {
	private final FlavorQueryMapper queryMapper;

	@Override
	public List<Flavor> findAllByIds(final List<Long> flavorIdList) {
		if (CollectionUtils.isEmpty(flavorIdList)) {
			return List.of();
		}
		List<Flavor> flavors = queryMapper.findAllByIds(flavorIdList);
		log.info("[Review] Flavor 조회 완료 - 요청: {}건, 조회성공: {}건", flavorIdList.size(), flavors.size());
		return flavors;
	}

	@Override
	public List<Flavor> findFlavorsByReviewId(final Long reviewId) {
		if (reviewId == null) {
			throw new BusinessException(ErrorCode.INVALID_INPUT);
		}
		List<Flavor> flavors = queryMapper.findAllByReviewId(reviewId);
		log.info("[Review] Flavor 조회 완료 - review ID: {}, 조회성공: {}건", reviewId, flavors.size());
		return flavors;
	}

	@Override
	public List<Flavor> findAll() {
		List<Flavor> flavors = queryMapper.findAll();
		log.info("[Review] 모든 Flavor 조회 완료 - 조회성공: {}건", flavors.size());
		return flavors;
	}

	@Override
	public List<Flavor> findByCodes(List<String> codes) {
		if (CollectionUtils.isEmpty(codes)) {
			return List.of();
		}
		List<Flavor> flavors = queryMapper.findByCodes(codes);
		log.debug("[Flavor] 코드로 Flavor 조회 완료 - 요청: {}건, 조회성공: {}건", codes.size(), flavors.size());
		return flavors;
	}

	@Override
	public List<String> getHierarchyPaths(List<String> codes) {
		if (CollectionUtils.isEmpty(codes)) {
			return List.of();
		}

		// 모든 Flavor를 조회하여 캐시 (계층 탐색용)
		List<Flavor> allFlavors = queryMapper.findAll();
		Map<Long, Flavor> flavorById = allFlavors.stream()
			.collect(Collectors.toMap(Flavor::getId, Function.identity()));
		Map<String, Flavor> flavorByCode = allFlavors.stream()
			.collect(Collectors.toMap(Flavor::getCode, Function.identity()));

		// 각 코드의 부모 경로(리프 제외)를 키로 하여 그룹화
		// 예: BLACKBERRY, RASPBERRY → 부모경로 "FRUITY > BERRY" 기준으로 그룹화
		Map<String, List<String>> parentPathToLeaves = new java.util.LinkedHashMap<>();

		for (String code : codes) {
			Flavor flavor = flavorByCode.get(code);
			if (flavor == null) {
				log.warn("[Flavor] 코드 '{}' 에 해당하는 Flavor를 찾을 수 없음", code);
				// 원본 코드를 단독 경로로 추가
				parentPathToLeaves.computeIfAbsent("", k -> new ArrayList<>()).add(code);
				continue;
			}

			// 계층 구조 경로 생성 (루트부터 부모까지, 리프 제외)
			List<String> parentParts = new ArrayList<>();
			Flavor current = flavor;
			List<String> fullPath = new ArrayList<>();

			// 먼저 전체 경로 수집
			while (current != null) {
				fullPath.add(0, current.getCode());
				if (current.getParentId() == null) {
					break;
				}
				current = flavorById.get(current.getParentId());
			}

			// 부모 경로 (리프 제외)
			if (fullPath.size() > 1) {
				parentParts = fullPath.subList(0, fullPath.size() - 1);
			}

			String parentPath = String.join(" > ", parentParts);
			parentPathToLeaves.computeIfAbsent(parentPath, k -> new ArrayList<>()).add(code);
		}

		// 트리 압축 표현 생성
		List<String> hierarchyPaths = new ArrayList<>();

		for (Map.Entry<String, List<String>> entry : parentPathToLeaves.entrySet()) {
			String parentPath = entry.getKey();
			List<String> leaves = entry.getValue();

			String compressedPath;
			if (parentPath.isEmpty()) {
				// 부모가 없는 경우 (루트 레벨 또는 찾을 수 없는 코드)
				compressedPath = String.join(", ", leaves);
			} else if (leaves.size() == 1) {
				// 리프가 하나면 대괄호 없이
				compressedPath = parentPath + " > " + leaves.get(0);
			} else {
				// 리프가 여러 개면 대괄호로 그룹화
				compressedPath = parentPath + " > [" + String.join(", ", leaves) + "]";
			}

			hierarchyPaths.add(compressedPath);
			log.debug("[Flavor] 트리 압축 경로 생성: {}", compressedPath);
		}

		return hierarchyPaths;
	}
}
