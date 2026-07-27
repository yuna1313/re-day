import { useMutation, useQueryClient } from '@tanstack/react-query'
import { reflectionApi } from '../api/reflection'

// 오늘의 회고 작성 mutation. 성공 시 오늘 회고 조회를 무효화해 보기 모드로 전환된다.
export function useCreateReflection() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: reflectionApi.createReflection,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['reflections', 'today'] })
    },
  })
}
