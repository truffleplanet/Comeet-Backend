package com.backend.common.sqids.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.backend.common.sqids.jackson.SqidLongDeserializer;
import com.backend.common.sqids.jackson.SqidLongSerializer;

@Configuration
public class SqidJacksonConfig {

	@Bean
	public Module sqidLongModule() {
		SimpleModule module = new SimpleModule("sqid-long-module");
		module.addSerializer(Long.class, new SqidLongSerializer());
		module.addSerializer(Long.TYPE, new SqidLongSerializer());
		module.addDeserializer(Long.class, new SqidLongDeserializer());
		module.addDeserializer(Long.TYPE, new SqidLongDeserializer());
		return module;
	}
}
