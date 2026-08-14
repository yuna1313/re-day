import { useQuery } from '@tanstack/react-query'
import { scheduleApi } from '../api/schedule'
import { toDisplayItem } from './useSchedules'

// 일정 제목 키워드 검색 훅. keyword 가 비어 있으면 조회하지 않는다.
// 키가 ['schedules', ...] 로 시작해 완료·미루기·삭제 후 검색 결과도 함께 갱신된다.
export function useSearchSchedules(keyword) {
  return useQuery({
    queryKey: ['schedules', 'search', keyword],
    queryFn: async () => {
      const data = await scheduleApi.searchSchedules({ keyword })
      return {
        hasMore: Boolean(data.hasMore),
        items: (data.schedules ?? []).map(toDisplayItem),
      }
    },
    enabled: Boolean(keyword),
  })
}
