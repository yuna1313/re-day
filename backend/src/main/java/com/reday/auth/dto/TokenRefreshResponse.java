package com.reday.auth.dto;

public record TokenRefreshResponse(
	String accessToken,
	String refreshToken
) {
}
