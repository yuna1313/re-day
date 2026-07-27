package com.reday.analytics.exception;

import org.springframework.http.HttpStatus;

import com.reday.global.exception.ErrorResponseCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AnalyticsErrorCode implements ErrorResponseCode {

	INVALID_PERIOD(HttpStatus.OK, "INSIGHT_INVALID_PERIOD_FAIL", "조회 기간 유형이 올바르지 않습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
