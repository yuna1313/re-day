# RE:DAY — Frontend

RE:DAY 웹 클라이언트 (React + Vite)  
프로젝트 전체 개요·아키텍처·**기술 스택**·배포 구성은 **[루트 README](../README.md)** 를 참고하세요.

## 🚀 실행 방법

**요구 사항: Node.js 20.19+ (또는 22+)**

```bash
npm install

# 1) 실제 백엔드 연동 개발 (Vite 프록시: /api → localhost:8080)
npm run dev

# 2) 목서버 개발 (백엔드 없이 MSW 목 데이터로 실행)
npm run dev:mock
```

- **`dev`** — [vite.config.js](./vite.config.js)의 프록시가 `/api` 요청을 로컬 백엔드(`localhost:8080`)로 전달합니다.
- **`dev:mock`** — `.env.mock`의 `VITE_USE_MOCK=true`로 MSW 목서버가 켜져, 백엔드 없이 전체 화면·플로우를 테스트할 수 있습니다.
  - 목서버 로그인 계정: **`test@reday.com` / `1234`**

## 🔑 환경 변수

| 변수 | 설명 | 기본값 |
| --- | --- | --- |
| `VITE_API_BASE_URL` | API 서버 base URL. 미설정 시 `/api/v1`(개발 프록시 경유). 운영에서는 백엔드 절대 주소 지정 | `/api/v1` |
| `VITE_USE_MOCK` | `true`면 MSW 목서버 활성화 (`dev:mock` 전용, `.env.mock`) | — |

운영(Vercel)에서는 `VITE_API_BASE_URL = https://reday-api.duckdns.org/api/v1` 로 설정되어 있습니다.

## 📜 스크립트

| 명령 | 설명 |
| --- | --- |
| `npm run dev` | 개발 서버 (백엔드 프록시) |
| `npm run dev:mock` | 개발 서버 (MSW 목서버) |
| `npm run build` | 프로덕션 빌드 (`dist/`) |
| `npm run preview` | 빌드 결과 로컬 미리보기 |
| `npm run lint` | ESLint 검사 |
| `npm run format` | Prettier 포매팅 |

## 📂 폴더 구조

```
frontend/
├── public/            # 정적 파일 (favicon, 폰트, MSW 워커)
├── src/
│   ├── api/           # Axios 인스턴스 + 도메인별 API 함수
│   ├── components/    # 공용 컴포넌트 (Layout, 바텀시트 등)
│   ├── constants/     # 상수 (약관, 미루기 사유 등)
│   ├── contexts/      # 인증 Context / Provider
│   ├── hooks/         # React Query 훅 (조회·뮤테이션)
│   ├── lib/           # QueryClient 등 설정
│   ├── mocks/         # MSW 핸들러 / 워커
│   ├── pages/         # 라우트 단위 화면 (+ 페이지별 CSS)
│   └── utils/         # 유틸 (검증 등)
├── vercel.json        # SPA 라우팅 fallback
└── vite.config.js     # 빌드 / 개발 프록시 설정
```

## 💡 구현 포인트

- **토큰 자동 재발급** — Axios 응답 인터셉터에서 401 감지 시 refresh 토큰으로 재발급 후 원요청 재시도. 동시 요청은 공유 Promise로 묶어 중복 재발급을 방지.
- **서버 상태 관리** — React Query로 캐싱·무효화 관리. 주/월 이동 시 `keepPreviousData`로 깜빡임 최소화.
- **목서버 기반 개발** — MSW로 백엔드 없이 UI·플로우를 먼저 검증한 뒤 실제 API를 연동하는 워크플로우.
- **라이브러리 없는 UI** — 월간 캘린더·시간 선택 스테퍼를 date-fns + 순수 CSS로 직접 구현(배포 의존성 최소화).
- **로딩 UX** — 데이터 로딩 구간은 스켈레톤(shimmer)으로 통일.

## ☁️ 배포

Vercel에 정적 호스팅되며, **`main` 브랜치 머지 시 자동 배포**됩니다.
클라이언트 사이드 라우팅(새로고침/딥링크) 대응을 위해 [vercel.json](./vercel.json)에서 모든 경로를 `index.html`로 rewrite 합니다.
