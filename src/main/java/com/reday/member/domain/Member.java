package com.reday.member.domain;

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
@Table(name = "member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "member_idx")
	private Integer memberIdx;

	@Column(nullable = false, length = 50)
	private String nickname;

	@Column(nullable = false, unique = true, length = 255)
	private String email;

	@Column(nullable = false, length = 255)
	private String password;

	@Column(name = "email_verified", nullable = false, columnDefinition = "TINYINT(1)")
	private boolean emailVerified;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "last_login_at")
	private LocalDateTime lastLoginAt;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	private Member(String nickname, String email, String encodedPassword, boolean emailVerified) {
		this.nickname = nickname;
		this.email = email;
		this.password = encodedPassword;
		this.emailVerified = emailVerified;
		this.createdAt = LocalDateTime.now();
	}

	/**
	 * 이메일 인증이 완료된 신규 회원을 생성합니다.
	 *
	 * @param nickname 회원 닉네임
	 * @param email 회원 이메일
	 * @param encodedPassword 암호화된 비밀번호
	 * @return 이메일 인증이 완료된 회원
	 */
	public static Member create(String nickname, String email, String encodedPassword) {
		return new Member(nickname, email, encodedPassword, true);
	}

	/**
	 * 이메일 인증이 완료되지 않은 회원을 생성합니다.
	 *
	 * @param nickname 회원 닉네임
	 * @param email 회원 이메일
	 * @param encodedPassword 암호화된 비밀번호
	 * @return 이메일 인증이 완료되지 않은 회원
	 */
	public static Member createUnverified(String nickname, String email, String encodedPassword) {
		return new Member(nickname, email, encodedPassword, false);
	}
}
