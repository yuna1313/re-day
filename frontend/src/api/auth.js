import client, { throwIfFailed } from './client'

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

  // 비밀번호 재설정 인증코드 발송 (실패도 200 + success:false, 코드 5분 유효)
  sendPasswordResetCode: async ({ email }) => {
    const { data } = await client.post(
      '/auth/password-reset/email/send-verification',
      { email },
    )
    return throwIfFailed(data, '비밀번호 재설정 인증코드 발송에 실패했습니다.')
  },

  // 비밀번호 재설정 인증코드 확인 (실패도 200 + success:false)
  // 성공 시 서버가 인증 완료 상태를 저장한다 (별도 토큰 없음).
  verifyPasswordResetCode: async ({ email, verificationCode }) => {
    const { data } = await client.post('/auth/password-reset/email/verify', {
      email,
      verificationCode,
    })
    return throwIfFailed(data, '비밀번호 재설정 인증에 실패했습니다.')
  },

  // 비밀번호 재설정 (실패도 200 + success:false)
  // 인증 완료된 이메일의 비밀번호를 변경. 성공 시 서버의 인증 기록은 삭제됨.
  resetPassword: async ({ email, newPassword, newPasswordConfirm }) => {
    const { data } = await client.post('/auth/password-reset', {
      email,
      newPassword,
      newPasswordConfirm,
    })
    return throwIfFailed(data, '비밀번호 재설정에 실패했습니다.')
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
