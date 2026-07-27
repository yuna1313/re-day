package com.reday.auth.application.port;

import java.util.Optional;

import com.reday.auth.domain.Email;
import com.reday.auth.domain.EmailVerification;

public interface PasswordResetVerificationStore {

	Optional<EmailVerification> findByEmail(Email email);

	void save(EmailVerification emailVerification);

	void complete(Email email);

	boolean isVerified(Email email);

	void delete(Email email);
}
