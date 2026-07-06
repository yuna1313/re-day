package com.reday.auth.infrastructure;

import java.time.LocalDateTime;

import com.reday.auth.domain.Email;
import com.reday.auth.domain.EmailVerification;
import com.reday.auth.domain.VerificationCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "password_reset_verification")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PasswordResetVerificationEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "password_reset_verification_idx")
	private Long passwordResetVerificationIdx;

	@Column(nullable = false, unique = true, length = 255)
	private String email;

	@Column(name = "verification_code", nullable = false, length = 6)
	private String verificationCode;

	@Column(name = "expires_at", nullable = false)
	private LocalDateTime expiresAt;

	@Column(name = "request_count", nullable = false)
	private int requestCount;

	@Column(name = "requested_at", nullable = false)
	private LocalDateTime requestedAt;

	@Column(name = "verified_at")
	private LocalDateTime verifiedAt;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	private PasswordResetVerificationEntity(EmailVerification emailVerification) {
		this.email = emailVerification.email().value();
		this.createdAt = LocalDateTime.now();
		update(emailVerification);
	}

	public static PasswordResetVerificationEntity from(EmailVerification emailVerification) {
		return new PasswordResetVerificationEntity(emailVerification);
	}

	public EmailVerification toDomain() {
		return new EmailVerification(
			Email.of(email),
			VerificationCode.of(verificationCode),
			expiresAt,
			requestedAt,
			requestCount
		);
	}

	public void update(EmailVerification emailVerification) {
		this.verificationCode = emailVerification.code().value();
		this.expiresAt = emailVerification.expiresAt();
		this.requestCount = emailVerification.requestCount();
		this.requestedAt = emailVerification.requestWindowStartedAt();
		this.verifiedAt = null;
		this.updatedAt = LocalDateTime.now();
	}
}
