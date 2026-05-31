package com.reday.global.security.jwt;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

	private static final String TOKEN_TYPE_CLAIM = "tokenType";
	private static final String ACCESS_TOKEN_TYPE = "access";
	private static final String REFRESH_TOKEN_TYPE = "refresh";

	private final JwtProperties jwtProperties;

	/**
	 * Spring Security 인증 객체에서 사용자 식별값(email)을 꺼내 access token을 생성합니다.
	 *
	 * 로그인 성공 후 AuthenticationManager가 반환한 Authentication을 그대로 넘길 때 사용합니다.
	 *
	 * @param authentication 인증이 완료된 Spring Security 인증 객체
	 * @return 생성된 access token
	 */
	public String createAccessToken(Authentication authentication) {
		return createAccessToken(authentication.getName());
	}

	/**
	 * email을 subject로 담아 access token을 생성합니다.
	 *
	 * access token은 일반 API 요청에서 Authorization 헤더의 Bearer 토큰으로 사용됩니다.
	 *
	 * @param email 토큰 subject로 저장할 사용자 email
	 * @return 생성된 access token
	 */
	public String createAccessToken(String email) {
		return createToken(email, ACCESS_TOKEN_TYPE, jwtProperties.getAccessTokenExpiration());
	}

	/**
	 * Spring Security 인증 객체에서 사용자 식별값(email)을 꺼내 refresh token을 생성합니다.
	 *
	 * 로그인 성공 후 AuthenticationManager가 반환한 Authentication을 그대로 넘길 때 사용합니다.
	 *
	 * @param authentication 인증이 완료된 Spring Security 인증 객체
	 * @return 생성된 refresh token
	 */
	public String createRefreshToken(Authentication authentication) {
		return createRefreshToken(authentication.getName());
	}

	/**
	 * email을 subject로 담아 refresh token을 생성합니다.
	 *
	 * refresh token은 access token 재발급에 사용되며 access token보다 긴 만료 시간을 가집니다.
	 *
	 * @param email 토큰 subject로 저장할 사용자 email
	 * @return 생성된 refresh token
	 */
	public String createRefreshToken(String email) {
		return createToken(email, REFRESH_TOKEN_TYPE, jwtProperties.getRefreshTokenExpiration());
	}

	/**
	 * 토큰의 서명과 만료 시간을 검증합니다.
	 *
	 * access token인지 refresh token인지는 확인하지 않고, JWT 자체가 유효한지만 확인합니다.
	 *
	 * @param token 검증할 JWT
	 * @return 토큰이 정상적으로 파싱되면 true, 서명/만료/형식이 잘못되었으면 false
	 */
	public boolean validateToken(String token) {
		try {
			parseClaims(token);
			return true;
		} catch (JwtException | IllegalArgumentException e) {
			return false;
		}
	}

	/**
	 * 토큰의 서명, 만료 시간, tokenType claim이 access인지 검증합니다.
	 *
	 * API 요청 인증 필터에서는 refresh token이 access token처럼 사용되지 않도록 이 메서드를 사용합니다.
	 *
	 * @param token 검증할 JWT
	 * @return 유효한 access token이면 true
	 */
	public boolean validateAccessToken(String token) {
		return validateTokenType(token, ACCESS_TOKEN_TYPE);
	}

	/**
	 * 토큰의 서명, 만료 시간, tokenType claim이 refresh인지 검증합니다.
	 *
	 * access token 재발급 API에서 전달받은 refresh token을 확인할 때 사용합니다.
	 *
	 * @param token 검증할 JWT
	 * @return 유효한 refresh token이면 true
	 */
	public boolean validateRefreshToken(String token) {
		return validateTokenType(token, REFRESH_TOKEN_TYPE);
	}

	/**
	 * refresh token이 만료되었는지 확인합니다.
	 *
	 * 토큰 형식이나 서명이 잘못된 경우는 만료가 아니라 유효하지 않은 토큰으로 처리하기 위해 false를 반환합니다.
	 *
	 * @param token 만료 여부를 확인할 refresh token
	 * @return 만료된 refresh token이면 true
	 */
	public boolean isExpiredRefreshToken(String token) {
		try {
			parseClaims(token);
			return false;
		} catch (ExpiredJwtException exception) {
			return REFRESH_TOKEN_TYPE.equals(exception.getClaims().get(TOKEN_TYPE_CLAIM, String.class));
		} catch (JwtException | IllegalArgumentException exception) {
			return false;
		}
	}

	/**
	 * 토큰 subject에 저장된 사용자 email을 꺼냅니다.
	 *
	 * 호출 전에 validateAccessToken 또는 validateRefreshToken으로 검증한 토큰에 사용하는 것을 전제로 합니다.
	 *
	 * @param token email을 꺼낼 JWT
	 * @return 토큰 subject에 저장된 email
	 */
	public String getEmail(String token) {
		return parseClaims(token).getSubject();
	}

	/**
	 * 토큰에 저장된 만료 일시를 조회합니다.
	 *
	 * @param token 만료 일시를 조회할 JWT
	 * @return 토큰 만료 일시
	 */
	public LocalDateTime getExpiresAt(String token) {
		return LocalDateTime.ofInstant(parseClaims(token).getExpiration().toInstant(), ZoneId.systemDefault());
	}

	/**
	 * 공통 JWT 생성 로직입니다.
	 *
	 * email은 subject에 저장하고, access/refresh 구분값은 tokenType claim에 저장합니다.
	 *
	 * @param email 토큰 subject로 저장할 사용자 email
	 * @param tokenType access 또는 refresh 토큰 구분값
	 * @param expiration 토큰 만료 시간
	 * @return 생성된 JWT
	 */
	private String createToken(String email, String tokenType, Duration expiration) {
		Instant now = Instant.now();
		Instant expiresAt = now.plus(expiration);

		return Jwts.builder()
			.subject(email)
			.claim(TOKEN_TYPE_CLAIM, tokenType)
			.issuedAt(Date.from(now))
			.expiration(Date.from(expiresAt))
			.signWith(secretKey())
			.compact();
	}

	/**
	 * JWT 자체를 파싱한 뒤 tokenType claim이 기대한 값인지 확인합니다.
	 *
	 * 파싱 과정에서 서명, 만료 시간, 토큰 형식도 함께 검증됩니다.
	 *
	 * @param token 검증할 JWT
	 * @param tokenType 기대하는 토큰 타입
	 * @return 토큰 타입까지 일치하면 true
	 */
	private boolean validateTokenType(String token, String tokenType) {
		try {
			return tokenType.equals(parseClaims(token).get(TOKEN_TYPE_CLAIM, String.class));
		} catch (JwtException | IllegalArgumentException e) {
			return false;
		}
	}

	/**
	 * JWT를 파싱해 Claims를 반환합니다.
	 *
	 * 서명이 맞지 않거나 만료되었거나 형식이 잘못된 토큰이면 jjwt가 예외를 던집니다.
	 *
	 * @param token 파싱할 JWT
	 * @return JWT payload에 담긴 claims
	 */
	private Claims parseClaims(String token) {
		return Jwts.parser()
			.verifyWith(secretKey())
			.build()
			.parseSignedClaims(token)
			.getPayload();
	}

	/**
	 * application 설정의 jwt.secret 값으로 HMAC 서명 키를 만듭니다.
	 *
	 * secret이 Base64 형식이면 디코딩해서 사용하고, 아니면 문자열의 UTF-8 바이트를 그대로 사용합니다.
	 *
	 * @return JWT 서명과 검증에 사용할 SecretKey
	 */
	private SecretKey secretKey() {
		String secret = jwtProperties.getSecret();
		if (!StringUtils.hasText(secret)) {
			throw new IllegalStateException("JWT secret must be configured");
		}

		byte[] keyBytes;
		try {
			keyBytes = Decoders.BASE64.decode(secret);
		} catch (IllegalArgumentException e) {
			keyBytes = secret.getBytes(StandardCharsets.UTF_8);
		}
		return Keys.hmacShaKeyFor(keyBytes);
	}
}
