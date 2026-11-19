package com.reday.member.application;

import org.springframework.stereotype.Component;

import com.reday.global.exception.BusinessException;
import com.reday.global.exception.ErrorCode;
import com.reday.member.domain.Member;
import com.reday.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MemberRecordAccess {

	private final MemberRepository memberRepository;

	// 기록 변경과 가져오기는 같은 소유자 행을 잠가 동시에 진행되지 않도록 한다.
	public Member lockActiveOwner(Integer memberIdx) {
		return memberRepository.findLockedByMemberIdx(memberIdx)
			.filter(Member::isActive)
			.orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
	}
}
