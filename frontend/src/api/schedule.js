import client, { throwIfFailed } from './client'

// 일정 관련 API (백엔드: /api/v1/schedules)
export const scheduleApi = {
  // 주간/월간 화면용 일정 목록 조회
  // viewType: 'WEEK' | 'MONTH', startDate/endDate: 'yyyy-MM-dd'
  // 반환: { viewType, startDate, endDate, schedules: [...] }
  getSchedules: async ({ viewType, startDate, endDate }) => {
    const { data } = await client.get('/schedules', {
      params: { viewType, startDate, endDate },
    })
    return throwIfFailed(data, '일정 목록 조회에 실패했습니다.').data
  },

  // 일정 생성
  // startAt: 'yyyy-MM-dd HH:mm:ss' / 반환: { scheduleId }
  createSchedule: async ({ title, startAt, estimatedMinutes, memo }) => {
    const { data } = await client.post('/schedules', {
      title,
      startAt,
      estimatedMinutes,
      memo,
    })
    return throwIfFailed(data, '일정 등록에 실패했습니다.').data
  },

  // 일정 수정 (제목/시작일시/예상시간/메모)
  updateSchedule: async ({
    scheduleId,
    title,
    startAt,
    estimatedMinutes,
    memo,
  }) => {
    const { data } = await client.patch(`/schedules/${scheduleId}`, {
      title,
      startAt,
      estimatedMinutes,
      memo,
    })
    return throwIfFailed(data, '일정 수정에 실패했습니다.').data
  },

  // 일정 완료 처리 (실제 소요 시간 저장)
  // 반환: { scheduleId, status, actualMinutes, completedAt }
  completeSchedule: async ({ scheduleId, actualMinutes }) => {
    const { data } = await client.post(`/schedules/${scheduleId}/complete`, {
      actualMinutes,
    })
    return throwIfFailed(data, '일정 완료 처리에 실패했습니다.').data
  },
}
