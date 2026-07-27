package com.reday.auth.infrastructure;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.reday.auth.application.port.EmailVerificationStore;
import com.reday.auth.domain.Email;
import com.reday.auth.domain.EmailVerification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseEmailVerificationStore implements EmailVerificationStore {

	private final EmailVerificationRepository emailVerificationRepository;

	/**
	 * 인증 완료되지 않은 이메일 인증 정보를 조회합니다.
	 *
	 * @param email 조회할 이메일
	 * @return 인증 대기 중인 이메일 인증 정보
	 */
	@Override
	@Transactional(readOnly = true)
	public Optional<EmailVerification> findByEmail(Email email) {
		return emailVerificationRepository.findByEmail(email.value())
			.filter(entity -> entity.getVerifiedAt() == null)
			.map(EmailVerificationEntity::toDomain);
	}

	/**
	 * 이메일 인증 정보를 저장하거나 기존 인증 정보를 갱신합니다.
	 *
	 * @param emailVerification 저장할 이메일 인증 정보
	 */
	@Override
	@Transactional
	public void save(EmailVerification emailVerification) {
		EmailVerificationEntity entity = emailVerificationRepository
			.findByEmail(emailVerification.email().value())
			.map(existingEntity -> {
				existingEntity.update(emailVerification);
				return existingEntity;
			})
			.orElseGet(() -> EmailVerificationEntity.from(emailVerification));

		emailVerificationRepository.save(entity);
		log.info("[emailVerificationStore.save] 이메일 인증 정보 저장 완료: {}", emailVerification.email().value());
	}

	/**
	 * 이메일 인증 완료 일시를 저장합니다.
	 *
	 * @param email 인증이 완료된 이메일
	 */
	@Override
	@Transactional
	public void complete(Email email) {
		EmailVerificationEntity entity = emailVerificationRepository.findByEmail(email.value())
			.orElseThrow(() -> new IllegalArgumentException("Email verification not found: " + email.value()));
		entity.complete();
		log.info("[emailVerificationStore.complete] 이메일 인증 완료 저장: {}", email.value());
	}

	/**
	 * 이메일 인증 완료 여부를 조회합니다.
	 *
	 * @param email 확인할 이메일
	 * @return 인증 완료 상태이면 true
	 */
	@Override
	@Transactional(readOnly = true)
	public boolean isVerified(Email email) {
		return emailVerificationRepository.existsByEmailAndVerifiedAtIsNotNull(email.value());
	}
}
