package com.reday.schedule.dto;

public record ScheduleCreateRequest(
	String title,
	String startAt,
	Integer estimatedMinutes,
	String memo
) {
}
