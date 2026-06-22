package com.reday.reflection.application;

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
import com.reday.reflection.domain.Reflection;
import com.reday.reflection.dto.ReflectionCreateRequest;
import com.reday.reflection.dto.ReflectionCreateResponse;
import com.reday.reflection.dto.ReflectionDetailResponse;
import com.reday.reflection.dto.ReflectionTodayResponse;
import com.reday.reflection.dto.ReflectionUpdateRequest;
import com.reday.reflection.exception.ReflectionErrorCode;
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
	 * 로그인한 사용자의 특정 날짜 회고와 해당 날짜에 완료한 일정 목록을 조회합니다.
	 *
	 * @param memberIdx 로그인 사용자 식별자
	 * @param date 조회할 날짜 문자열
	 * @return 날짜별 회고 조회 응답
	 * @throws BusinessException 날짜 형식이 올바르지 않거나 해당 날짜의 회고가 없는 경우 발생
	 */
	@Transactional(readOnly = true)
	public ReflectionDetailResponse getReflectionByDate(Integer memberIdx, String date) {
		log.info("[getReflectionByDate] 날짜별 회고 조회 요청: memberIdx={}, date={}", memberIdx, date);
		LocalDate reflectionDate = parseReflectionDate(memberIdx, date);
		Reflection reflection = reflectionRepository.findByMemberIdxAndReflectionDate(memberIdx, reflectionDate)
			.orElseThrow(() -> {
				log.warn(
					"[getReflectionByDate] 회고 없음: memberIdx={}, reflectionDate={}",
					memberIdx,
					reflectionDate
				);
				return new BusinessException(ReflectionErrorCode.NOT_FOUND);
			});

		List<Schedule> completedSchedules =
			scheduleRepository.findByMemberIdxAndDeletedAtIsNullAndStatusAndCompletedAtBetweenOrderByCompletedAtAsc(
				memberIdx,
				ScheduleStatus.DONE,
				reflectionDate.atStartOfDay(),
				LocalDateTime.of(reflectionDate, LocalTime.MAX)
			);

		log.info(
			"[getReflectionByDate] 날짜별 회고 조회 완료: memberIdx={}, reflectionDate={}, completedScheduleCount={}",
			memberIdx,
			reflectionDate,
			completedSchedules.size()
		);

		return new ReflectionDetailResponse(
			reflection.getReflectionIdx(),
			reflection.getReflectionDate().format(DATE_FORMATTER),
			reflection.getContent(),
			completedSchedules.stream()
				.map(this::toDetailCompletedScheduleSummary)
				.toList()
		);
	}

	/**
	 * 로그인한 사용자의 회고를 작성합니다.
	 *
	 * @param memberIdx 로그인 사용자 식별자
	 * @param request 회고 작성 요청
	 * @return 회고 작성 응답
	 * @throws BusinessException 날짜나 내용이 올바르지 않거나 같은 날짜 회고가 이미 있는 경우 발생
	 */
	@Transactional
	public ReflectionCreateResponse createReflection(Integer memberIdx, ReflectionCreateRequest request) {
		log.info("[createReflection] 회고 작성 요청: memberIdx={}", memberIdx);
		if (request == null) {
			log.warn("[createReflection] 요청 본문 누락: memberIdx={}", memberIdx);
			throw new BusinessException(ReflectionErrorCode.CREATE_FAIL);
		}

		LocalDate reflectionDate = parseReflectionDate(memberIdx, request.reflectionDate());
		String content = validateContent(memberIdx, reflectionDate, request.content());
		if (reflectionRepository.existsByMemberIdxAndReflectionDate(memberIdx, reflectionDate)) {
			log.warn("[createReflection] 같은 날짜 회고 이미 존재: memberIdx={}, reflectionDate={}", memberIdx, reflectionDate);
			throw new BusinessException(ReflectionErrorCode.ALREADY_EXISTS);
		}

		Reflection savedReflection = reflectionRepository.save(Reflection.create(memberIdx, content, reflectionDate));
		log.info(
			"[createReflection] 회고 작성 완료: memberIdx={}, reflectionDate={}, reflectionId={}",
			memberIdx,
			reflectionDate,
			savedReflection.getReflectionIdx()
		);

		return new ReflectionCreateResponse(savedReflection.getReflectionIdx());
	}

	/**
	 * 로그인한 사용자의 회고 내용을 수정합니다.
	 *
	 * @param memberIdx 로그인 사용자 식별자
	 * @param reflectionId 수정할 회고 식별자
	 * @param request 회고 수정 요청
	 * @throws BusinessException 수정 대상 회고가 없거나 내용이 올바르지 않은 경우 발생
	 */
	@Transactional
	public void updateReflection(Integer memberIdx, Integer reflectionId, ReflectionUpdateRequest request) {
		log.info("[updateReflection] 회고 수정 요청: memberIdx={}, reflectionId={}", memberIdx, reflectionId);
		if (request == null) {
			log.warn("[updateReflection] 요청 본문 누락: memberIdx={}, reflectionId={}", memberIdx, reflectionId);
			throw new BusinessException(ReflectionErrorCode.UPDATE_FAIL);
		}

		Reflection reflection = reflectionRepository.findByReflectionIdxAndMemberIdx(reflectionId, memberIdx)
			.orElseThrow(() -> {
				log.warn("[updateReflection] 수정 대상 회고 없음: memberIdx={}, reflectionId={}", memberIdx, reflectionId);
				return new BusinessException(ReflectionErrorCode.NOT_FOUND);
			});
		String content = validateContent(memberIdx, reflection.getReflectionDate(), request.content());

		reflection.updateContent(content);
		log.info("[updateReflection] 회고 수정 완료: memberIdx={}, reflectionId={}", memberIdx, reflectionId);
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

	/**
	 * 일정 엔티티를 날짜별 회고의 완료 일정 응답 항목으로 변환합니다.
	 *
	 * @param schedule 일정 엔티티
	 * @return 날짜별 회고 완료 일정 응답 항목
	 */
	private ReflectionDetailResponse.CompletedScheduleSummary toDetailCompletedScheduleSummary(Schedule schedule) {
		return new ReflectionDetailResponse.CompletedScheduleSummary(
			schedule.getScheduleIdx(),
			schedule.getTitle()
		);
	}

	/**
	 * 회고 조회 날짜 문자열을 날짜 값으로 변환합니다.
	 *
	 * @param memberIdx 로그인 사용자 식별자
	 * @param date 조회할 날짜 문자열
	 * @return 변환된 회고 조회 날짜
	 * @throws BusinessException 날짜 형식이 올바르지 않은 경우 발생
	 */
	private LocalDate parseReflectionDate(Integer memberIdx, String date) {
		try {
			return LocalDate.parse(date, DATE_FORMATTER);
		} catch (DateTimeParseException | NullPointerException exception) {
			log.warn("[parseReflectionDate] 회고 날짜 형식 오류: memberIdx={}, date={}", memberIdx, date);
			throw new BusinessException(ReflectionErrorCode.INVALID_DATE);
		}
	}

	/**
	 * 회고 내용을 검증하고 저장 가능한 값으로 정리합니다.
	 *
	 * @param memberIdx 로그인 사용자 식별자
	 * @param reflectionDate 회고 날짜
	 * @param content 요청 회고 내용
	 * @return 앞뒤 공백을 제거한 회고 내용
	 * @throws BusinessException 회고 내용이 비어 있는 경우 발생
	 */
	private String validateContent(Integer memberIdx, LocalDate reflectionDate, String content) {
		if (!StringUtils.hasText(content)) {
			log.warn("[validateContent] 회고 내용 누락: memberIdx={}, reflectionDate={}", memberIdx, reflectionDate);
			throw new BusinessException(ReflectionErrorCode.EMPTY_CONTENT);
		}

		return content.trim();
	}
}
