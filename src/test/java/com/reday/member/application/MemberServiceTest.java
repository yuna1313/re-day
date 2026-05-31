package com.reday.member.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.reday.global.exception.BusinessException;
import com.reday.member.domain.Member;
import com.reday.member.dto.MemberMeResponse;
import com.reday.member.exception.MemberErrorCode;
import com.reday.member.repository.MemberRepository;

class MemberServiceTest {

	private final MemberRepository memberRepository = org.mockito.Mockito.mock(MemberRepository.class);
	private final MemberService memberService = new MemberService(memberRepository);

	/**
	 * 로그인한 사용자의 이메일에 해당하는 회원 정보를 조회해 응답으로 반환합니다.
	 */
	@Test
	void getMyInfoSucceedsWithAuthenticatedMemberEmail() {
		Member member = Member.create("유나", "yuna1313@naver.com", "encoded-password");
		when(memberRepository.findByEmail("yuna1313@naver.com")).thenReturn(Optional.of(member));

		MemberMeResponse response = memberService.getMyInfo("yuna1313@naver.com");

		assertThat(response.nickname()).isEqualTo("유나");
		assertThat(response.email()).isEqualTo("yuna1313@naver.com");
	}

	/**
	 * 인증 정보의 이메일에 해당하는 회원이 없으면 회원 없음 오류로 처리합니다.
	 */
	@Test
	void getMyInfoRejectsMissingMember() {
		when(memberRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> memberService.getMyInfo("missing@example.com"))
			.isInstanceOf(BusinessException.class)
			.extracting(exception -> ((BusinessException)exception).getErrorCode())
			.isEqualTo(MemberErrorCode.NOT_FOUND);
	}
}
