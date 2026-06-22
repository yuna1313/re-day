package com.reday.reflection.exception;

import org.springframework.http.HttpStatus;

import com.reday.global.exception.ErrorResponseCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReflectionErrorCode implements ErrorResponseCode {

	CREATE_FAIL(HttpStatus.OK, "RETRO_CREATE_FAIL", "회고 작성에 실패하였습니다."),
	UPDATE_FAIL(HttpStatus.OK, "RETRO_UPDATE_FAIL", "회고 수정에 실패하였습니다."),
	ALREADY_EXISTS(HttpStatus.OK, "RETRO_ALREADY_EXISTS_FAIL", "해당 날짜의 회고가 이미 존재합니다."),
	NOT_FOUND(HttpStatus.OK, "RETRO_NOT_FOUND_FAIL", "회고를 찾을 수 없습니다."),
	INVALID_DATE(HttpStatus.OK, "RETRO_INVALID_DATE_FAIL", "회고 날짜가 올바르지 않습니다."),
	EMPTY_CONTENT(HttpStatus.OK, "RETRO_EMPTY_CONTENT_FAIL", "회고 내용을 입력해주세요.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
