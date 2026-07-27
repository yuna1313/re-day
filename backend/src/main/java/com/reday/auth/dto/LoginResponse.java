package com.reday.auth.dto;

public record LoginResponse(
	String accessToken,
	String refreshToken,
	MemberInfo member
) {

	public record MemberInfo(
		Integer memberId,
		String nickname,
		String email
	) {
	}
}
