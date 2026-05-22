package com.reday.auth.application.port;

import java.util.Optional;

import com.reday.auth.domain.Email;
import com.reday.auth.domain.EmailVerification;

/**
 * 이메일 인증 정보를 저장하고 조회하기 위한 포트입니다.
 */
public interface EmailVerificationStore {

	/**
	 * 이메일 인증 정보를 조회합니다.
	 *
	 * @param email 조회할 이메일
	 * @return 저장된 이메일 인증 정보
	 */
	Optional<EmailVerification> findByEmail(Email email);

	/**
	 * 이메일 인증 정보를 저장합니다.
	 *
	 * @param emailVerification 저장할 이메일 인증 정보
	 */
	void save(EmailVerification emailVerification);
}
