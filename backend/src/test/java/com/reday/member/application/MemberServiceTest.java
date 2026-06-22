package com.reday.member.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.reday.auth.exception.AuthErrorCode;
import com.reday.global.exception.BusinessException;
import com.reday.member.domain.Member;
import com.reday.member.dto.MemberMeResponse;
import com.reday.member.dto.PasswordChangeRequest;
import com.reday.member.exception.MemberErrorCode;
import com.reday.member.repository.MemberRepository;

class MemberServiceTest {

	private final MemberRepository memberRepository = org.mockito.Mockito.mock(MemberRepository.class);
	private final PasswordEncoder passwordEncoder = org.mockito.Mockito.mock(PasswordEncoder.class);
	private final MemberService memberService = new MemberService(memberRepository, passwordEncoder);

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

	/**
	 * 현재 비밀번호가 일치하고 새 비밀번호가 유효하면 암호화된 새 비밀번호로 변경합니다.
	 */
	@Test
	void changePasswordSucceedsWithValidRequest() {
		Member member = Member.create("유나", "yuna1313@naver.com", "encoded-old-password");
		when(memberRepository.findByEmail("yuna1313@naver.com")).thenReturn(Optional.of(member));
		when(passwordEncoder.matches("oldPassword123", "encoded-old-password")).thenReturn(true);
		when(passwordEncoder.matches("newPassword123", "encoded-old-password")).thenReturn(false);
		when(passwordEncoder.encode("newPassword123")).thenReturn("encoded-new-password");

		memberService.changePassword(
			"yuna1313@naver.com",
			new PasswordChangeRequest("oldPassword123", "newPassword123")
		);

		assertThat(member.getPassword()).isEqualTo("encoded-new-password");
	}

	/**
	 * 현재 비밀번호가 일치하지 않으면 비밀번호 변경을 거부합니다.
	 */
	@Test
	void changePasswordRejectsInvalidCurrentPassword() {
		Member member = Member.create("유나", "yuna1313@naver.com", "encoded-old-password");
		when(memberRepository.findByEmail("yuna1313@naver.com")).thenReturn(Optional.of(member));
		when(passwordEncoder.matches("wrongPassword123", "encoded-old-password")).thenReturn(false);

		assertThatThrownBy(() -> memberService.changePassword(
			"yuna1313@naver.com",
			new PasswordChangeRequest("wrongPassword123", "newPassword123")
		))
			.isInstanceOf(BusinessException.class)
			.extracting(exception -> ((BusinessException)exception).getErrorCode())
			.isEqualTo(MemberErrorCode.INVALID_CURRENT_PASSWORD);
	}

	/**
	 * 새 비밀번호가 비밀번호 정책을 만족하지 않으면 비밀번호 변경을 거부합니다.
	 */
	@Test
	void changePasswordRejectsInvalidNewPasswordFormat() {
		Member member = Member.create("유나", "yuna1313@naver.com", "encoded-old-password");
		when(memberRepository.findByEmail("yuna1313@naver.com")).thenReturn(Optional.of(member));
		when(passwordEncoder.matches("oldPassword123", "encoded-old-password")).thenReturn(true);

		assertThatThrownBy(() -> memberService.changePassword(
			"yuna1313@naver.com",
			new PasswordChangeRequest("oldPassword123", "short")
		))
			.isInstanceOf(BusinessException.class)
			.extracting(exception -> ((BusinessException)exception).getErrorCode())
			.isEqualTo(AuthErrorCode.INVALID_PASSWORD_FORMAT);
	}

	/**
	 * 새 비밀번호가 현재 비밀번호와 같으면 비밀번호 변경을 거부합니다.
	 */
	@Test
	void changePasswordRejectsSameAsOldPassword() {
		Member member = Member.create("유나", "yuna1313@naver.com", "encoded-old-password");
		when(memberRepository.findByEmail("yuna1313@naver.com")).thenReturn(Optional.of(member));
		when(passwordEncoder.matches("oldPassword123", "encoded-old-password")).thenReturn(true);

		assertThatThrownBy(() -> memberService.changePassword(
			"yuna1313@naver.com",
			new PasswordChangeRequest("oldPassword123", "oldPassword123")
		))
			.isInstanceOf(BusinessException.class)
			.extracting(exception -> ((BusinessException)exception).getErrorCode())
			.isEqualTo(MemberErrorCode.SAME_AS_OLD_PASSWORD);
	}
}
