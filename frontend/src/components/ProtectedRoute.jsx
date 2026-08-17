import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'

// 로그인하지 않은 사용자는 서비스 소개(랜딩)로 보낸다.
// 처음 방문한 사람에게 로그인 폼부터 들이밀면 무슨 서비스인지 모른 채 이탈하기 때문이다.
// (자식 라우트들을 감싸서 인증 게이트 역할을 한다.)
function ProtectedRoute() {
  const { isAuthenticated } = useAuth()

  if (!isAuthenticated) {
    return <Navigate to="/welcome" replace />
  }

  return <Outlet />
}

export default ProtectedRoute
