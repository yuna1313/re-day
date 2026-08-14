package com.reday.schedule.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.reday.global.exception.BusinessException;
import com.reday.schedule.domain.Schedule;
import com.reday.schedule.domain.ScheduleActionLog;
import com.reday.schedule.domain.ScheduleActionType;
import com.reday.schedule.domain.ScheduleStatus;
import com.reday.schedule.domain.ScheduleViewType;
import com.reday.schedule.dto.ScheduleCompleteRequest;
import com.reday.schedule.dto.ScheduleCompleteResponse;
import com.reday.schedule.dto.ScheduleCreateRequest;
import com.reday.schedule.dto.ScheduleCreateResponse;
import com.reday.schedule.dto.ScheduleDeferRequest;
import com.reday.schedule.dto.ScheduleDeferResponse;
import com.reday.schedule.dto.ScheduleDetailResponse;
import com.reday.schedule.dto.ScheduleListResponse;
import com.reday.schedule.dto.ScheduleSearchResponse;
import com.reday.schedule.dto.ScheduleUpdateRequest;
import com.reday.schedule.exception.ScheduleErrorCode;
import com.reday.schedule.repository.ScheduleActionLogRepository;
import com.reday.schedule.repository.ScheduleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleService {

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	private static final int MAX_TITLE_LENGTH = 500;
	private static final int MAX_ESTIMATED_MINUTES = 1440;
	private static final int MAX_DEFER_REASON_DETAIL_LENGTH = 500;
	private static final int MAX_KEYWORD_LENGTH = 100;
	// ScheduleRepository 의 findTop50... 조회 개수와 반드시 같아야 한다.
	private static final int MAX_SEARCH_RESULTS = 50;
	private static final String CUSTOM_DEFER_REASON_CODE = "CUSTOM";
	private static final Set<String> ALLOWED_DEFER_REASON_CODES = Set.of(
		"LONGER_THAN_EXPECTED",
		"NOT_STARTED",
		"NO_TIME",
		"COULD_NOT_FOCUS",
		"TOO_BIG",
		CUSTOM_DEFER_REASON_CODE
	);

	private final ScheduleRepository scheduleRepository;
	private final ScheduleActionLogRepository scheduleActionLogRepository;

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
	 * 로그인한 사용자의 일정 중 제목에 검색어가 포함된 일정을 최근 순으로 조회합니다.
	 *
	 * @param memberIdx 로그인 사용자 식별자
	 * @param keyword 검색어
	 * @return 일정 검색 응답. 결과가 최대 조회 개수까지 찼으면 hasMore 가 true
	 * @throws BusinessException 검색어가 비어 있거나 허용 길이를 넘었을 때 발생
	 */
	@Transactional(readOnly = true)
	public ScheduleSearchResponse searchSchedules(Integer memberIdx, String keyword) {
		log.info("[searchSchedules] 일정 검색 요청: memberIdx={}", memberIdx);
		String trimmedKeyword = validateKeyword(memberIdx, keyword);

		List<Schedule> schedules =
			scheduleRepository.findTop50ByMemberIdxAndDeletedAtIsNullAndTitleContainingOrderByStartAtDesc(
				memberIdx,
				trimmedKeyword
			);
		log.info("[searchSchedules] 일정 검색 완료: memberIdx={}, count={}", memberIdx, schedules.size());

		return new ScheduleSearchResponse(
			trimmedKeyword,
			schedules.size() >= MAX_SEARCH_RESULTS,
			schedules.stream()
				.map(this::toSummary)
				.toList()
		);
	}

	/**
	 * 로그인한 사용자의 새 일정을 생성합니다.
	 *
	 * @param memberIdx 로그인 사용자 식별자
	 * @param request 일정 생성 요청
	 * @return 생성된 일정 식별자 응답
	 * @throws BusinessException 제목, 시작 일시, 예상 시간이 올바르지 않을 때 발생
	 */
	@Transactional
	public ScheduleCreateResponse createSchedule(Integer memberIdx, ScheduleCreateRequest request) {
		log.info("[createSchedule] 일정 생성 요청: memberIdx={}", memberIdx);
		if (request == null) {
			log.warn("[createSchedule] 요청 본문 누락: memberIdx={}", memberIdx);
			throw new BusinessException(ScheduleErrorCode.CREATE_FAIL);
		}

		String title = validateTitle(memberIdx, request.title());
		LocalDateTime startAt = parseStartAt(memberIdx, request.startAt());
		Integer estimatedMinutes = validateEstimatedMinutes(memberIdx, request.estimatedMinutes());

		Schedule schedule = Schedule.createNew(
			memberIdx,
			title,
			startAt,
			estimatedMinutes,
			request.memo()
		);
		Schedule savedSchedule = scheduleRepository.save(schedule);
		log.info("[createSchedule] 일정 생성 완료: memberIdx={}, scheduleId={}", memberIdx, savedSchedule.getScheduleIdx());

		return new ScheduleCreateResponse(savedSchedule.getScheduleIdx());
	}

	/**
	 * 로그인한 사용자의 일정 상세 정보를 조회합니다.
	 *
	 * @param memberIdx 로그인 사용자 식별자
	 * @param scheduleId 조회할 일정 식별자
	 * @return 일정 상세 조회 응답
	 * @throws BusinessException 조회 대상 일정이 없을 때 발생
	 */
	@Transactional(readOnly = true)
	public ScheduleDetailResponse getScheduleDetail(Integer memberIdx, Integer scheduleId) {
		log.info("[getScheduleDetail] 일정 상세 조회 요청: memberIdx={}, scheduleId={}", memberIdx, scheduleId);
		Schedule schedule = scheduleRepository.findByScheduleIdxAndMemberIdxAndDeletedAtIsNull(scheduleId, memberIdx)
			.orElseThrow(() -> {
				log.warn("[getScheduleDetail] 조회 대상 일정 없음: memberIdx={}, scheduleId={}", memberIdx, scheduleId);
				return new BusinessException(ScheduleErrorCode.NOT_FOUND);
			});
		List<ScheduleActionLog> actionLogs = scheduleActionLogRepository.findByScheduleIdxOrderByActionAtAsc(scheduleId);

		log.info(
			"[getScheduleDetail] 일정 상세 조회 완료: memberIdx={}, scheduleId={}, actionLogCount={}",
			memberIdx,
			scheduleId,
			actionLogs.size()
		);
		return toDetailResponse(schedule, actionLogs);
	}

	/**
	 * 로그인한 사용자의 기존 일정을 수정합니다.
	 *
	 * @param memberIdx 로그인 사용자 식별자
	 * @param scheduleId 수정할 일정 식별자
	 * @param request 일정 수정 요청
	 * @throws BusinessException 수정 대상이 없거나 요청 값이 올바르지 않을 때 발생
	 */
	@Transactional
	public void updateSchedule(Integer memberIdx, Integer scheduleId, ScheduleUpdateRequest request) {
		log.info("[updateSchedule] 일정 수정 요청: memberIdx={}, scheduleId={}", memberIdx, scheduleId);
		if (request == null) {
			log.warn("[updateSchedule] 요청 본문 누락: memberIdx={}, scheduleId={}", memberIdx, scheduleId);
			throw new BusinessException(ScheduleErrorCode.UPDATE_FAIL);
		}

		Schedule schedule = scheduleRepository.findByScheduleIdxAndMemberIdxAndDeletedAtIsNull(scheduleId, memberIdx)
			.orElseThrow(() -> {
				log.warn("[updateSchedule] 수정 대상 일정 없음: memberIdx={}, scheduleId={}", memberIdx, scheduleId);
				return new BusinessException(ScheduleErrorCode.NOT_FOUND);
			});

		String title = validateTitle(memberIdx, request.title());
		LocalDateTime startAt = parseStartAt(memberIdx, request.startAt());
		Integer estimatedMinutes = validateEstimatedMinutes(memberIdx, request.estimatedMinutes());

		schedule.update(title, startAt, estimatedMinutes, request.memo());
		log.info("[updateSchedule] 일정 수정 완료: memberIdx={}, scheduleId={}", memberIdx, scheduleId);
	}

	/**
	 * 로그인한 사용자의 기존 일정을 실제 삭제하지 않고 삭제 일시를 기록합니다.
	 *
	 * @param memberIdx 로그인 사용자 식별자
	 * @param scheduleId 삭제할 일정 식별자
	 * @throws BusinessException 삭제 대상 일정이 없거나 이미 삭제된 경우 발생
	 */
	@Transactional
	public void deleteSchedule(Integer memberIdx, Integer scheduleId) {
		log.info("[deleteSchedule] 일정 삭제 요청: memberIdx={}, scheduleId={}", memberIdx, scheduleId);
		Schedule schedule = scheduleRepository.findByScheduleIdxAndMemberIdxAndDeletedAtIsNull(scheduleId, memberIdx)
			.orElseThrow(() -> {
				log.warn("[deleteSchedule] 삭제 대상 일정 없음: memberIdx={}, scheduleId={}", memberIdx, scheduleId);
				return new BusinessException(ScheduleErrorCode.NOT_FOUND);
			});

		schedule.delete();
		log.info("[deleteSchedule] 일정 삭제 완료: memberIdx={}, scheduleId={}", memberIdx, scheduleId);
	}

	/**
	 * 로그인한 사용자의 기존 일정을 완료 처리하고 실제 소요 시간을 저장합니다.
	 *
	 * @param memberIdx 로그인 사용자 식별자
	 * @param scheduleId 완료 처리할 일정 식별자
	 * @param request 일정 완료 처리 요청
	 * @return 일정 완료 처리 응답
	 * @throws BusinessException 완료 대상 일정이 없거나 요청 값이 올바르지 않은 경우 발생
	 */
	@Transactional
	public ScheduleCompleteResponse completeSchedule(
		Integer memberIdx,
		Integer scheduleId,
		ScheduleCompleteRequest request
	) {
		log.info("[completeSchedule] 일정 완료 요청: memberIdx={}, scheduleId={}", memberIdx, scheduleId);
		if (request == null) {
			log.warn("[completeSchedule] 요청 본문 누락: memberIdx={}, scheduleId={}", memberIdx, scheduleId);
			throw new BusinessException(ScheduleErrorCode.COMPLETE_FAIL);
		}

		Integer actualMinutes = validateActualMinutes(memberIdx, scheduleId, request.actualMinutes());
		Schedule schedule = scheduleRepository.findByScheduleIdxAndMemberIdxAndDeletedAtIsNull(scheduleId, memberIdx)
			.orElseThrow(() -> {
				log.warn("[completeSchedule] 완료 대상 일정 없음: memberIdx={}, scheduleId={}", memberIdx, scheduleId);
				return new BusinessException(ScheduleErrorCode.NOT_FOUND);
			});

		if (schedule.getStatus() == ScheduleStatus.DONE) {
			log.warn("[completeSchedule] 이미 완료된 일정: memberIdx={}, scheduleId={}", memberIdx, scheduleId);
			throw new BusinessException(ScheduleErrorCode.ALREADY_DONE);
		}

		schedule.complete(actualMinutes);
		scheduleActionLogRepository.save(ScheduleActionLog.create(
			scheduleId,
			ScheduleActionType.DONE,
			null,
			null,
			schedule.getCompletedAt()
		));
		log.info(
			"[completeSchedule] 일정 완료 처리 완료: memberIdx={}, scheduleId={}, actualMinutes={}",
			memberIdx,
			scheduleId,
			actualMinutes
		);

		return new ScheduleCompleteResponse(
			schedule.getScheduleIdx(),
			schedule.getStatus().name(),
			schedule.getActualMinutes(),
			formatDateTime(schedule.getCompletedAt())
		);
	}

	/**
	 * 로그인한 사용자의 기존 일정을 미루고 미루기 사유를 처리 로그에 기록합니다.
	 *
	 * @param memberIdx 로그인 사용자 식별자
	 * @param scheduleId 미루기 처리할 일정 식별자
	 * @param request 일정 미루기 처리 요청
	 * @return 일정 미루기 처리 응답
	 * @throws BusinessException 미루기 대상 일정이 없거나 요청 값이 올바르지 않은 경우 발생
	 */
	@Transactional
	public ScheduleDeferResponse deferSchedule(
		Integer memberIdx,
		Integer scheduleId,
		ScheduleDeferRequest request
	) {
		log.info("[deferSchedule] 일정 미루기 요청: memberIdx={}, scheduleId={}", memberIdx, scheduleId);
		if (request == null) {
			log.warn("[deferSchedule] 요청 본문 누락: memberIdx={}, scheduleId={}", memberIdx, scheduleId);
			throw new BusinessException(ScheduleErrorCode.DEFER_FAIL);
		}

		String deferReasonCode = validateDeferReasonCode(memberIdx, scheduleId, request.deferReasonCode());
		String deferReasonDetail = validateDeferReasonDetail(
			memberIdx,
			scheduleId,
			deferReasonCode,
			request.deferReasonDetail()
		);
		LocalDateTime newStartAt = parseNewStartAt(memberIdx, scheduleId, request.newStartAt());
		Schedule schedule = scheduleRepository.findByScheduleIdxAndMemberIdxAndDeletedAtIsNull(scheduleId, memberIdx)
			.orElseThrow(() -> {
				log.warn("[deferSchedule] 미루기 대상 일정 없음: memberIdx={}, scheduleId={}", memberIdx, scheduleId);
				return new BusinessException(ScheduleErrorCode.NOT_FOUND);
			});

		if (schedule.getStatus() == ScheduleStatus.DONE) {
			log.warn("[deferSchedule] 이미 완료된 일정: memberIdx={}, scheduleId={}", memberIdx, scheduleId);
			throw new BusinessException(ScheduleErrorCode.ALREADY_DONE);
		}

		schedule.defer(newStartAt);
		scheduleActionLogRepository.save(ScheduleActionLog.create(
			scheduleId,
			ScheduleActionType.DEFERRED,
			deferReasonCode,
			deferReasonDetail,
			schedule.getUpdatedAt()
		));
		log.info(
			"[deferSchedule] 일정 미루기 완료: memberIdx={}, scheduleId={}, deferReasonCode={}, deferCount={}",
			memberIdx,
			scheduleId,
			deferReasonCode,
			schedule.getDeferCount()
		);

		return new ScheduleDeferResponse(
			schedule.getScheduleIdx(),
			schedule.getStatus().name(),
			formatDateTime(schedule.getStartAt()),
			defaultZero(schedule.getDeferCount())
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
	 * 일정 엔티티를 상세 조회 응답으로 변환합니다.
	 *
	 * @param schedule 일정 엔티티
	 * @return 일정 상세 조회 응답
	 */
	private ScheduleDetailResponse toDetailResponse(Schedule schedule, List<ScheduleActionLog> actionLogs) {
		return new ScheduleDetailResponse(
			schedule.getScheduleIdx(),
			schedule.getTitle(),
			formatDateTime(schedule.getStartAt()),
			schedule.getEstimatedMinutes(),
			schedule.getActualMinutes(),
			schedule.getMemo(),
			schedule.getStatus().name(),
			formatDateTime(schedule.getCompletedAt()),
			formatDateTime(schedule.getCreatedAt()),
			formatDateTime(schedule.getUpdatedAt()),
			defaultZero(schedule.getDeferCount()),
			actionLogs.stream()
				.map(this::toDeferLogResponse)
				.toList()
		);
	}

	/**
	 * 일정 처리 로그 엔티티를 상세 조회 응답의 로그 항목으로 변환합니다.
	 *
	 * @param actionLog 일정 처리 로그 엔티티
	 * @return 일정 처리 로그 응답 항목
	 */
	private ScheduleDetailResponse.DeferLog toDeferLogResponse(ScheduleActionLog actionLog) {
		return new ScheduleDetailResponse.DeferLog(
			actionLog.getScheduleActionLogIdx(),
			actionLog.getActionType().name(),
			actionLog.getDeferReasonCode(),
			actionLog.getDeferReasonDetail(),
			formatDateTime(actionLog.getActionAt())
		);
	}

	/**
	 * 일정 제목을 검증하고 저장 가능한 값으로 정리합니다.
	 *
	 * @param memberIdx 로그인 사용자 식별자
	 * @param title 요청 제목
	 * @return 앞뒤 공백이 제거된 제목
	 * @throws BusinessException 제목이 비어 있거나 허용 길이를 초과할 때 발생
	 */
	private String validateTitle(Integer memberIdx, String title) {
		if (!StringUtils.hasText(title)) {
			log.warn("[validateTitle] 일정 제목 누락: memberIdx={}", memberIdx);
			throw new BusinessException(ScheduleErrorCode.INVALID_TITLE);
		}

		String trimmedTitle = title.trim();
		if (trimmedTitle.length() > MAX_TITLE_LENGTH) {
			log.warn("[validateTitle] 일정 제목 길이 초과: memberIdx={}, length={}", memberIdx, trimmedTitle.length());
			throw new BusinessException(ScheduleErrorCode.INVALID_TITLE);
		}

		return trimmedTitle;
	}

	/**
	 * 일정 검색어를 검증하고 조회 가능한 값으로 정리합니다.
	 *
	 * @param memberIdx 로그인 사용자 식별자
	 * @param keyword 요청 검색어
	 * @return 앞뒤 공백이 제거된 검색어
	 * @throws BusinessException 검색어가 비어 있거나 허용 길이를 초과할 때 발생
	 */
	private String validateKeyword(Integer memberIdx, String keyword) {
		if (!StringUtils.hasText(keyword)) {
			log.warn("[validateKeyword] 검색어 누락: memberIdx={}", memberIdx);
			throw new BusinessException(ScheduleErrorCode.INVALID_KEYWORD);
		}

		String trimmedKeyword = keyword.trim();
		if (trimmedKeyword.length() > MAX_KEYWORD_LENGTH) {
			log.warn("[validateKeyword] 검색어 길이 초과: memberIdx={}, length={}", memberIdx, trimmedKeyword.length());
			throw new BusinessException(ScheduleErrorCode.INVALID_KEYWORD);
		}

		return trimmedKeyword;
	}

	/**
	 * 일정 시작 일시 문자열을 날짜 시간 값으로 변환합니다.
	 *
	 * @param memberIdx 로그인 사용자 식별자
	 * @param startAt 요청 시작 일시
	 * @return 변환된 시작 일시
	 * @throws BusinessException 시작 일시가 없거나 형식이 올바르지 않을 때 발생
	 */
	private LocalDateTime parseStartAt(Integer memberIdx, String startAt) {
		if (!StringUtils.hasText(startAt)) {
			log.warn("[parseStartAt] 시작 일시 누락: memberIdx={}", memberIdx);
			throw new BusinessException(ScheduleErrorCode.INVALID_START_AT);
		}

		try {
			return LocalDateTime.parse(startAt, DATE_TIME_FORMATTER);
		} catch (DateTimeParseException exception) {
			log.warn("[parseStartAt] 시작 일시 형식 오류: memberIdx={}, startAt={}", memberIdx, startAt);
			throw new BusinessException(ScheduleErrorCode.INVALID_START_AT);
		}
	}

	/**
	 * 예상 소요 시간을 검증합니다.
	 *
	 * @param memberIdx 로그인 사용자 식별자
	 * @param estimatedMinutes 요청 예상 소요 시간
	 * @return 검증된 예상 소요 시간
	 * @throws BusinessException 예상 소요 시간이 없거나 허용 범위를 벗어났을 때 발생
	 */
	private Integer validateEstimatedMinutes(Integer memberIdx, Integer estimatedMinutes) {
		if (estimatedMinutes == null || estimatedMinutes <= 0 || estimatedMinutes > MAX_ESTIMATED_MINUTES) {
			log.warn(
				"[validateEstimatedMinutes] 예상 시간 오류: memberIdx={}, estimatedMinutes={}",
				memberIdx,
				estimatedMinutes
			);
			throw new BusinessException(ScheduleErrorCode.INVALID_ESTIMATED_MINUTES);
		}

		return estimatedMinutes;
	}

	/**
	 * 실제 소요 시간을 검증합니다.
	 *
	 * @param memberIdx 로그인 사용자 식별자
	 * @param scheduleId 완료 처리할 일정 식별자
	 * @param actualMinutes 요청 실제 소요 시간
	 * @return 검증된 실제 소요 시간
	 * @throws BusinessException 실제 소요 시간이 없거나 허용 범위를 벗어난 경우 발생
	 */
	private Integer validateActualMinutes(Integer memberIdx, Integer scheduleId, Integer actualMinutes) {
		if (actualMinutes == null || actualMinutes <= 0 || actualMinutes > MAX_ESTIMATED_MINUTES) {
			log.warn(
				"[validateActualMinutes] 실제 소요 시간 오류: memberIdx={}, scheduleId={}, actualMinutes={}",
				memberIdx,
				scheduleId,
				actualMinutes
			);
			throw new BusinessException(ScheduleErrorCode.INVALID_ACTUAL_MINUTES);
		}

		return actualMinutes;
	}

	/**
	 * 미루기 사유 코드를 검증합니다.
	 *
	 * @param memberIdx 로그인 사용자 식별자
	 * @param scheduleId 미루기 처리할 일정 식별자
	 * @param deferReasonCode 요청 미루기 사유 코드
	 * @return 검증된 미루기 사유 코드
	 * @throws BusinessException 미루기 사유 코드가 없거나 허용되지 않은 경우 발생
	 */
	private String validateDeferReasonCode(Integer memberIdx, Integer scheduleId, String deferReasonCode) {
		if (!StringUtils.hasText(deferReasonCode)) {
			log.warn("[validateDeferReasonCode] 미루기 사유 누락: memberIdx={}, scheduleId={}", memberIdx, scheduleId);
			throw new BusinessException(ScheduleErrorCode.INVALID_DEFER_REASON);
		}

		String trimmedDeferReasonCode = deferReasonCode.trim();
		if (!ALLOWED_DEFER_REASON_CODES.contains(trimmedDeferReasonCode)) {
			log.warn(
				"[validateDeferReasonCode] 허용되지 않은 미루기 사유: memberIdx={}, scheduleId={}, deferReasonCode={}",
				memberIdx,
				scheduleId,
				trimmedDeferReasonCode
			);
			throw new BusinessException(ScheduleErrorCode.INVALID_DEFER_REASON);
		}

		return trimmedDeferReasonCode;
	}

	/**
	 * 미루기 상세 사유를 검증합니다.
	 *
	 * @param memberIdx 로그인 사용자 식별자
	 * @param scheduleId 미루기 처리할 일정 식별자
	 * @param deferReasonCode 검증된 미루기 사유 코드
	 * @param deferReasonDetail 요청 미루기 상세 사유
	 * @return 검증된 미루기 상세 사유
	 * @throws BusinessException CUSTOM 사유의 상세 사유가 없거나 길이가 허용 범위를 벗어난 경우 발생
	 */
	private String validateDeferReasonDetail(
		Integer memberIdx,
		Integer scheduleId,
		String deferReasonCode,
		String deferReasonDetail
	) {
		if (!StringUtils.hasText(deferReasonDetail)) {
			if (CUSTOM_DEFER_REASON_CODE.equals(deferReasonCode)) {
				log.warn(
					"[validateDeferReasonDetail] CUSTOM 상세 사유 누락: memberIdx={}, scheduleId={}",
					memberIdx,
					scheduleId
				);
				throw new BusinessException(ScheduleErrorCode.DEFER_REASON_DETAIL_REQUIRED);
			}

			return null;
		}

		String trimmedDeferReasonDetail = deferReasonDetail.trim();
		if (trimmedDeferReasonDetail.length() > MAX_DEFER_REASON_DETAIL_LENGTH) {
			log.warn(
				"[validateDeferReasonDetail] 미루기 상세 사유 길이 초과: memberIdx={}, scheduleId={}, length={}",
				memberIdx,
				scheduleId,
				trimmedDeferReasonDetail.length()
			);
			throw new BusinessException(ScheduleErrorCode.DEFER_REASON_DETAIL_REQUIRED);
		}

		return trimmedDeferReasonDetail;
	}

	/**
	 * 변경할 시작 일시를 검증하고 일시 값으로 변환합니다.
	 *
	 * @param memberIdx 로그인 사용자 식별자
	 * @param scheduleId 미루기 처리할 일정 식별자
	 * @param newStartAt 요청 변경 시작 일시
	 * @return 변환된 변경 시작 일시. 값이 없으면 null
	 * @throws BusinessException 변경할 시작 일시 형식이 올바르지 않은 경우 발생
	 */
	private LocalDateTime parseNewStartAt(Integer memberIdx, Integer scheduleId, String newStartAt) {
		if (!StringUtils.hasText(newStartAt)) {
			return null;
		}

		try {
			return LocalDateTime.parse(newStartAt, DATE_TIME_FORMATTER);
		} catch (DateTimeParseException exception) {
			log.warn(
				"[parseNewStartAt] 변경 시작 일시 형식 오류: memberIdx={}, scheduleId={}, newStartAt={}",
				memberIdx,
				scheduleId,
				newStartAt
			);
			throw new BusinessException(ScheduleErrorCode.INVALID_NEW_START_AT);
		}
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
