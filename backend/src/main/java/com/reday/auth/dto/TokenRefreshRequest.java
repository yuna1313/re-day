package com.reday.auth.dto;

public record TokenRefreshRequest(
	String refreshToken
) {
}
