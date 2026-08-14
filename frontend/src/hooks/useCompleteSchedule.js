import { useMutation, useQueryClient } from '@tanstack/react-query'
import { scheduleApi } from '../api/schedule'

// 일정 완료 처리 mutation. 성공 시 관련 캐시를 무효화해 화면을 갱신한다.
export function useCompleteSchedule() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: scheduleApi.completeSchedule,
    onSuccess: (_data, variables) => {
      // 일정 목록
      queryClient.invalidateQueries({ queryKey: ['schedules'] })
      // 회고 탭 "완료한 일정" (날짜별로 캐시되므로 전체 무효화)
      queryClient.invalidateQueries({ queryKey: ['reflections'] })
      // 해당 일정 상세
      queryClient.invalidateQueries({
        queryKey: ['schedule', String(variables.scheduleId)],
      })
    },
  })
}
