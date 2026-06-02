package com.backend.common.sqids.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.backend.common.sqids.jackson.SqidDeserializer;
import com.backend.common.sqids.jackson.SqidSerializer;

/**
 * ID 난독화를 위한 래퍼(Wrapper) 타입.
 * 외부로 노출할 때는 Sqids 문자열로 직렬화되고, 인입될 때는 문자열에서 숫자 값으로 역직렬화됩니다.
 */
@JsonSerialize(using = SqidSerializer.class)
@JsonDeserialize(using = SqidDeserializer.class)
public record Sqid(Long value) {

	public static Sqid of(Long value) {
		if (value == null) {
			return null;
		}
		return new Sqid(value);
	}

	@Override
	public String toString() {
		return value != null ? value.toString() : "null";
	}
}
