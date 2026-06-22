package com.reday.auth.domain;

import java.util.regex.Pattern;

import com.reday.auth.exception.AuthErrorCode;
import com.reday.global.exception.BusinessException;

/**
 * 이메일 인증에 사용하는 6자리 인증코드 값 객체입니다.
 */
public record VerificationCode(String value) {

	private static final Pattern CODE_PATTERN = Pattern.compile("^\\d{6}$");

	/**
	 * 인증코드 형식을 검증합니다.
	 *
	 * @param value 검증할 인증코드
	 * @return 검증된 인증코드 값 객체
	 * @throws BusinessException 인증코드 형식이 올바르지 않은 경우
	 */
	public static VerificationCode of(String value) {
		if (value == null || !CODE_PATTERN.matcher(value).matches()) {
			throw new BusinessException(AuthErrorCode.INVALID_VERIFICATION_CODE);
		}

		return new VerificationCode(value);
	}
}
