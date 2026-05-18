package com.reday.global.exception;

import org.springframework.http.HttpStatus;

import com.reday.global.response.ResponseCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode implements ResponseCode {

	INVALID_INPUT_VALUE(HttpStatus.OK, "COMMON_INVALID_INPUT_VALUE_FAIL", "입력값이 올바르지 않습니다."),
	INVALID_REQUEST_BODY(HttpStatus.OK, "COMMON_INVALID_REQUEST_BODY_FAIL", "요청 본문이 올바르지 않습니다."),
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON_UNAUTHORIZED_FAIL", "인증이 필요합니다."),
	FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON_FORBIDDEN_FAIL", "접근 권한이 없습니다."),
	NOT_FOUND(HttpStatus.OK, "COMMON_NOT_FOUND_FAIL", "요청한 리소스를 찾을 수 없습니다."),
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_INTERNAL_SERVER_ERROR_FAIL", "서버 내부 오류가 발생했습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
