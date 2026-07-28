# RE:DAY — Backend

RE:DAY REST API 서버 (Spring Boot).
프로젝트 전체 개요·아키텍처·**기술 스택**·배포 구성은 **[루트 README](../README.md)** 를 참고하세요.

## 🚀 실행 방법 (로컬)

> 요구 사항: Java 17, MySQL 8 (로컬 실행 중)

1. 로컬 MySQL에 `reday` 데이터베이스 준비
   - JPA `ddl-auto: validate` 설정이라 **테이블이 미리 존재해야 합니다.** 스키마 덤프(`reday_dump.sql`)를 import 하세요.
2. IntelliJ에서 `RedayApplication` 실행 (또는 아래)

```bash
./gradlew bootRun
```

- 기본 프로필은 **`local`** 이며 [application-local.yml](./src/main/resources/application-local.yml)의 로컬 DB 접속 정보를 사용합니다.
- 서버 기동 후 API 문서: `http://localhost:8080/swagger-ui.html`

## ⚙️ 프로필

| 프로필 | 용도 | 설정 |
| --- | --- | --- |
| `local` (기본) | 로컬 개발 | `localhost:3306/reday` 직접 연결 |
| `test` | CI 테스트 | GitHub Actions의 MySQL 서비스 컨테이너 사용 |
| `prod` | 운영 | DB·JWT를 **환경변수로 주입** |

공통: `ddl-auto: validate`, `open-in-view: false`, JWT 만료 — access `30m` / refresh `14d`.

## 🔑 환경 변수 (운영 `.env`)

운영 서버(`/opt/reday/.env`)에서 주입되는 값 (Git 미포함):

| 변수 | 설명 |
| --- | --- |
| `SPRING_PROFILES_ACTIVE` | 활성 프로필 (운영: `prod`) |
| `DB_URL` | Spring 데이터소스 JDBC URL |
| `DB_USERNAME` / `DB_PASSWORD` | 애플리케이션 DB 계정 |
| `MYSQL_DATABASE` / `MYSQL_USER` / `MYSQL_PASSWORD` / `MYSQL_ROOT_PASSWORD` | MySQL 컨테이너 초기화용 |
| `JWT_SECRET` | JWT 서명 시크릿 (Base64) |
| `TZ` / `JAVA_TOOL_OPTIONS` | 타임존 (`Asia/Seoul`) |

## 📡 API 개요

Base path: `/api/v1` · 응답은 공통 envelope `{ success, code, message, data }` 형식.

| 도메인 | 메서드 · 경로 |
| --- | --- |
| **Auth** | `POST /auth/signup`, `/auth/login`, `/auth/logout`, `/auth/refresh` |
| | `POST /auth/email/send-verification`, `/auth/email/verify` |
| | `POST /auth/password-reset/email/send-verification`, `/auth/password-reset/email/verify`, `/auth/password-reset` |
| **Member** | `GET /members/me`, `PATCH /members/me/password` |
| **Schedule** | `GET /schedules`, `POST /schedules`, `GET·PATCH·DELETE /schedules/{id}` |
| | `POST /schedules/{id}/complete`, `POST /schedules/{id}/defer` |
| **Reflection** | `GET /reflections/today`, `GET /reflections/{date}`, `POST /reflections`, `PATCH /reflections/{id}` |
| **Analytics** | `GET /analytics/insights` |

> 전체 명세: Swagger UI (`/swagger-ui/index.html`) · OpenAPI (`/v3/api-docs`)
> 설계 문서: [`src/main/resources/docs/openapi/REDAY_openapi_v1.yaml`](./src/main/resources/docs/openapi/REDAY_openapi_v1.yaml)

## ☁️ 배포 (GitHub Actions → Vultr)

`main` 브랜치의 `backend/**` 변경 시 [deploy 워크플로우](../.github/workflows/deploy-backend.yml)가 자동 실행됩니다.

```
main(backend 변경) push
   → Java 17 셋업 + MySQL 서비스 컨테이너
   → ./gradlew clean test bootJar   (테스트 통과해야 배포)
   → 빌드 JAR을 SSH(scp)로 Vultr /opt/reday/app.jar 전송
   → docker compose up -d --build backend  (컨테이너 재기동)
```

필요한 GitHub Secrets: `VULTR_HOST`, `VULTR_USER`, `VULTR_SSH_KEY`, `VULTR_KNOWN_HOSTS`

## 🖥️ 서버 구성 & 운영

**서버 디렉터리 (`/opt/reday`)**

```
/opt/reday/
├── app.jar          # 배포된 Spring Boot 실행 파일
├── compose.yml      # backend + mysql 컨테이너 정의
├── Dockerfile       # backend 이미지 빌드
├── .env             # 운영 환경변수 (Git 미포함)
├── reday_dump.sql   # 초기 스키마/데이터 덤프
├── backup.sh        # DB 백업 스크립트
└── backups/         # 백업 저장 위치
```

**요청 흐름**: `Nginx :443 (TLS)` → `Spring (Docker) :8080` → `MySQL (Docker) :3306`

**운영 명령어**

```bash
cd /opt/reday

docker compose ps                    # 컨테이너 상태
docker compose logs -f backend       # Spring 로그
docker compose logs -f mysql         # MySQL 로그
docker compose restart backend       # Spring 재시작
docker compose restart               # 전체 재시작

docker stats                         # 컨테이너 리소스
df -h && docker system df            # 디스크 사용량
docker image prune -f                # 미사용 이미지 정리
```

## 🔒 Swagger 운영 노출 제어

운영에서 Swagger를 숨기려면 `application-prod.yml`에 아래를 추가합니다.

```yml
springdoc:
  api-docs:
    enabled: false
  swagger-ui:
    enabled: false
```
