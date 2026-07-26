import { useQuery } from '@tanstack/react-query'
import { analyticsApi } from '../api/analytics'

// 현재는 30일 고정 (7일/전체 선택은 추후)
const PERIOD_TYPE = 'LAST_30_DAYS'

export function useInsights() {
  return useQuery({
    queryKey: ['insights', PERIOD_TYPE],
    queryFn: () => analyticsApi.getInsights({ periodType: PERIOD_TYPE }),
  })
}
