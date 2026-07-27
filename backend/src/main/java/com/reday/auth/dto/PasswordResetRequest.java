package com.reday.auth.dto;

public record PasswordResetRequest(
	String email,
	String newPassword,
	String newPasswordConfirm
) {
}
