import client from './client'

// 인증 관련 API (백엔드: /api/v1/auth/**)
export const authApi = {
  // 응답 본문: { success, code, message, data: { accessToken, refreshToken, member } }
  login: ({ email, password }) =>
    client.post('/auth/login', { email, password }).then((res) => res.data),
}
