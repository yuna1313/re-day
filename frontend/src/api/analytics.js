import client, { throwIfFailed } from './client'

// 통계/인사이트 API (백엔드: /api/v1/analytics)
export const analyticsApi = {
  // 인사이트 조회 (시간대별 완료율 / 미루기 상위 이유 / 예상 vs 실제 / 피드백)
  // periodType: 'LAST_7_DAYS' | 'LAST_30_DAYS'
  getInsights: async ({ periodType = 'LAST_30_DAYS' } = {}) => {
    const { data } = await client.get('/analytics/insights', {
      params: { periodType },
    })
    return throwIfFailed(data, '인사이트 조회에 실패했습니다.').data
  },
}
