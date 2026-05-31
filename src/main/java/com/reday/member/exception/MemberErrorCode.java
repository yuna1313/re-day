package com.reday.member.exception;

import org.springframework.http.HttpStatus;

import com.reday.global.exception.ErrorResponseCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberErrorCode implements ErrorResponseCode {

	NOT_FOUND(HttpStatus.OK, "MEMBER_NOT_FOUND_FAIL", "회원 정보를 찾을 수 없습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
