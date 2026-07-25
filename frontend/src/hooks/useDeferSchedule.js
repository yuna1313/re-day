import { useMutation, useQueryClient } from '@tanstack/react-query'
import { scheduleApi } from '../api/schedule'

// 일정 미루기 mutation. 성공 시 일정 목록 캐시를 무효화해 화면을 갱신한다.
export function useDeferSchedule() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: scheduleApi.deferSchedule,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['schedules'] })
    },
  })
}
