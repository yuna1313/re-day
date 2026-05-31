package com.reday.schedule.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.reday.global.exception.BusinessException;
import com.reday.schedule.domain.Schedule;
import com.reday.schedule.domain.ScheduleViewType;
import com.reday.schedule.dto.ScheduleListResponse;
import com.reday.schedule.exception.ScheduleErrorCode;
import com.reday.schedule.repository.ScheduleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleService {

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	private final ScheduleRepository scheduleRepository;

	/**
	 * 로그인한 사용자의 일정 목록을 조회합니다.
	 *
	 * @param memberIdx 로그인 사용자 식별자
	 * @param viewType 요청 화면 유형
	 * @param startDate 조회 시작 날짜
	 * @param endDate 조회 종료 날짜
	 * @return 일정 목록 조회 응답
	 * @throws BusinessException 조회 기간이나 화면 유형이 올바르지 않을 때 발생
	 */
	@Transactional(readOnly = true)
	public ScheduleListResponse getSchedules(
		Integer memberIdx,
		String viewType,
		String startDate,
		String endDate
	) {
		log.info(
			"[getSchedules] 일정 목록 조회 요청: memberIdx={}, viewType={}, startDate={}, endDate={}",
			memberIdx,
			viewType,
			startDate,
			endDate
		);
		ScheduleViewType parsedViewType = ScheduleViewType.from(viewType);
		DateRange dateRange = parseDateRange(startDate, endDate);

		List<Schedule> schedules = scheduleRepository.findByMemberIdxAndDeletedAtIsNullAndStartAtBetweenOrderByStartAtAsc(
			memberIdx,
			dateRange.startAt(),
			dateRange.endAt()
		);
		log.info("[getSchedules] 일정 목록 조회 완료: memberIdx={}, count={}", memberIdx, schedules.size());

		return new ScheduleListResponse(
			parsedViewType.name(),
			dateRange.startDate().format(DATE_FORMATTER),
			dateRange.endDate().format(DATE_FORMATTER),
			schedules.stream()
				.map(this::toSummary)
				.toList()
		);
	}

	/**
	 * 일정 엔티티를 목록 응답 항목으로 변환합니다.
	 *
	 * @param schedule 일정 엔티티
	 * @return 일정 목록 응답 항목
	 */
	private ScheduleListResponse.ScheduleSummary toSummary(Schedule schedule) {
		return new ScheduleListResponse.ScheduleSummary(
			schedule.getScheduleIdx(),
			schedule.getTitle(),
			formatDateTime(schedule.getStartAt()),
			schedule.getEstimatedMinutes(),
			schedule.getActualMinutes(),
			schedule.getStatus().name(),
			formatDateTime(schedule.getCompletedAt()),
			defaultZero(schedule.getDeferCount())
		);
	}

	/**
	 * null인 값을 0으로 변환합니다.
	 *
	 * @param value DB에서 조회한 미루기 횟수
	 * @return null이 아닌 미루기 횟수
	 */
	private Integer defaultZero(Integer value) {
		if (value == null) {
			return 0;
		}

		return value;
	}

	/**
	 * 조회 시작 날짜와 종료 날짜를 조회 가능한 일시 범위로 변환합니다.
	 *
	 * @param startDate 조회 시작 날짜 문자열
	 * @param endDate 조회 종료 날짜 문자열
	 * @return 조회 일시 범위
	 * @throws BusinessException 날짜가 없거나 형식이 올바르지 않거나 시작 날짜가 종료 날짜보다 늦을 때 발생
	 */
	private DateRange parseDateRange(String startDate, String endDate) {
		if (!StringUtils.hasText(startDate) || !StringUtils.hasText(endDate)) {
			log.warn("[parseDateRange] 조회 기간 누락: startDate={}, endDate={}", startDate, endDate);
			throw new BusinessException(ScheduleErrorCode.INVALID_DATE_RANGE);
		}

		try {
			LocalDate parsedStartDate = LocalDate.parse(startDate, DATE_FORMATTER);
			LocalDate parsedEndDate = LocalDate.parse(endDate, DATE_FORMATTER);
			if (parsedStartDate.isAfter(parsedEndDate)) {
				log.warn("[parseDateRange] 조회 기간 역전: startDate={}, endDate={}", startDate, endDate);
				throw new BusinessException(ScheduleErrorCode.INVALID_DATE_RANGE);
			}

			return new DateRange(
				parsedStartDate,
				parsedEndDate,
				parsedStartDate.atStartOfDay(),
				parsedEndDate.atTime(LocalTime.MAX)
			);
		} catch (DateTimeParseException exception) {
			log.warn("[parseDateRange] 조회 기간 형식 오류: startDate={}, endDate={}", startDate, endDate);
			throw new BusinessException(ScheduleErrorCode.INVALID_DATE_RANGE);
		}
	}

	/**
	 * 일시 값을 OpenAPI 문서의 문자열 형식으로 변환합니다.
	 *
	 * @param dateTime 변환할 일시
	 * @return yyyy-MM-dd HH:mm:ss 형식 문자열 또는 null
	 */
	private String formatDateTime(LocalDateTime dateTime) {
		if (dateTime == null) {
			return null;
		}

		return dateTime.format(DATE_TIME_FORMATTER);
	}

	private record DateRange(
		LocalDate startDate,
		LocalDate endDate,
		LocalDateTime startAt,
		LocalDateTime endAt
	) {
	}
}
