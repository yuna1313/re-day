package com.reday.auth.dto;

public record LoginRequest(
	String email,
	String password
) {
}
