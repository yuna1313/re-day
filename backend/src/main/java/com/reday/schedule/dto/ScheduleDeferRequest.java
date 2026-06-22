package com.reday.schedule.dto;

public record ScheduleDeferRequest(
	String deferReasonCode,
	String deferReasonDetail,
	String newStartAt
) {
}
