import { useQuery } from '@tanstack/react-query'
import { memberApi } from '../api/member'

// 내 정보(마이페이지) 조회
export function useMe() {
  return useQuery({
    queryKey: ['me'],
    queryFn: memberApi.getMe,
  })
}
