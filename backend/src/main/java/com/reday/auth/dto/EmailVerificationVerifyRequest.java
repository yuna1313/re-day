package com.reday.auth.dto;

public record EmailVerificationVerifyRequest(
	String email,
	String verificationCode
) {
}
