import { useQuery } from '@tanstack/react-query'
import { reflectionApi } from '../api/reflection'

// 오늘 회고 + 오늘 완료한 일정 조회
export function useTodayReflection() {
  return useQuery({
    queryKey: ['reflections', 'today'],
    queryFn: reflectionApi.getTodayReflection,
  })
}
