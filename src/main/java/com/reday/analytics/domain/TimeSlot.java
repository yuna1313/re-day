package com.reday.analytics.domain;

import java.time.LocalTime;

public enum TimeSlot {

	MORNING("오전", LocalTime.of(0, 0), LocalTime.of(11, 59, 59)),
	AFTERNOON("오후", LocalTime.of(12, 0), LocalTime.of(17, 59, 59)),
	EVENING("저녁", LocalTime.of(18, 0), LocalTime.of(23, 59, 59));

	private final String label;
	private final LocalTime startTime;
	private final LocalTime endTime;

	TimeSlot(String label, LocalTime startTime, LocalTime endTime) {
		this.label = label;
		this.startTime = startTime;
		this.endTime = endTime;
	}

	/**
	 * 일정 시작 시간을 시간대로 변환합니다.
	 *
	 * @param time 일정 시작 시간
	 * @return 일정 시작 시간이 속한 시간대
	 */
	public static TimeSlot from(LocalTime time) {
		for (TimeSlot timeSlot : values()) {
			if (!time.isBefore(timeSlot.startTime) && !time.isAfter(timeSlot.endTime)) {
				return timeSlot;
			}
		}

		return EVENING;
	}

	/**
	 * 화면에 표시할 시간대 라벨을 반환합니다.
	 *
	 * @return 시간대 라벨
	 */
	public String label() {
		return label;
	}
}
