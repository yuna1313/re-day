# RE:DAY Backend

## OpenAPI

- 설계 문서 위치: `docs/openapi/REDAY_openapi_v1.yaml`
- 실행 후 Swagger UI 경로: `/swagger-ui.html`
- 실행 후 OpenAPI docs 경로: `/v3/api-docs`, `/v3/api-docs.yaml`

Swagger UI는 실행 중인 애플리케이션의 실제 컨트롤러와 OpenAPI 설정을 기준으로 문서를 보여줍니다.
`docs/openapi/REDAY_openapi_v1.yaml` 파일은 API 설계 문서로 보관합니다.

## Swagger 개발 환경 노출 제안

운영 환경에서 Swagger를 숨기고 싶다면 `application-prod.yml`에서 아래처럼 비활성화할 수 있습니다.

```yml
springdoc:
  api-docs:
    enabled: false
  swagger-ui:
    enabled: false
```
