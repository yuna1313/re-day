package com.reday.analytics.dto;

import java.util.List;

public record InsightResponse(
	String periodType,
	List<TimeSlotCompletionRate> timeSlotCompletionRates,
	List<TopDeferReason> topDeferReasons,
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

	public record EstimatedVsActual(
		Integer averageEstimatedMinutes,
		Integer averageActualMinutes,
		Integer averageDiffMinutes
	) {
	}
}
