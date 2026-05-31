package com.reday.schedule.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reday.schedule.domain.Schedule;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

	/**
	 * 특정 회원의 일정 중 시작 일시가 조회 기간에 포함되는 일정을 시작 일시 오름차순으로 조회합니다.
	 *
	 * @param memberIdx 일정 소유 회원 식별자
	 * @param startAt 조회 시작 일시
	 * @param endAt 조회 종료 일시
	 * @return 조회 기간에 포함되는 일정 목록
	 */
	List<Schedule> findByMemberIdxAndDeletedAtIsNullAndStartAtBetweenOrderByStartAtAsc(
		Integer memberIdx,
		LocalDateTime startAt,
		LocalDateTime endAt
	);
}
