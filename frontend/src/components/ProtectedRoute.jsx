import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'

// 로그인하지 않은 사용자는 로그인 페이지로 돌려보낸다.
// (자식 라우트들을 감싸서 인증 게이트 역할을 한다.)
function ProtectedRoute() {
  const { isAuthenticated } = useAuth()

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />
  }

  return <Outlet />
}

export default ProtectedRoute
