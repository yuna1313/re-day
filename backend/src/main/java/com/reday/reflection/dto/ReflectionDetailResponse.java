package com.reday.reflection.dto;

import java.util.List;

public record ReflectionDetailResponse(
	Integer reflectionId,
	String reflectionDate,
	String content,
	List<CompletedScheduleSummary> completedSchedules
) {

	public record CompletedScheduleSummary(
		Integer scheduleId,
		String title
	) {
	}
}
