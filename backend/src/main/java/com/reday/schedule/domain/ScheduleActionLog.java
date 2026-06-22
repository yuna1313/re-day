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
@Table(name = "schedule_action_log")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScheduleActionLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "schedule_action_log_idx")
	private Integer scheduleActionLogIdx;

	@Column(name = "schedule_idx", nullable = false)
	private Integer scheduleIdx;

	@Enumerated(EnumType.STRING)
	@Column(name = "action_type", nullable = false, length = 20)
	private ScheduleActionType actionType;

	@Column(name = "defer_reason_code", length = 100)
	private String deferReasonCode;

	@Column(name = "defer_reason_detail", length = 500)
	private String deferReasonDetail;

	@Column(name = "action_at", nullable = false)
	private LocalDateTime actionAt;

	private ScheduleActionLog(
		Integer scheduleIdx,
		ScheduleActionType actionType,
		String deferReasonCode,
		String deferReasonDetail,
		LocalDateTime actionAt
	) {
		this.scheduleIdx = scheduleIdx;
		this.actionType = actionType;
		this.deferReasonCode = deferReasonCode;
		this.deferReasonDetail = deferReasonDetail;
		this.actionAt = actionAt;
	}

	/**
	 * 일정 처리 로그 엔티티를 생성합니다.
	 *
	 * @param scheduleIdx 일정 식별자
	 * @param actionType 처리 유형
	 * @param deferReasonCode 미루기 사유 코드
	 * @param deferReasonDetail 미루기 상세 사유
	 * @param actionAt 처리 일시
	 * @return 일정 처리 로그 엔티티
	 */
	public static ScheduleActionLog create(
		Integer scheduleIdx,
		ScheduleActionType actionType,
		String deferReasonCode,
		String deferReasonDetail,
		LocalDateTime actionAt
	) {
		return new ScheduleActionLog(scheduleIdx, actionType, deferReasonCode, deferReasonDetail, actionAt);
	}
}
