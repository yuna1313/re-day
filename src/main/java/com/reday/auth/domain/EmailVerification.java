package com.reday.auth.domain;

import java.time.Duration;
import java.time.LocalDateTime;

import com.reday.auth.exception.AuthErrorCode;
import com.reday.global.exception.BusinessException;

/**
 * 이메일 인증코드와 재요청 제한 규칙을 표현하는 도메인 객체입니다.
 */
public record EmailVerification(
	Email email,
	VerificationCode code,
	LocalDateTime expiresAt,
	LocalDateTime requestWindowStartedAt,
	int requestCount
) {

	/**
	 * 첫 인증코드 발송 요청에 대한 인증 정보를 생성합니다.
	 *
	 * @param email 인증 대상 이메일
	 * @param code 인증코드
	 * @param now 생성 시각
	 * @param ttl 인증코드 유효 시간
	 * @return 새 이메일 인증 정보
	 */
	public static EmailVerification create(Email email, VerificationCode code, LocalDateTime now, Duration ttl) {
		return new EmailVerification(email, code, now.plus(ttl), now, 1);
	}

	/**
	 * 재요청 제한을 확인하고 새 인증코드로 인증 정보를 갱신합니다.
	 *
	 * @param code 새 인증코드
	 * @param now 재요청 시각
	 * @param ttl 인증코드 유효 시간
	 * @param requestWindow 요청 횟수 제한 구간
	 * @param maxRequests 제한 구간 내 최대 요청 횟수
	 * @return 갱신된 이메일 인증 정보
	 * @throws BusinessException 요청 횟수 제한을 초과한 경우
	 */
	public EmailVerification reissue(
		VerificationCode code,
		LocalDateTime now,
		Duration ttl,
		Duration requestWindow,
		int maxRequests
	) {
		if (isWithinRequestWindow(now, requestWindow) && requestCount >= maxRequests) {
			throw new BusinessException(AuthErrorCode.TOO_MANY_VERIFICATION_REQUESTS);
		}

		int nextRequestCount = isWithinRequestWindow(now, requestWindow) ? requestCount + 1 : 1;
		LocalDateTime nextRequestWindowStartedAt = isWithinRequestWindow(now, requestWindow)
			? requestWindowStartedAt
			: now;

		return new EmailVerification(email, code, now.plus(ttl), nextRequestWindowStartedAt, nextRequestCount);
	}

	/**
	 * 현재 시각이 인증코드 요청 횟수 제한 구간 안에 있는지 확인합니다.
	 *
	 * @param now 현재 시각
	 * @param requestWindow 요청 횟수 제한 구간
	 * @return 요청 횟수 제한 구간 안이면 true
	 */
	public boolean isWithinRequestWindow(LocalDateTime now, Duration requestWindow) {
		return requestWindowStartedAt.plus(requestWindow).isAfter(now);
	}
}
