import { Routes, Route } from 'react-router-dom'
import ProtectedRoute from './components/ProtectedRoute'
import Layout from './components/Layout'
import LoginPage from './pages/LoginPage'
import SignupPage from './pages/SignupPage'
import ForgotPasswordPage from './pages/ForgotPasswordPage'
import ResetPasswordPage from './pages/ResetPasswordPage'
import HomePage from './pages/HomePage'
import ScheduleDetailPage from './pages/ScheduleDetailPage'
import ScheduleFormPage from './pages/ScheduleFormPage'
import ReflectionPage from './pages/ReflectionPage'
import InsightsPage from './pages/InsightsPage'
import MyPage from './pages/MyPage'
import NotFoundPage from './pages/NotFoundPage'
import './App.css'

function App() {
  return (
    <Routes>
      {/* 로그인/회원가입/비밀번호 재설정은 공통 레이아웃(하단 탭) 밖의 독립 화면 */}
      <Route path="/login" element={<LoginPage />} />
      <Route path="/signup" element={<SignupPage />} />
      <Route path="/forgot-password" element={<ForgotPasswordPage />} />
      <Route path="/reset-password" element={<ResetPasswordPage />} />

      {/* 로그인해야만 접근 가능한 영역 */}
      <Route element={<ProtectedRoute />}>
        {/* 일정 등록/수정: 하단 탭 없는 전체화면 */}
        <Route path="/schedules/new" element={<ScheduleFormPage />} />
        <Route
          path="/schedules/:scheduleId/edit"
          element={<ScheduleFormPage />}
        />

        {/* 하단 탭바 공유 */}
        <Route element={<Layout />}>
          <Route path="/" element={<HomePage />} />
          <Route
            path="/schedules/:scheduleId"
            element={<ScheduleDetailPage />}
          />
          <Route path="/reflection" element={<ReflectionPage />} />
          <Route path="/insights" element={<InsightsPage />} />
          <Route path="/mypage" element={<MyPage />} />
          {/* 위에 매칭되지 않는 모든 경로는 404 */}
          <Route path="*" element={<NotFoundPage />} />
        </Route>
      </Route>
    </Routes>
  )
}

export default App
