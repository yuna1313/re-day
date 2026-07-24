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
}
