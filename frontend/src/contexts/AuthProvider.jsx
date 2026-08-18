import { useState } from 'react'
import { AuthContext } from './AuthContext'
import { tokenStorage } from '../api/client'
import { authApi } from '../api/auth'

const MEMBER_KEY = 'member'

// 새로고침해도 로그인이 유지되도록 localStorage 에서 회원 정보를 복원한다.
function readStoredMember() {
  const raw = localStorage.getItem(MEMBER_KEY)
  return raw ? JSON.parse(raw) : null
}

export function AuthProvider({ children }) {
  const [member, setMember] = useState(readStoredMember)

  // 로그인 성공 시 받은 토큰과 회원 정보를 저장한다.
  const login = ({ accessToken, refreshToken, member: loginMember }) => {
    tokenStorage.set({ accessToken, refreshToken })
    localStorage.setItem(MEMBER_KEY, JSON.stringify(loginMember))
    setMember(loginMember)
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
    } finally {
      tokenStorage.clear()
      localStorage.removeItem(MEMBER_KEY)
      setMember(null)
    }
  }

  const value = {
    member,
    isAuthenticated: Boolean(member),
    login,
    logout,
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
