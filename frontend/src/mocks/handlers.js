import { http, HttpResponse } from 'msw'

// 백엔드가 없을 때 사용할 가짜 응답 정의.
// 새 API를 mock 하려면 이 배열에 핸들러를 추가하면 된다.
// mock 일정을 오늘 기준으로 만들기 위한 헬퍼
const ymd = (d) =>
  `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(
    d.getDate(),
  ).padStart(2, '0')}`
const shiftDays = (base, n) => {
  const d = new Date(base)
  d.setDate(d.getDate() + n)
  return d
}

// 완료 처리된 일정을 기억하는 상태 저장형 mock: scheduleId -> { actualMinutes, completedAt }
const completedSchedules = {}
// 새로 등록된 일정을 기억
let nextScheduleId = 200
const createdSchedules = []
const nowStr = () => {
  const d = new Date()
  const p = (n) => String(n).padStart(2, '0')
  return `${ymd(d)} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`
}

export const handlers = [
  // 일정 목록 조회: GET /api/v1/schedules?viewType&startDate&endDate
  http.get('/api/v1/schedules', ({ request }) => {
    const url = new URL(request.url)
    const viewType = url.searchParams.get('viewType')
    const startDate = url.searchParams.get('startDate')
    const endDate = url.searchParams.get('endDate')

    // 오늘 근처에 예시 일정 배치
    const today = new Date()
    const all = [
      {
        scheduleId: 101,
        title: '운동하기',
        startAt: `${ymd(today)} 08:00:00`,
        estimatedMinutes: 15,
        actualMinutes: 20,
        status: 'DONE',
        completedAt: `${ymd(today)} 08:25:00`,
        deferCount: 0,
      },
      {
        scheduleId: 102,
        title: 'NCS 문제 풀기',
        startAt: `${ymd(today)} 13:30:00`,
        estimatedMinutes: 60,
        actualMinutes: null,
        status: 'PENDING',
        completedAt: null,
        deferCount: 1,
      },
      {
        scheduleId: 103,
        title: '이력서 수정',
        startAt: `${ymd(shiftDays(today, 4))} 10:00:00`,
        estimatedMinutes: 30,
        actualMinutes: null,
        status: 'PENDING',
        completedAt: null,
        deferCount: 0,
      },
      {
        scheduleId: 104,
        title: '알고리즘 문제',
        startAt: `${ymd(shiftDays(today, 4))} 14:00:00`,
        estimatedMinutes: 45,
        actualMinutes: null,
        status: 'PENDING',
        completedAt: null,
        deferCount: 0,
      },
      {
        scheduleId: 105,
        title: '독서',
        startAt: `${ymd(shiftDays(today, 4))} 19:00:00`,
        estimatedMinutes: 20,
        actualMinutes: null,
        status: 'PENDING',
        completedAt: null,
        deferCount: 0,
      },
      {
        scheduleId: 106,
        title: '스터디',
        startAt: `${ymd(shiftDays(today, 5))} 09:00:00`,
        estimatedMinutes: 90,
        actualMinutes: null,
        status: 'PENDING',
        completedAt: null,
        deferCount: 0,
      },
      // 새로 등록한 일정 포함
      ...createdSchedules,
    ]

    // 완료 처리된 일정 반영 후, 요청한 날짜 범위로 필터
    const schedules = all
      .map((s) => {
        const done = completedSchedules[s.scheduleId]
        return done
          ? {
              ...s,
              status: 'DONE',
              actualMinutes: done.actualMinutes,
              completedAt: done.completedAt,
            }
          : s
      })
      .filter((s) => {
        const d = s.startAt.slice(0, 10)
        return (!startDate || d >= startDate) && (!endDate || d <= endDate)
      })

    return HttpResponse.json({
      success: true,
      code: 'SCHEDULE_LIST_SUCCESS',
      message: '일정 목록 조회에 성공했습니다.',
      data: { viewType, startDate, endDate, schedules },
    })
  }),

  // 일정 생성: POST /api/v1/schedules
  http.post('/api/v1/schedules', async ({ request }) => {
    const { title, startAt, estimatedMinutes, memo } = await request.json()
    const scheduleId = nextScheduleId++

    createdSchedules.push({
      scheduleId,
      title,
      startAt,
      estimatedMinutes,
      actualMinutes: null,
      status: 'PENDING',
      completedAt: null,
      deferCount: 0,
      memo: memo ?? null,
    })

    return HttpResponse.json({
      success: true,
      code: 'SCHEDULE_CREATED',
      message: '일정이 등록되었습니다.',
      data: { scheduleId },
    })
  }),

  // 일정 완료 처리: POST /api/v1/schedules/:scheduleId/complete
  http.post(
    '/api/v1/schedules/:scheduleId/complete',
    async ({ request, params }) => {
      const scheduleId = Number(params.scheduleId)
      const { actualMinutes } = await request.json()
      const completedAt = nowStr()

      // 상태 저장 → 이후 목록 조회에 DONE 으로 반영됨
      completedSchedules[scheduleId] = { actualMinutes, completedAt }

      return HttpResponse.json({
        success: true,
        code: 'SCHEDULE_COMPLETED',
        message: '일정이 완료 처리되었습니다.',
        data: { scheduleId, status: 'DONE', actualMinutes, completedAt },
      })
    },
  ),

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
