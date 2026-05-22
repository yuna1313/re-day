package com.reday.auth.application.port;

/**
 * refresh token을 저장하고 검증하기 위한 포트입니다.
 */
public interface RefreshTokenStore {

	/**
	 * 회원 이메일 기준으로 최신 refresh token을 저장합니다.
	 *
	 * @param email refresh token 소유자 이메일
	 * @param refreshToken 저장할 refresh token
	 */
	void save(String email, String refreshToken);

	/**
	 * 전달된 refresh token이 서버에 저장된 최신 토큰과 일치하는지 확인합니다.
	 *
	 * @param email refresh token 소유자 이메일
	 * @param refreshToken 확인할 refresh token
	 * @return 저장된 refresh token과 일치하면 true
	 */
	boolean matches(String email, String refreshToken);
}
