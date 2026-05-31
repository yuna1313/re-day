package com.reday.auth.infrastructure;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailVerificationRepository extends JpaRepository<EmailVerificationEntity, Long> {

	/**
	 * 이메일 인증 정보를 조회합니다.
	 *
	 * @param email 조회할 이메일
	 * @return 이메일 인증 엔티티
	 */
	Optional<EmailVerificationEntity> findByEmail(String email);

	/**
	 * 이메일 인증 완료 여부를 확인합니다.
	 *
	 * @param email 확인할 이메일
	 * @return 인증 완료 기록이 있으면 true
	 */
	boolean existsByEmailAndVerifiedAtIsNotNull(String email);
}
