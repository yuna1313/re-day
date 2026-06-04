package com.reday.schedule.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reday.schedule.domain.ScheduleActionLog;
import com.reday.schedule.domain.ScheduleActionType;

public interface ScheduleActionLogRepository extends JpaRepository<ScheduleActionLog, Long> {

	/**
	 * 특정 일정의 처리 로그를 처리 일시 오름차순으로 조회합니다.
	 *
	 * @param scheduleIdx 일정 식별자
	 * @return 일정 처리 로그 목록
	 */
	List<ScheduleActionLog> findByScheduleIdxOrderByActionAtAsc(Integer scheduleIdx);

	/**
	 * 특정 일정 목록의 특정 처리 유형 로그를 처리 일시 범위 안에서 조회합니다.
	 *
	 * @param scheduleIdxes 일정 식별자 목록
	 * @param actionType 처리 유형
	 * @param startAt 처리 일시 조회 시작
	 * @param endAt 처리 일시 조회 종료
	 * @return 처리 일시 범위 안의 일정 처리 로그 목록
	 */
	List<ScheduleActionLog> findByScheduleIdxInAndActionTypeAndActionAtBetween(
		List<Integer> scheduleIdxes,
		ScheduleActionType actionType,
		LocalDateTime startAt,
		LocalDateTime endAt
	);
}
