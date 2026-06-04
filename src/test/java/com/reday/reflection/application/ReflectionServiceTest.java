package com.reday.reflection.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.reday.reflection.domain.Reflection;
import com.reday.reflection.dto.ReflectionTodayResponse;
import com.reday.reflection.repository.ReflectionRepository;
import com.reday.schedule.domain.Schedule;
import com.reday.schedule.domain.ScheduleStatus;
import com.reday.schedule.repository.ScheduleRepository;

class ReflectionServiceTest {

	private final ReflectionRepository reflectionRepository = org.mockito.Mockito.mock(ReflectionRepository.class);
	private final ScheduleRepository scheduleRepository = org.mockito.Mockito.mock(ScheduleRepository.class);
	private final ReflectionService reflectionService = new ReflectionService(reflectionRepository, scheduleRepository);

	/**
	 * 오늘 작성한 회고와 오늘 완료한 일정 목록을 함께 조회합니다.
	 */
	@Test
	void getTodayReflectionSucceedsWithReflectionAndCompletedSchedules() {
		LocalDate today = LocalDate.now();
		Reflection reflection = Reflection.create(1, "운동은 시간이 안 맞아서 조금 늦게 했다...", today);
		ReflectionTestUtils.setField(reflection, "reflectionIdx", 11);
		Schedule schedule = Schedule.create(
			1,
			"운동하기",
			LocalDateTime.of(today, LocalTime.of(8, 0)),
			15,
			20,
			null,
			ScheduleStatus.DONE,
			LocalDateTime.of(today, LocalTime.of(8, 25)),
			0
		);
		ReflectionTestUtils.setField(schedule, "scheduleIdx", 101);
		when(reflectionRepository.findByMemberIdxAndReflectionDate(1, today))
			.thenReturn(Optional.of(reflection));
		when(scheduleRepository.findByMemberIdxAndDeletedAtIsNullAndStatusAndCompletedAtBetweenOrderByCompletedAtAsc(
			1,
			ScheduleStatus.DONE,
			today.atStartOfDay(),
			LocalDateTime.of(today, LocalTime.MAX)
		)).thenReturn(List.of(schedule));

		ReflectionTodayResponse response = reflectionService.getTodayReflection(1);

		assertThat(response.reflection()).isNotNull();
		assertThat(response.reflection().reflectionId()).isEqualTo(11);
		assertThat(response.reflection().reflectionDate()).isEqualTo(today.toString());
		assertThat(response.reflection().content()).isEqualTo("운동은 시간이 안 맞아서 조금 늦게 했다...");
		assertThat(response.completedSchedules()).hasSize(1);
		assertThat(response.completedSchedules().get(0).scheduleId()).isEqualTo(101);
		assertThat(response.completedSchedules().get(0).title()).isEqualTo("운동하기");
	}

	/**
	 * 오늘 작성한 회고가 없어도 오늘 완료한 일정 목록은 조회합니다.
	 */
	@Test
	void getTodayReflectionSucceedsWithoutReflection() {
		LocalDate today = LocalDate.now();
		when(reflectionRepository.findByMemberIdxAndReflectionDate(1, today))
			.thenReturn(Optional.empty());
		when(scheduleRepository.findByMemberIdxAndDeletedAtIsNullAndStatusAndCompletedAtBetweenOrderByCompletedAtAsc(
			1,
			ScheduleStatus.DONE,
			today.atStartOfDay(),
			LocalDateTime.of(today, LocalTime.MAX)
		)).thenReturn(List.of());

		ReflectionTodayResponse response = reflectionService.getTodayReflection(1);

		assertThat(response.reflection()).isNull();
		assertThat(response.completedSchedules()).isEmpty();
	}
}
