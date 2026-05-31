package com.reday.schedule.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "schedule")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Schedule {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "schedule_idx")
	private Integer scheduleIdx;

	@Column(name = "member_idx", nullable = false)
	private Integer memberIdx;

	@Column(nullable = false, length = 100)
	private String title;

	@Column(name = "start_at", nullable = false)
	private LocalDateTime startAt;

	@Column(name = "estimated_minutes", nullable = false)
	private Integer estimatedMinutes;

	@Column(name = "actual_minutes")
	private Integer actualMinutes;

	@Column(columnDefinition = "TEXT")
	private String memo;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private ScheduleStatus status;

	@Column(name = "completed_at")
	private LocalDateTime completedAt;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	@Column(name = "defer_count")
	private Integer deferCount;

	private Schedule(
		Integer memberIdx,
		String title,
		LocalDateTime startAt,
		Integer estimatedMinutes,
		Integer actualMinutes,
		String memo,
		ScheduleStatus status,
		LocalDateTime completedAt,
		Integer deferCount
	) {
		this.memberIdx = memberIdx;
		this.title = title;
		this.startAt = startAt;
		this.estimatedMinutes = estimatedMinutes;
		this.actualMinutes = actualMinutes;
		this.memo = memo;
		this.status = status;
		this.completedAt = completedAt;
		this.deferCount = deferCount;
	}

	/**
	 * 테스트와 초기 구현에서 사용할 일정 엔티티를 생성합니다.
	 *
	 * @param memberIdx 일정 소유 회원 식별자
	 * @param title 일정 제목
	 * @param startAt 시작 일시
	 * @param estimatedMinutes 예상 소요 시간
	 * @param actualMinutes 실제 소요 시간
	 * @param memo 메모
	 * @param status 일정 상태
	 * @param completedAt 완료 일시
	 * @param deferCount 미루기 횟수
	 * @return 일정 엔티티
	 */
	public static Schedule create(
		Integer memberIdx,
		String title,
		LocalDateTime startAt,
		Integer estimatedMinutes,
		Integer actualMinutes,
		String memo,
		ScheduleStatus status,
		LocalDateTime completedAt,
		Integer deferCount
	) {
		return new Schedule(
			memberIdx,
			title,
			startAt,
			estimatedMinutes,
			actualMinutes,
			memo,
			status,
			completedAt,
			deferCount
		);
	}
}
