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

	/**
	 * 이메일 인증 완료 후 저장된 인증 정보를 제거합니다.
	 *
	 * @param email 인증을 완료한 이메일
	 */
	void complete(Email email);

	/**
	 * 이메일 인증 완료 여부를 확인합니다.
	 *
	 * @param email 확인할 이메일
	 * @return 이메일 인증이 완료되었으면 true
	 */
	boolean isVerified(Email email);
}
