package com.reday.global.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reday.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

	private final MemberRepository memberRepository;

	/**
	 * Spring Security가 로그인 요청을 처리할 때 호출하는 메소드입니다.
	 *
	 * 메소드 이름은 loadUserByUsername이지만, 이 프로젝트에서는 이메일을 로그인 ID로 사용합니다.
	 * 따라서 username 파라미터 자리에는 email 값이 들어온다고 보면 됩니다.
	 *
	 * 처리 흐름은 다음과 같습니다.
	 * 1. 이메일로 DB에서 회원을 조회합니다.
	 * 2. 회원이 있으면 Member 엔티티를 UserPrincipal로 변환합니다.
	 * 3. 회원이 없으면 UsernameNotFoundException을 던져 인증 실패로 처리합니다.
	 *
	 * @param email 로그인 ID로 사용하는 이메일
	 * @return Spring Security가 인증에 사용할 UserDetails 구현체
	 * @throws UsernameNotFoundException 이메일에 해당하는 회원이 없을 때 발생
	 */
	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		return memberRepository.findByEmail(email)
			.map(UserPrincipal::from)
			.orElseThrow(() -> new UsernameNotFoundException("Member not found: " + email));
	}
}
