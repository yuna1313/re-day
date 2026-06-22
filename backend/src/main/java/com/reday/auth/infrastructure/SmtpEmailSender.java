package com.reday.auth.infrastructure;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import com.reday.auth.application.port.EmailSender;
import com.reday.auth.domain.Email;
import com.reday.auth.domain.VerificationCode;
import com.reday.auth.exception.AuthErrorCode;
import com.reday.global.exception.BusinessException;

/**
 * Spring JavaMailSender를 사용해 이메일 인증코드를 발송하는 SMTP 어댑터입니다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SmtpEmailSender implements EmailSender {

	private final ObjectProvider<JavaMailSender> javaMailSenderProvider;

	/**
	 * SMTP 서버를 통해 이메일 인증코드를 발송합니다.
	 *
	 * @param email 수신 이메일
	 * @param verificationCode 발송할 인증코드
	 * @throws BusinessException 메일 발송 설정이 없거나 SMTP 발송에 실패한 경우
	 */
	@Override
	public void sendVerificationCode(Email email, VerificationCode verificationCode) {
		JavaMailSender javaMailSender = javaMailSenderProvider.getIfAvailable();
		if (javaMailSender == null) {
			log.warn("[sendVerificationCode] JavaMailSender bean is not available. spring.mail 설정을 확인하세요.");
			throw new BusinessException(AuthErrorCode.EMAIL_SEND_FAIL);
		}

		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(email.value());
		message.setSubject("[RE:DAY] 이메일 인증코드");
		message.setText("RE:DAY 이메일 인증코드는 " + verificationCode.value() + " 입니다. 5분 안에 입력해주세요.");

		try {
			log.info("[sendVerificationCode] 메일 발송 시도: {}", email.value());
			javaMailSender.send(message);
		} catch (MailException exception) {
			log.warn("[sendVerificationCode] email send failed: {}", email.value(), exception);
			throw new BusinessException(AuthErrorCode.EMAIL_SEND_FAIL);
		}
	}
}
