package com.reday.schedule.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.reday.global.response.ApiResponse;
import com.reday.global.security.UserPrincipal;
import com.reday.schedule.application.ScheduleService;
import com.reday.schedule.dto.ScheduleCompleteRequest;
import com.reday.schedule.dto.ScheduleCompleteResponse;
import com.reday.schedule.dto.ScheduleCreateRequest;
import com.reday.schedule.dto.ScheduleCreateResponse;
import com.reday.schedule.dto.ScheduleDeferRequest;
import com.reday.schedule.dto.ScheduleDeferResponse;
import com.reday.schedule.dto.ScheduleDetailResponse;
import com.reday.schedule.dto.ScheduleListResponse;
import com.reday.schedule.dto.ScheduleSearchResponse;
import com.reday.schedule.dto.ScheduleUpdateRequest;
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

	/**
	 * 로그인한 사용자의 일정을 제목 키워드로 검색합니다.
	 * ('search' 는 고정 경로라 /schedules/{scheduleId} 보다 우선 매칭됩니다)
	 *
	 * @param userPrincipal JWT 인증 필터가 SecurityContext에 저장한 사용자 정보
	 * @param keyword 제목에 포함될 검색어
	 * @return 일정 검색 성공 응답
	 */
	@GetMapping("/api/v1/schedules/search")
	public ApiResponse<ScheduleSearchResponse> searchSchedules(
		@AuthenticationPrincipal UserPrincipal userPrincipal,
		@RequestParam String keyword
	) {
		ScheduleSearchResponse response = scheduleService.searchSchedules(userPrincipal.getMemberIdx(), keyword);
		return ApiResponse.success(ScheduleResponseCode.SEARCH_SUCCESS, response);
	}

	/**
	 * 로그인한 사용자의 새 일정을 생성합니다.
	 *
	 * @param userPrincipal JWT 인증 필터가 SecurityContext에 저장한 사용자 정보
	 * @param request 일정 생성 요청
	 * @return 일정 생성 성공 응답
	 */
	@PostMapping("/api/v1/schedules")
	public ApiResponse<ScheduleCreateResponse> createSchedule(
		@AuthenticationPrincipal UserPrincipal userPrincipal,
		@RequestBody ScheduleCreateRequest request
	) {
		ScheduleCreateResponse response = scheduleService.createSchedule(userPrincipal.getMemberIdx(), request);
		return ApiResponse.success(ScheduleResponseCode.CREATED, response);
	}

	/**
	 * 로그인한 사용자의 일정 상세 정보를 조회합니다.
	 *
	 * @param userPrincipal JWT 인증 필터가 SecurityContext에 저장한 사용자 정보
	 * @param scheduleId 조회할 일정 식별자
	 * @return 일정 상세 조회 성공 응답
	 */
	@GetMapping("/api/v1/schedules/{scheduleId}")
	public ApiResponse<ScheduleDetailResponse> getScheduleDetail(
		@AuthenticationPrincipal UserPrincipal userPrincipal,
		@PathVariable Integer scheduleId
	) {
		ScheduleDetailResponse response = scheduleService.getScheduleDetail(userPrincipal.getMemberIdx(), scheduleId);
		return ApiResponse.success(ScheduleResponseCode.DETAIL_SUCCESS, response);
	}

	/**
	 * 로그인한 사용자의 기존 일정을 수정합니다.
	 *
	 * @param userPrincipal JWT 인증 필터가 SecurityContext에 저장한 사용자 정보
	 * @param scheduleId 수정할 일정 식별자
	 * @param request 일정 수정 요청
	 * @return 일정 수정 성공 응답
	 */
	@PatchMapping("/api/v1/schedules/{scheduleId}")
	public ApiResponse<Void> updateSchedule(
		@AuthenticationPrincipal UserPrincipal userPrincipal,
		@PathVariable Integer scheduleId,
		@RequestBody ScheduleUpdateRequest request
	) {
		scheduleService.updateSchedule(userPrincipal.getMemberIdx(), scheduleId, request);
		return ApiResponse.success(ScheduleResponseCode.UPDATED);
	}

	/**
	 * 로그인한 사용자의 기존 일정을 실제 삭제하지 않고 삭제 일시를 기록합니다.
	 *
	 * @param userPrincipal JWT 인증 필터가 SecurityContext에 저장한 사용자 정보
	 * @param scheduleId 삭제할 일정 식별자
	 * @return 일정 삭제 성공 응답
	 */
	@DeleteMapping("/api/v1/schedules/{scheduleId}")
	public ApiResponse<Void> deleteSchedule(
		@AuthenticationPrincipal UserPrincipal userPrincipal,
		@PathVariable Integer scheduleId
	) {
		scheduleService.deleteSchedule(userPrincipal.getMemberIdx(), scheduleId);
		return ApiResponse.success(ScheduleResponseCode.DELETED);
	}

	/**
	 * 로그인한 사용자의 기존 일정을 완료 처리하고 실제 소요 시간을 저장합니다.
	 *
	 * @param userPrincipal JWT 인증 필터가 SecurityContext에 저장한 사용자 정보
	 * @param scheduleId 완료 처리할 일정 식별자
	 * @param request 일정 완료 처리 요청
	 * @return 일정 완료 처리 성공 응답
	 */
	@PostMapping("/api/v1/schedules/{scheduleId}/complete")
	public ApiResponse<ScheduleCompleteResponse> completeSchedule(
		@AuthenticationPrincipal UserPrincipal userPrincipal,
		@PathVariable Integer scheduleId,
		@RequestBody ScheduleCompleteRequest request
	) {
		ScheduleCompleteResponse response = scheduleService.completeSchedule(
			userPrincipal.getMemberIdx(),
			scheduleId,
			request
		);
		return ApiResponse.success(ScheduleResponseCode.COMPLETED, response);
	}

	/**
	 * 로그인한 사용자의 기존 일정을 미루고 미루기 사유를 기록합니다.
	 *
	 * @param userPrincipal JWT 인증 필터가 SecurityContext에 저장한 사용자 정보
	 * @param scheduleId 미루기 처리할 일정 식별자
	 * @param request 일정 미루기 처리 요청
	 * @return 일정 미루기 처리 성공 응답
	 */
	@PostMapping("/api/v1/schedules/{scheduleId}/defer")
	public ApiResponse<ScheduleDeferResponse> deferSchedule(
		@AuthenticationPrincipal UserPrincipal userPrincipal,
		@PathVariable Integer scheduleId,
		@RequestBody ScheduleDeferRequest request
	) {
		ScheduleDeferResponse response = scheduleService.deferSchedule(
			userPrincipal.getMemberIdx(),
			scheduleId,
			request
		);
		return ApiResponse.success(ScheduleResponseCode.DEFERRED, response);
	}
}
