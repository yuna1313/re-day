package com.reday.member.dto;

public record MemberMeResponse(
	Integer memberId,
	String nickname,
	String email
) {
}
