import client from './client'

// 인증 관련 API (백엔드: /api/v1/auth/**)
export const authApi = {
  // 응답 본문: { success, code, message, data: { accessToken, refreshToken, member } }
  login: ({ email, password }) =>
    client.post('/auth/login', { email, password }).then((res) => res.data),

  // 이메일 인증코드 발송.
  // 이 API는 실패도 HTTP 200 + success:false 로 내려오므로 success 를 직접 확인하고,
  // 실패면 에러를 던져 React Query 의 onError 로 처리되게 한다.
  sendEmailVerification: async ({ email }) => {
    const { data } = await client.post('/auth/email/send-verification', {
      email,
    })
    if (!data.success) {
      const error = new Error(
        data.message || '이메일 인증코드 발송에 실패했습니다.',
      )
      error.code = data.code
      throw error
    }
    return data
  },
}
