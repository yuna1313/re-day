package com.reday.schedule.response;

import com.reday.global.response.ResponseCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ScheduleResponseCode implements ResponseCode {

	LIST_SUCCESS("SCHEDULE_LIST_SUCCESS", "일정 목록 조회에 성공했습니다."),
	SEARCH_SUCCESS("SCHEDULE_SEARCH_SUCCESS", "일정 검색에 성공했습니다."),
	DETAIL_SUCCESS("SCHEDULE_DETAIL_SUCCESS", "일정 상세 조회에 성공했습니다."),
	CREATED("SCHEDULE_CREATED", "일정이 등록되었습니다."),
	UPDATED("SCHEDULE_UPDATED", "일정이 수정되었습니다."),
	DELETED("SCHEDULE_DELETED", "일정이 삭제되었습니다."),
	COMPLETED("SCHEDULE_COMPLETED", "일정이 완료 처리되었습니다."),
	DEFERRED("SCHEDULE_DEFERRED", "일정이 미뤄졌습니다.");

	private final String code;
	private final String message;
}
