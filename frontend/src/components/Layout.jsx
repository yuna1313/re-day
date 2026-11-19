import { useState } from 'react'
import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom'
import { Home, CalendarDays, BarChart3, User } from 'lucide-react'
import { useAuth } from '../contexts/AuthContext'
import './Layout.css'

// 하단 탭바 (모든 메인 화면 공통)
const TABS = [
  { to: '/', label: '홈', Icon: Home, end: true },
  { to: '/reflection', label: '회고', Icon: CalendarDays },
  { to: '/insights', label: '인사이트', Icon: BarChart3 },
  { to: '/mypage', label: '마이페이지', Icon: User },
]

function Layout() {
  const { member, logout } = useAuth()
  const navigate = useNavigate()
  const [isExitingDemo, setIsExitingDemo] = useState(false)
  const isDemo = member?.isDemo === true
  const isLegacyGuest = member?.isGuest && member.isDemo === undefined

  const exitDemo = async () => {
    setIsExitingDemo(true)
    const restoredGuest = await logout()
    setIsExitingDemo(false)
    navigate(restoredGuest ? '/' : '/welcome', { replace: true })
  }

  return (
    <div className="app-shell">
      {(isDemo || isLegacyGuest) && (
        <div className="guest-banner">
          <span>{isDemo ? '데모 모드 · 예시 일정' : '게스트 모드'}</span>
          {isDemo ? (
            <button
              type="button"
              className="guest-banner-action"
              disabled={isExitingDemo}
              onClick={exitDemo}
            >
              {isExitingDemo ? '나가는 중…' : '데모 나가기'}
            </button>
          ) : (
            <Link
              to="/welcome"
              state={{ fromGuest: true }}
              className="guest-banner-action"
            >
              서비스 소개
            </Link>
          )}
        </div>
      )}
      <main className="app-main">
        <Outlet />
      </main>

      <nav className="bottom-nav">
        {TABS.map(({ to, label, Icon, end }) => (
          <NavLink key={to} to={to} end={end} className="bottom-nav-item">
            <Icon size={24} />
            <span>{label}</span>
          </NavLink>
        ))}
      </nav>
    </div>
  )
}

export default Layout
