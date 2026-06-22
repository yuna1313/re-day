package com.reday.auth.dto;

public record LogoutRequest(
	String refreshToken
) {
}
