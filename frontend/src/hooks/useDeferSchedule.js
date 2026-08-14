import { useMutation, useQueryClient } from '@tanstack/react-query'
import { scheduleApi } from '../api/schedule'

// 일정 미루기 mutation. 성공 시 관련 캐시를 무효화해 화면을 갱신한다.
export function useDeferSchedule() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: scheduleApi.deferSchedule,
    onSuccess: (_data, variables) => {
      // 일정 목록
      queryClient.invalidateQueries({ queryKey: ['schedules'] })
      // 해당 일정 상세 (미루기 횟수 · 처리 기록)
      queryClient.invalidateQueries({
        queryKey: ['schedule', String(variables.scheduleId)],
      })
    },
  })
}
