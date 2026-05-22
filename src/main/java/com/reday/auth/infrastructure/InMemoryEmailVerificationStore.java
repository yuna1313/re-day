package com.reday.auth.infrastructure;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.reday.auth.application.port.EmailVerificationStore;
import com.reday.auth.domain.Email;
import com.reday.auth.domain.EmailVerification;

/**
 * 로컬 실행을 위한 메모리 기반 이메일 인증 정보 저장소입니다.
 */
@Component
public class InMemoryEmailVerificationStore implements EmailVerificationStore {

	private final Map<String, EmailVerification> emailVerifications = new ConcurrentHashMap<>();

	/**
	 * 메모리에 저장된 이메일 인증 정보를 조회합니다.
	 *
	 * @param email 조회할 이메일
	 * @return 저장된 이메일 인증 정보
	 */
	@Override
	public Optional<EmailVerification> findByEmail(Email email) {
		return Optional.ofNullable(emailVerifications.get(email.value()));
	}

	/**
	 * 이메일 인증 정보를 메모리에 저장합니다.
	 *
	 * @param emailVerification 저장할 이메일 인증 정보
	 */
	@Override
	public void save(EmailVerification emailVerification) {
		emailVerifications.put(emailVerification.email().value(), emailVerification);
	}
}
