import { useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { Eye, EyeOff } from 'lucide-react'
import { authApi } from '../api/auth'
import { getApiErrorMessage } from '../api/client'
import './SignupPage.css'

function SignupPage() {
  const [nickname, setNickname] = useState('')
  const [email, setEmail] = useState('')
  const [code, setCode] = useState('')
  const [password, setPassword] = useState('')
  const [passwordConfirm, setPasswordConfirm] = useState('')
  const [agreeTerms, setAgreeTerms] = useState(false)
  const [isEmailVerified, setIsEmailVerified] = useState(false)
  const [emailTouched, setEmailTouched] = useState(false)
  const [showPassword, setShowPassword] = useState(false)
  const [showPasswordConfirm, setShowPasswordConfirm] = useState(false)

  // 이메일 형식 검증 (인증번호 발송 버튼 활성화 조건)
  const isEmailValid = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)
  // 입력란을 벗어났고(touched), 입력값이 있는데 형식이 틀릴 때만 안내
  const showEmailError = emailTouched && email.length > 0 && !isEmailValid

  // 프론트에서 비밀번호 일치 확인 (확인란을 입력했는데 서로 다를 때만 표시)
  const passwordMismatch =
    passwordConfirm.length > 0 && password !== passwordConfirm

  // 이메일 인증코드 발송
  const sendCodeMutation = useMutation({
    mutationFn: authApi.sendEmailVerification,
  })

  const handleRequestCode = () => {
    sendCodeMutation.mutate({ email })
  }

  // 이메일 인증코드 확인 (성공해야만 인증완료로 잠금, 실패면 버튼 유지)
  const verifyCodeMutation = useMutation({
    mutationFn: authApi.verifyEmailCode,
    onSuccess: () => setIsEmailVerified(true),
  })

  const handleVerifyCode = () => {
    verifyCodeMutation.mutate({ email, verificationCode: code })
  }

  const handleSubmit = (event) => {
    event.preventDefault()
    // TODO: 회원가입 API 연동 예정
  }

  const isSubmitDisabled =
    !nickname ||
    !email ||
    !isEmailVerified ||
    !password ||
    !passwordConfirm ||
    passwordMismatch ||
    !agreeTerms

  return (
    <div className="signup-page">
      <h1 className="signup-logo">RE:DAY</h1>
      <p className="signup-desc">회원가입을 위해 아래 정보들을 입력해주세요.</p>

      <form className="signup-form" onSubmit={handleSubmit} noValidate>
        {/* 닉네임 */}
        <input
          type="text"
          className="signup-input"
          placeholder="닉네임"
          value={nickname}
          onChange={(event) => setNickname(event.target.value)}
        />

        {/* 이메일 + 인증번호 발송 */}
        <div className="signup-row">
          <input
            type="email"
            className="signup-input"
            placeholder="이메일 주소"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            onBlur={() => setEmailTouched(true)}
            autoComplete="email"
          />
          <button
            type="button"
            className="signup-side-button"
            onClick={handleRequestCode}
            disabled={!isEmailValid || sendCodeMutation.isPending}
          >
            인증번호
          </button>
        </div>

        {/* 이메일 형식 안내 (입력란을 벗어났을 때만) */}
        {showEmailError && (
          <p className="signup-error">이메일 형식을 다시 한번 확인해주세요.</p>
        )}

        {/* 인증코드 발송 결과 안내 */}
        {sendCodeMutation.isSuccess && (
          <p className="signup-hint">{sendCodeMutation.data.message}</p>
        )}
        {sendCodeMutation.isError && (
          <p className="signup-error">
            {getApiErrorMessage(sendCodeMutation.error)}
          </p>
        )}

        {/* 인증번호 입력 + 확인 */}
        <div className="signup-row">
          <input
            type="text"
            className="signup-input"
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
            className={
              isEmailVerified
                ? 'signup-side-button signup-side-button--done'
                : 'signup-side-button'
            }
            onClick={handleVerifyCode}
            disabled={isEmailVerified || verifyCodeMutation.isPending || !code}
          >
            {isEmailVerified ? '인증완료' : '확인하기'}
          </button>
        </div>

        {/* 인증코드 확인 결과 안내 */}
        {verifyCodeMutation.isSuccess && (
          <p className="signup-hint">{verifyCodeMutation.data.message}</p>
        )}
        {verifyCodeMutation.isError && (
          <p className="signup-error">
            {getApiErrorMessage(verifyCodeMutation.error)}
          </p>
        )}

        {/* 비밀번호 */}
        <div className="signup-field">
          <input
            type={showPassword ? 'text' : 'password'}
            className="signup-input"
            placeholder="비밀번호"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            autoComplete="new-password"
          />
          <button
            type="button"
            className="signup-toggle"
            onClick={() => setShowPassword((prev) => !prev)}
            aria-label={showPassword ? '비밀번호 숨기기' : '비밀번호 표시'}
          >
            {showPassword ? <EyeOff size={22} /> : <Eye size={22} />}
          </button>
        </div>

        {/* 비밀번호 확인 */}
        <div className="signup-field">
          <input
            type={showPasswordConfirm ? 'text' : 'password'}
            className="signup-input"
            placeholder="비밀번호 확인"
            value={passwordConfirm}
            onChange={(event) => setPasswordConfirm(event.target.value)}
            autoComplete="new-password"
          />
          <button
            type="button"
            className="signup-toggle"
            onClick={() => setShowPasswordConfirm((prev) => !prev)}
            aria-label={
              showPasswordConfirm ? '비밀번호 숨기기' : '비밀번호 표시'
            }
          >
            {showPasswordConfirm ? <EyeOff size={22} /> : <Eye size={22} />}
          </button>
        </div>

        {passwordMismatch && (
          <p className="signup-error">비밀번호가 일치하지 않습니다.</p>
        )}

        {/* 이용약관 동의 */}
        <label className="signup-terms">
          <input
            type="checkbox"
            className="signup-checkbox"
            checked={agreeTerms}
            onChange={(event) => setAgreeTerms(event.target.checked)}
          />
          <span>이용약관 동의</span>
        </label>

        <button
          type="submit"
          className="signup-submit"
          disabled={isSubmitDisabled}
        >
          회원가입
        </button>
      </form>
    </div>
  )
}

export default SignupPage
