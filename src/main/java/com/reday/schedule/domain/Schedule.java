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

	@Column(nullable = false, length = 500)
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
		this.createdAt = LocalDateTime.now();
		this.deferCount = deferCount;
	}

	/**
	 * 사용자가 새로 등록한 대기 상태 일정을 생성합니다.
	 *
	 * @param memberIdx 일정 소유 회원 식별자
	 * @param title 일정 제목
	 * @param startAt 시작 일시
	 * @param estimatedMinutes 예상 소요 시간
	 * @param memo 메모
	 * @return 대기 상태의 새 일정 엔티티
	 */
	public static Schedule createNew(
		Integer memberIdx,
		String title,
		LocalDateTime startAt,
		Integer estimatedMinutes,
		String memo
	) {
		return new Schedule(
			memberIdx,
			title,
			startAt,
			estimatedMinutes,
			null,
			memo,
			ScheduleStatus.PENDING,
			null,
			0
		);
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

	/**
	 * 일정의 기본 정보를 수정합니다.
	 *
	 * @param title 수정할 일정 제목
	 * @param startAt 수정할 시작 일시
	 * @param estimatedMinutes 수정할 예상 소요 시간
	 * @param memo 수정할 메모
	 */
	public void update(
		String title,
		LocalDateTime startAt,
		Integer estimatedMinutes,
		String memo
	) {
		this.title = title;
		this.startAt = startAt;
		this.estimatedMinutes = estimatedMinutes;
		this.memo = memo;
		this.updatedAt = LocalDateTime.now();
	}

	/**
	 * 일정을 실제로 삭제하지 않고 삭제 일시를 기록하여 삭제 상태로 변경합니다.
	 */
	public void delete() {
		LocalDateTime deletedTime = LocalDateTime.now();
		this.deletedAt = deletedTime;
		this.updatedAt = deletedTime;
	}

	/**
	 * 일정을 완료 상태로 변경하고 실제 소요 시간과 완료 일시를 기록합니다.
	 *
	 * @param actualMinutes 실제 소요 시간
	 */
	public void complete(Integer actualMinutes) {
		LocalDateTime completedTime = LocalDateTime.now();
		this.status = ScheduleStatus.DONE;
		this.actualMinutes = actualMinutes;
		this.completedAt = completedTime;
		this.updatedAt = completedTime;
	}

	/**
	 * 일정을 미루고 미루기 횟수와 시작 일시를 갱신합니다.
	 *
	 * @param newStartAt 변경할 시작 일시. null이면 기존 시작 일시를 유지합니다.
	 */
	public void defer(LocalDateTime newStartAt) {
		if (newStartAt != null) {
			this.startAt = newStartAt;
		}
		this.deferCount = this.deferCount == null ? 1 : this.deferCount + 1;
		this.updatedAt = LocalDateTime.now();
	}
}
