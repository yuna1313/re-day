package com.reday.auth.response;

import com.reday.global.response.ResponseCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthResponseCode implements ResponseCode {

	SIGNUP_SUCCESS("AUTH_SIGNUP_SUCCESS", "회원가입이 완료되었습니다."),
	LOGIN_SUCCESS("AUTH_LOGIN_SUCCESS", "로그인에 성공했습니다."),
	TOKEN_REFRESHED("AUTH_TOKEN_REFRESHED", "토큰이 재발급되었습니다."),
	LOGOUT_SUCCESS("AUTH_LOGOUT_SUCCESS", "로그아웃되었습니다."),
	EMAIL_SENT("AUTH_EMAIL_SENT", "인증코드를 발송했습니다."),
	EMAIL_VERIFIED("AUTH_EMAIL_VERIFIED", "이메일 인증이 완료되었습니다.");

	private final String code;
	private final String message;
}
