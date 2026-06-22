package com.reday.auth.dto;

public record SignupRequest(
	String nickname,
	String email,
	String password,
	String passwordConfirm,
	Boolean agreeTerms
) {
}
