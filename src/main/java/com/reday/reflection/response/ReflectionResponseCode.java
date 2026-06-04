package com.reday.reflection.response;

import com.reday.global.response.ResponseCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReflectionResponseCode implements ResponseCode {

	TODAY_SUCCESS("REFLECTION_TODAY_SUCCESS", "오늘 회고 조회에 성공했습니다."),
	DETAIL_SUCCESS("REFLECTION_DETAIL_SUCCESS", "회고 조회에 성공했습니다."),
	CREATED("REFLECTION_CREATED", "회고가 작성되었습니다.");

	private final String code;
	private final String message;
}
