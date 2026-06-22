import { useState } from 'react'
import { Navigate, useNavigate } from 'react-router-dom'
import { useMutation } from '@tanstack/react-query'
import { authApi } from '../api/auth'
import { useAuth } from '../contexts/AuthContext'
import './LoginPage.css'

function LoginPage() {
  const navigate = useNavigate()
  const { isAuthenticated, login } = useAuth()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)

  const loginMutation = useMutation({
    mutationFn: authApi.login,
    onSuccess: (res) => {
      // res.data = { accessToken, refreshToken, member }
      login(res.data)
      navigate('/', { replace: true })
    },
  })

  // 이미 로그인된 상태라면 로그인 화면 대신 메인으로 보낸다.
  if (isAuthenticated) {
    return <Navigate to="/" replace />
  }

  const handleSubmit = (event) => {
    event.preventDefault()
    loginMutation.mutate({ email, password })
  }

  const isSubmitDisabled = !email || !password || loginMutation.isPending

  // 서버가 내려준 실패 메시지를 우선 사용하고, 없으면 기본 문구를 보여준다.
  const errorMessage = loginMutation.isError
    ? (loginMutation.error?.response?.data?.message ??
      '로그인에 실패했습니다. 잠시 후 다시 시도해주세요.')
    : null

  return (
    <div className="login-page">
      <h1 className="login-logo">RE:DAY</h1>

      <p className="login-greeting">
        안녕하세요 :)
        <br />
        RE:DAY 사용을 위해 로그인을 먼저 해주세요.
      </p>

      <form className="login-form" onSubmit={handleSubmit} noValidate>
        <div className="login-field">
          <input
            type="email"
            className="login-input"
            placeholder="이메일 주소"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            autoComplete="email"
          />
        </div>

        <div className="login-field">
          <input
            type={showPassword ? 'text' : 'password'}
            className="login-input"
            placeholder="비밀번호"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            autoComplete="current-password"
          />
          <button
            type="button"
            className="login-toggle"
            onClick={() => setShowPassword((prev) => !prev)}
            aria-label={showPassword ? '비밀번호 숨기기' : '비밀번호 표시'}
          >
            {showPassword ? <EyeOffIcon /> : <EyeIcon />}
          </button>
        </div>

        {errorMessage && <p className="login-error">{errorMessage}</p>}

        <button
          type="submit"
          className="login-button"
          disabled={isSubmitDisabled}
        >
          {loginMutation.isPending ? '로그인 중...' : '로그인'}
        </button>
      </form>

      <div className="login-forgot">
        {/* TODO: 비밀번호 찾기 화면 연결 (현재 백엔드 API 미정) */}
        <button type="button" className="login-forgot-link">
          비밀번호를 잊어버렸어요.
        </button>
      </div>
    </div>
  )
}

// 비밀번호 표시/숨기기 아이콘 (외부 아이콘 라이브러리 없이 인라인 SVG 사용)
function EyeIcon() {
  return (
    <svg
      width="22"
      height="22"
      viewBox="0 0 24 24"
      fill="none"
      aria-hidden="true"
    >
      <path
        d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7-10-7-10-7Z"
        stroke="currentColor"
        strokeWidth="1.8"
      />
      <circle cx="12" cy="12" r="3" stroke="currentColor" strokeWidth="1.8" />
    </svg>
  )
}

function EyeOffIcon() {
  return (
    <svg
      width="22"
      height="22"
      viewBox="0 0 24 24"
      fill="none"
      aria-hidden="true"
    >
      <path
        d="M3 3l18 18M10.6 10.7a3 3 0 0 0 4.2 4.2M9.9 4.6A10.9 10.9 0 0 1 12 5c6.5 0 10 7 10 7a16.4 16.4 0 0 1-3.4 4.3M6.6 6.6A16.4 16.4 0 0 0 2 12s3.5 7 10 7a10.9 10.9 0 0 0 3-.4"
        stroke="currentColor"
        strokeWidth="1.8"
        strokeLinecap="round"
      />
    </svg>
  )
}

export default LoginPage
