package com.reday.schedule.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
	 * 특정 회원의 특정 처리 유형 로그를 처리 일시 범위 안에서 조회합니다.
	 *
	 * <p>일정의 시작 일시가 아니라 <b>로그가 남은 시각</b>을 기준으로 조회합니다.
	 * 미루면 일정의 시작 일시가 미래로 옮겨지므로, 일정을 기간으로 먼저 거르면
	 * 그 기간에 실제로 미룬 기록까지 함께 빠집니다.
	 *
	 * <p>로그 테이블에는 회원 식별자가 없어 일정 테이블과 연결해 조회합니다.
	 *
	 * @param memberIdx 일정 소유 회원 식별자
	 * @param actionType 처리 유형
	 * @param startAt 처리 일시 조회 시작
	 * @param endAt 처리 일시 조회 종료
	 * @return 처리 일시 범위 안의 일정 처리 로그 목록
	 */
	@Query("""
		select actionLog
		from ScheduleActionLog actionLog, Schedule schedule
		where actionLog.scheduleIdx = schedule.scheduleIdx
			and schedule.memberIdx = :memberIdx
			and schedule.deletedAt is null
			and actionLog.actionType = :actionType
			and actionLog.actionAt between :startAt and :endAt
		""")
	List<ScheduleActionLog> findMemberActionLogs(
		@Param("memberIdx") Integer memberIdx,
		@Param("actionType") ScheduleActionType actionType,
		@Param("startAt") LocalDateTime startAt,
		@Param("endAt") LocalDateTime endAt
	);
}
