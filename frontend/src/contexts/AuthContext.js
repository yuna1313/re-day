import { createContext, useContext } from 'react'

// 인증 상태를 담는 Context. Provider는 AuthProvider.jsx 에서 제공한다.
// (Context 객체는 컴포넌트가 아니므로 별도 파일로 분리해 Fast Refresh 경고를 피한다.)
export const AuthContext = createContext(null)

// 어디서든 인증 상태(member, isAuthenticated, login, logout)에 접근하는 훅
export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth는 AuthProvider 내부에서만 사용할 수 있습니다.')
  }
  return context
}
