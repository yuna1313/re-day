package com.reday.auth.domain;

import java.util.regex.Pattern;

import org.springframework.util.StringUtils;

import com.reday.auth.exception.AuthErrorCode;
import com.reday.global.exception.BusinessException;

/**
 * 회원가입 요청에서 전달된 평문 비밀번호 값 객체입니다.
 */
public record RawPassword(String value) {

	private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{8,64}$");

	/**
	 * 비밀번호 형식과 확인값 일치 여부를 검증합니다.
	 *
	 * @param value 검증할 비밀번호
	 * @param confirm 비밀번호 확인값
	 * @return 검증된 평문 비밀번호 값 객체
	 * @throws BusinessException 비밀번호 형식이 올바르지 않거나 확인값이 일치하지 않는 경우
	 */
	public static RawPassword of(String value, String confirm) {
		if (!StringUtils.hasText(value) || !PASSWORD_PATTERN.matcher(value).matches()) {
			throw new BusinessException(AuthErrorCode.INVALID_PASSWORD_FORMAT);
		}

		if (!value.equals(confirm)) {
			throw new BusinessException(AuthErrorCode.PASSWORD_CONFIRM_MISMATCH);
		}

		return new RawPassword(value);
	}
}
