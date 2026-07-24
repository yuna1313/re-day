import { http, HttpResponse } from 'msw'

// 백엔드가 없을 때 사용할 가짜 응답 정의.
// 새 API를 mock 하려면 이 배열에 핸들러를 추가하면 된다.
export const handlers = [
  // 로그인: POST /api/v1/auth/login
  http.post('/api/v1/auth/login', async ({ request }) => {
    const { email, password } = await request.json()

    // 테스트용 성공 계정
    if (email === 'test@reday.com' && password === '1234') {
      return HttpResponse.json({
        success: true,
        code: 'AUTH_LOGIN_SUCCESS',
        message: '로그인에 성공했습니다.',
        data: {
          accessToken: 'mock-access-token',
          refreshToken: 'mock-refresh-token',
          member: {
            memberId: 1,
            nickname: '망고미',
            email,
          },
        },
      })
    }

    // 그 외에는 로그인 실패(401)
    return HttpResponse.json(
      {
        success: false,
        code: 'AUTH_LOGIN_FAIL',
        message: '로그인에 실패하였습니다.',
        data: null,
      },
      { status: 401 },
    )
  }),

  // 토큰 재발급: POST /api/v1/auth/refresh
  http.post('/api/v1/auth/refresh', async ({ request }) => {
    const { refreshToken } = await request.json()

    // refreshToken 이 있으면 새 토큰 발급, 없거나 유효하지 않으면 401
    if (refreshToken) {
      return HttpResponse.json({
        success: true,
        code: 'AUTH_TOKEN_REFRESHED',
        message: '토큰이 재발급되었습니다.',
        data: {
          accessToken: 'new-mock-access-token',
          refreshToken: 'new-mock-refresh-token',
        },
      })
    }

    return HttpResponse.json(
      {
        success: false,
        code: 'AUTH_TOKEN_REFRESH_FAIL',
        message: '토큰 재발급에 실패했습니다.',
        data: null,
      },
      { status: 401 },
    )
  }),

  // 이메일 인증코드 발송: POST /api/v1/auth/email/send-verification
  // 실패도 HTTP 200 + success:false 로 내려온다.
  http.post('/api/v1/auth/email/send-verification', async ({ request }) => {
    const { email } = await request.json()

    // 실패 케이스 테스트용: 이메일에 'fail' 이 들어가면 발송 실패로 응답
    if (email.includes('fail')) {
      return HttpResponse.json({
        success: false,
        code: 'AUTH_EMAIL_SEND_FAIL',
        message: '이메일 인증번호 발송에 실패하였습니다.',
        data: null,
      })
    }

    return HttpResponse.json({
      success: true,
      code: 'AUTH_EMAIL_SENT',
      message: '인증번호를 발송했습니다.',
      data: null,
    })
  }),

  // 이메일 인증코드 확인: POST /api/v1/auth/email/verify
  // 실패도 HTTP 200 + success:false 로 내려온다.
  http.post('/api/v1/auth/email/verify', async ({ request }) => {
    const { verificationCode } = await request.json()

    // 테스트용 정답 코드: 123456
    if (verificationCode === '123456') {
      return HttpResponse.json({
        success: true,
        code: 'AUTH_EMAIL_VERIFIED',
        message: '이메일 인증이 완료되었습니다.',
        data: null,
      })
    }

    return HttpResponse.json({
      success: false,
      code: 'AUTH_EMAIL_VERIFY_FAIL',
      message: '인증번호가 일치하지 않습니다.',
      data: null,
    })
  }),

  // 회원가입: POST /api/v1/auth/signup
  // 실패도 HTTP 200 + success:false 로 내려온다.
  http.post('/api/v1/auth/signup', async ({ request }) => {
    const { email } = await request.json()

    // 실패 케이스 테스트용: 이메일에 'dup' 이 들어가면 이미 가입된 이메일로 응답
    if (email.includes('dup')) {
      return HttpResponse.json({
        success: false,
        code: 'AUTH_SIGNUP_FAIL',
        message: '이미 가입된 이메일입니다.',
        data: null,
      })
    }

    return HttpResponse.json({
      success: true,
      code: 'AUTH_SIGNUP_SUCCESS',
      message: '회원가입이 완료되었습니다.',
      data: { memberId: 1, email },
    })
  }),

  // 비밀번호 재설정 인증코드 발송: POST /api/v1/auth/password-reset/email/send-verification
  // 실패도 HTTP 200 + success:false 로 내려온다.
  http.post(
    '/api/v1/auth/password-reset/email/send-verification',
    async ({ request }) => {
      const { email } = await request.json()

      // 실패 케이스 테스트용: 이메일에 'fail' 이 들어가면 발송 실패로 응답
      if (email.includes('fail')) {
        return HttpResponse.json({
          success: false,
          code: 'AUTH_PASSWORD_RESET_VERIFICATION_SEND_FAIL',
          message: '비밀번호 재설정 인증코드 발송에 실패하였습니다.',
          data: null,
        })
      }

      return HttpResponse.json({
        success: true,
        code: 'AUTH_PASSWORD_RESET_VERIFICATION_SENT',
        message: '비밀번호 재설정 인증코드를 발송했습니다.',
        data: null,
      })
    },
  ),

  // 비밀번호 재설정 인증코드 확인: POST /api/v1/auth/password-reset/email/verify
  // 실패도 HTTP 200 + success:false 로 내려온다.
  http.post('/api/v1/auth/password-reset/email/verify', async ({ request }) => {
    const { verificationCode } = await request.json()

    // 테스트용 정답 코드: 123456
    if (verificationCode === '123456') {
      return HttpResponse.json({
        success: true,
        code: 'AUTH_PASSWORD_RESET_VERIFIED',
        message: '비밀번호 재설정 이메일 인증이 완료되었습니다.',
        data: null,
      })
    }

    return HttpResponse.json({
      success: false,
      code: 'AUTH_PASSWORD_RESET_VERIFY_FAIL',
      message: '인증번호가 일치하지 않습니다.',
      data: null,
    })
  }),

  // 비밀번호 재설정: POST /api/v1/auth/password-reset
  // 실패도 HTTP 200 + success:false 로 내려온다.
  http.post('/api/v1/auth/password-reset', async ({ request }) => {
    const { newPassword, newPasswordConfirm } = await request.json()

    // 서버도 비밀번호 일치를 재확인한다고 가정 (실패 케이스 테스트용)
    if (newPassword !== newPasswordConfirm) {
      return HttpResponse.json({
        success: false,
        code: 'AUTH_PASSWORD_RESET_FAIL',
        message: '비밀번호가 일치하지 않습니다.',
        data: null,
      })
    }

    return HttpResponse.json({
      success: true,
      code: 'AUTH_PASSWORD_RESET_SUCCESS',
      message: '비밀번호 재설정이 완료되었습니다.',
      data: null,
    })
  }),
]
