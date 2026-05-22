package com.reday.auth.exception;

import org.springframework.http.HttpStatus;

import com.reday.global.exception.ErrorResponseCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorResponseCode {

	SIGNUP_FAIL(HttpStatus.OK, "AUTH_SIGNUP_FAIL", "회원가입에 실패하였습니다."),
	LOGIN_FAIL(HttpStatus.UNAUTHORIZED, "AUTH_LOGIN_FAIL", "로그인에 실패하였습니다."),
	TOKEN_REFRESH_FAIL(HttpStatus.UNAUTHORIZED, "AUTH_TOKEN_REFRESH_FAIL", "토큰 재발급에 실패하였습니다."),
	INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH_INVALID_CREDENTIALS_FAIL", "이메일 또는 비밀번호가 올바르지 않습니다."),
	INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_INVALID_REFRESH_TOKEN_FAIL", "유효하지 않은 리프레시 토큰입니다."),
	REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_REFRESH_TOKEN_EXPIRED_FAIL", "리프레시 토큰이 만료되었습니다."),
	REFRESH_TOKEN_REVOKED(HttpStatus.FORBIDDEN, "AUTH_REFRESH_TOKEN_REVOKED_FAIL", "이미 무효화된 리프레시 토큰입니다."),
	EMAIL_NOT_VERIFIED(HttpStatus.FORBIDDEN, "AUTH_EMAIL_NOT_VERIFIED_FAIL", "이메일 인증이 완료되지 않았습니다."),
	EMAIL_DUPLICATED(HttpStatus.OK, "AUTH_EMAIL_DUPLICATED_FAIL", "이미 사용 중인 이메일입니다."),
	INVALID_EMAIL_FORMAT(HttpStatus.OK, "AUTH_INVALID_EMAIL_FORMAT_FAIL", "이메일 형식이 올바르지 않습니다."),
	INVALID_PASSWORD_FORMAT(HttpStatus.OK, "AUTH_INVALID_PASSWORD_FORMAT_FAIL", "비밀번호 형식이 올바르지 않습니다."),
	INVALID_NICKNAME(HttpStatus.OK, "AUTH_INVALID_NICKNAME_FAIL", "닉네임 형식이 올바르지 않습니다."),
	PASSWORD_CONFIRM_MISMATCH(HttpStatus.OK, "AUTH_PASSWORD_CONFIRM_MISMATCH_FAIL", "비밀번호 확인이 일치하지 않습니다."),
	REQUIRED_AGREEMENT_MISSING(HttpStatus.OK, "AUTH_REQUIRED_AGREEMENT_MISSING_FAIL", "필수 약관 동의가 필요합니다."),
	EMAIL_SEND_FAIL(HttpStatus.OK, "AUTH_EMAIL_SEND_FAIL", "이메일 인증코드 발송에 실패했습니다."),
	EMAIL_VERIFY_FAIL(HttpStatus.OK, "AUTH_EMAIL_VERIFY_FAIL", "이메일 인증에 실패하였습니다."),
	INVALID_VERIFICATION_CODE(HttpStatus.OK, "AUTH_INVALID_VERIFICATION_CODE_FAIL", "인증코드가 올바르지 않습니다."),
	VERIFICATION_CODE_EXPIRED(HttpStatus.OK, "AUTH_VERIFICATION_CODE_EXPIRED_FAIL", "인증코드가 만료되었습니다."),
	TOO_MANY_VERIFICATION_REQUESTS(
		HttpStatus.OK,
		"AUTH_TOO_MANY_VERIFICATION_REQUESTS_FAIL",
		"인증코드 요청 횟수가 너무 많습니다."
	);

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
