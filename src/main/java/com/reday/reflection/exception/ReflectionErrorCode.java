package com.reday.reflection.exception;

import org.springframework.http.HttpStatus;

import com.reday.global.exception.ErrorResponseCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReflectionErrorCode implements ErrorResponseCode {

	NOT_FOUND(HttpStatus.OK, "RETRO_NOT_FOUND_FAIL", "회고를 찾을 수 없습니다."),
	INVALID_DATE(HttpStatus.OK, "RETRO_INVALID_DATE_FAIL", "회고 날짜가 올바르지 않습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
