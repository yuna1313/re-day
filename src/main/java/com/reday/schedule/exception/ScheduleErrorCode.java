package com.reday.schedule.exception;

import org.springframework.http.HttpStatus;

import com.reday.global.exception.ErrorResponseCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ScheduleErrorCode implements ErrorResponseCode {

	CREATE_FAIL(HttpStatus.OK, "SCH_CREATE_FAIL", "일정 생성에 실패하였습니다."),
	UPDATE_FAIL(HttpStatus.OK, "SCH_UPDATE_FAIL", "일정 수정에 실패하였습니다."),
	DELETE_FAIL(HttpStatus.OK, "SCH_DELETE_FAIL", "일정 삭제에 실패하였습니다."),
	COMPLETE_FAIL(HttpStatus.OK, "SCH_COMPLETE_FAIL", "일정 완료 처리에 실패하였습니다."),
	NOT_FOUND(HttpStatus.OK, "SCH_NOT_FOUND_FAIL", "일정을 찾을 수 없습니다."),
	ALREADY_DONE(HttpStatus.OK, "SCH_ALREADY_DONE_FAIL", "이미 완료된 일정입니다."),
	INVALID_DATE_RANGE(HttpStatus.OK, "SCH_INVALID_DATE_RANGE_FAIL", "조회 기간이 올바르지 않습니다."),
	INVALID_TITLE(HttpStatus.OK, "SCH_INVALID_TITLE_FAIL", "일정 제목이 올바르지 않습니다."),
	INVALID_START_AT(HttpStatus.OK, "SCH_INVALID_START_AT_FAIL", "시작 일시가 올바르지 않습니다."),
	INVALID_ESTIMATED_MINUTES(HttpStatus.OK, "SCH_INVALID_ESTIMATED_MINUTES_FAIL", "예상 시간이 올바르지 않습니다."),
	INVALID_ACTUAL_MINUTES(HttpStatus.OK, "SCH_INVALID_ACTUAL_MINUTES_FAIL", "실제 소요 시간이 올바르지 않습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
