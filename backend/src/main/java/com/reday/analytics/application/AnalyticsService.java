package com.reday.analytics.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reday.analytics.domain.InsightPeriodType;
import com.reday.analytics.domain.TimeSlot;
import com.reday.analytics.dto.InsightResponse;
import com.reday.schedule.domain.Schedule;
import com.reday.schedule.domain.ScheduleActionLog;
import com.reday.schedule.domain.ScheduleActionType;
import com.reday.schedule.domain.ScheduleStatus;
import com.reday.schedule.repository.ScheduleActionLogRepository;
import com.reday.schedule.repository.ScheduleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

	private static final int TOP_DEFERRED_SCHEDULE_LIMIT = 3;

	private final ScheduleRepository scheduleRepository;
	private final ScheduleActionLogRepository scheduleActionLogRepository;

	/**
	 * 로그인한 사용자의 일정 기반 인사이트를 조회합니다.
	 *
	 * @param memberIdx 로그인 사용자 식별자
	 * @param periodType 조회 기간 유형
	 * @return 인사이트 조회 응답
	 */
	@Transactional(readOnly = true)
	public InsightResponse getInsights(Integer memberIdx, String periodType) {
		InsightPeriodType parsedPeriodType = InsightPeriodType.from(periodType);
		LocalDate today = LocalDate.now();
		LocalDateTime startAt = parsedPeriodType.startAt(today);
		LocalDateTime endAt = parsedPeriodType.endAt(today);
		log.info(
			"[getInsights] 인사이트 조회 요청: memberIdx={}, periodType={}, startAt={}, endAt={}",
			memberIdx,
			parsedPeriodType,
			startAt,
			endAt
		);

		List<Schedule> schedules = scheduleRepository.findByMemberIdxAndDeletedAtIsNullAndStartAtBetweenOrderByStartAtAsc(
			memberIdx,
			startAt,
			endAt
		);
		List<ScheduleActionLog> deferLogs = findDeferLogs(memberIdx, startAt, endAt);
		List<InsightResponse.TimeSlotCompletionRate> completionRates = calculateCompletionRates(schedules);
		List<InsightResponse.TopDeferReason> topDeferReasons = calculateTopDeferReasons(deferLogs);
		List<InsightResponse.TopDeferredSchedule> topDeferredSchedules =
			calculateTopDeferredSchedules(memberIdx, deferLogs);
		InsightResponse.EstimatedVsActual estimatedVsActual = calculateEstimatedVsActual(schedules);
		List<String> feedbackMessages = createFeedbackMessages(
			completionRates,
			topDeferReasons,
			topDeferredSchedules,
			estimatedVsActual
		);

		log.info(
			"[getInsights] 인사이트 조회 완료: memberIdx={}, periodType={}, scheduleCount={}, deferLogCount={}",
			memberIdx,
			parsedPeriodType,
			schedules.size(),
			deferLogs.size()
		);

		return new InsightResponse(
			parsedPeriodType.name(),
			completionRates,
			topDeferReasons,
			topDeferredSchedules,
			estimatedVsActual,
			feedbackMessages
		);
	}

	/**
	 * 조회 기간 안에 가장 많이 미룬 일정 상위 목록을 계산합니다.
	 *
	 * <p>일정의 누적 미루기 횟수(defer_count)가 아니라 미루기 상위 이유와 <b>같은 로그 집합</b>을 사용합니다.
	 * 같은 로그를 사유로 묶느냐 일정으로 묶느냐의 차이라, 두 통계가 서로 어긋날 수 없습니다.
	 * 같은 이유로 일정 상태(완료 여부)로 걸러내지 않습니다. 한쪽에만 조건을 걸면 다시 어긋납니다.
	 *
	 * @param memberIdx 로그인 사용자 식별자
	 * @param deferLogs 조회 기간 안의 미루기 처리 로그 목록
	 * @return 미룬 횟수 상위 일정 목록
	 */
	private List<InsightResponse.TopDeferredSchedule> calculateTopDeferredSchedules(
		Integer memberIdx,
		List<ScheduleActionLog> deferLogs
	) {
		Map<Integer, Long> counts = deferLogs.stream()
			.collect(Collectors.groupingBy(ScheduleActionLog::getScheduleIdx, Collectors.counting()));

		List<Map.Entry<Integer, Long>> rankedSchedules = counts.entrySet().stream()
			.sorted(Map.Entry.<Integer, Long>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()))
			.limit(TOP_DEFERRED_SCHEDULE_LIMIT)
			.toList();
		if (rankedSchedules.isEmpty()) {
			return List.of();
		}

		Map<Integer, String> titles = scheduleRepository.findByScheduleIdxInAndMemberIdxAndDeletedAtIsNull(
				rankedSchedules.stream()
					.map(Map.Entry::getKey)
					.toList(),
				memberIdx
			).stream()
			.collect(Collectors.toMap(Schedule::getScheduleIdx, Schedule::getTitle));

		List<InsightResponse.TopDeferredSchedule> responses = new ArrayList<>();
		for (Map.Entry<Integer, Long> rankedSchedule : rankedSchedules) {
			String title = titles.get(rankedSchedule.getKey());
			if (title == null) {
				continue;
			}

			responses.add(new InsightResponse.TopDeferredSchedule(
				responses.size() + 1,
				rankedSchedule.getKey(),
				title,
				rankedSchedule.getValue().intValue()
			));
		}

		return responses;
	}

	/**
	 * 조회 기간 안에 남은 미루기 처리 로그를 조회합니다.
	 *
	 * <p>일정이 아니라 로그가 남은 시각을 기준으로 조회합니다.
	 * 미루면 일정의 시작 일시가 미래로 옮겨져 기간 조회에서 빠지는데,
	 * 일정을 먼저 걸러 로그를 찾으면 많이 미룬 일정일수록 집계에서 사라집니다.
	 *
	 * @param memberIdx 로그인 사용자 식별자
	 * @param startAt 조회 시작 일시
	 * @param endAt 조회 종료 일시
	 * @return 미루기 처리 로그 목록
	 */
	private List<ScheduleActionLog> findDeferLogs(Integer memberIdx, LocalDateTime startAt, LocalDateTime endAt) {
		return scheduleActionLogRepository.findMemberActionLogs(
			memberIdx,
			ScheduleActionType.DEFERRED,
			startAt,
			endAt
		);
	}

	/**
	 * 시간대별 완료율을 계산합니다.
	 *
	 * @param schedules 조회 기간 일정 목록
	 * @return 시간대별 완료율 목록
	 */
	private List<InsightResponse.TimeSlotCompletionRate> calculateCompletionRates(List<Schedule> schedules) {
		Map<TimeSlot, SlotCounter> counters = new EnumMap<>(TimeSlot.class);
		for (TimeSlot timeSlot : TimeSlot.values()) {
			counters.put(timeSlot, new SlotCounter());
		}

		for (Schedule schedule : schedules) {
			TimeSlot timeSlot = TimeSlot.from(schedule.getStartAt().toLocalTime());
			SlotCounter counter = counters.get(timeSlot);
			counter.total++;
			if (schedule.getStatus() == ScheduleStatus.DONE) {
				counter.completed++;
			}
		}

		List<InsightResponse.TimeSlotCompletionRate> responses = new ArrayList<>();
		for (TimeSlot timeSlot : TimeSlot.values()) {
			SlotCounter counter = counters.get(timeSlot);
			responses.add(new InsightResponse.TimeSlotCompletionRate(
				timeSlot.name(),
				timeSlot.label(),
				percentage(counter.completed, counter.total)
			));
		}
		return responses;
	}

	/**
	 * 미루기 사유 상위 3개를 계산합니다.
	 *
	 * @param deferLogs 미루기 처리 로그 목록
	 * @return 미루기 사유 상위 목록
	 */
	private List<InsightResponse.TopDeferReason> calculateTopDeferReasons(List<ScheduleActionLog> deferLogs) {
		Map<String, Long> counts = deferLogs.stream()
			.filter(log -> log.getDeferReasonCode() != null)
			.collect(Collectors.groupingBy(ScheduleActionLog::getDeferReasonCode, Collectors.counting()));

		List<Map.Entry<String, Long>> rankedReasons = counts.entrySet().stream()
			.sorted(Map.Entry.<String, Long>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()))
			.limit(3)
			.toList();

		List<InsightResponse.TopDeferReason> responses = new ArrayList<>();
		for (int index = 0; index < rankedReasons.size(); index++) {
			Map.Entry<String, Long> reason = rankedReasons.get(index);
			responses.add(new InsightResponse.TopDeferReason(
				index + 1,
				reason.getKey(),
				deferReasonLabel(reason.getKey()),
				reason.getValue().intValue()
			));
		}
		return responses;
	}

	/**
	 * 예상 시간과 실제 소요 시간 평균 차이를 계산합니다.
	 *
	 * @param schedules 조회 기간 일정 목록
	 * @return 예상 대비 실제 소요 시간 평균 응답
	 */
	private InsightResponse.EstimatedVsActual calculateEstimatedVsActual(List<Schedule> schedules) {
		List<Schedule> completedSchedules = schedules.stream()
			.filter(schedule -> schedule.getStatus() == ScheduleStatus.DONE)
			.filter(schedule -> schedule.getActualMinutes() != null)
			.toList();
		if (completedSchedules.isEmpty()) {
			return new InsightResponse.EstimatedVsActual(0, 0, 0);
		}

		int averageEstimatedMinutes = roundedAverage(completedSchedules.stream()
			.mapToInt(Schedule::getEstimatedMinutes)
			.sum(), completedSchedules.size());
		int averageActualMinutes = roundedAverage(completedSchedules.stream()
			.mapToInt(Schedule::getActualMinutes)
			.sum(), completedSchedules.size());

		return new InsightResponse.EstimatedVsActual(
			averageEstimatedMinutes,
			averageActualMinutes,
			averageActualMinutes - averageEstimatedMinutes
		);
	}

	/**
	 * 인사이트 피드백 문구를 생성합니다.
	 *
	 * @param completionRates 시간대별 완료율 목록
	 * @param topDeferReasons 미루기 사유 상위 목록
	 * @param topDeferredSchedules 미룬 횟수 상위 일정 목록
	 * @param estimatedVsActual 예상 대비 실제 소요 시간 평균
	 * @return 피드백 문구 목록
	 */
	private List<String> createFeedbackMessages(
		List<InsightResponse.TimeSlotCompletionRate> completionRates,
		List<InsightResponse.TopDeferReason> topDeferReasons,
		List<InsightResponse.TopDeferredSchedule> topDeferredSchedules,
		InsightResponse.EstimatedVsActual estimatedVsActual
	) {
		List<String> messages = new ArrayList<>();
		completionRates.stream()
			.max(Comparator.comparing(InsightResponse.TimeSlotCompletionRate::completionRate))
			.filter(rate -> rate.completionRate() > 0)
			.ifPresent(rate -> messages.add(rate.label() + " 일정 완료율이 가장 높아요."));

		if (!topDeferReasons.isEmpty()) {
			messages.add(topDeferReasons.get(0).label() + " 사유로 미루는 경우가 가장 많아요.");
		}

		if (!topDeferredSchedules.isEmpty()) {
			InsightResponse.TopDeferredSchedule mostDeferred = topDeferredSchedules.get(0);
			messages.add("'" + mostDeferred.title() + "'을(를) " + mostDeferred.deferCount() + "번 미뤘어요.");
		}

		if (estimatedVsActual.averageDiffMinutes() > 0) {
			messages.add("실제 소요 시간이 예상보다 평균 " + estimatedVsActual.averageDiffMinutes() + "분 더 걸렸어요.");
		} else if (estimatedVsActual.averageDiffMinutes() < 0) {
			messages.add("실제 소요 시간이 예상보다 평균 " + Math.abs(estimatedVsActual.averageDiffMinutes()) + "분 짧았어요.");
		}

		if (messages.isEmpty()) {
			messages.add("아직 충분한 일정 데이터가 없어 인사이트를 만들기 어려워요.");
		}
		return messages;
	}

	/**
	 * 비율을 반올림 정수 퍼센트로 계산합니다.
	 *
	 * @param numerator 분자
	 * @param denominator 분모
	 * @return 반올림된 퍼센트
	 */
	private Integer percentage(int numerator, int denominator) {
		if (denominator == 0) {
			return 0;
		}
		return Math.round((float)numerator * 100 / denominator);
	}

	/**
	 * 평균값을 반올림 정수로 계산합니다.
	 *
	 * @param sum 합계
	 * @param count 개수
	 * @return 반올림된 평균값
	 */
	private Integer roundedAverage(int sum, int count) {
		if (count == 0) {
			return 0;
		}
		return Math.round((float)sum / count);
	}

	/**
	 * 미루기 사유 코드의 표시 라벨을 반환합니다.
	 *
	 * @param deferReasonCode 미루기 사유 코드
	 * @return 표시 라벨
	 */
	private String deferReasonLabel(String deferReasonCode) {
		return switch (deferReasonCode) {
			case "LONGER_THAN_EXPECTED" -> "예상보다 오래 걸림";
			case "NOT_STARTED" -> "시작을 못 함";
			case "COULD_NOT_FOCUS" -> "집중 안 됨";
			case "NO_TIME" -> "시간이 없었음";
			case "TOO_BIG" -> "작업량이 너무 큼";
			case "CUSTOM" -> "직접 입력";
			default -> deferReasonCode;
		};
	}

	private static class SlotCounter {
		private int total;
		private int completed;
	}
}
