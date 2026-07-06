package com.reday.auth.dto;

public record PasswordResetVerificationVerifyRequest(
	String email,
	String verificationCode
) {
}
