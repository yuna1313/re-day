package com.reday.member.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.reday.global.response.ApiResponse;
import com.reday.global.security.UserPrincipal;
import com.reday.member.application.MemberService;
import com.reday.member.dto.MemberMeResponse;
import com.reday.member.dto.PasswordChangeRequest;
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

	/**
	 * 로그인한 사용자의 비밀번호를 변경합니다.
	 *
	 * @param userPrincipal JWT 인증 필터가 SecurityContext에 저장한 사용자 정보
	 * @param request 현재 비밀번호와 새 비밀번호
	 * @return 비밀번호 변경 성공 응답
	 */
	@PatchMapping("/api/v1/members/me/password")
	public ApiResponse<Void> changePassword(
		@AuthenticationPrincipal UserPrincipal userPrincipal,
		@RequestBody PasswordChangeRequest request
	) {
		memberService.changePassword(userPrincipal.getUsername(), request);
		return ApiResponse.success(MemberResponseCode.PASSWORD_UPDATED);
	}
}
