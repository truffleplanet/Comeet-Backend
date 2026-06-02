package com.backend.common.sqids.jackson;

import java.io.IOException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.backend.common.sqids.domain.Sqid;
import com.backend.common.sqids.service.IdObfuscator;

/**
 * JSON의 난독화된 문자열을 Sqid 객체로 역직렬화합니다.
 */
public class SqidDeserializer extends JsonDeserializer<Sqid> {

	@Override
	public Sqid deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
		String text = p.getText();
		if (text == null || text.isBlank()) {
			return null;
		}

		Long decoded = IdObfuscator.getRequiredInstance().decode(text);
		return Sqid.of(decoded);
	}
}
