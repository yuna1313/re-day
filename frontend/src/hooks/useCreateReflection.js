import { useMutation } from '@tanstack/react-query'
import { reflectionApi } from '../api/reflection'

// 오늘의 회고 작성 mutation.
// (아직 회고 조회 쿼리가 없어 무효화 대상 없음 — 조회 연동 시 추가 예정)
export function useCreateReflection() {
  return useMutation({
    mutationFn: reflectionApi.createReflection,
  })
}
