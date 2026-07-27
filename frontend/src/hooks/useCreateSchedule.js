import { useMutation, useQueryClient } from '@tanstack/react-query'
import { scheduleApi } from '../api/schedule'

// 일정 생성 mutation. 성공 시 일정 목록 캐시를 무효화해 화면을 갱신한다.
export function useCreateSchedule() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: scheduleApi.createSchedule,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['schedules'] })
    },
  })
}
