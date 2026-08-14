package com.reday.schedule.dto;

import java.util.List;

public record ScheduleSearchResponse(
	String keyword,
	Boolean hasMore,
	List<ScheduleListResponse.ScheduleSummary> schedules
) {
}
