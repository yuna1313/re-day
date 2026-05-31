package com.reday.schedule.exception;

import org.springframework.http.HttpStatus;

import com.reday.global.exception.ErrorResponseCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ScheduleErrorCode implements ErrorResponseCode {

	INVALID_DATE_RANGE(HttpStatus.OK, "SCH_INVALID_DATE_RANGE_FAIL", "조회 기간이 올바르지 않습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
