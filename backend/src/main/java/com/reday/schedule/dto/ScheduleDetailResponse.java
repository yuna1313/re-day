package com.reday.schedule.dto;

import java.util.List;

public record ScheduleDetailResponse(
	Integer scheduleId,
	String title,
	String startAt,
	Integer estimatedMinutes,
	Integer actualMinutes,
	String memo,
	String status,
	String completedAt,
	String createdAt,
	String updatedAt,
	Integer deferCount,
	List<DeferLog> deferLogs
) {

	public record DeferLog(
		Integer actionLogId,
		String actionType,
		String deferReasonCode,
		String deferReasonDetail,
		String actionAt
	) {
	}
}
