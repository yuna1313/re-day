package com.reday.auth.infrastructure;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.reday.auth.application.port.RefreshTokenStore;
import com.reday.member.domain.Member;
import com.reday.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseRefreshTokenStore implements RefreshTokenStore {

	private final MemberRepository memberRepository;
	private final MemberRefreshTokenRepository memberRefreshTokenRepository;

	/**
	 * 회원의 기존 활성 refresh token을 폐기하고 새 refresh token을 저장합니다.
	 *
	 * @param email refresh token 소유자 이메일
	 * @param refreshToken 저장할 refresh token
	 * @param expiresAt refresh token 만료 일시
	 */
	@Override
	@Transactional
	public void save(String email, String refreshToken, LocalDateTime expiresAt) {
		Member member = findMember(email);
		log.info("[refreshTokenStore.save] refresh token 저장 요청: memberIdx={}", member.getMemberIdx());

		memberRefreshTokenRepository.revokeActiveTokensByMemberIdx(member.getMemberIdx(), LocalDateTime.now());
		memberRefreshTokenRepository.save(MemberRefreshToken.create(member.getMemberIdx(), refreshToken, expiresAt));
		log.info("[refreshTokenStore.save] refresh token 저장 완료: memberIdx={}", member.getMemberIdx());
	}

	/**
	 * 전달받은 refresh token이 DB에 저장된 활성 token인지 확인합니다.
	 *
	 * @param email refresh token 소유자 이메일
	 * @param refreshToken 확인할 refresh token
	 * @return 활성 refresh token이면 true
	 */
	@Override
	@Transactional(readOnly = true)
	public boolean matches(String email, String refreshToken) {
		Member member = findMember(email);
		boolean matched = memberRefreshTokenRepository
			.findByMemberIdxAndRefreshTokenAndRevokedAtIsNullAndExpiresAtAfter(
				member.getMemberIdx(),
				refreshToken,
				LocalDateTime.now()
			)
			.isPresent();

		if (!matched) {
			log.warn("[refreshTokenStore.matches] refresh token 불일치 또는 폐기됨: memberIdx={}", member.getMemberIdx());
		}
		return matched;
	}

	/**
	 * 회원의 모든 활성 refresh token을 폐기합니다.
	 *
	 * @param email refresh token 소유자 이메일
	 */
	@Override
	@Transactional
	public void revoke(String email) {
		Member member = findMember(email);
		log.info("[refreshTokenStore.revoke] refresh token 폐기 요청: memberIdx={}", member.getMemberIdx());
		memberRefreshTokenRepository.revokeActiveTokensByMemberIdx(member.getMemberIdx(), LocalDateTime.now());
		log.info("[refreshTokenStore.revoke] refresh token 폐기 완료: memberIdx={}", member.getMemberIdx());
	}

	/**
	 * 이메일로 회원을 조회합니다.
	 *
	 * @param email 조회할 회원 이메일
	 * @return 조회된 회원
	 * @throws IllegalArgumentException 이메일에 해당하는 회원이 없을 때 발생
	 */
	private Member findMember(String email) {
		return memberRepository.findByEmail(email)
			.orElseThrow(() -> new IllegalArgumentException("Member not found: " + email));
	}
}
