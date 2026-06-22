package com.reday.global.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

	private final ErrorResponseCode errorCode;

	public BusinessException(ErrorResponseCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}

	public BusinessException(ErrorResponseCode errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}
}
