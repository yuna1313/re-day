import { useNavigate } from 'react-router-dom'
import {
  User,
  Lock,
  LogIn,
  UserPlus,
  Download,
  History,
  ChevronRight,
} from 'lucide-react'
import { authStorage } from '../api/client'
import { useAuth } from '../contexts/AuthContext'
import { useMe } from '../hooks/useMe'
import './MyPage.css'

function MyPage({ installApp }) {
  const navigate = useNavigate()
  const { member, isGuest, logout } = useAuth()
  // 저장된 정보로 즉시 표시하고, 최신 정보를 조회해 갱신한다.
  const { data } = useMe(!isGuest)
  const profile = isGuest ? member : (data ?? member)
  const canImportGuestRecords = !isGuest && authStorage.getGuestImportToken()

  const handleLogout = async () => {
    const restoredGuest = await logout()
    navigate(restoredGuest ? '/' : '/login', { replace: true })
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
            <span className="profile-name">
              {profile?.nickname ?? '사용자'}
            </span>
            <span className="profile-email">
              {isGuest
                ? '이 브라우저에서 게스트로 사용 중이에요.'
                : profile?.email}
            </span>
          </div>
        </div>

        <div className="mypage-divider" />

        {isGuest && (
          <>
            <button
              type="button"
              className="menu-row"
              onClick={() => navigate('/signup')}
            >
              <span className="menu-left">
                <UserPlus size={18} className="menu-lock" />
                회원가입하고 기록 이어가기
              </span>
              <ChevronRight size={18} className="menu-chevron" />
            </button>
            <div className="mypage-divider" />
          </>
        )}

        <button
          type="button"
          className="menu-row"
          onClick={isGuest ? () => navigate('/login') : handlePasswordChange}
        >
          <span className="menu-left">
            {isGuest ? (
              <LogIn size={18} className="menu-lock" />
            ) : (
              <Lock size={18} className="menu-lock" />
            )}
            {isGuest ? '기존 계정으로 로그인' : '비밀번호 변경'}
          </span>
          <ChevronRight size={18} className="menu-chevron" />
        </button>
      </div>

      {canImportGuestRecords && (
        <div className="mypage-card">
          <button
            type="button"
            className="menu-row"
            onClick={() =>
              navigate('/mypage/guest-records', { state: { fromMyPage: true } })
            }
          >
            <span className="menu-left">
              <History size={18} className="menu-lock" />
              게스트 기록 가져오기
            </span>
            <ChevronRight size={18} className="menu-chevron" />
          </button>
        </div>
      )}

      {installApp && (
        <div className="mypage-card">
          <button type="button" className="menu-row" onClick={installApp}>
            <span className="menu-left">
              <Download size={18} className="menu-lock" />홈 화면에 설치
            </span>
            <ChevronRight size={18} className="menu-chevron" />
          </button>
        </div>
      )}

      {/* 로그아웃 */}
      {!isGuest && (
        <div className="mypage-card">
          <button type="button" className="logout-btn" onClick={handleLogout}>
            로그아웃
          </button>
        </div>
      )}
    </div>
  )
}

export default MyPage
