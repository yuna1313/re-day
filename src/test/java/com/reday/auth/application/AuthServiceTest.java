package com.reday.auth.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.reday.auth.application.port.EmailSender;
import com.reday.auth.application.port.EmailVerificationStore;
import com.reday.auth.application.port.VerificationCodeGenerator;
import com.reday.auth.domain.Email;
import com.reday.auth.domain.EmailVerification;
import com.reday.auth.domain.VerificationCode;
import com.reday.auth.dto.EmailVerificationSendRequest;
import com.reday.auth.dto.EmailVerificationVerifyRequest;
import com.reday.auth.exception.AuthErrorCode;
import com.reday.global.exception.BusinessException;
import com.reday.member.repository.MemberRepository;

class AuthServiceTest {

	private final MemberRepository memberRepository = org.mockito.Mockito.mock(MemberRepository.class);
	private final PasswordEncoder passwordEncoder = org.mockito.Mockito.mock(PasswordEncoder.class);
	private final EmailSender emailSender = org.mockito.Mockito.mock(EmailSender.class);
	private final EmailVerificationStore emailVerificationStore = org.mockito.Mockito.mock(EmailVerificationStore.class);
	private final VerificationCodeGenerator verificationCodeGenerator = org.mockito.Mockito.mock(VerificationCodeGenerator.class);
	private final AuthService authService = new AuthService(
		memberRepository,
		passwordEncoder,
		emailSender,
		emailVerificationStore,
		verificationCodeGenerator
	);

	/**
	 * 최상위 도메인이 없는 이메일은 인증코드 발송 전에 형식 오류로 거부합니다.
	 */
	@Test
	void sendEmailVerificationRejectsEmailWithoutTopLevelDomain() {
		assertThatThrownBy(() -> authService.sendEmailVerification(new EmailVerificationSendRequest("yuna1313@naver")))
			.isInstanceOf(BusinessException.class)
			.extracting(exception -> ((BusinessException)exception).getErrorCode())
			.isEqualTo(AuthErrorCode.INVALID_EMAIL_FORMAT);

		verify(memberRepository, never()).existsByEmail("yuna1313@naver");
	}

	/**
	 * 저장된 인증정보의 코드와 요청 코드가 일치하면 이메일 인증을 완료합니다.
	 */
	@Test
	void verifyEmailVerificationSucceedsWhenCodeMatches() {
		Email email = Email.of("yuna1313@naver.com");
		EmailVerification verification = EmailVerification.create(
			email,
			VerificationCode.of("123456"),
			LocalDateTime.now(),
			Duration.ofMinutes(5)
		);
		when(emailVerificationStore.findByEmail(email)).thenReturn(Optional.of(verification));

		authService.verifyEmailVerification(new EmailVerificationVerifyRequest("yuna1313@naver.com", "123456"));

		verify(emailVerificationStore, times(1)).complete(email);
	}

	/**
	 * 저장된 인증정보가 없으면 인증코드 오류로 처리합니다.
	 */
	@Test
	void verifyEmailVerificationRejectsMissingVerification() {
		Email email = Email.of("yuna1313@naver.com");
		when(emailVerificationStore.findByEmail(email)).thenReturn(Optional.empty());

		assertThatThrownBy(() ->
			authService.verifyEmailVerification(new EmailVerificationVerifyRequest("yuna1313@naver.com", "123456")))
			.isInstanceOf(BusinessException.class)
			.extracting(exception -> ((BusinessException)exception).getErrorCode())
			.isEqualTo(AuthErrorCode.INVALID_VERIFICATION_CODE);
	}
}
