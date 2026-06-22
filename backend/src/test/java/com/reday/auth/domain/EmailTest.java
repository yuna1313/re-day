package com.reday.auth.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.reday.auth.exception.AuthErrorCode;
import com.reday.global.exception.BusinessException;

class EmailTest {

	/**
	 * 이메일 값 객체는 앞뒤 공백을 제거한 값을 보관합니다.
	 */
	@Test
	void emailTrimsValue() {
		Email email = Email.of(" yuna1313@naver.com ");

		assertThat(email.value()).isEqualTo("yuna1313@naver.com");
	}

	/**
	 * 최상위 도메인이 없는 이메일은 생성할 수 없습니다.
	 */
	@Test
	void emailRejectsValueWithoutTopLevelDomain() {
		assertThatThrownBy(() -> Email.of("yuna1313@naver"))
			.isInstanceOf(BusinessException.class)
			.extracting(exception -> ((BusinessException)exception).getErrorCode())
			.isEqualTo(AuthErrorCode.INVALID_EMAIL_FORMAT);
	}
}
