package com.reday.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

	private static final String BEARER_AUTH = "bearerAuth";

	/**
	 * Swagger UI와 OpenAPI docs에 표시될 API 문서 기본 설정을 만듭니다.
	 *
	 * 이 Bean은 springdoc-openapi가 애플리케이션을 시작할 때 읽어갑니다.
	 * info에는 문서 제목, 설명, 버전을 넣고, components에는 JWT Bearer 인증 방식을 등록합니다.
	 * addSecurityItem을 설정하면 Swagger UI의 Authorize 버튼에 입력한 토큰이
	 * 보호된 API 요청의 Authorization 헤더로 전달됩니다.
	 *
	 * 실제 JWT 값은 코드에 저장하지 않습니다.
	 * 사용자가 Swagger UI 화면에서 직접 토큰을 입력해야 하므로 public GitHub에 올려도 민감정보가 포함되지 않습니다.
	 *
	 * @return springdoc-openapi가 사용할 OpenAPI 설정 객체
	 */
	@Bean
	public OpenAPI openAPI() {
		return new OpenAPI()
			.info(apiInfo())
			.components(securityComponents())
			.addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
	}

	/**
	 * OpenAPI 문서의 메타데이터를 설정합니다.
	 *
	 * 이 값은 Swagger UI 화면 상단과 /v3/api-docs 응답의 info 영역에 표시됩니다.
	 * API 문서 이름이나 버전이 바뀌면 이 메소드의 값만 수정하면 됩니다.
	 *
	 * @return API 문서 제목, 설명, 버전 정보
	 */
	private Info apiInfo() {
		return new Info()
			.title("RE:DAY API")
			.description("RE:DAY 백엔드 API 문서")
			.version("v1");
	}

	/**
	 * Swagger UI에서 사용할 JWT Bearer 인증 방식을 등록합니다.
	 *
	 * type HTTP와 scheme bearer를 함께 사용하면 Swagger UI가 Bearer Token 인증으로 인식합니다.
	 * bearerFormat은 사람이 문서를 볼 때 JWT 토큰을 넣어야 한다는 힌트를 주는 값입니다.
	 * name은 실제 HTTP 요청에 들어가는 Authorization 헤더 이름입니다.
	 *
	 * Swagger UI의 Authorize 창에는 Bearer 접두어를 제외한 토큰 값만 입력하면 됩니다.
	 * 요청을 실행할 때 Swagger가 Authorization: Bearer {token} 형태로 헤더를 만들어줍니다.
	 *
	 * @return OpenAPI Components에 등록할 보안 스키마 설정
	 */
	private Components securityComponents() {
		return new Components()
			.addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
				.type(SecurityScheme.Type.HTTP)
				.scheme("bearer")
				.bearerFormat("JWT")
				.in(SecurityScheme.In.HEADER)
				.name("Authorization"));
	}
}
