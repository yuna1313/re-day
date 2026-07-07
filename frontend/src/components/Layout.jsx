import { NavLink, Outlet } from 'react-router-dom'
import { Home, CalendarDays, BarChart3, User } from 'lucide-react'
import './Layout.css'

// 하단 탭바 (모든 메인 화면 공통)
const TABS = [
  { to: '/', label: '홈', Icon: Home, end: true },
  { to: '/reflection', label: '회고', Icon: CalendarDays },
  { to: '/insights', label: '인사이트', Icon: BarChart3 },
  { to: '/mypage', label: '마이', Icon: User },
]

function Layout() {
  return (
    <div className="app-shell">
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
