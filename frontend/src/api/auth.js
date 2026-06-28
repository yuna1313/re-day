import client from './client'

// 백엔드 공통 응답 형태: { success, code, message, data }
// 일부 API는 실패도 HTTP 200 + success:false 로 내려온다. 그런 경우 success 를 직접
// 확인해 false 면 메시지를 담은 에러를 던져, React Query 의 onError 로 처리되게 한다.
function throwIfFailed(data, fallbackMessage) {
  if (!data.success) {
    const error = new Error(data.message || fallbackMessage)
    error.code = data.code
    throw error
  }
  return data
}

// 인증 관련 API (백엔드: /api/v1/auth/**)
export const authApi = {
  // 로그인 (실패 시 HTTP 401 → axios 가 에러를 던짐)
  // 응답 본문: { success, code, message, data: { accessToken, refreshToken, member } }
  login: ({ email, password }) =>
    client.post('/auth/login', { email, password }).then((res) => res.data),

  // 이메일 인증코드 발송 (실패도 200 + success:false)
  sendEmailVerification: async ({ email }) => {
    const { data } = await client.post('/auth/email/send-verification', {
      email,
    })
    return throwIfFailed(data, '이메일 인증코드 발송에 실패했습니다.')
  },

  // 이메일 인증코드 확인 (실패도 200 + success:false)
  verifyEmailCode: async ({ email, verificationCode }) => {
    const { data } = await client.post('/auth/email/verify', {
      email,
      verificationCode,
    })
    return throwIfFailed(data, '이메일 인증에 실패했습니다.')
  },

  // 회원가입 (실패도 200 + success:false)
  // 응답 data: { memberId, email }
  signup: async ({
    nickname,
    email,
    password,
    passwordConfirm,
    agreeTerms,
  }) => {
    const { data } = await client.post('/auth/signup', {
      nickname,
      email,
      password,
      passwordConfirm,
      agreeTerms,
    })
    return throwIfFailed(data, '회원가입에 실패했습니다.')
  },
}
