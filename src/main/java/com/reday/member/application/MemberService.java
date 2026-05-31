package com.reday.member.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;

import com.reday.auth.domain.RawPassword;
import com.reday.global.exception.BusinessException;
import com.reday.member.domain.Member;
import com.reday.member.dto.MemberMeResponse;
import com.reday.member.dto.PasswordChangeRequest;
import com.reday.member.exception.MemberErrorCode;
import com.reday.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
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
		Member member = memberRepository.findByEmail(email)
			.orElseThrow(() -> new BusinessException(MemberErrorCode.NOT_FOUND));

		return new MemberMeResponse(
			member.getMemberIdx(),
			member.getNickname(),
			member.getEmail()
		);
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
		Member member = memberRepository.findByEmail(email)
			.orElseThrow(() -> new BusinessException(MemberErrorCode.NOT_FOUND));

		if (request == null || !StringUtils.hasText(request.currentPassword())
			|| !passwordEncoder.matches(request.currentPassword(), member.getPassword())) {
			throw new BusinessException(MemberErrorCode.INVALID_CURRENT_PASSWORD);
		}

		RawPassword newPassword = RawPassword.of(request.newPassword(), request.newPassword());
		if (passwordEncoder.matches(newPassword.value(), member.getPassword())) {
			throw new BusinessException(MemberErrorCode.SAME_AS_OLD_PASSWORD);
		}

		member.changePassword(passwordEncoder.encode(newPassword.value()));
	}
}
