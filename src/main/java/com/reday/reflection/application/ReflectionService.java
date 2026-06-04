package com.reday.reflection.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reday.reflection.domain.Reflection;
import com.reday.reflection.dto.ReflectionTodayResponse;
import com.reday.reflection.repository.ReflectionRepository;
import com.reday.schedule.domain.Schedule;
import com.reday.schedule.domain.ScheduleStatus;
import com.reday.schedule.repository.ScheduleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReflectionService {

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

	private final ReflectionRepository reflectionRepository;
	private final ScheduleRepository scheduleRepository;

	/**
	 * 로그인한 사용자의 오늘 회고와 오늘 완료한 일정 목록을 조회합니다.
	 *
	 * @param memberIdx 로그인 사용자 식별자
	 * @return 오늘 회고 조회 응답
	 */
	@Transactional(readOnly = true)
	public ReflectionTodayResponse getTodayReflection(Integer memberIdx) {
		LocalDate today = LocalDate.now();
		log.info("[getTodayReflection] 오늘 회고 조회 요청: memberIdx={}, today={}", memberIdx, today);

		ReflectionTodayResponse.ReflectionSummary reflection = reflectionRepository
			.findByMemberIdxAndReflectionDate(memberIdx, today)
			.map(this::toReflectionSummary)
			.orElse(null);

		List<Schedule> completedSchedules =
			scheduleRepository.findByMemberIdxAndDeletedAtIsNullAndStatusAndCompletedAtBetweenOrderByCompletedAtAsc(
				memberIdx,
				ScheduleStatus.DONE,
				today.atStartOfDay(),
				LocalDateTime.of(today, LocalTime.MAX)
			);

		log.info(
			"[getTodayReflection] 오늘 회고 조회 완료: memberIdx={}, today={}, hasReflection={}, completedScheduleCount={}",
			memberIdx,
			today,
			reflection != null,
			completedSchedules.size()
		);

		return new ReflectionTodayResponse(
			reflection,
			completedSchedules.stream()
				.map(this::toCompletedScheduleSummary)
				.toList()
		);
	}

	/**
	 * 회고 엔티티를 오늘 회고 응답 항목으로 변환합니다.
	 *
	 * @param reflection 회고 엔티티
	 * @return 오늘 회고 응답 항목
	 */
	private ReflectionTodayResponse.ReflectionSummary toReflectionSummary(Reflection reflection) {
		return new ReflectionTodayResponse.ReflectionSummary(
			reflection.getReflectionIdx(),
			reflection.getReflectionDate().format(DATE_FORMATTER),
			reflection.getContent()
		);
	}

	/**
	 * 일정 엔티티를 완료 일정 응답 항목으로 변환합니다.
	 *
	 * @param schedule 일정 엔티티
	 * @return 완료 일정 응답 항목
	 */
	private ReflectionTodayResponse.CompletedScheduleSummary toCompletedScheduleSummary(Schedule schedule) {
		return new ReflectionTodayResponse.CompletedScheduleSummary(
			schedule.getScheduleIdx(),
			schedule.getTitle()
		);
	}
}
