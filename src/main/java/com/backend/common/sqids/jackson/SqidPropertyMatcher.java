package com.backend.common.sqids.jackson;

import com.fasterxml.jackson.databind.BeanProperty;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class SqidPropertyMatcher {

	static boolean shouldObfuscate(BeanProperty property) {
		if (property == null || property.getName() == null) {
			return false;
		}
		Class<?> declaringClass = property.getMember() == null ? null : property.getMember().getDeclaringClass();
		return isExternalDto(declaringClass) && isIdProperty(property.getName());
	}

	private static boolean isExternalDto(Class<?> declaringClass) {
		if (declaringClass == null || declaringClass.getPackageName() == null) {
			return false;
		}
		String packageName = declaringClass.getPackageName();
		return packageName.startsWith("com.backend.domain.")
			&& packageName.contains(".dto.")
			&& !packageName.contains(".dto.internal.");
	}

	private static boolean isIdProperty(String propertyName) {
		String normalized = propertyName.toLowerCase();
		return normalized.equals("id")
			|| normalized.endsWith("id")
			|| normalized.endsWith("ids")
			|| normalized.endsWith("idlist");
	}
}
