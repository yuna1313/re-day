package com.reday.auth.infrastructure;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "member_refresh_token")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberRefreshToken {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "member_refresh_token_idx")
	private Integer memberRefreshTokenIdx;

	@Column(name = "member_idx", nullable = false)
	private Integer memberIdx;

	@Column(name = "refresh_token", nullable = false, length = 500)
	private String refreshToken;

	@Column(name = "expires_at", nullable = false)
	private LocalDateTime expiresAt;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "revoked_at")
	private LocalDateTime revokedAt;

	private MemberRefreshToken(Integer memberIdx, String refreshToken, LocalDateTime expiresAt) {
		this.memberIdx = memberIdx;
		this.refreshToken = refreshToken;
		this.expiresAt = expiresAt;
		this.createdAt = LocalDateTime.now();
	}

	/**
	 * 회원 refresh token 엔티티를 생성합니다.
	 *
	 * @param memberIdx refresh token 소유 회원 식별자
	 * @param refreshToken 저장할 refresh token
	 * @param expiresAt refresh token 만료 일시
	 * @return 회원 refresh token 엔티티
	 */
	public static MemberRefreshToken create(Integer memberIdx, String refreshToken, LocalDateTime expiresAt) {
		return new MemberRefreshToken(memberIdx, refreshToken, expiresAt);
	}
}
