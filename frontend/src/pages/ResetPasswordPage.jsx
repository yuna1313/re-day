import { useState } from 'react'
import { Navigate, useLocation, useNavigate } from 'react-router-dom'
import { useMutation } from '@tanstack/react-query'
import { Eye, EyeOff } from 'lucide-react'
import { authApi } from '../api/auth'
import { getApiErrorMessage } from '../api/client'
import './ResetPasswordPage.css'

function ResetPasswordPage() {
  const location = useLocation()
  const navigate = useNavigate()
  const [password, setPassword] = useState('')
  const [passwordConfirm, setPasswordConfirm] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [showPasswordConfirm, setShowPasswordConfirm] = useState(false)

  const resetMutation = useMutation({
    mutationFn: authApi.resetPassword,
    onSuccess: () => {
      // 재설정 완료 → 로그인 화면으로
      navigate('/login', { replace: true })
    },
  })

  // 이메일 인증을 거치지 않고 직접 들어오면 인증 화면으로 되돌린다.
  if (!location.state?.verified) {
    return <Navigate to="/forgot-password" replace />
  }

  // 앞 화면(인증)에서 넘겨받은 이메일
  const email = location.state?.email

  // 프론트에서 비밀번호 일치 확인 (확인란을 입력했는데 서로 다를 때만 표시)
  const passwordMismatch =
    passwordConfirm.length > 0 && password !== passwordConfirm

  const isSubmitDisabled =
    !password || !passwordConfirm || passwordMismatch || resetMutation.isPending

  const handleSubmit = (event) => {
    event.preventDefault()
    resetMutation.mutate({
      email,
      newPassword: password,
      newPasswordConfirm: passwordConfirm,
    })
  }

  return (
    <div className="reset-page">
      <h1 className="reset-logo">RE:DAY</h1>
      <p className="reset-desc">비밀번호 재설정을 진행해주세요.</p>

      <form className="reset-form" onSubmit={handleSubmit} noValidate>
        {/* 비밀번호 */}
        <div className="reset-field">
          <label className="reset-label" htmlFor="reset-password">
            비밀번호
          </label>
          <div className="reset-input-wrap">
            <input
              id="reset-password"
              type={showPassword ? 'text' : 'password'}
              className="reset-input"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              autoComplete="new-password"
            />
            <button
              type="button"
              className="reset-toggle"
              onClick={() => setShowPassword((prev) => !prev)}
              aria-label={showPassword ? '비밀번호 숨기기' : '비밀번호 표시'}
            >
              {showPassword ? <EyeOff size={22} /> : <Eye size={22} />}
            </button>
          </div>
        </div>

        {/* 비밀번호 확인 */}
        <div className="reset-field">
          <label className="reset-label" htmlFor="reset-password-confirm">
            비밀번호 확인
          </label>
          <div className="reset-input-wrap">
            <input
              id="reset-password-confirm"
              type={showPasswordConfirm ? 'text' : 'password'}
              className="reset-input"
              value={passwordConfirm}
              onChange={(event) => setPasswordConfirm(event.target.value)}
              autoComplete="new-password"
            />
            <button
              type="button"
              className="reset-toggle"
              onClick={() => setShowPasswordConfirm((prev) => !prev)}
              aria-label={
                showPasswordConfirm ? '비밀번호 숨기기' : '비밀번호 표시'
              }
            >
              {showPasswordConfirm ? <EyeOff size={22} /> : <Eye size={22} />}
            </button>
          </div>
        </div>

        {/* 문구 자리를 항상 확보해 버튼이 밀리지 않게 한다 */}
        <p className="reset-error" aria-live="polite">
          {passwordMismatch
            ? '비밀번호가 일치하지 않습니다.'
            : resetMutation.isError
              ? getApiErrorMessage(resetMutation.error)
              : ''}
        </p>

        <button
          type="submit"
          className="reset-submit"
          disabled={isSubmitDisabled}
        >
          {resetMutation.isPending ? '변경 중...' : '완료'}
        </button>
      </form>
    </div>
  )
}

export default ResetPasswordPage
