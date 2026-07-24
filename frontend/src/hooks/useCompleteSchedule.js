import { useMutation, useQueryClient } from '@tanstack/react-query'
import { scheduleApi } from '../api/schedule'

// 일정 완료 처리 mutation. 성공 시 일정 목록 캐시를 무효화해 화면을 갱신한다.
export function useCompleteSchedule() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: scheduleApi.completeSchedule,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['schedules'] })
    },
  })
}
