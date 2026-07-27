import { useMutation } from '@tanstack/react-query'
import { memberApi } from '../api/member'

// 비밀번호 변경 mutation
export function useChangePassword() {
  return useMutation({
    mutationFn: memberApi.changePassword,
  })
}
