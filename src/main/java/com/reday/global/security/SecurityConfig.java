package com.reday.global.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reday.global.exception.ErrorCode;
import com.reday.global.response.ApiResponse;
import com.reday.global.security.jwt.JwtAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * 애플리케이션의 HTTP 보안 규칙을 설정합니다.
	 *
	 * 이 프로젝트는 JWT 기반 인증을 사용할 예정이므로 서버 세션을 사용하지 않습니다.
	 * 로그인 후 서버가 세션을 저장하는 방식이 아니라, 클라이언트가 JWT를 가지고 있다가
	 * 요청마다 Authorization 헤더로 보내는 구조입니다.
	 *
	 * /api/auth/** 는 회원가입, 로그인, 토큰 재발급처럼 로그인 전에도 접근해야 하는 API입니다.
	 * /swagger-ui.html, /swagger-ui/**, /v3/api-docs/** 는 API 문서를 확인하기 위한 Swagger 경로입니다.
	 * 위 경로를 제외한 Todo 관련 API는 반드시 로그인한 사용자만 접근할 수 있도록 authenticated()로 막습니다.
	 *
	 * @param http Spring Security의 HTTP 보안 설정 객체
	 * @return Spring Security가 사용할 보안 필터 체인
	 * @throws Exception 보안 설정 구성 중 문제가 발생할 때
	 */
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
			.csrf(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.exceptionHandling(exception -> exception
				.authenticationEntryPoint((request, response, authException) ->
					writeErrorResponse(response, ErrorCode.UNAUTHORIZED))
				.accessDeniedHandler((request, response, accessDeniedException) ->
					writeErrorResponse(response, ErrorCode.FORBIDDEN))
			)
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(
					"/api/v1/auth/**",
					"/docs/openapi/**",
					"/swagger-ui.html",
					"/swagger-ui/**",
					"/v3/api-docs/**",
					"/error"
				).permitAll()
				.anyRequest().authenticated()
			)
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
			.build();
	}

	/**
	 * 로그인 인증을 직접 수행할 때 사용할 AuthenticationManager를 Bean으로 등록합니다.
	 *
	 * 로그인 API를 만들 때 이메일과 비밀번호를 AuthenticationManager에 전달하면,
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

	private void writeErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws java.io.IOException {
		response.setStatus(errorCode.getHttpStatus().value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");
		objectMapper.writeValue(response.getWriter(), ApiResponse.error(errorCode));
	}
}
