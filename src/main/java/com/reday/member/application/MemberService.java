package com.reday.member.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reday.global.exception.BusinessException;
import com.reday.member.domain.Member;
import com.reday.member.dto.MemberMeResponse;
import com.reday.member.exception.MemberErrorCode;
import com.reday.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberService {

	private final MemberRepository memberRepository;

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
}
