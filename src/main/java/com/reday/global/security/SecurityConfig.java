package com.reday.global.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	/**
	 * 애플리케이션의 HTTP 보안 규칙을 설정합니다.
	 *
	 * 이 프로젝트는 JWT 기반 인증을 사용할 예정이므로 서버 세션을 사용하지 않습니다.
	 * 로그인 후 서버가 세션을 저장하는 방식이 아니라, 클라이언트가 JWT를 가지고 있다가
	 * 요청마다 Authorization 헤더로 보내는 구조입니다.
	 *
	 * 현재 열어둔 경로는 /api/auth/** 와 /error 입니다.
	 * /api/auth/** 는 회원가입, 로그인, 토큰 재발급 같은 인증 전 API가 들어갈 자리입니다.
	 * 그 외 Todo 관련 API는 반드시 로그인한 사용자만 사용할 수 있도록 authenticated()로 막습니다.
	 *
	 * @param http Spring Security의 HTTP 보안 설정 객체
	 * @return Spring Security가 사용할 보안 필터 체인
	 * @throws Exception 보안 설정 구성 중 문제가 발생할 때
	 */
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
			.csrf(AbstractHttpConfigurer::disable)
			// JWT API 서버는 브라우저 세션 쿠키 기반 인증을 사용하지 않으므로 CSRF 보호를 비활성
			.formLogin(AbstractHttpConfigurer::disable)
			// 기본으로 제공하는 HTML 로그인 화면을 사용하지 않음
			.httpBasic(AbstractHttpConfigurer::disable) // JWT 구조에서는 토큰을 보내야 하므로 Basic 인증을 비활성
			// JWT 인증은 서버가 로그인 상태를 세션에 저장하지 않는 stateless 구조
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			// 인증 없이 접근 가능한 URL과 인증이 필요한 URL을 구분
			// /api/auth/** 는 로그인 전에도 접근 가능해야 하고, 나머지 모든 요청은 인증된 사용자만 접근할 수 있습니다.
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/api/auth/**", "/error").permitAll()
				.anyRequest().authenticated()
			)
			.build();
	}

	/**
	 * 로그인 인증을 직접 수행할 때 사용할 AuthenticationManager를 Bean으로 등록합니다.
	 *
	 * 로그인 API를 만들 때 이메일/비밀번호를 AuthenticationManager에 전달하면,
	 * Spring Security가 CustomUserDetailsService와 PasswordEncoder를 사용해서
	 * 회원 조회와 비밀번호 검증을 처리합니다.
	 *
	 * @param authenticationConfiguration Spring Security가 자동 구성한 인증 설정 객체
	 * @return 인증 처리를 담당하는 AuthenticationManager
	 * @throws Exception AuthenticationManager를 가져오는 중 문제가 발생할 때
	 */
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
		throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}

	/**
	 * 비밀번호를 안전하게 암호화하고 검증하는 PasswordEncoder를 등록합니다.
	 *
	 * BCryptPasswordEncoder는 같은 비밀번호라도 매번 다른 해시값을 만들 수 있도록 salt를 사용합니다.
	 * 회원가입 시에는 passwordEncoder.encode(rawPassword)로 비밀번호를 암호화해서 저장하고,
	 * 로그인 시에는 Spring Security가 passwordEncoder.matches(rawPassword, encodedPassword)로
	 * 입력한 비밀번호와 DB의 암호화 비밀번호를 비교합니다.
	 *
	 * @return BCrypt 기반 비밀번호 암호화 객체
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
