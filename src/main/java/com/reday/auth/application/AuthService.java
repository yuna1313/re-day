package com.reday.auth.application;

import java.time.Duration;
import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reday.auth.application.port.EmailSender;
import com.reday.auth.application.port.EmailVerificationStore;
import com.reday.auth.application.port.VerificationCodeGenerator;
import com.reday.auth.domain.Email;
import com.reday.auth.domain.EmailVerification;
import com.reday.auth.domain.Nickname;
import com.reday.auth.domain.RawPassword;
import com.reday.auth.domain.VerificationCode;
import com.reday.auth.dto.EmailVerificationSendRequest;
import com.reday.auth.dto.SignupRequest;
import com.reday.auth.dto.SignupResponse;
import com.reday.auth.exception.AuthErrorCode;
import com.reday.global.exception.BusinessException;
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
