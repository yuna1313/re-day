package com.reday.analytics.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.reday.analytics.application.AnalyticsService;
import com.reday.analytics.dto.InsightResponse;
import com.reday.analytics.response.AnalyticsResponseCode;
import com.reday.global.response.ApiResponse;
import com.reday.global.security.UserPrincipal;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AnalyticsController {

	private final AnalyticsService analyticsService;

	/**
	 * 로그인한 사용자의 일정 기반 인사이트를 조회합니다.
	 *
	 * @param userPrincipal JWT 인증 필터가 SecurityContext에 저장한 사용자 정보
	 * @param periodType 조회 기간 유형
	 * @return 인사이트 조회 성공 응답
	 */
	@GetMapping("/api/v1/analytics/insights")
	public ApiResponse<InsightResponse> getInsights(
		@AuthenticationPrincipal UserPrincipal userPrincipal,
		@RequestParam(required = false) String periodType
	) {
		InsightResponse response = analyticsService.getInsights(userPrincipal.getMemberIdx(), periodType);
		return ApiResponse.success(AnalyticsResponseCode.INSIGHT_SUCCESS, response);
	}
}
