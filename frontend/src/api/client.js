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

const REFRESH_URL = '/auth/refresh'
const MEMBER_KEY = 'member'

// 만료 시 로그아웃: 토큰·회원정보 정리 후 로그인 화면으로.
// (인터셉터는 React 밖이라 라우터 대신 location 이동을 사용)
function forceLogout() {
  tokenStorage.clear()
  localStorage.removeItem(MEMBER_KEY)
  if (window.location.pathname !== '/login') {
    window.location.href = '/login'
  }
}

// 동시에 여러 요청이 401 이 나도 재발급은 1번만 하도록 공유 Promise 로 묶는다.
let refreshPromise = null

function refreshAccessToken() {
  if (!refreshPromise) {
    const refreshToken = tokenStorage.getRefreshToken()
    refreshPromise = client
      .post(REFRESH_URL, { refreshToken })
      .then((res) => {
        const { data } = res
        if (!data.success) throw new Error(data.message || '토큰 재발급 실패')
        tokenStorage.set({
          accessToken: data.data.accessToken,
          refreshToken: data.data.refreshToken,
        })
        return data.data.accessToken
      })
      .finally(() => {
        refreshPromise = null
      })
  }
  return refreshPromise
}

// 응답 인터셉터: 401 이면 refresh 로 토큰을 재발급받아 원래 요청을 재시도한다.
client.interceptors.response.use(
  (response) => response,
  async (error) => {
    const { response, config } = error

    // 인증(auth) 요청 자체의 401(로그인 실패·refresh 만료 등)은 재발급 대상이 아님
    const isAuthRequest = config?.url?.includes('/auth/')

    if (
      response?.status === 401 &&
      config &&
      !config._retry &&
      !isAuthRequest
    ) {
      if (!tokenStorage.getRefreshToken()) {
        forceLogout()
        return Promise.reject(error)
      }
      config._retry = true
      try {
        const newAccessToken = await refreshAccessToken()
        // 새 토큰으로 원래 요청 재시도
        config.headers.Authorization = `Bearer ${newAccessToken}`
        return client(config)
      } catch (refreshError) {
        // refresh 도 실패 → 세션 만료로 간주하고 로그아웃
        forceLogout()
        return Promise.reject(refreshError)
      }
    }

    return Promise.reject(error)
  },
)

// 백엔드 공통 응답에서 success:false 면(HTTP 200이어도) 메시지를 담은 에러를 던진다.
// (여러 API 모듈에서 재사용)
export function throwIfFailed(data, fallbackMessage) {
  if (!data.success) {
    const error = new Error(data.message || fallbackMessage)
    error.code = data.code
    throw error
  }
  return data
}

// API 실패 시 사용자에게 보여줄 메시지를 통일된 방식으로 뽑아낸다.
// - HTTP 에러(예: 401): 서버 메시지가 error.response.data.message 에 있음
// - 200 + success:false: throwIfFailed 가 던진 Error 의 message 에 있음
// 둘 다 없으면(네트워크 오류 등) fallback 을 사용한다.
export function getApiErrorMessage(
  error,
  fallback = '요청 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.',
) {
  const serverMessage = error?.response?.data?.message
  if (serverMessage) return serverMessage
  if (error && !error.response && error.message) return error.message
  return fallback
}

export default client
