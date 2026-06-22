package com.reday.analytics.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.reday.analytics.dto.InsightResponse;
import com.reday.analytics.exception.AnalyticsErrorCode;
import com.reday.global.exception.BusinessException;
import com.reday.schedule.domain.Schedule;
import com.reday.schedule.domain.ScheduleActionLog;
import com.reday.schedule.domain.ScheduleActionType;
import com.reday.schedule.domain.ScheduleStatus;
import com.reday.schedule.repository.ScheduleActionLogRepository;
import com.reday.schedule.repository.ScheduleRepository;

class AnalyticsServiceTest {

	private final ScheduleRepository scheduleRepository = org.mockito.Mockito.mock(ScheduleRepository.class);
	private final ScheduleActionLogRepository scheduleActionLogRepository =
		org.mockito.Mockito.mock(ScheduleActionLogRepository.class);
	private final AnalyticsService analyticsService = new AnalyticsService(scheduleRepository, scheduleActionLogRepository);

	/**
	 * 조회 기간 안의 일정과 미루기 로그를 기반으로 인사이트를 계산합니다.
	 */
	@Test
	void getInsightsSucceedsWithScheduleStatistics() {
		LocalDate today = LocalDate.now();
		Schedule morningDone = Schedule.create(
			1,
			"운동하기",
			today.atTime(8, 0),
			30,
			40,
			null,
			ScheduleStatus.DONE,
			today.atTime(8, 40),
			0
		);
		ReflectionTestUtils.setField(morningDone, "scheduleIdx", 101);
		Schedule afternoonPending = Schedule.create(
			1,
			"NCS 공부하기",
			today.atTime(13, 30),
			60,
			null,
			null,
			ScheduleStatus.PENDING,
			null,
			1
		);
		ReflectionTestUtils.setField(afternoonPending, "scheduleIdx", 102);
		ScheduleActionLog deferLog = ScheduleActionLog.create(
			102,
			ScheduleActionType.DEFERRED,
			"NO_TIME",
			null,
			today.atTime(14, 0)
		);
		when(scheduleRepository.findByMemberIdxAndDeletedAtIsNullAndStartAtBetweenOrderByStartAtAsc(
			eq(1),
			any(LocalDateTime.class),
			any(LocalDateTime.class)
		)).thenReturn(List.of(morningDone, afternoonPending));
		when(scheduleActionLogRepository.findByScheduleIdxInAndActionTypeAndActionAtBetween(
			eq(List.of(101, 102)),
			eq(ScheduleActionType.DEFERRED),
			any(LocalDateTime.class),
			any(LocalDateTime.class)
		)).thenReturn(List.of(deferLog));

		InsightResponse response = analyticsService.getInsights(1, "LAST_7_DAYS");

		assertThat(response.periodType()).isEqualTo("LAST_7_DAYS");
		assertThat(response.timeSlotCompletionRates()).hasSize(3);
		assertThat(response.timeSlotCompletionRates().get(0).timeSlot()).isEqualTo("MORNING");
		assertThat(response.timeSlotCompletionRates().get(0).completionRate()).isEqualTo(100);
		assertThat(response.timeSlotCompletionRates().get(1).timeSlot()).isEqualTo("AFTERNOON");
		assertThat(response.timeSlotCompletionRates().get(1).completionRate()).isZero();
		assertThat(response.topDeferReasons()).hasSize(1);
		assertThat(response.topDeferReasons().get(0).deferReasonCode()).isEqualTo("NO_TIME");
		assertThat(response.topDeferReasons().get(0).label()).isEqualTo("시간이 없었음");
		assertThat(response.topDeferReasons().get(0).count()).isEqualTo(1);
		assertThat(response.estimatedVsActual().averageEstimatedMinutes()).isEqualTo(30);
		assertThat(response.estimatedVsActual().averageActualMinutes()).isEqualTo(40);
		assertThat(response.estimatedVsActual().averageDiffMinutes()).isEqualTo(10);
		assertThat(response.feedbackMessages()).isNotEmpty();
	}

	/**
	 * 조회 기간 유형이 없으면 최근 30일 기준으로 인사이트를 조회합니다.
	 */
	@Test
	void getInsightsUsesLast30DaysWhenPeriodTypeIsBlank() {
		when(scheduleRepository.findByMemberIdxAndDeletedAtIsNullAndStartAtBetweenOrderByStartAtAsc(
			eq(1),
			any(LocalDateTime.class),
			any(LocalDateTime.class)
		)).thenReturn(List.of());

		InsightResponse response = analyticsService.getInsights(1, null);

		assertThat(response.periodType()).isEqualTo("LAST_30_DAYS");
		assertThat(response.timeSlotCompletionRates()).hasSize(3);
		assertThat(response.topDeferReasons()).isEmpty();
		assertThat(response.estimatedVsActual().averageEstimatedMinutes()).isZero();
	}

	/**
	 * 지원하지 않는 조회 기간 유형이면 오류로 처리합니다.
	 */
	@Test
	void getInsightsRejectsInvalidPeriodType() {
		assertThatThrownBy(() -> analyticsService.getInsights(1, "LAST_365_DAYS"))
			.isInstanceOf(BusinessException.class)
			.extracting(exception -> ((BusinessException)exception).getErrorCode())
			.isEqualTo(AnalyticsErrorCode.INVALID_PERIOD);
	}
}
