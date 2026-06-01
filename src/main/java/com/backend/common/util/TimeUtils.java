package com.backend.common.util;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;

import lombok.experimental.UtilityClass;

@UtilityClass
public class TimeUtils {
	/**
	 * "HH:mm-HH:mm" 형식의 문자열을 파싱합니다.
	 */
	public Optional<OpeningHours> parseOpeningHours(final String openingHours) {
		if (openingHours == null || openingHours.isBlank()) {
			return Optional.empty();
		}

		String[] parts = openingHours.split("-");
		if (parts.length != 2) {
			return Optional.empty();
		}

		try {
			LocalTime openTime = LocalTime.parse(parts[0].trim());
			LocalTime closeTime = LocalTime.parse(parts[1].trim());
			return Optional.of(new OpeningHours(openTime, closeTime));
		} catch (DateTimeParseException e) {
			return Optional.empty();
		}
	}

	/**
	 * 오픈 시간과 마감 시간을 "HH:mm-HH:mm" 형식으로 포맷팅합니다.
	 */
	public Optional<String> formatOpeningHours(final LocalTime openTime, final LocalTime closeTime) {
		if (openTime == null || closeTime == null) {
			return Optional.empty();
		}
		return Optional.of(openTime + "-" + closeTime);
	}

	public record OpeningHours(LocalTime openTime, LocalTime closeTime) {
	}
}
