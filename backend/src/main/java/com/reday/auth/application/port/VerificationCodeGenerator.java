package com.reday.auth.application.port;

import com.reday.auth.domain.VerificationCode;

/**
 * 인증코드 생성 정책을 애플리케이션 계층에서 사용하기 위한 포트입니다.
 */
public interface VerificationCodeGenerator {

	/**
	 * 새 인증코드를 생성합니다.
	 *
	 * @return 생성된 인증코드
	 */
	VerificationCode generate();
}
