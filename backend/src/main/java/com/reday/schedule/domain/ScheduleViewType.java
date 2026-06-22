package com.reday.schedule.domain;

import com.reday.global.exception.BusinessException;
import com.reday.schedule.exception.ScheduleErrorCode;

public enum ScheduleViewType {

	WEEK,
	MONTH;

	/**
	 * 요청 파라미터로 전달된 문자열을 일정 목록 화면 유형으로 변환합니다.
	 *
	 * @param value 요청 파라미터 viewType 값
	 * @return 변환된 일정 목록 화면 유형
	 * @throws BusinessException 지원하지 않는 화면 유형일 때 발생
	 */
	public static ScheduleViewType from(String value) {
		try {
			return ScheduleViewType.valueOf(value);
		} catch (IllegalArgumentException | NullPointerException exception) {
			throw new BusinessException(ScheduleErrorCode.INVALID_DATE_RANGE);
		}
	}
}
