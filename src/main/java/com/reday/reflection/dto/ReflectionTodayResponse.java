package com.reday.reflection.dto;

import java.util.List;

public record ReflectionTodayResponse(
	ReflectionSummary reflection,
	List<CompletedScheduleSummary> completedSchedules
) {

	public record ReflectionSummary(
		Integer reflectionId,
		String reflectionDate,
		String content
	) {
	}

	public record CompletedScheduleSummary(
		Integer scheduleId,
		String title
	) {
	}
}
