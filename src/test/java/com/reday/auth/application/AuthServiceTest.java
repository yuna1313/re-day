package com.reday.auth.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.reday.auth.application.port.EmailSender;
import com.reday.auth.application.port.EmailVerificationStore;
import com.reday.auth.application.port.VerificationCodeGenerator;
import com.reday.auth.domain.Email;
import com.reday.auth.domain.EmailVerification;
import com.reday.auth.domain.VerificationCode;
import com.reday.auth.dto.EmailVerificationSendRequest;
import com.reday.auth.dto.EmailVerificationVerifyRequest;
import com.reday.auth.dto.LoginRequest;
import com.reday.auth.dto.LoginResponse;
import com.reday.auth.dto.SignupRequest;
import com.reday.auth.exception.AuthErrorCode;
import com.reday.global.exception.BusinessException;
import com.reday.global.security.jwt.JwtTokenProvider;
import com.reday.member.domain.Member;
import com.reday.member.repository.MemberRepository;

class AuthServiceTest {

	private final MemberRepository memberRepository = org.mockito.Mockito.mock(MemberRepository.class);
	private final PasswordEncoder passwordEncoder = org.mockito.Mockito.mock(PasswordEncoder.class);
	private final EmailSender emailSender = org.mockito.Mockito.mock(EmailSender.class);
	private final EmailVerificationStore emailVerificationStore = org.mockito.Mockito.mock(EmailVerificationStore.class);
	private final VerificationCodeGenerator verificationCodeGenerator = org.mockito.Mockito.mock(VerificationCodeGenerator.class);
	private final AuthenticationManager authenticationManager = org.mockito.Mockito.mock(AuthenticationManager.class);
	private final JwtTokenProvider jwtTokenProvider = org.mockito.Mockito.mock(JwtTokenProvider.class);
	private final AuthService authService = new AuthService(
		memberRepository,
		passwordEncoder,
		emailSender,
		emailVerificationStore,
		verificationCodeGenerator,
		authenticationManager,
		jwtTokenProvider
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

	/**
	 * 이메일 인증을 완료하지 않은 이메일은 회원가입할 수 없습니다.
	 */
	@Test
	void signupRejectsUnverifiedEmail() {
		Email email = Email.of("yuna1313@naver.com");
		SignupRequest request = new SignupRequest(
			"유나",
			"yuna1313@naver.com",
			"password123",
			"password123",
			true
		);
		when(memberRepository.existsByEmail("yuna1313@naver.com")).thenReturn(false);
		when(emailVerificationStore.isVerified(email)).thenReturn(false);

		assertThatThrownBy(() -> authService.signup(request))
			.isInstanceOf(BusinessException.class)
			.extracting(exception -> ((BusinessException)exception).getErrorCode())
			.isEqualTo(AuthErrorCode.EMAIL_NOT_VERIFIED);

		verify(passwordEncoder, never()).encode("password123");
	}

	/**
	 * 이메일과 비밀번호 인증이 성공하면 access token, refresh token, 회원 정보를 반환합니다.
	 */
	@Test
	void loginSucceedsWithValidCredentials() {
		LoginRequest request = new LoginRequest("yuna1313@naver.com", "password123");
		Authentication authentication = new UsernamePasswordAuthenticationToken("yuna1313@naver.com", null);
		Member member = Member.create("유나", "yuna1313@naver.com", "encoded-password");

		when(authenticationManager.authenticate(org.mockito.ArgumentMatchers.any(UsernamePasswordAuthenticationToken.class)))
			.thenReturn(authentication);
		when(jwtTokenProvider.createAccessToken(authentication)).thenReturn("access-token");
		when(jwtTokenProvider.createRefreshToken(authentication)).thenReturn("refresh-token");
		when(memberRepository.findByEmail("yuna1313@naver.com")).thenReturn(Optional.of(member));

		LoginResponse response = authService.login(request);

		assertThat(response.accessToken()).isEqualTo("access-token");
		assertThat(response.refreshToken()).isEqualTo("refresh-token");
		assertThat(response.member().nickname()).isEqualTo("유나");
		assertThat(response.member().email()).isEqualTo("yuna1313@naver.com");
	}

	/**
	 * Spring Security 인증에 실패하면 로그인 자격 증명 오류로 처리합니다.
	 */
	@Test
	void loginRejectsInvalidCredentials() {
		LoginRequest request = new LoginRequest("yuna1313@naver.com", "wrong-password");
		when(authenticationManager.authenticate(org.mockito.ArgumentMatchers.any(UsernamePasswordAuthenticationToken.class)))
			.thenThrow(new BadCredentialsException("bad credentials"));

		assertThatThrownBy(() -> authService.login(request))
			.isInstanceOf(BusinessException.class)
			.extracting(exception -> ((BusinessException)exception).getErrorCode())
			.isEqualTo(AuthErrorCode.INVALID_CREDENTIALS);
	}

	/**
	 * 이메일 인증이 완료되지 않은 회원은 로그인할 수 없습니다.
	 */
	@Test
	void loginRejectsUnverifiedMember() {
		LoginRequest request = new LoginRequest("yuna1313@naver.com", "password123");
		Authentication authentication = new UsernamePasswordAuthenticationToken("yuna1313@naver.com", null);
		Member member = Member.createUnverified("유나", "yuna1313@naver.com", "encoded-password");

		when(authenticationManager.authenticate(org.mockito.ArgumentMatchers.any(UsernamePasswordAuthenticationToken.class)))
			.thenReturn(authentication);
		when(memberRepository.findByEmail("yuna1313@naver.com")).thenReturn(Optional.of(member));

		assertThatThrownBy(() -> authService.login(request))
			.isInstanceOf(BusinessException.class)
			.extracting(exception -> ((BusinessException)exception).getErrorCode())
			.isEqualTo(AuthErrorCode.EMAIL_NOT_VERIFIED);
	}
}
