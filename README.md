# RE:DAY

> **미루는 하루를 돌아보고 다시 계획하는, 회고 기반 일정 관리 서비스**  
> 일정을 완료·미루기하며 남는 행동 로그를 분석해, "언제 집중이 잘 되는지 / 왜 미루는지"를 인사이트로 돌려줍니다.

<p>
  <img alt="React" src="https://img.shields.io/badge/React-19-61DAFB?logo=react&logoColor=white">
  <img alt="Vite" src="https://img.shields.io/badge/Vite-8-646CFF?logo=vite&logoColor=white">
  <img alt="Spring Boot" src="https://img.shields.io/badge/Spring%20Boot-4.0-6DB33F?logo=springboot&logoColor=white">
  <img alt="MySQL" src="https://img.shields.io/badge/MySQL-8-4479A1?logo=mysql&logoColor=white">
  <img alt="Docker" src="https://img.shields.io/badge/Docker-2496ED?logo=docker&logoColor=white">
</p>

**1인 개발** (기획 · 디자인 · Frontend · Backend · 인프라/배포) &nbsp;·&nbsp; **2026.04 – 2026.07**

## 🔗 링크

| 구분 | URL |
| --- | --- |
| 서비스 (Frontend) | https://re-day-one.vercel.app |
| API 서버 (Backend) | https://reday-api.duckdns.org |
| API 문서 (Swagger) | https://reday-api.duckdns.org/swagger-ui/index.html |

## 데모 계정

회원가입 없이 바로 로그인 가능

| ID | PW |
| --- | --- |
| manggom@test.com | Test1234! |

## 📖 소개

RE:DAY는 단순한 To-Do 앱이 아니라, **미루기(deferral)라는 행동 자체를 기록·분석**하는 데 초점을 둔 서비스입니다.

- 일정을 **완료 / 미루기**할 때마다 사유·시간 데이터가 쌓이고
- 이를 **시간대별 완료율, 미루기 상위 이유, 예상 vs 실제 소요시간**으로 집계해
- 사용자가 자신의 패턴을 인지하고 다음 계획을 더 현실적으로 세우도록 돕습니다.

## ✨ 주요 기능

- **인증** — 회원가입 / 로그인, 이메일 인증코드, 비밀번호 재설정(코드 방식), JWT + 리프레시 토큰 자동 재발급
- **일정 관리** — 주간·월간 캘린더, 일정 등록/수정/삭제, **완료 처리(실제 소요시간 기록)**, **미루기(사유 기록)**
- **오늘의 회고** — 오늘 완료한 일정 + 회고 작성/수정
- **인사이트** — 시간대별 완료율, 미루기 상위 이유, 예상 vs 실제 평균, 피드백 메시지
- **마이페이지** — 내 정보 조회, 비밀번호 변경

## 🏗️ 시스템 아키텍처

```
[ 사용자 브라우저 ]
        │  정적 파일(React) 로드
        ▼
[ Vercel ]  ──────────── 프론트엔드 호스팅
        │
        │  API 요청 (HTTPS)
        ▼
[ reday-api.duckdns.org ]           ← DuckDNS 도메인
        │
        ▼  (Vultr VPS 내부)
   Nginx  :443   ── TLS 종료 / 리버스 프록시
        ▼
   Spring (Docker) :8080  ── REST API
        ▼
   MySQL (Docker) :3306   ── 데이터 저장
```

- 프론트엔드는 Vercel에 정적 호스팅되고, API는 절대 주소(`VITE_API_BASE_URL`)로 백엔드를 호출합니다.
- 백엔드 서버(Vultr)는 **Nginx가 443에서 TLS를 종료**하고 내부 Docker 컨테이너(Spring·MySQL)로 프록시합니다.

## 🚀 배포

**Frontend — Vercel (자동 배포)**
```
main 브랜치 머지 → Vercel 자동 빌드 & 배포
```

**Backend — GitHub Actions → Vultr (자동 배포)**
```
main 브랜치의 backend 변경 → GitHub Actions → Vultr 서버 자동 배포 (docker compose)
```

## 🧰 기술 스택

**Frontend**

| 분류 | 사용 기술 |
| --- | --- |
| 언어/빌드 | JavaScript, Vite |
| 프레임워크 | React 19 |
| 라우팅 | React Router v7 |
| 서버 상태 | TanStack Query (React Query) v5 |
| HTTP | Axios (요청/응답 인터셉터 · 토큰 자동 재발급) |
| 날짜 | date-fns |
| 아이콘 | lucide-react |
| 모킹 | MSW (Mock Service Worker) |
| 스타일 | 순수 CSS (컴포넌트 co-location), Goorm Sans |
| 배포 | Vercel |

**Backend**

| 분류 | 사용 기술 |
| --- | --- |
| 언어 | Java 17 |
| 프레임워크 | Spring Boot 4.0 (Web MVC, Data JPA, Security, Mail) |
| 인증 | Spring Security + JWT (jjwt) |
| DB | MySQL 8 |
| API 문서 | springdoc-openapi (Swagger UI) |
| 빌드 | Gradle |

**Infra**

| 분류 | 사용 기술 |
| --- | --- |
| 서버 | Vultr VPS |
| 컨테이너 | Docker / Docker Compose |
| 웹서버 | Nginx (리버스 프록시 · TLS) |
| 도메인 | DuckDNS |
| CI/CD | GitHub Actions (Backend), Vercel (Frontend) |

## 📂 프로젝트 구조

```
re-day/
├── frontend/          # React + Vite 웹 클라이언트  (→ frontend/README.md)
├── backend/           # Spring Boot REST API 서버    (→ backend/README.md)
└── .github/workflows/ # 백엔드 자동 배포 파이프라인
```

각 파트의 실행 방법·환경변수·상세 문서는 하위 README를 참고하세요.

- **[frontend/README.md](./frontend/README.md)** — 웹 클라이언트 실행 / 환경변수 / 스크립트
- **[backend/README.md](./backend/README.md)** — API 서버 실행 / 배포 / 운영 명령어
