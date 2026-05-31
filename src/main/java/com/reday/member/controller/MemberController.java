package com.reday.member.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reday.global.response.ApiResponse;
import com.reday.global.security.UserPrincipal;
import com.reday.member.application.MemberService;
import com.reday.member.dto.MemberMeResponse;
import com.reday.member.response.MemberResponseCode;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class MemberController {

	private final MemberService memberService;

	/**
	 * 로그인한 사용자의 회원 정보를 조회합니다.
	 *
	 * @param userPrincipal JWT 인증 필터가 SecurityContext에 저장한 사용자 정보
	 * @return 내 정보 조회 성공 응답
	 */
	@GetMapping("/api/v1/members/me")
	public ApiResponse<MemberMeResponse> getMyInfo(@AuthenticationPrincipal UserPrincipal userPrincipal) {
		MemberMeResponse response = memberService.getMyInfo(userPrincipal.getUsername());
		return ApiResponse.success(MemberResponseCode.ME_SUCCESS, response);
	}
}
