package com.reday.auth.infrastructure;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetVerificationRepository extends JpaRepository<PasswordResetVerificationEntity, Long> {

	Optional<PasswordResetVerificationEntity> findByEmail(String email);

	boolean existsByEmailAndVerifiedAtIsNotNull(String email);

	void deleteByEmail(String email);
}
