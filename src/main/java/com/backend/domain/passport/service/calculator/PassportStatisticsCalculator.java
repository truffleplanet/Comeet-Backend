package com.backend.domain.passport.service.calculator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class PassportStatisticsCalculator {

	private final DistanceCalculator distanceCalculator;

	public PassportStatistics calculate(List<Map<String, Object>> visits) {
		if (CollectionUtils.isEmpty(visits)) {
			return PassportStatistics.empty();
		}

		Set<Long> uniqueStores = new HashSet<>();
		Set<Long> uniqueBeans = new HashSet<>();
		Map<String, Integer> originCounts = new HashMap<>();
		Map<String, Integer> roasteryCounts = new HashMap<>();
		List<String> originSequence = new ArrayList<>();

		for (Map<String, Object> visit : visits) {
			processVisit(visit, uniqueStores, uniqueBeans, originCounts, roasteryCounts, originSequence);
		}
		double totalOriginDistance = distanceCalculator.calculateTotalOriginDistance(originSequence);

		return PassportStatistics.of(
			visits,
			uniqueStores,
			uniqueBeans,
			findTopEntry(originCounts),
			findTopEntry(roasteryCounts),
			originSequence,
			totalOriginDistance
		);
	}

	private void processVisit(
		Map<String, Object> visit,
		Set<Long> uniqueStores,
		Set<Long> uniqueBeans,
		Map<String, Integer> originCounts,
		Map<String, Integer> roasteryCounts,
		List<String> originSequence
	) {
		Long storeId = (Long)visit.get("store_id");
		Long beanId = (Long)visit.get("bean_id");
		String origin = (String)visit.get("origin");
		String roasteryName = (String)visit.get("roastery_name");

		if (storeId != null) {
			uniqueStores.add(storeId);
		}

		if (beanId != null) {
			uniqueBeans.add(beanId);
		}

		if (origin != null && !origin.isEmpty()) {
			originCounts.put(origin, originCounts.getOrDefault(origin, 0) + 1);
			originSequence.add(origin);
		}

		if (roasteryName != null && !roasteryName.isEmpty()) {
			roasteryCounts.put(roasteryName, roasteryCounts.getOrDefault(roasteryName, 0) + 1);
		}
	}

	private String findTopEntry(Map<String, Integer> countMap) {
		return countMap.entrySet().stream()
			.max(Map.Entry.comparingByValue())
			.map(Map.Entry::getKey)
			.orElse(null);
	}

	@Builder
	public record PassportStatistics(
		int totalCoffeeCount,
		int totalStoreCount,
		int totalBeanCount,
		String topOrigin,
		String topRoastery,
		String originSequence,
		double totalOriginDistance
	) {
		public static PassportStatistics empty() {
			return PassportStatistics.builder()
				.totalCoffeeCount(0)
				.totalStoreCount(0)
				.totalBeanCount(0)
				.topOrigin(null)
				.topRoastery(null)
				.originSequence(null)
				.totalOriginDistance(0)
				.build();
		}

		public static PassportStatistics of(
			List<Map<String, Object>> visits,
			Set<Long> uniqueStores,
			Set<Long> uniqueBeans,
			String topOrigin,
			String topRoastery,
			List<String> originSequence,
			double totalOriginDistance
		) {
			return PassportStatistics.builder()
				.totalCoffeeCount(visits.size())
				.totalStoreCount(uniqueStores.size())
				.totalBeanCount(uniqueBeans.size())
				.topOrigin(topOrigin)
				.topRoastery(topRoastery)
				.originSequence(originSequence.isEmpty() ? null : String.join(",", originSequence))
				.totalOriginDistance(totalOriginDistance)
				.build();
		}
	}
}
