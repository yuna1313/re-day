package com.reday.auth.infrastructure;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.reday.auth.application.port.RefreshTokenStore;

/**
 * 로컬 실행을 위한 메모리 기반 refresh token 저장소입니다.
 */
@Component
public class InMemoryRefreshTokenStore implements RefreshTokenStore {

	private final Map<String, String> refreshTokens = new ConcurrentHashMap<>();

	/**
	 * 회원 이메일 기준으로 최신 refresh token을 메모리에 저장합니다.
	 *
	 * @param email refresh token 소유자 이메일
	 * @param refreshToken 저장할 refresh token
	 */
	@Override
	public void save(String email, String refreshToken) {
		refreshTokens.put(email, refreshToken);
	}

	/**
	 * 전달된 refresh token이 메모리에 저장된 최신 토큰과 일치하는지 확인합니다.
	 *
	 * @param email refresh token 소유자 이메일
	 * @param refreshToken 확인할 refresh token
	 * @return 저장된 refresh token과 일치하면 true
	 */
	@Override
	public boolean matches(String email, String refreshToken) {
		return refreshToken.equals(refreshTokens.get(email));
	}

	/**
	 * 회원 이메일 기준으로 메모리에 저장된 refresh token을 제거합니다.
	 *
	 * @param email refresh token 소유자 이메일
	 */
	@Override
	public void revoke(String email) {
		refreshTokens.remove(email);
	}
}
