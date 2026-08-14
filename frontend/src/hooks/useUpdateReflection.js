import { useMutation, useQueryClient } from '@tanstack/react-query'
import { reflectionApi } from '../api/reflection'

// 회고 수정 mutation. 성공 시 날짜별 회고 조회를 무효화해 화면을 갱신한다.
export function useUpdateReflection() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: reflectionApi.updateReflection,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['reflections'] })
    },
  })
}
