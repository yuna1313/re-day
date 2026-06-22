package com.reday.member.response;

import com.reday.global.response.ResponseCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberResponseCode implements ResponseCode {

	ME_SUCCESS("MEMBER_ME_SUCCESS", "내 정보 조회에 성공했습니다."),
	PASSWORD_UPDATED("MEMBER_PASSWORD_UPDATED", "비밀번호가 변경되었습니다.");

	private final String code;
	private final String message;
}
