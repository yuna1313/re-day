import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useMutation } from '@tanstack/react-query'
import { authApi } from '../api/auth'
import { getApiErrorMessage } from '../api/client'
import { isValidEmail } from '../utils/validators'
import AuthHeader from '../components/AuthHeader'
import './ForgotPasswordPage.css'

// 재전송까지 대기 시간(초). 버튼 연타/반복 요청 방지용.
const RESEND_COOLDOWN = 60

function ForgotPasswordPage() {
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [emailTouched, setEmailTouched] = useState(false)
  const [code, setCode] = useState('')
  const [cooldown, setCooldown] = useState(0)

  const isEmailOk = isValidEmail(email)
  const showEmailError = emailTouched && email.length > 0 && !isEmailOk

  // 인증코드 발송
  const sendCodeMutation = useMutation({
    mutationFn: authApi.sendPasswordResetCode,
    onSuccess: () => setCooldown(RESEND_COOLDOWN),
  })

  // 재전송 쿨다운 카운트다운 (1초마다 감소)
  useEffect(() => {
    if (cooldown <= 0) return
    const timer = setTimeout(() => setCooldown((prev) => prev - 1), 1000)
    return () => clearTimeout(timer)
  }, [cooldown])

  const handleSendCode = () => {
    if (!isEmailOk || cooldown > 0 || sendCodeMutation.isPending) return
    sendCodeMutation.mutate({ email })
  }

  // 인증코드 확인 (성공했을 때만 새 비밀번호 화면으로 이동, 실패면 버튼 유지)
  const verifyCodeMutation = useMutation({
    mutationFn: authApi.verifyPasswordResetCode,
    onSuccess: () => {
      // 서버가 인증 완료 상태를 저장했으므로 이메일만 다음 화면으로 넘긴다.
      navigate('/reset-password', { state: { email, verified: true } })
    },
  })

  const handleVerify = () => {
    if (code.length !== 6) return
    verifyCodeMutation.mutate({ email, verificationCode: code })
  }

  const sendLabel =
    cooldown > 0
      ? `${cooldown}초`
      : sendCodeMutation.isPending
        ? '전송 중'
        : sendCodeMutation.isSuccess
          ? '재전송'
          : '인증번호 전송'

  return (
    <div className="forgot-page">
      <AuthHeader title="비밀번호 찾기" onBack={() => navigate(-1)} />
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
            disabled={!isEmailOk || cooldown > 0 || sendCodeMutation.isPending}
          >
            {sendLabel}
          </button>
        </div>

        {/* 안내 문구 (자리 고정) */}
        <p className="forgot-message" aria-live="polite">
          {showEmailError && (
            <span className="forgot-error">이메일 형식을 확인해주세요.</span>
          )}
          {!showEmailError && sendCodeMutation.isError && (
            <span className="forgot-error">
              {getApiErrorMessage(sendCodeMutation.error)}
            </span>
          )}
          {!showEmailError && sendCodeMutation.isSuccess && (
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
            disabled={code.length !== 6 || verifyCodeMutation.isPending}
          >
            확인하기
          </button>
        </div>

        {/* 인증코드 확인 실패 안내 (성공 시엔 다음 화면으로 이동) */}
        <p className="forgot-message" aria-live="polite">
          {verifyCodeMutation.isError && (
            <span className="forgot-error">
              {getApiErrorMessage(verifyCodeMutation.error)}
            </span>
          )}
        </p>
      </form>
    </div>
  )
}

export default ForgotPasswordPage
