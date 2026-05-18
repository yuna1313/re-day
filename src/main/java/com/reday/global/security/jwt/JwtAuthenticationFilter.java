package com.reday.global.security.jwt;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reday.global.exception.ErrorCode;
import com.reday.global.response.ApiResponse;
import com.reday.global.security.CustomUserDetailsService;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final String BEARER_PREFIX = "Bearer ";

	private final JwtTokenProvider jwtTokenProvider;
	private final CustomUserDetailsService customUserDetailsService;
	private final ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * 요청마다 Authorization 헤더의 Bearer access token을 확인하고 인증 정보를 SecurityContext에 저장합니다.
	 *
	 * 1. 토큰이 없으면 인증 처리 없이 다음 필터로 넘기고, 토큰이 있으면 access token인지 검증합니다.
	 * 2. 검증에 성공하면 토큰 subject의 email로 회원 정보를 조회해서 Authentication 객체를 만들고,
	 * 3. 이후 컨트롤러와 보안 설정에서 인증된 사용자로 인식할 수 있도록 SecurityContext에 저장합니다.
	 *
	 * 토큰이 만료되었거나 잘못되었거나, 토큰의 email에 해당하는 회원을 찾을 수 없으면 401 Unauthorized로 응답합니다.
	 *
	 * @param request 현재 HTTP 요청
	 * @param response 현재 HTTP 응답
	 * @param filterChain 다음 필터로 요청을 넘기기 위한 필터 체인
	 * @throws ServletException 필터 처리 중 Servlet 관련 예외가 발생할 때
	 * @throws IOException 응답 처리 또는 다음 필터 호출 중 입출력 예외가 발생할 때
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {
		String token = resolveToken(request);

		if (StringUtils.hasText(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
			try {
				if (!jwtTokenProvider.validateAccessToken(token)) {
					writeUnauthorizedResponse(response);
					return;
				}

				String email = jwtTokenProvider.getEmail(token);
				UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);
				UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
					userDetails,
					null,
					userDetails.getAuthorities()
				);
				authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(authentication);
			} catch (AuthenticationException | JwtException | IllegalArgumentException e) {
				writeUnauthorizedResponse(response);
				return;
			}
		}

		filterChain.doFilter(request, response);
	}

	/**
	 * Authorization 헤더에서 Bearer 접두어를 제외한 JWT 문자열만 추출합니다.
	 *
	 * 헤더가 없거나 "Bearer "로 시작하지 않으면 토큰이 없는 요청으로 보고 null을 반환합니다.
	 *
	 * @param request Authorization 헤더를 읽을 HTTP 요청
	 * @return Bearer 접두어를 제거한 JWT 문자열, 없으면 null
	 */
	private String resolveToken(HttpServletRequest request) {
		String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith(BEARER_PREFIX)) {
			return authorizationHeader.substring(BEARER_PREFIX.length());
		}
		return null;
	}

	/**
	 * JWT 인증 실패 응답을 공통 API 응답 형식으로 작성합니다.
	 *
	 * @param response 현재 HTTP 응답
	 * @throws IOException 응답 본문 작성 중 입출력 오류가 발생한 경우
	 */
	private void writeUnauthorizedResponse(HttpServletResponse response) throws IOException {
		response.setStatus(ErrorCode.UNAUTHORIZED.getHttpStatus().value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");
		objectMapper.writeValue(response.getWriter(), ApiResponse.error(ErrorCode.UNAUTHORIZED));
	}
}
