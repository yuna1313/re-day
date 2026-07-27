package com.reday.auth.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.reday.auth.exception.AuthErrorCode;
import com.reday.global.exception.BusinessException;

class EmailVerificationTest {

	private static final Duration TTL = Duration.ofMinutes(5);
	private static final Duration REQUEST_WINDOW = Duration.ofHours(1);
	private static final int MAX_REQUESTS = 5;

	/**
	 * 첫 인증코드 발송 요청은 요청 횟수를 1로 기록합니다.
	 */
	@Test
	void createInitialEmailVerification() {
		LocalDateTime now = LocalDateTime.of(2026, 5, 22, 15, 0);

		EmailVerification verification = EmailVerification.create(
			Email.of("yuna1313@naver.com"),
			VerificationCode.of("123456"),
			now,
			TTL
		);

		assertThat(verification.requestCount()).isEqualTo(1);
		assertThat(verification.expiresAt()).isEqualTo(now.plus(TTL));
	}

	/**
	 * 요청 제한 구간 안의 재요청은 요청 횟수를 증가시킵니다.
	 */
	@Test
	void reissueIncrementsRequestCountWithinWindow() {
		LocalDateTime now = LocalDateTime.of(2026, 5, 22, 15, 0);
		EmailVerification verification = EmailVerification.create(
			Email.of("yuna1313@naver.com"),
			VerificationCode.of("123456"),
			now,
			TTL
		);

		EmailVerification reissued = verification.reissue(
			VerificationCode.of("654321"),
			now.plusMinutes(10),
			TTL,
			REQUEST_WINDOW,
			MAX_REQUESTS
		);

		assertThat(reissued.requestCount()).isEqualTo(2);
		assertThat(reissued.requestWindowStartedAt()).isEqualTo(now);
	}

	/**
	 * 요청 제한 구간 안에서 최대 요청 횟수를 초과하면 재발급할 수 없습니다.
	 */
	@Test
	void reissueRejectsTooManyRequestsWithinWindow() {
		LocalDateTime now = LocalDateTime.of(2026, 5, 22, 15, 0);
		EmailVerification verification = new EmailVerification(
			Email.of("yuna1313@naver.com"),
			VerificationCode.of("123456"),
			now.plus(TTL),
			now,
			MAX_REQUESTS
		);

		assertThatThrownBy(() -> verification.reissue(
			VerificationCode.of("654321"),
			now.plusMinutes(10),
			TTL,
			REQUEST_WINDOW,
			MAX_REQUESTS
		))
			.isInstanceOf(BusinessException.class)
			.extracting(exception -> ((BusinessException)exception).getErrorCode())
			.isEqualTo(AuthErrorCode.TOO_MANY_VERIFICATION_REQUESTS);
	}

	/**
	 * 저장된 인증코드와 입력한 인증코드가 같고 만료되지 않았으면 검증에 성공합니다.
	 */
	@Test
	void verifySucceedsWhenCodeMatchesAndNotExpired() {
		LocalDateTime now = LocalDateTime.of(2026, 5, 22, 15, 0);
		EmailVerification verification = EmailVerification.create(
			Email.of("yuna1313@naver.com"),
			VerificationCode.of("123456"),
			now,
			TTL
		);

		verification.verify(VerificationCode.of("123456"), now.plusMinutes(4));
	}

	/**
	 * 저장된 인증코드와 입력한 인증코드가 다르면 검증에 실패합니다.
	 */
	@Test
	void verifyRejectsMismatchedCode() {
		LocalDateTime now = LocalDateTime.of(2026, 5, 22, 15, 0);
		EmailVerification verification = EmailVerification.create(
			Email.of("yuna1313@naver.com"),
			VerificationCode.of("123456"),
			now,
			TTL
		);

		assertThatThrownBy(() -> verification.verify(VerificationCode.of("654321"), now.plusMinutes(4)))
			.isInstanceOf(BusinessException.class)
			.extracting(exception -> ((BusinessException)exception).getErrorCode())
			.isEqualTo(AuthErrorCode.INVALID_VERIFICATION_CODE);
	}

	/**
	 * 인증코드 유효 시간이 지나면 검증에 실패합니다.
	 */
	@Test
	void verifyRejectsExpiredCode() {
		LocalDateTime now = LocalDateTime.of(2026, 5, 22, 15, 0);
		EmailVerification verification = EmailVerification.create(
			Email.of("yuna1313@naver.com"),
			VerificationCode.of("123456"),
			now,
			TTL
		);

		assertThatThrownBy(() -> verification.verify(VerificationCode.of("123456"), now.plusMinutes(6)))
			.isInstanceOf(BusinessException.class)
			.extracting(exception -> ((BusinessException)exception).getErrorCode())
			.isEqualTo(AuthErrorCode.VERIFICATION_CODE_EXPIRED);
	}
}
