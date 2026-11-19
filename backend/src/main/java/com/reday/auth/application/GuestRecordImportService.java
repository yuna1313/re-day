package com.reday.auth.application;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.reday.auth.application.port.RefreshTokenStore;
import com.reday.auth.exception.AuthErrorCode;
import com.reday.auth.response.AuthResponseCode;
import com.reday.global.exception.BusinessException;
import com.reday.global.security.jwt.JwtTokenProvider;
import com.reday.member.application.MemberRecordAccess;
import com.reday.member.domain.Member;
import com.reday.member.repository.MemberRepository;
import com.reday.reflection.domain.Reflection;
import com.reday.reflection.repository.ReflectionRepository;
import com.reday.schedule.repository.ScheduleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GuestRecordImportService {

	private static final String REFLECTION_SEPARATOR = "\n\n게스트 모드에서 작성한 회고\n";
	private static final int REFLECTION_MAX_BYTES = 65_535;

	private final JwtTokenProvider jwtTokenProvider;
	private final RefreshTokenStore refreshTokenStore;
	private final MemberRecordAccess memberRecordAccess;
	private final MemberRepository memberRepository;
	private final ScheduleRepository scheduleRepository;
	private final ReflectionRepository reflectionRepository;

	@Transactional
	public AuthResponseCode importRecords(Integer recipientId, String guestRefreshToken) {
		if (!StringUtils.hasText(guestRefreshToken) || !jwtTokenProvider.validateRefreshToken(guestRefreshToken)) {
			throw new BusinessException(AuthErrorCode.GUEST_RECORD_IMPORT_UNAVAILABLE);
		}
		String guestSubject = jwtTokenProvider.getEmail(guestRefreshToken);
		if (!Member.isGuestSubject(guestSubject)) {
			throw new BusinessException(AuthErrorCode.GUEST_RECORD_IMPORT_UNAVAILABLE);
		}
		Member guest = memberRepository.findLockedByEmail(guestSubject)
			.filter(member -> !member.isDemo())
			.orElseThrow(() -> new BusinessException(AuthErrorCode.GUEST_RECORD_IMPORT_UNAVAILABLE));
		if (!guest.isActive()) {
			return AuthResponseCode.GUEST_RECORDS_ALREADY_IMPORTED;
		}

		// 두 소유자 잠금이 끝난 뒤 기록을 읽어 동시에 저장되는 회고를 빠뜨리지 않는다.
		if (memberRecordAccess.lockActiveOwner(recipientId).isGuest()
			|| !refreshTokenStore.matches(guestSubject, guestRefreshToken)) {
			throw new BusinessException(AuthErrorCode.GUEST_RECORD_IMPORT_UNAVAILABLE);
		}

		Integer guestId = guest.getMemberIdx();
		mergeReflections(guestId, recipientId);
		// 일정의 식별자를 유지해 연결된 미루기와 완료 로그도 함께 가져간다.
		scheduleRepository.transferOwnership(guestId, recipientId);
		guest.retireAfterRecordImport();
		refreshTokenStore.revoke(guestSubject);
		return AuthResponseCode.GUEST_RECORDS_IMPORTED;
	}

	private void mergeReflections(Integer guestId, Integer recipientId) {
		List<Reflection> guestReflections = reflectionRepository.findAllByMemberIdx(guestId);
		if (guestReflections.isEmpty()) {
			return;
		}
		List<LocalDate> dates = guestReflections.stream().map(Reflection::getReflectionDate).toList();
		Map<LocalDate, Reflection> existingByDate = reflectionRepository
			.findAllByMemberIdxAndReflectionDateIn(recipientId, dates).stream()
			.collect(Collectors.toMap(Reflection::getReflectionDate, Function.identity()));
		for (Reflection reflection : guestReflections) {
			Reflection existing = existingByDate.get(reflection.getReflectionDate());
			if (existing == null) {
				reflection.transferOwnership(recipientId);
				continue;
			}
			String content = existing.getContent() + REFLECTION_SEPARATOR + reflection.getContent();
			if (content.getBytes(StandardCharsets.UTF_8).length > REFLECTION_MAX_BYTES) {
				throw new BusinessException(AuthErrorCode.GUEST_REFLECTION_TOO_LONG);
			}
			existing.updateContent(content);
		}
	}
}
