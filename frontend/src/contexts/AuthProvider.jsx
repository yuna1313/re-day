import { useState } from 'react'
import { AuthContext } from './AuthContext'
import { tokenStorage } from '../api/client'

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

  const logout = () => {
    tokenStorage.clear()
    localStorage.removeItem(MEMBER_KEY)
    setMember(null)
  }

  const value = {
    member,
    isAuthenticated: Boolean(member),
    login,
    logout,
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
