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
// 수정된 일정 내용을 기억: scheduleId -> { title, startAt, estimatedMinutes, memo }
const updatedSchedules = {}
// 삭제(soft delete)된 일정 id
const deletedScheduleIds = new Set()
// 미루기/완료 처리 로그: scheduleId -> [{ actionLogId, actionType, ... }] (기본 예시 로그 뒤에 붙는다)
const actionLogsBySchedule = {}
let nextActionLogId = 900
// 작성된 회고: 'yyyy-MM-dd' -> { reflectionId, reflectionDate, content }
// 지난 날짜 회고 열람을 확인할 수 있도록 이틀 전 회고를 하나 심어둔다.
const seededReflectionDate = ymd(shiftDays(new Date(), -2))
const reflectionsByDate = {
  [seededReflectionDate]: {
    reflectionId: 10,
    reflectionDate: seededReflectionDate,
    content:
      '오늘 잘한 점: 미루던 이력서를 드디어 시작했다.\n아쉬웠던 점: 밤에 몰아서 하느라 집중이 잘 안 됐다.\n내일 딱 하나: 저녁 먹기 전에 30분만 앉아보기.',
  },
}
let nextReflectionId = 11
const nowStr = () => {
  const d = new Date()
  const p = (n) => String(n).padStart(2, '0')
  return `${ymd(d)} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`
}

// 기본 예시 일정 + 등록/수정/완료 상태를 반영한 목록 (날짜 필터 전)
function getResolvedSchedules() {
  const today = new Date()
  const base = [
    // 밀린 일정(과거인데 아직 PENDING) 확인용
    {
      scheduleId: 98,
      title: '병원 예약하기',
      startAt: `${ymd(shiftDays(today, -9))} 11:00:00`,
      estimatedMinutes: 10,
      actualMinutes: null,
      status: 'PENDING',
      completedAt: null,
      deferCount: 3,
    },
    {
      scheduleId: 99,
      title: '방 정리',
      startAt: `${ymd(shiftDays(today, -4))} 15:00:00`,
      estimatedMinutes: 45,
      actualMinutes: null,
      status: 'PENDING',
      completedAt: null,
      deferCount: 0,
    },
    // 지난 날짜 회고 화면의 "이날 완료한 일정" 확인용
    {
      scheduleId: 100,
      title: '이력서 초안 쓰기',
      startAt: `${ymd(shiftDays(today, -2))} 20:00:00`,
      estimatedMinutes: 60,
      actualMinutes: 75,
      status: 'DONE',
      completedAt: `${ymd(shiftDays(today, -2))} 21:15:00`,
      deferCount: 0,
    },
    {
      scheduleId: 101,
      title: '운동하기',
      startAt: `${ymd(today)} 08:00:00`,
      estimatedMinutes: 15,
      actualMinutes: 20,
      status: 'DONE',
      completedAt: `${ymd(today)} 08:25:00`,
      deferCount: 0,
      memo: '아침 스트레칭 위주로',
      deferLogs: [
        {
          actionLogId: 801,
          actionType: 'DONE',
          deferReasonCode: null,
          deferReasonDetail: null,
          actionAt: `${ymd(today)} 08:25:00`,
        },
      ],
    },
    {
      scheduleId: 102,
      title: 'NCS 문제 풀기',
      startAt: `${ymd(today)} 13:30:00`,
      estimatedMinutes: 60,
      actualMinutes: null,
      status: 'PENDING',
      completedAt: null,
      deferCount: 2,
      memo: null,
      deferLogs: [
        {
          actionLogId: 802,
          actionType: 'DEFERRED',
          deferReasonCode: 'NO_TIME',
          deferReasonDetail: null,
          actionAt: `${ymd(shiftDays(today, -2))} 21:40:00`,
        },
        {
          actionLogId: 803,
          actionType: 'DEFERRED',
          deferReasonCode: 'CUSTOM',
          deferReasonDetail: '문제집을 두고 와서 시작을 못 했어요',
          actionAt: `${ymd(shiftDays(today, -1))} 22:05:00`,
        },
      ],
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
    // 같은 날 4개 → 월간 점이 3개 + '+' 로 줄어드는 경우
    {
      scheduleId: 107,
      title: '포트폴리오 정리',
      startAt: `${ymd(shiftDays(today, 4))} 21:00:00`,
      estimatedMinutes: 40,
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

  return base
    .map((s) => {
      const merged = { ...s, ...(updatedSchedules[s.scheduleId] ?? {}) }
      const done = completedSchedules[merged.scheduleId]
      return done
        ? {
            ...merged,
            status: 'DONE',
            actualMinutes: done.actualMinutes,
            completedAt: done.completedAt,
          }
        : merged
    })
    .filter((s) => !deletedScheduleIds.has(s.scheduleId))
}

export const handlers = [
  // 일정 목록 조회: GET /api/v1/schedules?viewType&startDate&endDate
  http.get('/api/v1/schedules', ({ request }) => {
    const url = new URL(request.url)
    const viewType = url.searchParams.get('viewType')
    const startDate = url.searchParams.get('startDate')
    const endDate = url.searchParams.get('endDate')

    // 수정·완료 반영된 목록을 요청한 날짜 범위로 필터
    const schedules = getResolvedSchedules().filter((s) => {
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

  // 밀린 일정 조회: GET /api/v1/schedules/overdue
  // (반드시 /schedules/:scheduleId 핸들러 앞에 둘 것)
  // 오늘 이전에 시작했지만 아직 끝내지 않은 일정만
  http.get('/api/v1/schedules/overdue', () => {
    const todayKey = ymd(new Date())
    const overdue = getResolvedSchedules()
      .filter((s) => s.startAt.slice(0, 10) < todayKey && s.status !== 'DONE')
      .sort((a, b) => (a.startAt < b.startAt ? 1 : -1))

    return HttpResponse.json({
      success: true,
      code: 'SCHEDULE_OVERDUE_SUCCESS',
      message: '밀린 일정 조회에 성공했습니다.',
      data: {
        totalCount: overdue.length,
        hasMore: overdue.length > 50,
        schedules: overdue.slice(0, 50),
      },
    })
  }),

  // 일정 검색: GET /api/v1/schedules/search?keyword=
  // (반드시 /schedules/:scheduleId 핸들러 앞에 둘 것 — 뒤에 두면 'search' 를 id 로 해석한다)
  http.get('/api/v1/schedules/search', ({ request }) => {
    const keyword = (
      new URL(request.url).searchParams.get('keyword') ?? ''
    ).trim()

    if (!keyword) {
      return HttpResponse.json({
        success: false,
        code: 'SCH_INVALID_KEYWORD_FAIL',
        message: '검색어가 올바르지 않습니다.',
        data: null,
      })
    }

    // 제목에 키워드가 포함된 일정을 최근 시작일시 순으로
    const matched = getResolvedSchedules()
      .filter((s) => s.title.includes(keyword))
      .sort((a, b) => (a.startAt < b.startAt ? 1 : -1))
      .slice(0, 50)

    return HttpResponse.json({
      success: true,
      code: 'SCHEDULE_SEARCH_SUCCESS',
      message: '일정 검색에 성공했습니다.',
      data: { keyword, hasMore: matched.length >= 50, schedules: matched },
    })
  }),

  // 일정 상세 조회: GET /api/v1/schedules/:scheduleId
  http.get('/api/v1/schedules/:scheduleId', ({ params }) => {
    const scheduleId = Number(params.scheduleId)
    const s = getResolvedSchedules().find((x) => x.scheduleId === scheduleId)

    if (!s) {
      return HttpResponse.json({
        success: false,
        code: 'SCHEDULE_NOT_FOUND',
        message: '일정을 찾을 수 없습니다.',
        data: null,
      })
    }

    return HttpResponse.json({
      success: true,
      code: 'SCHEDULE_DETAIL_SUCCESS',
      message: '일정 상세 조회에 성공했습니다.',
      data: {
        scheduleId: s.scheduleId,
        title: s.title,
        startAt: s.startAt,
        estimatedMinutes: s.estimatedMinutes,
        actualMinutes: s.actualMinutes ?? null,
        memo: s.memo ?? null,
        status: s.status,
        completedAt: s.completedAt ?? null,
        createdAt: s.createdAt ?? '2026-01-08 22:10:00',
        updatedAt: s.updatedAt ?? s.startAt,
        deferCount: s.deferCount ?? 0,
        // 기본 예시 로그 + 이번 세션에 미루기/완료한 로그 (처리 시각 오름차순)
        deferLogs: [
          ...(s.deferLogs ?? []),
          ...(actionLogsBySchedule[scheduleId] ?? []),
        ],
      },
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

  // 일정 수정: PATCH /api/v1/schedules/:scheduleId
  http.patch('/api/v1/schedules/:scheduleId', async ({ request, params }) => {
    const scheduleId = Number(params.scheduleId)
    const { title, startAt, estimatedMinutes, memo } = await request.json()

    updatedSchedules[scheduleId] = {
      ...(updatedSchedules[scheduleId] ?? {}),
      title,
      startAt,
      estimatedMinutes,
      memo,
    }

    return HttpResponse.json({
      success: true,
      code: 'SCHEDULE_UPDATED',
      message: '일정이 수정되었습니다.',
      data: null,
    })
  }),

  // 일정 삭제(soft delete): DELETE /api/v1/schedules/:scheduleId
  http.delete('/api/v1/schedules/:scheduleId', ({ params }) => {
    deletedScheduleIds.add(Number(params.scheduleId))

    return HttpResponse.json({
      success: true,
      code: 'SCHEDULE_DELETED',
      message: '일정이 삭제되었습니다.',
      data: null,
    })
  }),

  // 일정 미루기: POST /api/v1/schedules/:scheduleId/defer
  http.post(
    '/api/v1/schedules/:scheduleId/defer',
    async ({ request, params }) => {
      const scheduleId = Number(params.scheduleId)
      const { deferReasonCode, deferReasonDetail, newStartAt } =
        await request.json()

      // 현재 상태 기준으로 미루기 횟수 +1, 새 시작일시 반영
      const current = getResolvedSchedules().find(
        (s) => s.scheduleId === scheduleId,
      )
      const nextDeferCount = (current?.deferCount ?? 0) + 1

      updatedSchedules[scheduleId] = {
        ...(updatedSchedules[scheduleId] ?? {}),
        ...(newStartAt ? { startAt: newStartAt } : {}),
        deferCount: nextDeferCount,
      }
      // 상세 화면의 처리 기록에 남도록 로그 추가
      ;(actionLogsBySchedule[scheduleId] ??= []).push({
        actionLogId: nextActionLogId++,
        actionType: 'DEFERRED',
        deferReasonCode,
        deferReasonDetail: deferReasonDetail || null,
        actionAt: nowStr(),
      })

      return HttpResponse.json({
        success: true,
        code: 'SCHEDULE_DEFERRED',
        message: '일정이 미뤄졌습니다.',
        data: {
          scheduleId,
          status: 'PENDING',
          startAt: newStartAt ?? current?.startAt ?? null,
          deferCount: nextDeferCount,
        },
      })
    },
  ),

  // 일정 완료 처리: POST /api/v1/schedules/:scheduleId/complete
  http.post(
    '/api/v1/schedules/:scheduleId/complete',
    async ({ request, params }) => {
      const scheduleId = Number(params.scheduleId)
      const { actualMinutes } = await request.json()
      const completedAt = nowStr()

      // 상태 저장 → 이후 목록 조회에 DONE 으로 반영됨
      completedSchedules[scheduleId] = { actualMinutes, completedAt }
      ;(actionLogsBySchedule[scheduleId] ??= []).push({
        actionLogId: nextActionLogId++,
        actionType: 'DONE',
        deferReasonCode: null,
        deferReasonDetail: null,
        actionAt: completedAt,
      })

      return HttpResponse.json({
        success: true,
        code: 'SCHEDULE_COMPLETED',
        message: '일정이 완료 처리되었습니다.',
        data: { scheduleId, status: 'DONE', actualMinutes, completedAt },
      })
    },
  ),

  // 오늘 회고 조회: GET /api/v1/reflections/today
  // 오늘 회고(없으면 null) + 오늘 완료한 일정 목록
  http.get('/api/v1/reflections/today', () => {
    const todayKey = ymd(new Date())
    const reflection = reflectionsByDate[todayKey] ?? null
    const completedList = getResolvedSchedules()
      .filter((s) => s.startAt.slice(0, 10) === todayKey && s.status === 'DONE')
      .map((s) => ({ scheduleId: s.scheduleId, title: s.title }))

    return HttpResponse.json({
      success: true,
      code: 'REFLECTION_TODAY_SUCCESS',
      message: '오늘 회고 조회에 성공했습니다.',
      data: { reflection, completedSchedules: completedList },
    })
  }),

  // 날짜별 회고 조회: GET /api/v1/reflections/:date
  // (반드시 /reflections/today 핸들러 뒤에 둘 것 — 먼저 두면 :date 가 'today' 까지 가로챈다)
  // 회고를 쓰지 않은 날짜도 조회 가능: reflectionId·content 가 null 로 내려온다.
  http.get('/api/v1/reflections/:date', ({ params }) => {
    const date = params.date
    const saved = reflectionsByDate[date] ?? null
    const completedList = getResolvedSchedules()
      .filter((s) => s.startAt.slice(0, 10) === date && s.status === 'DONE')
      .map((s) => ({ scheduleId: s.scheduleId, title: s.title }))

    return HttpResponse.json({
      success: true,
      code: 'REFLECTION_DETAIL_SUCCESS',
      message: '회고 조회에 성공했습니다.',
      data: {
        reflectionId: saved?.reflectionId ?? null,
        reflectionDate: date,
        content: saved?.content ?? null,
        completedSchedules: completedList,
      },
    })
  }),

  // 회고 작성: POST /api/v1/reflections
  // 회원별 같은 날짜에는 1개만 작성 가능 → 실패도 HTTP 200 + success:false 로 내려온다.
  http.post('/api/v1/reflections', async ({ request }) => {
    const { reflectionDate, content } = await request.json()

    // 이미 작성한 날짜면 실패 응답 (하루 1개 제한)
    if (reflectionsByDate[reflectionDate]) {
      return HttpResponse.json({
        success: false,
        code: 'REFLECTION_ALREADY_EXISTS',
        message: '이미 오늘의 회고를 작성했습니다.',
        data: null,
      })
    }

    const reflectionId = nextReflectionId++
    reflectionsByDate[reflectionDate] = {
      reflectionId,
      reflectionDate,
      content,
    }
    return HttpResponse.json({
      success: true,
      code: 'REFLECTION_CREATED',
      message: '회고가 작성되었습니다.',
      data: { reflectionId },
    })
  }),

  // 회고 수정: PATCH /api/v1/reflections/:reflectionId
  http.patch(
    '/api/v1/reflections/:reflectionId',
    async ({ request, params }) => {
      const reflectionId = Number(params.reflectionId)
      const { content } = await request.json()

      const entry = Object.values(reflectionsByDate).find(
        (r) => r.reflectionId === reflectionId,
      )
      if (entry) entry.content = content

      return HttpResponse.json({
        success: true,
        code: 'REFLECTION_UPDATED',
        message: '회고가 수정되었습니다.',
        data: null,
      })
    },
  ),

  // 인사이트 조회: GET /api/v1/analytics/insights?periodType
  http.get('/api/v1/analytics/insights', ({ request }) => {
    const url = new URL(request.url)
    const periodType = url.searchParams.get('periodType') || 'LAST_30_DAYS'

    return HttpResponse.json({
      success: true,
      code: 'INSIGHT_SUCCESS',
      message: '인사이트 조회에 성공했습니다.',
      data: {
        periodType,
        timeSlotCompletionRates: [
          { timeSlot: 'MORNING', label: '오전', completionRate: 38 },
          { timeSlot: 'AFTERNOON', label: '오후', completionRate: 97 },
          { timeSlot: 'EVENING', label: '저녁', completionRate: 74 },
        ],
        topDeferReasons: [
          {
            rank: 1,
            deferReasonCode: 'LONGER_THAN_EXPECTED',
            label: '예상보다 오래 걸림',
            count: 9,
          },
          {
            rank: 2,
            deferReasonCode: 'COULD_NOT_FOCUS',
            label: '집중 안 됨',
            count: 5,
          },
          {
            rank: 3,
            deferReasonCode: 'NO_TIME',
            label: '시간이 없었음',
            count: 1,
          },
        ],
        // 위 미루기 상위 이유와 같은 로그를 일정별로 묶은 것 (합계 15로 동일)
        topDeferredSchedules: [
          { rank: 1, scheduleId: 98, title: '병원 예약하기', deferCount: 7 },
          { rank: 2, scheduleId: 102, title: 'NCS 문제 풀기', deferCount: 5 },
          { rank: 3, scheduleId: 99, title: '방 정리', deferCount: 3 },
        ],
        estimatedVsActual: {
          averageEstimatedMinutes: 30,
          averageActualMinutes: 50,
          averageDiffMinutes: 20,
        },
        feedbackMessages: [
          '오후 일정 완료율이 가장 높아요.',
          "'병원 예약하기'을(를) 7번 미뤘어요.",
        ],
      },
    })
  }),

  // 내 정보 조회: GET /api/v1/members/me
  http.get('/api/v1/members/me', () => {
    return HttpResponse.json({
      success: true,
      code: 'MEMBER_ME_SUCCESS',
      message: '내 정보 조회에 성공했습니다.',
      data: {
        memberId: 1,
        nickname: '망고미',
        email: 'manggom@example.com',
      },
    })
  }),

  // 비밀번호 변경: PATCH /api/v1/members/me/password
  // 현재 비밀번호가 틀리면 HTTP 200 + success:false 로 내려온다.
  http.patch('/api/v1/members/me/password', async ({ request }) => {
    const { currentPassword } = await request.json()

    // 테스트 계정 비밀번호(1234)와 다르면 실패로 응답
    if (currentPassword !== '1234') {
      return HttpResponse.json({
        success: false,
        code: 'MEMBER_PASSWORD_MISMATCH',
        message: '현재 비밀번호가 일치하지 않습니다.',
        data: null,
      })
    }

    return HttpResponse.json({
      success: true,
      code: 'MEMBER_PASSWORD_UPDATED',
      message: '비밀번호가 변경되었습니다.',
      data: null,
    })
  }),

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
