package com.reday.schedule.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.reday.global.response.ApiResponse;
import com.reday.global.security.UserPrincipal;
import com.reday.schedule.application.ScheduleService;
import com.reday.schedule.dto.ScheduleListResponse;
import com.reday.schedule.response.ScheduleResponseCode;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ScheduleController {

	private final ScheduleService scheduleService;

	/**
	 * 로그인한 사용자의 주간 또는 월간 일정 목록을 조회합니다.
	 *
	 * @param userPrincipal JWT 인증 필터가 SecurityContext에 저장한 사용자 정보
	 * @param viewType 주간 또는 월간 화면 유형
	 * @param startDate 조회 시작 날짜
	 * @param endDate 조회 종료 날짜
	 * @return 일정 목록 조회 성공 응답
	 */
	@GetMapping("/api/v1/schedules")
	public ApiResponse<ScheduleListResponse> getSchedules(
		@AuthenticationPrincipal UserPrincipal userPrincipal,
		@RequestParam String viewType,
		@RequestParam String startDate,
		@RequestParam String endDate
	) {
		ScheduleListResponse response = scheduleService.getSchedules(
			userPrincipal.getMemberIdx(),
			viewType,
			startDate,
			endDate
		);
		return ApiResponse.success(ScheduleResponseCode.LIST_SUCCESS, response);
	}
}
