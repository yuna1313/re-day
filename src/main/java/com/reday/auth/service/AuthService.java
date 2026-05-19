package com.reday.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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

	private static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
	private static final String PASSWORD_PATTERN = "^(?=.*[A-Za-z])(?=.*\\d).{8,64}$";

	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;

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
}
