package com.reday.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	private static final String FRONTEND_ORIGIN = "https://re-day-one.vercel.app";

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/api/v1/**")
			.allowedOrigins(FRONTEND_ORIGIN)
			.allowedMethods("GET", "POST", "PATCH", "DELETE", "OPTIONS")
			.allowedHeaders("*")
			.allowCredentials(true);
	}

	/**
	 * 설계용 OpenAPI YAML 파일을 Swagger UI에서 직접 읽을 수 있도록 정적 리소스로 노출합니다.
	 *
	 * @param registry 정적 리소스 핸들러 등록 객체
	 */
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry
			.addResourceHandler("/docs/openapi/**")
			.addResourceLocations("classpath:/docs/openapi/");
	}
}
