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
@Table(name = "email_verification")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailVerificationEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "email_verification_idx")
	private Integer emailVerificationIdx;

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

	private EmailVerificationEntity(EmailVerification emailVerification) {
		this.email = emailVerification.email().value();
		this.createdAt = LocalDateTime.now();
		update(emailVerification);
	}

	/**
	 * 이메일 인증 도메인 객체를 DB 엔티티로 생성합니다.
	 *
	 * @param emailVerification 이메일 인증 도메인 객체
	 * @return 이메일 인증 엔티티
	 */
	public static EmailVerificationEntity from(EmailVerification emailVerification) {
		return new EmailVerificationEntity(emailVerification);
	}

	/**
	 * DB 엔티티 값을 이메일 인증 도메인 객체로 변환합니다.
	 *
	 * @return 이메일 인증 도메인 객체
	 */
	public EmailVerification toDomain() {
		return new EmailVerification(
			Email.of(email),
			VerificationCode.of(verificationCode),
			expiresAt,
			requestedAt,
			requestCount
		);
	}

	/**
	 * 이메일 인증 정보를 새 인증코드 발급 상태로 갱신합니다.
	 *
	 * @param emailVerification 갱신할 이메일 인증 도메인 객체
	 */
	public void update(EmailVerification emailVerification) {
		this.verificationCode = emailVerification.code().value();
		this.expiresAt = emailVerification.expiresAt();
		this.requestCount = emailVerification.requestCount();
		this.requestedAt = emailVerification.requestWindowStartedAt();
		this.verifiedAt = null;
		this.updatedAt = LocalDateTime.now();
	}

	/**
	 * 이메일 인증 완료 일시를 기록합니다.
	 */
	public void complete() {
		this.verifiedAt = LocalDateTime.now();
		this.updatedAt = this.verifiedAt;
	}
}
