package com.reday.auth.infrastructure;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRefreshTokenRepository extends JpaRepository<MemberRefreshToken, Long> {

	/**
	 * 아직 폐기되지 않고 만료되지 않은 회원 refresh token을 조회합니다.
	 *
	 * @param memberIdx 회원 식별자
	 * @param refreshToken 조회할 refresh token
	 * @param now 현재 일시
	 * @return 활성 상태 refresh token
	 */
	Optional<MemberRefreshToken> findByMemberIdxAndRefreshTokenAndRevokedAtIsNullAndExpiresAtAfter(
		Integer memberIdx,
		String refreshToken,
		LocalDateTime now
	);

	/**
	 * 회원의 폐기되지 않은 모든 refresh token을 폐기합니다.
	 *
	 * @param memberIdx 회원 식별자
	 * @param revokedAt 폐기 일시
	 */
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
		update MemberRefreshToken token
		set token.revokedAt = :revokedAt
		where token.memberIdx = :memberIdx
			and token.revokedAt is null
		""")
	void revokeActiveTokensByMemberIdx(
		@Param("memberIdx") Integer memberIdx,
		@Param("revokedAt") LocalDateTime revokedAt
	);
}
