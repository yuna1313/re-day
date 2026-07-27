package com.reday.auth.domain;

import java.util.regex.Pattern;

import org.springframework.util.StringUtils;

import com.reday.auth.exception.AuthErrorCode;
import com.reday.global.exception.BusinessException;

/**
 * 인증과 회원가입에서 사용하는 이메일 값 객체입니다.
 */
public record Email(String value) {

	private static final Pattern EMAIL_PATTERN = Pattern.compile(
		"^[A-Za-z0-9+_.-]+@([A-Za-z0-9-]+\\.)+[A-Za-z]{2,}$"
	);

	/**
	 * 이메일 문자열을 정규화하고 형식을 검증합니다.
	 *
	 * @param value 검증할 이메일
	 * @return 정규화된 이메일 값 객체
	 * @throws BusinessException 이메일 형식이 올바르지 않은 경우
	 */
	public static Email of(String value) {
		if (!StringUtils.hasText(value)) {
			throw new BusinessException(AuthErrorCode.INVALID_EMAIL_FORMAT);
		}

		String trimmedValue = value.trim();
		if (!EMAIL_PATTERN.matcher(trimmedValue).matches()) {
			throw new BusinessException(AuthErrorCode.INVALID_EMAIL_FORMAT);
		}

		return new Email(trimmedValue);
	}
}
