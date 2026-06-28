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

  // 이메일 인증코드 발송: POST /api/v1/auth/email/send-verification
  // 실패도 HTTP 200 + success:false 로 내려온다.
  http.post('/api/v1/auth/email/send-verification', async ({ request }) => {
    const { email } = await request.json()

    // 실패 케이스 테스트용: 이메일에 'fail' 이 들어가면 발송 실패로 응답
    if (email.includes('fail')) {
      return HttpResponse.json({
        success: false,
        code: 'AUTH_EMAIL_SEND_FAIL',
        message: '이메일 인증코드 발송에 실패하였습니다.',
        data: null,
      })
    }

    return HttpResponse.json({
      success: true,
      code: 'AUTH_EMAIL_SENT',
      message: '인증코드를 발송했습니다.',
      data: null,
    })
  }),
]
