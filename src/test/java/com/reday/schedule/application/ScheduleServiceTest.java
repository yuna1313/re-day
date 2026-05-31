package com.reday.schedule.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.reday.global.exception.BusinessException;
import com.reday.schedule.domain.Schedule;
import com.reday.schedule.domain.ScheduleStatus;
import com.reday.schedule.dto.ScheduleCreateRequest;
import com.reday.schedule.dto.ScheduleCreateResponse;
import com.reday.schedule.dto.ScheduleListResponse;
import com.reday.schedule.exception.ScheduleErrorCode;
import com.reday.schedule.repository.ScheduleRepository;

class ScheduleServiceTest {

	private final ScheduleRepository scheduleRepository = org.mockito.Mockito.mock(ScheduleRepository.class);
	private final ScheduleService scheduleService = new ScheduleService(scheduleRepository);

	/**
	 * 조회 기간에 포함된 로그인 사용자의 일정 목록을 시작 일시 오름차순으로 조회합니다.
	 */
	@Test
	void getSchedulesSucceedsWithValidDateRange() {
		Schedule schedule = Schedule.create(
			1,
			"운동하기",
			LocalDateTime.of(2026, 1, 9, 8, 0),
			15,
			20,
			null,
			ScheduleStatus.DONE,
			LocalDateTime.of(2026, 1, 9, 8, 25),
			0
		);
		when(scheduleRepository.findByMemberIdxAndDeletedAtIsNullAndStartAtBetweenOrderByStartAtAsc(
			1,
			LocalDateTime.of(2026, 1, 5, 0, 0),
			LocalDateTime.of(2026, 1, 11, 23, 59, 59, 999999999)
		)).thenReturn(List.of(schedule));

		ScheduleListResponse response = scheduleService.getSchedules(
			1,
			"WEEK",
			"2026-01-05",
			"2026-01-11"
		);

		assertThat(response.viewType()).isEqualTo("WEEK");
		assertThat(response.startDate()).isEqualTo("2026-01-05");
		assertThat(response.endDate()).isEqualTo("2026-01-11");
		assertThat(response.schedules()).hasSize(1);
		assertThat(response.schedules().get(0).title()).isEqualTo("운동하기");
		assertThat(response.schedules().get(0).startAt()).isEqualTo("2026-01-09 08:00:00");
		assertThat(response.schedules().get(0).completedAt()).isEqualTo("2026-01-09 08:25:00");
	}

	/**
	 * 조회 시작 날짜가 종료 날짜보다 늦으면 일정 목록 조회를 거부합니다.
	 */
	@Test
	void getSchedulesRejectsReversedDateRange() {
		assertThatThrownBy(() -> scheduleService.getSchedules(
			1,
			"WEEK",
			"2026-01-12",
			"2026-01-11"
		))
			.isInstanceOf(BusinessException.class)
			.extracting(exception -> ((BusinessException)exception).getErrorCode())
			.isEqualTo(ScheduleErrorCode.INVALID_DATE_RANGE);
	}

	/**
	 * 조회 날짜 형식이 올바르지 않으면 일정 목록 조회를 거부합니다.
	 */
	@Test
	void getSchedulesRejectsInvalidDateFormat() {
		assertThatThrownBy(() -> scheduleService.getSchedules(
			1,
			"WEEK",
			"2026/01/05",
			"2026-01-11"
		))
			.isInstanceOf(BusinessException.class)
			.extracting(exception -> ((BusinessException)exception).getErrorCode())
			.isEqualTo(ScheduleErrorCode.INVALID_DATE_RANGE);
	}

	/**
	 * 일정 생성 요청 값이 유효하면 대기 상태 일정을 저장하고 생성된 일정 식별자를 반환합니다.
	 */
	@Test
	void createScheduleSucceedsWithValidRequest() {
		Schedule savedSchedule = Schedule.createNew(
			1,
			"운동하기",
			LocalDateTime.of(2026, 1, 9, 8, 0),
			30,
			"아침 유산소"
		);
		ReflectionTestUtils.setField(savedSchedule, "scheduleIdx", 101);
		when(scheduleRepository.save(any(Schedule.class))).thenReturn(savedSchedule);

		ScheduleCreateResponse response = scheduleService.createSchedule(
			1,
			new ScheduleCreateRequest(" 운동하기 ", "2026-01-09 08:00:00", 30, "아침 유산소")
		);

		assertThat(response.scheduleId()).isEqualTo(101);
	}

	/**
	 * 제목이 비어 있으면 일정 생성을 거부합니다.
	 */
	@Test
	void createScheduleRejectsBlankTitle() {
		assertThatThrownBy(() -> scheduleService.createSchedule(
			1,
			new ScheduleCreateRequest(" ", "2026-01-09 08:00:00", 30, null)
		))
			.isInstanceOf(BusinessException.class)
			.extracting(exception -> ((BusinessException)exception).getErrorCode())
			.isEqualTo(ScheduleErrorCode.INVALID_TITLE);
	}

	/**
	 * 시작 일시 형식이 올바르지 않으면 일정 생성을 거부합니다.
	 */
	@Test
	void createScheduleRejectsInvalidStartAt() {
		assertThatThrownBy(() -> scheduleService.createSchedule(
			1,
			new ScheduleCreateRequest("운동하기", "2026/01/09 08:00:00", 30, null)
		))
			.isInstanceOf(BusinessException.class)
			.extracting(exception -> ((BusinessException)exception).getErrorCode())
			.isEqualTo(ScheduleErrorCode.INVALID_START_AT);
	}

	/**
	 * 예상 소요 시간이 0 이하이면 일정 생성을 거부합니다.
	 */
	@Test
	void createScheduleRejectsInvalidEstimatedMinutes() {
		assertThatThrownBy(() -> scheduleService.createSchedule(
			1,
			new ScheduleCreateRequest("운동하기", "2026-01-09 08:00:00", 0, null)
		))
			.isInstanceOf(BusinessException.class)
			.extracting(exception -> ((BusinessException)exception).getErrorCode())
			.isEqualTo(ScheduleErrorCode.INVALID_ESTIMATED_MINUTES);
	}
}
