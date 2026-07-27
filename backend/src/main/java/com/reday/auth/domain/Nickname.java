package com.reday.auth.domain;

import org.springframework.util.StringUtils;

import com.reday.auth.exception.AuthErrorCode;
import com.reday.global.exception.BusinessException;

/**
 * 회원가입에서 사용하는 닉네임 값 객체입니다.
 */
public record Nickname(String value) {

	private static final int MIN_LENGTH = 2;
	private static final int MAX_LENGTH = 50;

	/**
	 * 닉네임 문자열을 정규화하고 길이 조건을 검증합니다.
	 *
	 * @param value 검증할 닉네임
	 * @return 정규화된 닉네임 값 객체
	 * @throws BusinessException 닉네임 형식이 올바르지 않은 경우
	 */
	public static Nickname of(String value) {
		if (!StringUtils.hasText(value)) {
			throw new BusinessException(AuthErrorCode.INVALID_NICKNAME);
		}

		String trimmedValue = value.trim();
		if (trimmedValue.length() < MIN_LENGTH || trimmedValue.length() > MAX_LENGTH) {
			throw new BusinessException(AuthErrorCode.INVALID_NICKNAME);
		}

		return new Nickname(trimmedValue);
	}
}
