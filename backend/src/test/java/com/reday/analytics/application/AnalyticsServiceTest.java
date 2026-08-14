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
		when(scheduleActionLogRepository.findMemberActionLogs(
			eq(1),
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
	 * 미뤄서 시작 일시가 조회 기간 밖으로 밀려난 일정이라도, 기간 안에 남은 미루기 기록은 사유 집계에 포함합니다.
	 * (일정을 기간으로 먼저 걸러 로그를 찾으면 많이 미룬 일정일수록 집계에서 빠지는 문제)
	 */
	@Test
	void getInsightsCountsDeferReasonsOfSchedulesPushedOutOfPeriod() {
		LocalDate today = LocalDate.now();
		// 여러 번 미뤄 시작 일시가 미래(기간 밖)로 옮겨진 일정의 로그
		ScheduleActionLog deferLog = ScheduleActionLog.create(
			103,
			ScheduleActionType.DEFERRED,
			"COULD_NOT_FOCUS",
			null,
			today.minusDays(11).atTime(14, 0)
		);

		// 조회 기간 안에는 일정이 하나도 없다
		when(scheduleRepository.findByMemberIdxAndDeletedAtIsNullAndStartAtBetweenOrderByStartAtAsc(
			eq(1),
			any(LocalDateTime.class),
			any(LocalDateTime.class)
		)).thenReturn(List.of());
		when(scheduleActionLogRepository.findMemberActionLogs(
			eq(1),
			eq(ScheduleActionType.DEFERRED),
			any(LocalDateTime.class),
			any(LocalDateTime.class)
		)).thenReturn(List.of(deferLog));

		InsightResponse response = analyticsService.getInsights(1, "LAST_30_DAYS");

		assertThat(response.topDeferReasons()).hasSize(1);
		assertThat(response.topDeferReasons().get(0).deferReasonCode()).isEqualTo("COULD_NOT_FOCUS");
		assertThat(response.topDeferReasons().get(0).count()).isEqualTo(1);
	}

	/**
	 * 조회 기간 안의 미루기 로그를 일정별로 묶어 많이 미룬 순으로 반환합니다.
	 * 미루기 상위 이유와 같은 로그를 쓰므로 두 통계의 합계가 서로 어긋나지 않습니다.
	 */
	@Test
	void getInsightsReturnsTopDeferredSchedulesFromSameDeferLogs() {
		LocalDate today = LocalDate.now();
		Schedule mostDeferred = Schedule.create(
			1,
			"이력서 수정",
			today.plusDays(10).atTime(10, 0),
			30,
			null,
			null,
			ScheduleStatus.PENDING,
			null,
			5
		);
		ReflectionTestUtils.setField(mostDeferred, "scheduleIdx", 103);
		Schedule secondDeferred = Schedule.create(
			1,
			"방 정리",
			today.plusDays(3).atTime(15, 0),
			45,
			null,
			null,
			ScheduleStatus.PENDING,
			null,
			2
		);
		ReflectionTestUtils.setField(secondDeferred, "scheduleIdx", 104);
		// 103 을 2번, 104 를 1번 미룬 로그
		List<ScheduleActionLog> deferLogs = List.of(
			ScheduleActionLog.create(103, ScheduleActionType.DEFERRED, "NO_TIME", null, today.atTime(9, 0)),
			ScheduleActionLog.create(103, ScheduleActionType.DEFERRED, "TOO_BIG", null, today.atTime(10, 0)),
			ScheduleActionLog.create(104, ScheduleActionType.DEFERRED, "NO_TIME", null, today.atTime(11, 0))
		);

		// 조회 기간 안에는 일정이 하나도 없는 상황 (미뤄서 시작 일시가 미래로 밀린 경우)
		when(scheduleRepository.findByMemberIdxAndDeletedAtIsNullAndStartAtBetweenOrderByStartAtAsc(
			eq(1),
			any(LocalDateTime.class),
			any(LocalDateTime.class)
		)).thenReturn(List.of());
		when(scheduleActionLogRepository.findMemberActionLogs(
			eq(1),
			eq(ScheduleActionType.DEFERRED),
			any(LocalDateTime.class),
			any(LocalDateTime.class)
		)).thenReturn(deferLogs);
		when(scheduleRepository.findByScheduleIdxInAndMemberIdxAndDeletedAtIsNull(List.of(103, 104), 1))
			.thenReturn(List.of(mostDeferred, secondDeferred));

		InsightResponse response = analyticsService.getInsights(1, "LAST_30_DAYS");

		assertThat(response.topDeferredSchedules()).hasSize(2);
		assertThat(response.topDeferredSchedules().get(0).rank()).isEqualTo(1);
		assertThat(response.topDeferredSchedules().get(0).scheduleId()).isEqualTo(103);
		assertThat(response.topDeferredSchedules().get(0).title()).isEqualTo("이력서 수정");
		assertThat(response.topDeferredSchedules().get(0).deferCount()).isEqualTo(2);
		assertThat(response.topDeferredSchedules().get(1).rank()).isEqualTo(2);
		assertThat(response.topDeferredSchedules().get(1).deferCount()).isEqualTo(1);
		assertThat(response.feedbackMessages()).contains("'이력서 수정'을(를) 2번 미뤘어요.");

		// 두 통계는 같은 로그에서 나오므로 합계가 일치한다
		int reasonTotal = response.topDeferReasons().stream()
			.mapToInt(InsightResponse.TopDeferReason::count)
			.sum();
		int scheduleTotal = response.topDeferredSchedules().stream()
			.mapToInt(InsightResponse.TopDeferredSchedule::deferCount)
			.sum();
		assertThat(reasonTotal).isEqualTo(scheduleTotal);
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
