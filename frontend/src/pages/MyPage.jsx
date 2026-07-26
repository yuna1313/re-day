import { useNavigate } from 'react-router-dom'
import { User, Lock, ChevronRight } from 'lucide-react'
import { useAuth } from '../contexts/AuthContext'
import { useMe } from '../hooks/useMe'
import './MyPage.css'

function MyPage() {
  const navigate = useNavigate()
  const { member, logout } = useAuth()
  // 저장된 정보로 즉시 표시하고, 최신 정보를 조회해 갱신한다.
  const { data } = useMe()
  const profile = data ?? member

  const handleLogout = () => {
    logout()
    navigate('/login', { replace: true })
  }

  const handlePasswordChange = () => {
    navigate('/mypage/password')
  }

  return (
    <div className="mypage">
      <header className="mypage-header">
        <h1 className="mypage-title">마이페이지</h1>
      </header>

      {/* 프로필 + 메뉴 */}
      <div className="mypage-card">
        <div className="profile-row">
          <span className="profile-avatar">
            <User size={26} />
          </span>
          <div className="profile-info">
            <span className="profile-name">{profile?.nickname ?? '사용자'}</span>
            <span className="profile-email">{profile?.email ?? ''}</span>
          </div>
        </div>

        <div className="mypage-divider" />

        <button
          type="button"
          className="menu-row"
          onClick={handlePasswordChange}
        >
          <span className="menu-left">
            <Lock size={18} className="menu-lock" />
            비밀번호 변경
          </span>
          <ChevronRight size={18} className="menu-chevron" />
        </button>
      </div>

      {/* 로그아웃 */}
      <div className="mypage-card">
        <button type="button" className="logout-btn" onClick={handleLogout}>
          로그아웃
        </button>
      </div>
    </div>
  )
}

export default MyPage
