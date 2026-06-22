import { NavLink, Outlet } from 'react-router-dom'

function Layout() {
  return (
    <div className="app-shell">
      <header className="app-header">
        <NavLink to="/" className="brand">
          RE:DAY
        </NavLink>
        <nav className="app-nav">
          {/* NavLink는 현재 경로와 일치하면 active 클래스를 자동으로 붙여준다 */}
          <NavLink to="/" end>
            홈
          </NavLink>
          <NavLink to="/retrospectives">회고</NavLink>
        </nav>
      </header>

      <main className="app-main">
        {/* 자식 라우트의 페이지가 여기에 렌더링된다 */}
        <Outlet />
      </main>
    </div>
  )
}

export default Layout
