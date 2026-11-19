package com.reday.auth.application;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import com.reday.schedule.application.ScheduleService;
import com.reday.schedule.dto.ScheduleCompleteRequest;
import com.reday.schedule.dto.ScheduleCreateRequest;
import com.reday.schedule.dto.ScheduleDeferRequest;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GuestDemoData {

	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	private final ScheduleService scheduleService;

	public void createSchedules(Integer memberId) {
		LocalDate today = LocalDate.now();
		scheduleService.createSchedule(memberId, new ScheduleCreateRequest(
			"[예시] 책 읽기", today.atTime(19, 0).format(DATE_TIME_FORMATTER), 30,
			"미루기를 눌러 이유를 고르고 언제 다시 할지 정해보세요."
		));

		Integer completed = scheduleService.createSchedule(memberId, new ScheduleCreateRequest(
			"[예시] 산책하기", today.atTime(9, 0).format(DATE_TIME_FORMATTER), 20, null
		)).scheduleId();
		scheduleService.completeSchedule(memberId, completed, new ScheduleCompleteRequest(25));

		Integer deferred = scheduleService.createSchedule(memberId, new ScheduleCreateRequest(
			"[예시] 발표 자료 정리", today.minusDays(1).atTime(14, 0).format(DATE_TIME_FORMATTER), 45, null
		)).scheduleId();
		scheduleService.deferSchedule(memberId, deferred, new ScheduleDeferRequest(
			"NO_TIME", null, today.atTime(16, 0).format(DATE_TIME_FORMATTER)
		));
	}
}
