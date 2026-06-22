package com.reday.auth.application.port;

import com.reday.auth.domain.Email;
import com.reday.auth.domain.VerificationCode;

/**
 * 이메일 발송 인프라를 애플리케이션 계층에서 사용하기 위한 포트입니다.
 */
public interface EmailSender {

	/**
	 * 이메일 인증코드를 발송합니다.
	 *
	 * @param email 수신 이메일
	 * @param verificationCode 발송할 인증코드
	 */
	void sendVerificationCode(Email email, VerificationCode verificationCode);
}
