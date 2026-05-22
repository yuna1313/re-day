package com.reday.auth.response;

import com.reday.global.response.ResponseCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthResponseCode implements ResponseCode {

	SIGNUP_SUCCESS("AUTH_SIGNUP_SUCCESS", "회원가입이 완료되었습니다. 이메일 인증을 진행해주세요."),
	EMAIL_SENT("AUTH_EMAIL_SENT", "인증코드를 발송했습니다.");

	private final String code;
	private final String message;
}
