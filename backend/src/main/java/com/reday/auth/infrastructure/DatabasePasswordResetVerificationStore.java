package com.reday.auth.infrastructure;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.reday.auth.application.port.PasswordResetVerificationStore;
import com.reday.auth.domain.Email;
import com.reday.auth.domain.EmailVerification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabasePasswordResetVerificationStore implements PasswordResetVerificationStore {

	private final PasswordResetVerificationRepository passwordResetVerificationRepository;

	@Override
	@Transactional(readOnly = true)
	public Optional<EmailVerification> findByEmail(Email email) {
		return passwordResetVerificationRepository.findByEmail(email.value())
			.filter(entity -> entity.getVerifiedAt() == null)
			.map(PasswordResetVerificationEntity::toDomain);
	}

	@Override
	@Transactional
	public void save(EmailVerification emailVerification) {
		PasswordResetVerificationEntity entity = passwordResetVerificationRepository
			.findByEmail(emailVerification.email().value())
			.map(existingEntity -> {
				existingEntity.update(emailVerification);
				return existingEntity;
			})
			.orElseGet(() -> PasswordResetVerificationEntity.from(emailVerification));

		passwordResetVerificationRepository.save(entity);
		log.info("[passwordResetVerificationStore.save] password reset verification saved: {}",
			emailVerification.email().value());
	}

	@Override
	@Transactional
	public void complete(Email email) {
		PasswordResetVerificationEntity entity = passwordResetVerificationRepository.findByEmail(email.value())
			.orElseThrow(() -> new IllegalArgumentException("Password reset verification not found: " + email.value()));
		entity.complete();
		log.info("[passwordResetVerificationStore.complete] password reset verification completed: {}", email.value());
	}

	@Override
	@Transactional(readOnly = true)
	public boolean isVerified(Email email) {
		return passwordResetVerificationRepository.existsByEmailAndVerifiedAtIsNotNull(email.value());
	}

	@Override
	@Transactional
	public void delete(Email email) {
		passwordResetVerificationRepository.deleteByEmail(email.value());
		log.info("[passwordResetVerificationStore.delete] password reset verification deleted: {}", email.value());
	}
}
