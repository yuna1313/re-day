package com.reday.schedule.dto;

public record ScheduleCompleteResponse(
	Integer scheduleId,
	String status,
	Integer actualMinutes,
	String completedAt
) {
}
