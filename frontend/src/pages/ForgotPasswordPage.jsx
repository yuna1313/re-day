import { useState, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { isValidEmail } from '../utils/validators'
import './ForgotPasswordPage.css'

// 재전송까지 대기 시간(초). 버튼 연타/반복 요청 방지용.
const RESEND_COOLDOWN = 60

function ForgotPasswordPage() {
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [emailTouched, setEmailTouched] = useState(false)
  const [code, setCode] = useState('')
  const [isSent, setIsSent] = useState(false)
  const [cooldown, setCooldown] = useState(0)

  const isEmailOk = isValidEmail(email)
  const showEmailError = emailTouched && email.length > 0 && !isEmailOk

  // 재전송 쿨다운 카운트다운 (1초마다 감소)
  useEffect(() => {
    if (cooldown <= 0) return
    const timer = setTimeout(() => setCooldown((prev) => prev - 1), 1000)
    return () => clearTimeout(timer)
  }, [cooldown])

  const handleSendCode = () => {
    if (!isEmailOk || cooldown > 0) return
    // TODO: 재설정 인증코드 발송 API 연동 예정
    setIsSent(true)
    setCooldown(RESEND_COOLDOWN)
  }

  const handleVerify = () => {
    if (code.length !== 6) return
    // TODO: 재설정 인증코드 확인 API 연동 예정 (성공했을 때만 이동)
    // 인증 완료 → 새 비밀번호 화면으로 (검증 여부를 state 로 전달)
    navigate('/reset-password', { state: { email, verified: true } })
  }

  const sendLabel =
    cooldown > 0 ? `${cooldown}초` : isSent ? '재전송' : '인증번호 전송'

  return (
    <div className="forgot-page">
      <h1 className="forgot-logo">RE:DAY</h1>
      <p className="forgot-desc">
        비밀번호 재설정을 위해 이메일 인증을 진행해주세요.
      </p>

      <form
        className="forgot-form"
        onSubmit={(event) => event.preventDefault()}
        noValidate
      >
        {/* 이메일 + 인증번호 전송 */}
        <div className="forgot-row">
          <input
            type="email"
            className="forgot-input"
            placeholder="이메일 주소"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            onBlur={() => setEmailTouched(true)}
            autoComplete="email"
          />
          <button
            type="button"
            className="forgot-side-button"
            onClick={handleSendCode}
            disabled={!isEmailOk || cooldown > 0}
          >
            {sendLabel}
          </button>
        </div>

        {/* 안내 문구 (자리 고정: 있든 없든 같은 높이) */}
        <p className="forgot-message" aria-live="polite">
          {showEmailError && (
            <span className="forgot-error">이메일 형식을 확인해주세요.</span>
          )}
          {!showEmailError && isSent && (
            <span className="forgot-hint">인증코드를 메일로 보냈어요.</span>
          )}
        </p>

        {/* 인증번호 + 확인 */}
        <div className="forgot-row">
          <input
            type="text"
            className="forgot-input"
            placeholder="인증번호"
            value={code}
            // 숫자만, 최대 6자리
            onChange={(event) =>
              setCode(event.target.value.replace(/\D/g, '').slice(0, 6))
            }
            inputMode="numeric"
            maxLength={6}
          />
          <button
            type="button"
            className="forgot-side-button"
            onClick={handleVerify}
            disabled={code.length !== 6}
          >
            확인하기
          </button>
        </div>
      </form>

      <p className="forgot-back">
        <Link to="/login" className="forgot-back-link">
          로그인으로 돌아가기
        </Link>
      </p>
    </div>
  )
}

export default ForgotPasswordPage
