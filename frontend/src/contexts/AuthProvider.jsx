import { useEffect, useState } from 'react'
import { useQueryClient } from '@tanstack/react-query'
import { AuthContext } from './AuthContext'
import { authStorage, tokenStorage } from '../api/client'
import { authApi } from '../api/auth'

export function AuthProvider({ children }) {
  const [member, setMember] = useState(authStorage.getMember)
  const queryClient = useQueryClient()

  useEffect(
    () =>
      authStorage.subscribe((nextMember) => {
        queryClient.clear()
        setMember(nextMember)
      }),
    [queryClient],
  )

  // 로그인 성공 시 받은 토큰과 회원 정보를 저장한다.
  const login = (session) => {
    if (
      member?.isGuest &&
      !session.member.isGuest &&
      member.memberId !== session.member.memberId
    ) {
      authStorage.saveGuest(member)
    }
    queryClient.clear()
    authStorage.setSession(session)
    setMember(session.member)
  }

  // 서버의 refresh token 을 폐기한 뒤 로컬 저장소를 정리한다.
  // 서버 요청은 토큰을 지우기 전에 보내야 하고, 실패하더라도 로컬 로그아웃은 그대로 진행한다.
  const logout = async () => {
    const refreshToken = tokenStorage.getRefreshToken()
    try {
      if (refreshToken) {
        await authApi.logout({ refreshToken })
      }
    } catch {
      // 이미 만료·폐기된 토큰이면 서버가 거부한다. 어차피 무효라 무시해도 된다.
    }
    queryClient.clear()
    const restoredGuest = authStorage.endSession()
    setMember(restoredGuest)
    return restoredGuest
  }

  const value = {
    member,
    isAuthenticated: Boolean(member),
    isGuest: Boolean(member?.isGuest),
    login,
    logout,
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
