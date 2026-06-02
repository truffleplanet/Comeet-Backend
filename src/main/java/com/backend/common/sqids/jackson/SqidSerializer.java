package com.backend.common.sqids.jackson;

import java.io.IOException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.backend.common.sqids.domain.Sqid;
import com.backend.common.sqids.service.IdObfuscator;

/**
 * Sqid 객체를 JSON 직렬화 시 난독화된 문자열로 변환합니다.
 */
public class SqidSerializer extends JsonSerializer<Sqid> {

	@Override
	public void serialize(Sqid value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
		if (value == null || value.value() == null) {
			gen.writeNull();
			return;
		}

		gen.writeString(IdObfuscator.getRequiredInstance().encode(value.value()));
	}
}
