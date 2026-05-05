package com.reday.global.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.reday.member.domain.Member;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UserPrincipal implements UserDetails {

	private final Long memberIdx;
	private final String email;
	private final String password;
	private final Collection<? extends GrantedAuthority> authorities;

	/**
	 * DB에서 조회한 Member 엔티티를 Spring Security가 이해할 수 있는 UserPrincipal로 변환합니다.
	 *
	 * 현재 프로젝트는 관리자 권한 없이 일반 사용자만 사용하므로 모든 회원에게 ROLE_USER를 부여합니다.
	 * 나중에 관리자나 유료 사용자 같은 권한이 생기면 이 메소드에서 Member의 권한 정보를 읽어
	 * authorities 목록을 다르게 만들어주면 됩니다.
	 *
	 * @param member DB에서 조회한 회원 엔티티
	 * @return Spring Security 인증 과정에서 사용할 사용자 정보
	 */
	public static UserPrincipal from(Member member) {
		return new UserPrincipal(
			member.getMemberIdx(),
			member.getEmail(),
			member.getPassword(),
			List.of(new SimpleGrantedAuthority("ROLE_USER"))
		);
	}

	/**
	 * 사용자가 가진 권한 목록을 반환합니다.
	 *
	 * Spring Security는 이 값을 보고 특정 API에 접근할 수 있는지 판단합니다.
	 * 예를 들어 ROLE_USER 권한이 있으면 사용자 전용 API에 접근할 수 있게 설정할 수 있습니다.
	 *
	 * @return 인증된 사용자의 권한 목록
	 */
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	/**
	 * 사용자의 암호화된 비밀번호를 반환합니다.
	 *
	 * 로그인할 때 사용자가 입력한 비밀번호를 PasswordEncoder로 암호화해서
	 * 이 값과 비교합니다. DB에는 반드시 평문 비밀번호가 아니라 BCrypt 같은 방식으로
	 * 암호화된 비밀번호가 저장되어 있어야 합니다.
	 *
	 * @return DB에 저장된 암호화 비밀번호
	 */
	@Override
	public String getPassword() {
		return password;
	}

	/**
	 * Spring Security에서 사용자를 식별할 이름을 반환합니다.
	 *
	 * Spring Security의 메소드 이름은 getUsername이지만,
	 * 이 프로젝트에서는 이메일을 로그인 ID로 사용하므로 email을 반환합니다.
	 *
	 * @return 로그인 ID로 사용하는 이메일
	 */
	@Override
	public String getUsername() {
		return email;
	}

	/**
	 * 계정 만료 여부를 반환합니다.
	 *
	 * true이면 계정이 만료되지 않았다는 뜻입니다.
	 * 아직 계정 만료 정책을 사용하지 않으므로 항상 true를 반환합니다.
	 *
	 * @return 계정이 만료되지 않았으면 true
	 */
	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	/**
	 * 계정 잠금 여부를 반환합니다.
	 *
	 * true이면 계정이 잠기지 않았다는 뜻입니다.
	 * 로그인 실패 횟수 제한 같은 기능을 추가하면 이 값을 Member 상태에 따라 바꿀 수 있습니다.
	 *
	 * @return 계정이 잠기지 않았으면 true
	 */
	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	/**
	 * 비밀번호 만료 여부를 반환합니다.
	 *
	 * true이면 비밀번호가 만료되지 않았다는 뜻입니다.
	 * 주기적인 비밀번호 변경 정책을 사용하지 않으므로 항상 true를 반환합니다.
	 *
	 * @return 비밀번호가 만료되지 않았으면 true
	 */
	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	/**
	 * 계정 활성화 여부를 반환합니다.
	 *
	 * true이면 로그인 가능한 활성 계정이라는 뜻입니다.
	 * 나중에 deletedAt 값이 있는 탈퇴 회원의 로그인을 막으려면
	 * 이 메소드에서 deletedAt 여부를 확인하도록 확장할 수 있습니다.
	 *
	 * @return 계정이 활성화되어 있으면 true
	 */
	@Override
	public boolean isEnabled() {
		return true;
	}
}
