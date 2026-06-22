package com.reday.schedule.dto;

public record ScheduleUpdateRequest(
	String title,
	String startAt,
	Integer estimatedMinutes,
	String memo
) {
}
