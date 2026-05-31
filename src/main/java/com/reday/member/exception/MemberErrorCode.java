package com.reday.member.exception;

import org.springframework.http.HttpStatus;

import com.reday.global.exception.ErrorResponseCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberErrorCode implements ErrorResponseCode {

	NOT_FOUND(HttpStatus.OK, "MEMBER_NOT_FOUND_FAIL", "회원 정보를 찾을 수 없습니다."),
	PASSWORD_UPDATE_FAIL(HttpStatus.OK, "MEMBER_PASSWORD_UPDATE_FAIL", "비밀번호 변경에 실패하였습니다."),
	INVALID_CURRENT_PASSWORD(HttpStatus.OK, "MEMBER_INVALID_CURRENT_PASSWORD_FAIL", "현재 비밀번호가 올바르지 않습니다."),
	SAME_AS_OLD_PASSWORD(HttpStatus.OK, "MEMBER_SAME_AS_OLD_PASSWORD_FAIL", "새 비밀번호가 현재 비밀번호와 동일합니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
