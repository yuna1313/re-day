package com.reday.member.dto;

public record PasswordChangeRequest(
	String currentPassword,
	String newPassword
) {
}
