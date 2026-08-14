package com.reday.analytics.dto;

import java.util.List;

public record InsightResponse(
	String periodType,
	List<TimeSlotCompletionRate> timeSlotCompletionRates,
	List<TopDeferReason> topDeferReasons,
	List<TopDeferredSchedule> topDeferredSchedules,
	EstimatedVsActual estimatedVsActual,
	List<String> feedbackMessages
) {

	public record TimeSlotCompletionRate(
		String timeSlot,
		String label,
		Integer completionRate
	) {
	}

	public record TopDeferReason(
		Integer rank,
		String deferReasonCode,
		String label,
		Integer count
	) {
	}

	/**
	 * 아직 끝내지 않은 일정 중 미룬 횟수가 많은 일정. 조회 기간과 무관하게 집계한다.
	 */
	public record TopDeferredSchedule(
		Integer rank,
		Integer scheduleId,
		String title,
		Integer deferCount
	) {
	}

	public record EstimatedVsActual(
		Integer averageEstimatedMinutes,
		Integer averageActualMinutes,
		Integer averageDiffMinutes
	) {
	}
}
