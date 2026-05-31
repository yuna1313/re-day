package com.reday.schedule.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

	/**
	 * 수정 대상 일정이 로그인 사용자에게 속하고 삭제되지 않았는지 확인하며 조회합니다.
	 *
	 * @param scheduleIdx 일정 식별자
	 * @param memberIdx 일정 소유 회원 식별자
	 * @return 삭제되지 않은 사용자 소유 일정
	 */
	Optional<Schedule> findByScheduleIdxAndMemberIdxAndDeletedAtIsNull(Integer scheduleIdx, Integer memberIdx);
}
