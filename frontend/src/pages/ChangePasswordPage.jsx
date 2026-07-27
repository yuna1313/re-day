import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { ChevronLeft, Eye, EyeOff, CheckCircle2, Circle } from 'lucide-react'
import { useChangePassword } from '../hooks/useChangePassword'
import { getApiErrorMessage } from '../api/client'
import './ChangePasswordPage.css'

// 새 비밀번호 규칙 (실시간 검사)
const PASSWORD_RULES = [
  { label: '8자리 이상 입력해주세요.', test: (v) => v.length >= 8 },
  { label: '대문자 1개를 포함해주세요.', test: (v) => /[A-Z]/.test(v) },
  { label: '숫자를 1개 포함해주세요.', test: (v) => /\d/.test(v) },
]

function ChangePasswordPage() {
  const navigate = useNavigate()
  const [currentPassword, setCurrentPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [showCurrent, setShowCurrent] = useState(false)
  const [showNew, setShowNew] = useState(false)

  const ruleResults = PASSWORD_RULES.map((rule) => ({
    label: rule.label,
    passed: rule.test(newPassword),
  }))
  const allPassed = ruleResults.every((rule) => rule.passed)
  const canSubmit = currentPassword.length > 0 && allPassed

  const changeMutation = useChangePassword()

  const handleSubmit = (event) => {
    event.preventDefault()
    if (!canSubmit || changeMutation.isPending) return
    changeMutation.mutate(
      { currentPassword, newPassword },
      { onSuccess: () => navigate('/mypage', { replace: true }) },
    )
  }

  return (
    <div className="pw-page">
      <header className="pw-header">
        <button
          type="button"
          className="pw-back"
          onClick={() => navigate(-1)}
          aria-label="뒤로"
        >
          <ChevronLeft size={26} />
        </button>
        <h1 className="pw-title">비밀번호 변경</h1>
      </header>

      <form className="pw-form" onSubmit={handleSubmit} noValidate>
        <div className="pw-body">
          {/* 현재 비밀번호 */}
          <div className="pw-field">
            <label className="pw-label" htmlFor="current-password">
              현재 비밀번호를 입력해주세요
            </label>
            <div className="pw-input-wrap">
              <input
                id="current-password"
                type={showCurrent ? 'text' : 'password'}
                className="pw-input"
                placeholder="현재 비밀번호"
                value={currentPassword}
                onChange={(event) => setCurrentPassword(event.target.value)}
                autoComplete="current-password"
              />
              <button
                type="button"
                className="pw-toggle"
                onClick={() => setShowCurrent((prev) => !prev)}
                aria-label={showCurrent ? '비밀번호 숨기기' : '비밀번호 표시'}
              >
                {showCurrent ? <EyeOff size={22} /> : <Eye size={22} />}
              </button>
            </div>
          </div>

          {/* 새 비밀번호 */}
          <div className="pw-field">
            <label className="pw-label" htmlFor="new-password">
              새로운 비밀번호를 입력해주세요
            </label>
            <div className="pw-input-wrap">
              <input
                id="new-password"
                type={showNew ? 'text' : 'password'}
                className="pw-input"
                placeholder="새로운 비밀번호"
                value={newPassword}
                onChange={(event) => setNewPassword(event.target.value)}
                autoComplete="new-password"
              />
              <button
                type="button"
                className="pw-toggle"
                onClick={() => setShowNew((prev) => !prev)}
                aria-label={showNew ? '비밀번호 숨기기' : '비밀번호 표시'}
              >
                {showNew ? <EyeOff size={22} /> : <Eye size={22} />}
              </button>
            </div>

            {/* 규칙 체크리스트 */}
            <ul className="pw-rules">
              {ruleResults.map((rule) => (
                <li
                  key={rule.label}
                  className={`pw-rule${rule.passed ? ' passed' : ''}`}
                >
                  {rule.passed ? (
                    <CheckCircle2 size={15} />
                  ) : (
                    <Circle size={15} />
                  )}
                  {rule.label}
                </li>
              ))}
            </ul>
          </div>
        </div>

        {changeMutation.isError && (
          <p className="pw-error">{getApiErrorMessage(changeMutation.error)}</p>
        )}

        <button
          type="submit"
          className="pw-submit"
          disabled={!canSubmit || changeMutation.isPending}
        >
          {changeMutation.isPending ? '변경 중...' : '변경하기'}
        </button>
      </form>
    </div>
  )
}

export default ChangePasswordPage
