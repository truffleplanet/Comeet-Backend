package com.backend.common.config;

	import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

@Configuration
public class ModuleConfig {

	@Bean
	public ObjectMapper objectMapper(List<Module> modules) {
		ObjectMapper objectMapper = new ObjectMapper();
		
		JavaTimeModule javaTimeModule = new JavaTimeModule();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
		javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(formatter));
		
		objectMapper.registerModule(javaTimeModule);
		modules.forEach(objectMapper::registerModule);
		return objectMapper;
	}
}
