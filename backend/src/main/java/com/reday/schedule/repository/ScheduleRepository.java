package com.reday.schedule.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reday.schedule.domain.Schedule;
import com.reday.schedule.domain.ScheduleStatus;

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

	/**
	 * 특정 회원의 일정 중 제목에 키워드가 포함된 일정을 최근 시작 일시 순으로 조회합니다.
	 * 결과가 지나치게 커지지 않도록 최대 50건까지만 조회합니다.
	 * (개수를 바꿀 때는 ScheduleService.MAX_SEARCH_RESULTS 도 함께 맞춰야 합니다)
	 *
	 * @param memberIdx 일정 소유 회원 식별자
	 * @param title 제목에 포함될 검색 키워드
	 * @return 키워드가 포함된 일정 목록
	 */
	List<Schedule> findTop50ByMemberIdxAndDeletedAtIsNullAndTitleContainingOrderByStartAtDesc(
		Integer memberIdx,
		String title
	);

	/**
	 * 특정 회원의 일정 중 기준 일시 이전에 시작했지만 아직 끝내지 않은 일정을 최근 순으로 조회합니다.
	 * 목록이 지나치게 길어지지 않도록 최대 50건까지만 조회합니다.
	 * (개수를 바꿀 때는 ScheduleService.MAX_OVERDUE_RESULTS 도 함께 맞춰야 합니다)
	 *
	 * @param memberIdx 일정 소유 회원 식별자
	 * @param status 조회할 일정 상태
	 * @param startAt 기준 일시 (이 시각 이전에 시작한 일정)
	 * @return 밀린 일정 목록
	 */
	List<Schedule> findTop50ByMemberIdxAndDeletedAtIsNullAndStatusAndStartAtBeforeOrderByStartAtDesc(
		Integer memberIdx,
		ScheduleStatus status,
		LocalDateTime startAt
	);

	/**
	 * 특정 회원의 일정 중 식별자 목록에 해당하는 일정을 조회합니다.
	 *
	 * @param scheduleIdxes 일정 식별자 목록
	 * @param memberIdx 일정 소유 회원 식별자
	 * @return 삭제되지 않은 사용자 소유 일정 목록
	 */
	List<Schedule> findByScheduleIdxInAndMemberIdxAndDeletedAtIsNull(
		List<Integer> scheduleIdxes,
		Integer memberIdx
	);

	/**
	 * 특정 회원의 밀린 일정 전체 개수를 조회합니다.
	 *
	 * @param memberIdx 일정 소유 회원 식별자
	 * @param status 조회할 일정 상태
	 * @param startAt 기준 일시 (이 시각 이전에 시작한 일정)
	 * @return 밀린 일정 개수
	 */
	long countByMemberIdxAndDeletedAtIsNullAndStatusAndStartAtBefore(
		Integer memberIdx,
		ScheduleStatus status,
		LocalDateTime startAt
	);

	/**
	 * 특정 회원의 특정 완료 일시 범위 안에 완료된 일정을 완료 일시 오름차순으로 조회합니다.
	 *
	 * @param memberIdx 일정 소유 회원 식별자
	 * @param status 조회할 일정 상태
	 * @param startAt 완료 일시 조회 시작
	 * @param endAt 완료 일시 조회 종료
	 * @return 완료 일시 범위 안에 포함되는 완료 일정 목록
	 */
	List<Schedule> findByMemberIdxAndDeletedAtIsNullAndStatusAndCompletedAtBetweenOrderByCompletedAtAsc(
		Integer memberIdx,
		ScheduleStatus status,
		LocalDateTime startAt,
		LocalDateTime endAt
	);
}
