package com.backend.common.sqids.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.backend.common.sqids.service.IdObfuscator;

public class SqidLongSerializer extends JsonSerializer<Long> implements ContextualSerializer {

	private final boolean obfuscate;

	public SqidLongSerializer() {
		this(false);
	}

	private SqidLongSerializer(boolean obfuscate) {
		this.obfuscate = obfuscate;
	}

	@Override
	public void serialize(Long value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
		if (value == null) {
			gen.writeNull();
			return;
		}
		if (obfuscate) {
			gen.writeString(IdObfuscator.getRequiredInstance().encode(value));
			return;
		}
		gen.writeNumber(value);
	}

	@Override
	public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property)
		throws JsonMappingException {
		return new SqidLongSerializer(SqidPropertyMatcher.shouldObfuscate(property));
	}
}
