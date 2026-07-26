import { useMutation, useQueryClient } from '@tanstack/react-query'
import { scheduleApi } from '../api/schedule'

// 일정 수정 mutation. 성공 시 일정 목록 캐시를 무효화해 화면을 갱신한다.
export function useUpdateSchedule() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: scheduleApi.updateSchedule,
    onSuccess: (_data, variables) => {
      queryClient.invalidateQueries({ queryKey: ['schedules'] })
      // 상세 화면으로 돌아갔을 때 최신 내용이 반영되도록 상세도 무효화
      queryClient.invalidateQueries({
        queryKey: ['schedule', String(variables.scheduleId)],
      })
    },
  })
}
