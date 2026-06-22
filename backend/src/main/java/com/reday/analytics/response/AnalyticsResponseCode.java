package com.reday.analytics.response;

import com.reday.global.response.ResponseCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AnalyticsResponseCode implements ResponseCode {

	INSIGHT_SUCCESS("INSIGHT_SUCCESS", "인사이트 조회에 성공했습니다.");

	private final String code;
	private final String message;
}
