import { useQuery } from '@tanstack/react-query'
import { format } from 'date-fns'
import { reflectionApi } from '../api/reflection'

// 날짜별 응답(평면 구조)을 오늘 응답과 같은 형태로 맞춘다.
function toReflectionView(detail) {
  return {
    reflection: detail.reflectionId
      ? {
          reflectionId: detail.reflectionId,
          reflectionDate: detail.reflectionDate,
          content: detail.content,
        }
      : null,
    completedSchedules: detail.completedSchedules ?? [],
  }
}

// 특정 날짜의 회고 + 그날 완료한 일정 조회. date: 'yyyy-MM-dd'
// 오늘은 서버가 자체 기준으로 날짜를 판단하는 기존 엔드포인트를 그대로 사용한다.
export function useReflection(date) {
  return useQuery({
    queryKey: ['reflections', date],
    queryFn: async () => {
      if (date === format(new Date(), 'yyyy-MM-dd')) {
        return reflectionApi.getTodayReflection()
      }
      return toReflectionView(await reflectionApi.getReflectionByDate({ date }))
    },
    enabled: Boolean(date),
  })
}
