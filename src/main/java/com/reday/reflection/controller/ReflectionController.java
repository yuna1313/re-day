package com.reday.reflection.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.reday.global.response.ApiResponse;
import com.reday.global.security.UserPrincipal;
import com.reday.reflection.application.ReflectionService;
import com.reday.reflection.dto.ReflectionDetailResponse;
import com.reday.reflection.dto.ReflectionTodayResponse;
import com.reday.reflection.response.ReflectionResponseCode;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ReflectionController {

	private final ReflectionService reflectionService;

	/**
	 * 로그인한 사용자의 오늘 회고와 오늘 완료한 일정 목록을 조회합니다.
	 *
	 * @param userPrincipal JWT 인증 필터가 SecurityContext에 저장한 사용자 정보
	 * @return 오늘 회고 조회 성공 응답
	 */
	@GetMapping("/api/v1/reflections/today")
	public ApiResponse<ReflectionTodayResponse> getTodayReflection(
		@AuthenticationPrincipal UserPrincipal userPrincipal
	) {
		ReflectionTodayResponse response = reflectionService.getTodayReflection(userPrincipal.getMemberIdx());
		return ApiResponse.success(ReflectionResponseCode.TODAY_SUCCESS, response);
	}

	/**
	 * 로그인한 사용자의 특정 날짜 회고와 해당 날짜에 완료한 일정 목록을 조회합니다.
	 *
	 * @param userPrincipal JWT 인증 필터가 SecurityContext에 저장한 사용자 정보
	 * @param date 조회할 날짜
	 * @return 날짜별 회고 조회 성공 응답
	 */
	@GetMapping("/api/v1/reflections/{date}")
	public ApiResponse<ReflectionDetailResponse> getReflectionByDate(
		@AuthenticationPrincipal UserPrincipal userPrincipal,
		@PathVariable String date
	) {
		ReflectionDetailResponse response = reflectionService.getReflectionByDate(userPrincipal.getMemberIdx(), date);
		return ApiResponse.success(ReflectionResponseCode.DETAIL_SUCCESS, response);
	}
}
