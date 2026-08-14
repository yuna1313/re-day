package com.reday.schedule.dto;

import java.util.List;

public record ScheduleOverdueResponse(
	Integer totalCount,
	Boolean hasMore,
	List<ScheduleListResponse.ScheduleSummary> schedules
) {
}
