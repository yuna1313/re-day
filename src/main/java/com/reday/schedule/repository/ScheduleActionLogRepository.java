package com.reday.schedule.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reday.schedule.domain.ScheduleActionLog;

public interface ScheduleActionLogRepository extends JpaRepository<ScheduleActionLog, Long> {

	/**
	 * 특정 일정의 처리 로그를 처리 일시 오름차순으로 조회합니다.
	 *
	 * @param scheduleIdx 일정 식별자
	 * @return 일정 처리 로그 목록
	 */
	List<ScheduleActionLog> findByScheduleIdxOrderByActionAtAsc(Integer scheduleIdx);
}
