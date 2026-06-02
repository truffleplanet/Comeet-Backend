package com.backend.common.sqids.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.backend.common.error.ErrorCode;
import com.backend.common.error.exception.BusinessException;
import com.backend.common.sqids.service.IdObfuscator;

public class SqidLongDeserializer extends JsonDeserializer<Long> implements ContextualDeserializer {

	private final boolean obfuscate;

	public SqidLongDeserializer() {
		this(false);
	}

	private SqidLongDeserializer(boolean obfuscate) {
		this.obfuscate = obfuscate;
	}

	@Override
	public Long deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
		if (p.currentToken() == JsonToken.VALUE_NULL) {
			return null;
		}
		if (obfuscate) {
			if (p.currentToken() != JsonToken.VALUE_STRING) {
				throw new BusinessException(ErrorCode.INVALID_INPUT);
			}
			return IdObfuscator.getRequiredInstance().decode(p.getText());
		}
		if (p.currentToken().isNumeric()) {
			return p.getLongValue();
		}
		return Long.parseLong(p.getText());
	}

	@Override
	public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property)
		throws JsonMappingException {
		return new SqidLongDeserializer(SqidPropertyMatcher.shouldObfuscate(property));
	}
}
