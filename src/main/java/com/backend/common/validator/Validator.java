package com.backend.common.validator;

public interface Validator<T> {
	void validate(T t);
}
