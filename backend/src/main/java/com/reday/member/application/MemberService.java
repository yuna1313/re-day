package com.reday.member.application;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.reday.auth.domain.RawPassword;
import com.reday.global.exception.BusinessException;
import com.reday.member.domain.Member;
import com.reday.member.dto.MemberMeResponse;
import com.reday.member.dto.PasswordChangeRequest;
import com.reday.member.exception.MemberErrorCode;
import com.reday.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;

	/**
	 * 로그인한 사용자의 이메일로 회원 정보를 조회합니다.
	 *
	 * @param email Spring Security 인증 정보에서 꺼낸 로그인 사용자 이메일
	 * @return 내 정보 조회 응답
	 * @throws BusinessException 이메일에 해당하는 회원이 없을 때 발생
	 */
	@Transactional(readOnly = true)
	public MemberMeResponse getMyInfo(String email) {
		log.info("[getMyInfo] 내 정보 조회 요청: {}", email);
		Member member = memberRepository.findByEmail(email)
			.orElseThrow(() -> {
				log.warn("[getMyInfo] 회원 정보 없음: {}", email);
				return new BusinessException(MemberErrorCode.NOT_FOUND);
			});

		MemberMeResponse response = new MemberMeResponse(
			member.getMemberIdx(),
			member.getNickname(),
			member.getEmail()
		);
		log.info("[getMyInfo] 내 정보 조회 완료: {}", email);

		return response;
	}

	/**
	 * 로그인한 사용자의 현재 비밀번호를 검증한 뒤 새 비밀번호로 변경합니다.
	 *
	 * @param email Spring Security 인증 정보에서 꺼낸 로그인 사용자 이메일
	 * @param request 현재 비밀번호와 새 비밀번호
	 * @throws BusinessException 회원이 없거나, 현재 비밀번호가 틀렸거나, 새 비밀번호가 기존 비밀번호와 같을 때 발생
	 */
	@Transactional
	public void changePassword(String email, PasswordChangeRequest request) {
		log.info("[changePassword] 비밀번호 변경 요청: {}", email);
		Member member = memberRepository.findByEmail(email)
			.orElseThrow(() -> {
				log.warn("[changePassword] 회원 정보 없음: {}", email);
				return new BusinessException(MemberErrorCode.NOT_FOUND);
			});

		if (request == null) {
			log.warn("[changePassword] 요청 본문 누락: {}", email);
			throw new BusinessException(MemberErrorCode.INVALID_CURRENT_PASSWORD);
		}

		if (!StringUtils.hasText(request.currentPassword())) {
			log.warn("[changePassword] 현재 비밀번호 누락: {}", email);
			throw new BusinessException(MemberErrorCode.INVALID_CURRENT_PASSWORD);
		}

		if (!passwordEncoder.matches(request.currentPassword(), member.getPassword())) {
			log.warn("[changePassword] 현재 비밀번호 검증 실패: {}", email);
			throw new BusinessException(MemberErrorCode.INVALID_CURRENT_PASSWORD);
		}

		log.info("[changePassword] 새 비밀번호 형식 검증: {}", email);
		RawPassword newPassword = RawPassword.of(request.newPassword(), request.newPassword());
		if (passwordEncoder.matches(newPassword.value(), member.getPassword())) {
			log.warn("[changePassword] 새 비밀번호가 현재 비밀번호와 동일: {}", email);
			throw new BusinessException(MemberErrorCode.SAME_AS_OLD_PASSWORD);
		}

		member.changePassword(passwordEncoder.encode(newPassword.value()));
		log.info("[changePassword] 비밀번호 변경 완료: {}", email);
	}
}
