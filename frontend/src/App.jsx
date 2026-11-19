import { useEffect, useState } from 'react'
import { Routes, Route } from 'react-router-dom'
import ProtectedRoute from './components/ProtectedRoute'
import Layout from './components/Layout'
import LandingPage from './pages/LandingPage'
import LoginPage from './pages/LoginPage'
import GuestRecordImportPage from './pages/GuestRecordImportPage'
import SignupPage from './pages/SignupPage'
import ForgotPasswordPage from './pages/ForgotPasswordPage'
import ResetPasswordPage from './pages/ResetPasswordPage'
import HomePage from './pages/HomePage'
import ScheduleDetailPage from './pages/ScheduleDetailPage'
import ScheduleFormPage from './pages/ScheduleFormPage'
import SearchPage from './pages/SearchPage'
import OverduePage from './pages/OverduePage'
import ReflectionPage from './pages/ReflectionPage'
import InsightsPage from './pages/InsightsPage'
import MyPage from './pages/MyPage'
import ChangePasswordPage from './pages/ChangePasswordPage'
import NotFoundPage from './pages/NotFoundPage'

function App() {
  const [installPrompt, setInstallPrompt] = useState(null)

  useEffect(() => {
    const showInstallAction = (event) => {
      event.preventDefault()
      setInstallPrompt(event)
    }
    const hideInstallAction = () => setInstallPrompt(null)
    window.addEventListener('beforeinstallprompt', showInstallAction)
    window.addEventListener('appinstalled', hideInstallAction)
    return () => {
      window.removeEventListener('beforeinstallprompt', showInstallAction)
      window.removeEventListener('appinstalled', hideInstallAction)
    }
  }, [])

  const installApp = installPrompt
    ? () => {
        setInstallPrompt(null)
        installPrompt.prompt().catch((error) => {
          console.warn('RE:DAY 설치 창을 열지 못했습니다.', error)
        })
      }
    : null

  return (
    <Routes>
      {/* 로그인 전 화면들은 공통 레이아웃(하단 탭) 밖의 독립 화면 */}
      <Route path="/welcome" element={<LandingPage />} />
      <Route path="/demo" element={<LandingPage demo />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/signup" element={<SignupPage />} />
      <Route path="/forgot-password" element={<ForgotPasswordPage />} />
      <Route path="/reset-password" element={<ResetPasswordPage />} />

      {/* 게스트 또는 회원 세션으로 접근하는 영역 */}
      <Route element={<ProtectedRoute />}>
        <Route element={<ProtectedRoute memberOnly />}>
          <Route
            path="/mypage/guest-records"
            element={<GuestRecordImportPage />}
          />
        </Route>
        {/* 일정 수정: 하단 탭 없는 전체화면 (등록은 홈의 바텀시트) */}
        <Route
          path="/schedules/:scheduleId/edit"
          element={<ScheduleFormPage />}
        />

        {/* 하단 탭바 공유 */}
        <Route element={<Layout />}>
          <Route path="/" element={<HomePage />} />
          <Route path="/search" element={<SearchPage />} />
          <Route path="/overdue" element={<OverduePage />} />
          <Route
            path="/schedules/:scheduleId"
            element={<ScheduleDetailPage />}
          />
          <Route path="/reflection" element={<ReflectionPage />} />
          <Route path="/insights" element={<InsightsPage />} />
          <Route path="/mypage" element={<MyPage installApp={installApp} />} />
          <Route element={<ProtectedRoute memberOnly />}>
            <Route path="/mypage/password" element={<ChangePasswordPage />} />
          </Route>
          {/* 위에 매칭되지 않는 모든 경로는 404 */}
          <Route path="*" element={<NotFoundPage />} />
        </Route>
      </Route>
    </Routes>
  )
}

export default App
