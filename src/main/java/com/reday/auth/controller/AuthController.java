package com.reday.auth.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.reday.auth.application.AuthService;
import com.reday.auth.dto.EmailVerificationSendRequest;
import com.reday.auth.dto.SignupRequest;
import com.reday.auth.dto.SignupResponse;
import com.reday.auth.response.AuthResponseCode;
import com.reday.global.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@PostMapping("/api/v1/auth/signup")
	public ApiResponse<SignupResponse> signup(@RequestBody SignupRequest request) {
		SignupResponse response = authService.signup(request);
		return ApiResponse.success(AuthResponseCode.SIGNUP_SUCCESS, response);
	}

	@PostMapping("/api/v1/auth/email/send-verification")
	public ApiResponse<Void> sendEmailVerification(@RequestBody EmailVerificationSendRequest request) {
		authService.sendEmailVerification(request);
		return ApiResponse.success(AuthResponseCode.EMAIL_SENT);
	}
}
