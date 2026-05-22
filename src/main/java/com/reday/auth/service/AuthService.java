package com.reday.auth.service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.reday.auth.dto.EmailVerificationSendRequest;
import com.reday.auth.dto.SignupRequest;
import com.reday.auth.dto.SignupResponse;
import com.reday.auth.exception.AuthErrorCode;
import com.reday.global.exception.BusinessException;
import com.reday.member.domain.Member;
import com.reday.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

	private static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9-]+\\.)+[A-Za-z]{2,}$";
	private static final String PASSWORD_PATTERN = "^(?=.*[A-Za-z])(?=.*\\d).{8,64}$";
	private static final int VERIFICATION_CODE_BOUND = 1_000_000;
	private static final int VERIFICATION_CODE_LENGTH = 6;
	private static final Duration VERIFICATION_CODE_TTL = Duration.ofMinutes(5);
	private static final Duration VERIFICATION_REQUEST_WINDOW = Duration.ofHours(1);
	private static final int MAX_VERIFICATION_REQUESTS = 5;

	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final ObjectProvider<JavaMailSender> javaMailSenderProvider;
	private final SecureRandom secureRandom = new SecureRandom();
	private final Map<String, EmailVerification> emailVerifications = new ConcurrentHashMap<>();

	/**
	 * 회원가입 요청을 검증하고 신규 회원을 저장합니다.
	 *
	 * @param request 회원가입 요청 정보
	 * @return 생성된 회원의 식별자와 이메일
	 */
	@Transactional
	public SignupResponse signup(SignupRequest request) {
		log.info("[signup] 회원가입 진행");
		validateSignupRequest(request);

		String nickname = request.nickname().trim();
		String email = request.email().trim();

		log.info("[signup] 이메일 존재하는지 여부 확인");
		if (memberRepository.existsByEmail(email)) {
			throw new BusinessException(AuthErrorCode.EMAIL_DUPLICATED);
		}

		Member member = Member.create(
			nickname,
			email,
			passwordEncoder.encode(request.password())
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
		if (request == null || !isValidEmail(request.email())) {
			log.warn("[sendEmailVerification] 이메일 형식 검증 실패");
			throw new BusinessException(AuthErrorCode.INVALID_EMAIL_FORMAT);
		}

		String email = request.email().trim();
		log.info("[sendEmailVerification] 가입된 이메일 여부 확인: {}", email);
		if (memberRepository.existsByEmail(email)) {
			log.warn("[sendEmailVerification] 이미 가입된 이메일: {}", email);
			throw new BusinessException(AuthErrorCode.EMAIL_DUPLICATED);
		}

		EmailVerification verification = createVerification(email);
		sendVerificationEmail(email, verification.code());
		emailVerifications.put(email, verification);
		log.info("[sendEmailVerification] 이메일 인증코드 발송 완료: {}", email);
	}

	/**
	 * 회원가입 요청 필드의 필수값, 형식, 비밀번호 확인, 약관 동의 여부를 검증합니다.
	 *
	 * @param request 회원가입 요청 정보
	 */
	private void validateSignupRequest(SignupRequest request) {
		log.info("[validateSignupRequest] 회원가입 형식 확인");
		if (request == null) {
			throw new BusinessException(AuthErrorCode.SIGNUP_FAIL);
		}

		log.info("[validateSignupRequest] 닉네임 형식 확인");
		if (!isValidNickname(request.nickname())) {
			throw new BusinessException(AuthErrorCode.INVALID_NICKNAME);
		}

		log.info("[validateSignupRequest] 이메일 형식 확인");
		if (!isValidEmail(request.email())) {
			throw new BusinessException(AuthErrorCode.INVALID_EMAIL_FORMAT);
		}

		log.info("[validateSignupRequest] 비밀번호 형식 확인");
		if (!isValidPassword(request.password())) {
			throw new BusinessException(AuthErrorCode.INVALID_PASSWORD_FORMAT);
		}

		log.info("[validateSignupRequest] 비밀번호 확인 일치 여부 확인");
		if (!request.password().equals(request.passwordConfirm())) {
			throw new BusinessException(AuthErrorCode.PASSWORD_CONFIRM_MISMATCH);
		}

		log.info("[validateSignupRequest] 약관 동의 여부 확인");
		if (!Boolean.TRUE.equals(request.agreeTerms())) {
			throw new BusinessException(AuthErrorCode.REQUIRED_AGREEMENT_MISSING);
		}
	}

	/**
	 * 닉네임이 허용 길이와 공백 조건을 만족하는지 확인합니다.
	 *
	 * @param nickname 검증할 닉네임
	 * @return 닉네임이 유효하면 true
	 */
	private boolean isValidNickname(String nickname) {
		log.info("[isValidNickname] 닉네임 validation 확인: {}", nickname);
		if (!StringUtils.hasText(nickname)) {
			return false;
		}
		String trimmedNickname = nickname.trim();
		return trimmedNickname.length() >= 2 && trimmedNickname.length() <= 50;
	}

	/**
	 * 이메일이 비어 있지 않고 이메일 형식 조건을 만족하는지 확인합니다.
	 *
	 * @param email 검증할 이메일
	 * @return 이메일이 유효하면 true
	 */
	private boolean isValidEmail(String email) {
		log.info("[isValidEmail] 이메일 validation 확인: {}", email);
		return StringUtils.hasText(email) && email.trim().matches(EMAIL_PATTERN);
	}

	/**
	 * 비밀번호가 비어 있지 않고 영문자와 숫자를 포함한 길이 조건을 만족하는지 확인합니다.
	 *
	 * @param password 검증할 비밀번호
	 * @return 비밀번호가 유효하면 true
	 */
	private boolean isValidPassword(String password) {
		log.info("[isValidPassword] 비밀번호 validation 확인");
		return StringUtils.hasText(password) && password.matches(PASSWORD_PATTERN);
	}

	/**
	 * 이메일별 인증코드 요청 횟수를 확인하고 새 인증 정보를 생성합니다.
	 *
	 * @param email 인증코드를 발송할 이메일
	 * @return 생성된 인증코드 정보
	 * @throws BusinessException 요청 횟수 제한을 초과한 경우
	 */
	private EmailVerification createVerification(String email) {
		LocalDateTime now = LocalDateTime.now();
		EmailVerification previousVerification = emailVerifications.get(email);
		log.info("[createVerification] 인증코드 요청 기록 확인: {}", email);

		if (previousVerification != null
			&& previousVerification.isWithinRequestWindow(now)
			&& previousVerification.requestCount() >= MAX_VERIFICATION_REQUESTS) {
			log.warn(
				"[createVerification] 인증코드 요청 횟수 초과: email={}, requestCount={}",
				email,
				previousVerification.requestCount()
			);
			throw new BusinessException(AuthErrorCode.TOO_MANY_VERIFICATION_REQUESTS);
		}

		int requestCount = previousVerification == null || !previousVerification.isWithinRequestWindow(now)
			? 1
			: previousVerification.requestCount() + 1;

		return new EmailVerification(
			generateVerificationCode(),
			now.plus(VERIFICATION_CODE_TTL),
			now,
			requestCount
		);
	}

	/**
	 * 0으로 시작할 수 있는 6자리 이메일 인증코드를 생성합니다.
	 *
	 * @return 6자리 인증코드 문자열
	 */
	private String generateVerificationCode() {
		return String.format(
			"%0" + VERIFICATION_CODE_LENGTH + "d",
			secureRandom.nextInt(VERIFICATION_CODE_BOUND)
		);
	}

	/**
	 * 생성된 인증코드를 대상 이메일로 발송합니다.
	 *
	 * @param email 인증코드를 받을 이메일
	 * @param verificationCode 발송할 인증코드
	 * @throws BusinessException 메일 발송 설정이 없거나 발송에 실패한 경우
	 */
	private void sendVerificationEmail(String email, String verificationCode) {
		JavaMailSender javaMailSender = javaMailSenderProvider.getIfAvailable();
		if (javaMailSender == null) {
			log.warn("[sendVerificationEmail] JavaMailSender bean is not available. spring.mail 설정을 확인하세요.");
			throw new BusinessException(AuthErrorCode.EMAIL_SEND_FAIL);
		}

		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(email);
		message.setSubject("[RE:DAY] 이메일 인증코드");
		message.setText("RE:DAY 이메일 인증코드는 " + verificationCode + " 입니다. 5분 안에 입력해주세요.");

		try {
			log.info("[sendVerificationEmail] 메일 발송 시도: {}", email);
			javaMailSender.send(message);
		} catch (MailException exception) {
			log.warn("[sendVerificationEmail] email send failed: {}", email, exception);
			throw new BusinessException(AuthErrorCode.EMAIL_SEND_FAIL);
		}
	}

	private record EmailVerification(
		String code,
		LocalDateTime expiresAt,
		LocalDateTime requestWindowStartedAt,
		int requestCount
	) {
		/**
		 * 현재 시각이 인증코드 요청 횟수 제한 구간 안에 있는지 확인합니다.
		 *
		 * @param now 현재 시각
		 * @return 요청 횟수 제한 구간 안이면 true
		 */
		private boolean isWithinRequestWindow(LocalDateTime now) {
			return requestWindowStartedAt.plus(VERIFICATION_REQUEST_WINDOW).isAfter(now);
		}
	}
}
