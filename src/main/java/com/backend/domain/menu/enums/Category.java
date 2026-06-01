package com.backend.domain.menu.enums;

import lombok.Getter;

@Getter
public enum Category {
	// Coffee Variations
	HAND_DRIP("핸드드립"),
	ESPRESSO("에스프레소"),
	AMERICANO("아메리카노"),
	LATTE("라떼"),
	CAPPUCCINO("카푸치노"),
	FLAT_WHITE("플랫화이트"),
	COLD_BREW("콜드브루");

	private final String description;

	Category(String description) {
		this.description = description;
	}
}
