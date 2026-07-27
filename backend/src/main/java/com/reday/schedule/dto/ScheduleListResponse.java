package com.reday.schedule.dto;

import java.util.List;

public record ScheduleListResponse(
	String viewType,
	String startDate,
	String endDate,
	List<ScheduleSummary> schedules
) {

	public record ScheduleSummary(
		Integer scheduleId,
		String title,
		String startAt,
		Integer estimatedMinutes,
		Integer actualMinutes,
		String status,
		String completedAt,
		Integer deferCount
	) {
	}
}
