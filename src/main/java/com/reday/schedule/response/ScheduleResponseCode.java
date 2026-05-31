package com.reday.schedule.response;

import com.reday.global.response.ResponseCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ScheduleResponseCode implements ResponseCode {

	LIST_SUCCESS("SCHEDULE_LIST_SUCCESS", "일정 목록 조회에 성공했습니다."),
	CREATED("SCHEDULE_CREATED", "일정이 등록되었습니다."),
	UPDATED("SCHEDULE_UPDATED", "일정이 수정되었습니다.");

	private final String code;
	private final String message;
}
