import { useQuery } from '@tanstack/react-query'
import { scheduleApi } from '../api/schedule'
import { toDisplayItem } from './useSchedules'

// 밀린 일정 조회 훅.
// 키가 ['schedules', ...] 로 시작해 완료·미루기·삭제 후 밀린 목록도 함께 갱신된다.
export function useOverdueSchedules() {
  return useQuery({
    queryKey: ['schedules', 'overdue'],
    queryFn: async () => {
      const data = await scheduleApi.getOverdueSchedules()
      return {
        totalCount: data.totalCount ?? 0,
        hasMore: Boolean(data.hasMore),
        items: (data.schedules ?? []).map(toDisplayItem),
      }
    },
  })
}
