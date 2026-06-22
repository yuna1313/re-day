package com.reday.auth.infrastructure;

import java.security.SecureRandom;

import org.springframework.stereotype.Component;

import com.reday.auth.application.port.VerificationCodeGenerator;
import com.reday.auth.domain.VerificationCode;

/**
 * 보안 난수를 사용해 6자리 이메일 인증코드를 생성합니다.
 */
@Component
public class SecureRandomVerificationCodeGenerator implements VerificationCodeGenerator {

	private static final int VERIFICATION_CODE_BOUND = 1_000_000;
	private static final int VERIFICATION_CODE_LENGTH = 6;

	private final SecureRandom secureRandom = new SecureRandom();

	/**
	 * 0으로 시작할 수 있는 6자리 이메일 인증코드를 생성합니다.
	 *
	 * @return 생성된 인증코드
	 */
	@Override
	public VerificationCode generate() {
		return VerificationCode.of(String.format(
			"%0" + VERIFICATION_CODE_LENGTH + "d",
			secureRandom.nextInt(VERIFICATION_CODE_BOUND)
		));
	}
}
