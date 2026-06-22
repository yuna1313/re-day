import axios from 'axios'

// 개발 환경에서는 Vite 프록시를 통해 /api/v1 -> http://localhost:8080/api/v1 로 전달된다.
// 운영 환경에서는 .env 의 VITE_API_BASE_URL 로 실제 백엔드 주소를 지정할 수 있다.
const baseURL = import.meta.env.VITE_API_BASE_URL || '/api/v1'

const ACCESS_TOKEN_KEY = 'accessToken'
const REFRESH_TOKEN_KEY = 'refreshToken'

export const tokenStorage = {
  getAccessToken: () => localStorage.getItem(ACCESS_TOKEN_KEY),
  getRefreshToken: () => localStorage.getItem(REFRESH_TOKEN_KEY),
  set: ({ accessToken, refreshToken }) => {
    if (accessToken) localStorage.setItem(ACCESS_TOKEN_KEY, accessToken)
    if (refreshToken) localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken)
  },
  clear: () => {
    localStorage.removeItem(ACCESS_TOKEN_KEY)
    localStorage.removeItem(REFRESH_TOKEN_KEY)
  },
}

const client = axios.create({
  baseURL,
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' },
})

// 요청 인터셉터: 저장된 JWT access token 을 Authorization 헤더에 자동 첨부한다.
client.interceptors.request.use((config) => {
  const token = tokenStorage.getAccessToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 응답 인터셉터: 성공 응답은 그대로, 에러는 공통 처리한다.
client.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // 인증 만료/실패: 토큰 정리 (이후 라우팅 처리는 호출부/가드에서)
      tokenStorage.clear()
    }
    return Promise.reject(error)
  },
)

export default client
