import { Routes, Route } from 'react-router-dom'
import Layout from './components/Layout'
import HomePage from './pages/HomePage'
import RetrospectivesPage from './pages/RetrospectivesPage'
import NotFoundPage from './pages/NotFoundPage'
import './App.css'

function App() {
  return (
    <Routes>
      {/* 공통 레이아웃(네비게이션)을 공유하는 라우트들 */}
      <Route path="/" element={<Layout />}>
        <Route index element={<HomePage />} />
        <Route path="retrospectives" element={<RetrospectivesPage />} />
        {/* 위에 매칭되지 않는 모든 경로는 404 */}
        <Route path="*" element={<NotFoundPage />} />
      </Route>
    </Routes>
  )
}

export default App
