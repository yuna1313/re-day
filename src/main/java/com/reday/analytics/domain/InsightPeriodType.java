package com.reday.analytics.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.reday.analytics.exception.AnalyticsErrorCode;
import com.reday.global.exception.BusinessException;

public enum InsightPeriodType {

	LAST_7_DAYS(7),
	LAST_30_DAYS(30);

	private final int days;

	InsightPeriodType(int days) {
		this.days = days;
	}

	/**
	 * 요청 기간 유형 문자열을 인사이트 기간 유형으로 변환합니다.
	 *
	 * @param value 요청 기간 유형
	 * @return 인사이트 기간 유형
	 * @throws BusinessException 지원하지 않는 기간 유형인 경우 발생
	 */
	public static InsightPeriodType from(String value) {
		if (value == null || value.isBlank()) {
			return LAST_30_DAYS;
		}

		try {
			return InsightPeriodType.valueOf(value);
		} catch (IllegalArgumentException exception) {
			throw new BusinessException(AnalyticsErrorCode.INVALID_PERIOD);
		}
	}

	/**
	 * 오늘을 포함한 조회 시작 일시를 계산합니다.
	 *
	 * @param today 기준 날짜
	 * @return 조회 시작 일시
	 */
	public LocalDateTime startAt(LocalDate today) {
		return today.minusDays(days - 1L).atStartOfDay();
	}

	/**
	 * 조회 종료 일시를 계산합니다.
	 *
	 * @param today 기준 날짜
	 * @return 조회 종료 일시
	 */
	public LocalDateTime endAt(LocalDate today) {
		return today.atTime(LocalTime.MAX);
	}
}
