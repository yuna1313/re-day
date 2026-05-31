package com.reday.schedule.exception;

import org.springframework.http.HttpStatus;

import com.reday.global.exception.ErrorResponseCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ScheduleErrorCode implements ErrorResponseCode {

	CREATE_FAIL(HttpStatus.OK, "SCH_CREATE_FAIL", "일정 생성에 실패하였습니다."),
	INVALID_DATE_RANGE(HttpStatus.OK, "SCH_INVALID_DATE_RANGE_FAIL", "조회 기간이 올바르지 않습니다."),
	INVALID_TITLE(HttpStatus.OK, "SCH_INVALID_TITLE_FAIL", "일정 제목이 올바르지 않습니다."),
	INVALID_START_AT(HttpStatus.OK, "SCH_INVALID_START_AT_FAIL", "시작 일시가 올바르지 않습니다."),
	INVALID_ESTIMATED_MINUTES(HttpStatus.OK, "SCH_INVALID_ESTIMATED_MINUTES_FAIL", "예상 시간이 올바르지 않습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
