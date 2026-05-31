package com.reday.auth.application;

import java.time.Duration;
import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.reday.auth.application.port.EmailSender;
import com.reday.auth.application.port.EmailVerificationStore;
import com.reday.auth.application.port.RefreshTokenStore;
import com.reday.auth.application.port.VerificationCodeGenerator;
import com.reday.auth.domain.Email;
import com.reday.auth.domain.EmailVerification;
import com.reday.auth.domain.Nickname;
import com.reday.auth.domain.RawPassword;
import com.reday.auth.domain.VerificationCode;
import com.reday.auth.dto.EmailVerificationSendRequest;
import com.reday.auth.dto.EmailVerificationVerifyRequest;
import com.reday.auth.dto.LoginRequest;
import com.reday.auth.dto.LoginResponse;
import com.reday.auth.dto.LogoutRequest;
import com.reday.auth.dto.SignupRequest;
import com.reday.auth.dto.SignupResponse;
import com.reday.auth.dto.TokenRefreshRequest;
import com.reday.auth.dto.TokenRefreshResponse;
import com.reday.auth.exception.AuthErrorCode;
import com.reday.global.exception.BusinessException;
import com.reday.global.security.jwt.JwtTokenProvider;
import com.reday.member.domain.Member;
import com.reday.member.repository.MemberRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

	private static final Duration VERIFICATION_CODE_TTL = Duration.ofMinutes(5);
	private static final Duration VERIFICATION_REQUEST_WINDOW = Duration.ofHours(1);
	private static final int MAX_VERIFICATION_REQUESTS = 5;

	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final EmailSender emailSender;
	private final EmailVerificationStore emailVerificationStore;
	private final VerificationCodeGenerator verificationCodeGenerator;
	private final RefreshTokenStore refreshTokenStore;
	private final AuthenticationManager authenticationManager;
	private final JwtTokenProvider jwtTokenProvider;

	/**
	 * 회원가입 요청을 검증하고 신규 회원을 저장합니다.
	 *
	 * @param request 회원가입 요청 정보
	 * @return 생성된 회원의 식별자와 이메일
	 */
	@Transactional
	public SignupResponse signup(SignupRequest request) {
		log.info("[signup] 회원가입 진행");
		SignupFields signupFields = validateSignupRequest(request);

		String nickname = signupFields.nickname().value();
		String email = signupFields.email().value();
		String password = signupFields.password().value();

		log.info("[signup] 이메일 존재하는지 여부 확인");
		if (memberRepository.existsByEmail(email)) {
			throw new BusinessException(AuthErrorCode.EMAIL_DUPLICATED);
		}
		if (!emailVerificationStore.isVerified(signupFields.email())) {
			throw new BusinessException(AuthErrorCode.EMAIL_NOT_VERIFIED);
		}

		Member member = Member.create(
			nickname,
			email,
			passwordEncoder.encode(password)
		);
		Member savedMember = memberRepository.save(member);
		log.info("[signup] 회원가입 완료");

		return new SignupResponse(savedMember.getMemberIdx(), savedMember.getEmail());
	}

	/**
	 * 회원가입에 사용할 이메일로 인증코드를 발송합니다.
	 *
	 * @param request 인증코드를 받을 이메일
	 */
	public void sendEmailVerification(EmailVerificationSendRequest request) {
		log.info("[sendEmailVerification] 이메일 인증코드 발송 요청");
		if (request == null) {
			log.warn("[sendEmailVerification] 이메일 형식 검증 실패");
			throw new BusinessException(AuthErrorCode.INVALID_EMAIL_FORMAT);
		}

		Email email = Email.of(request.email());
		log.info("[sendEmailVerification] 가입된 이메일 여부 확인: {}", email.value());
		if (memberRepository.existsByEmail(email.value())) {
			log.warn("[sendEmailVerification] 이미 가입된 이메일: {}", email.value());
			throw new BusinessException(AuthErrorCode.EMAIL_DUPLICATED);
		}

		EmailVerification verification = createVerification(email);
		emailSender.sendVerificationCode(email, verification.code());
		emailVerificationStore.save(verification);
		log.info("[sendEmailVerification] 이메일 인증코드 발송 완료: {}", email.value());
	}

	/**
	 * 이메일 인증코드를 검증하고 인증을 완료합니다.
	 *
	 * @param request 이메일 인증코드 확인 요청
	 */
	public void verifyEmailVerification(EmailVerificationVerifyRequest request) {
		log.info("[verifyEmailVerification] 이메일 인증코드 확인 요청");
		if (request == null) {
			log.warn("[verifyEmailVerification] 요청 본문 누락");
			throw new BusinessException(AuthErrorCode.EMAIL_VERIFY_FAIL);
		}

		Email email = Email.of(request.email());
		VerificationCode inputCode = VerificationCode.of(request.verificationCode());
		EmailVerification verification = emailVerificationStore.findByEmail(email)
			.orElseThrow(() -> new BusinessException(AuthErrorCode.INVALID_VERIFICATION_CODE));

		verification.verify(inputCode, LocalDateTime.now());
		emailVerificationStore.complete(email);
		log.info("[verifyEmailVerification] 이메일 인증 완료: {}", email.value());
	}

	/**
	 * 이메일과 비밀번호를 검증하고 JWT 토큰을 발급합니다.
	 *
	 * @param request 로그인 요청 정보
	 * @return 발급된 토큰과 로그인 회원 정보
	 */
	public LoginResponse login(LoginRequest request) {
		log.info("[login] 로그인 요청");
		if (request == null) {
			throw new BusinessException(AuthErrorCode.LOGIN_FAIL);
		}

		Email email = Email.of(request.email());
		Authentication authentication = authenticate(email, request.password());
		Member member = memberRepository.findByEmail(email.value())
			.orElseThrow(() -> new BusinessException(AuthErrorCode.LOGIN_FAIL));
		if (!member.isEmailVerified()) {
			throw new BusinessException(AuthErrorCode.EMAIL_NOT_VERIFIED);
		}

		String accessToken = jwtTokenProvider.createAccessToken(authentication);
		String refreshToken = jwtTokenProvider.createRefreshToken(authentication);
		refreshTokenStore.save(email.value(), refreshToken);

		log.info("[login] 로그인 성공: {}", email.value());
		return new LoginResponse(
			accessToken,
			refreshToken,
			new LoginResponse.MemberInfo(member.getMemberIdx(), member.getNickname(), member.getEmail())
		);
	}

	/**
	 * refresh token을 검증하고 access token과 refresh token을 재발급합니다.
	 *
	 * @param request 토큰 재발급 요청
	 * @return 재발급된 access token과 refresh token
	 */
	public TokenRefreshResponse refreshToken(TokenRefreshRequest request) {
		log.info("[refreshToken] 토큰 재발급 요청");
		if (request == null || !StringUtils.hasText(request.refreshToken())) {
			throw new BusinessException(AuthErrorCode.INVALID_REFRESH_TOKEN);
		}

		String requestedRefreshToken = request.refreshToken();
		if (jwtTokenProvider.isExpiredRefreshToken(requestedRefreshToken)) {
			throw new BusinessException(AuthErrorCode.REFRESH_TOKEN_EXPIRED);
		}
		if (!jwtTokenProvider.validateRefreshToken(requestedRefreshToken)) {
			throw new BusinessException(AuthErrorCode.INVALID_REFRESH_TOKEN);
		}

		String email = jwtTokenProvider.getEmail(requestedRefreshToken);
		if (!refreshTokenStore.matches(email, requestedRefreshToken)) {
			throw new BusinessException(AuthErrorCode.REFRESH_TOKEN_REVOKED);
		}
		if (!memberRepository.existsByEmail(email)) {
			throw new BusinessException(AuthErrorCode.INVALID_REFRESH_TOKEN);
		}

		String accessToken = jwtTokenProvider.createAccessToken(email);
		String refreshToken = jwtTokenProvider.createRefreshToken(email);
		refreshTokenStore.save(email, refreshToken);
		log.info("[refreshToken] 토큰 재발급 완료: {}", email);

		return new TokenRefreshResponse(accessToken, refreshToken);
	}

	/**
	 * refresh token을 검증하고 서버 저장소에서 제거해 로그아웃 처리합니다.
	 *
	 * @param request 로그아웃 요청
	 */
	public void logout(LogoutRequest request) {
		log.info("[logout] 로그아웃 요청");
		if (request == null || !StringUtils.hasText(request.refreshToken())) {
			throw new BusinessException(AuthErrorCode.INVALID_REFRESH_TOKEN);
		}

		String refreshToken = request.refreshToken();
		if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
			throw new BusinessException(AuthErrorCode.INVALID_REFRESH_TOKEN);
		}

		String email = jwtTokenProvider.getEmail(refreshToken);
		if (!refreshTokenStore.matches(email, refreshToken)) {
			throw new BusinessException(AuthErrorCode.REFRESH_TOKEN_REVOKED);
		}

		refreshTokenStore.revoke(email);
		log.info("[logout] 로그아웃 완료: {}", email);
	}

	/**
	 * Spring Security 인증 매니저를 통해 이메일과 비밀번호를 검증합니다.
	 *
	 * @param email 로그인 이메일
	 * @param password 로그인 비밀번호
	 * @return 인증 완료 객체
	 * @throws BusinessException 인증에 실패한 경우
	 */
	private Authentication authenticate(Email email, String password) {
		try {
			return authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(email.value(), password)
			);
		} catch (AuthenticationException exception) {
			log.warn("[authenticate] 로그인 인증 실패: {}", email.value());
			throw new BusinessException(AuthErrorCode.INVALID_CREDENTIALS);
		}
	}

	/**
	 * 회원가입 요청 필드를 도메인 값 객체로 변환하며 검증합니다.
	 *
	 * @param request 회원가입 요청 정보
	 * @return 검증된 회원가입 입력값
	 */
	private SignupFields validateSignupRequest(SignupRequest request) {
		log.info("[validateSignupRequest] 회원가입 형식 확인");
		if (request == null) {
			throw new BusinessException(AuthErrorCode.SIGNUP_FAIL);
		}

		log.info("[validateSignupRequest] 닉네임 형식 확인");
		Nickname nickname = Nickname.of(request.nickname());

		log.info("[validateSignupRequest] 이메일 형식 확인");
		Email email = Email.of(request.email());

		log.info("[validateSignupRequest] 비밀번호 형식 확인");
		RawPassword password = RawPassword.of(request.password(), request.passwordConfirm());

		log.info("[validateSignupRequest] 약관 동의 여부 확인");
		if (!Boolean.TRUE.equals(request.agreeTerms())) {
			throw new BusinessException(AuthErrorCode.REQUIRED_AGREEMENT_MISSING);
		}

		return new SignupFields(nickname, email, password);
	}

	/**
	 * 저장된 인증 요청 이력을 기반으로 새 이메일 인증 정보를 생성합니다.
	 *
	 * @param email 인증코드를 발송할 이메일
	 * @return 생성되거나 갱신된 이메일 인증 정보
	 */
	private EmailVerification createVerification(Email email) {
		LocalDateTime now = LocalDateTime.now();
		VerificationCode verificationCode = verificationCodeGenerator.generate();
		log.info("[createVerification] 인증코드 요청 기록 확인: {}", email.value());

		return emailVerificationStore.findByEmail(email)
			.map(verification -> verification.reissue(
				verificationCode,
				now,
				VERIFICATION_CODE_TTL,
				VERIFICATION_REQUEST_WINDOW,
				MAX_VERIFICATION_REQUESTS
			))
			.orElseGet(() -> EmailVerification.create(email, verificationCode, now, VERIFICATION_CODE_TTL));
	}

	/**
	 * 회원가입 요청 검증을 통과한 도메인 값 객체 묶음입니다.
	 *
	 * @param nickname 검증된 닉네임
	 * @param email 검증된 이메일
	 * @param password 검증된 평문 비밀번호
	 */
	private record SignupFields(
		Nickname nickname,
		Email email,
		RawPassword password
	) {
	}
}
