package com.reday.schedule.dto;

public record ScheduleDeferResponse(
	Integer scheduleId,
	String status,
	String startAt,
	Integer deferCount
) {
}
