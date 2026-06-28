import { useState } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import { useMutation } from '@tanstack/react-query'
import { authApi } from '../api/auth'
import { getApiErrorMessage } from '../api/client'
import { useAuth } from '../contexts/AuthContext'
import { Eye, EyeOff } from 'lucide-react'
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
    ? getApiErrorMessage(
        loginMutation.error,
        '로그인에 실패했습니다. 잠시 후 다시 시도해주세요.',
      )
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
            {showPassword ? <EyeOff size={22} /> : <Eye size={22} />}
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

      <p className="login-signup">
        아직 회원이 아니신가요?
        <Link to="/signup" className="login-signup-link">
          회원가입
        </Link>
      </p>
    </div>
  )
}

export default LoginPage
