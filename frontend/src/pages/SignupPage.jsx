import { useState } from 'react'
import { Eye, EyeOff } from 'lucide-react'
import './SignupPage.css'

function SignupPage() {
  const [nickname, setNickname] = useState('')
  const [email, setEmail] = useState('')
  const [code, setCode] = useState('')
  const [password, setPassword] = useState('')
  const [passwordConfirm, setPasswordConfirm] = useState('')
  const [agreeTerms, setAgreeTerms] = useState(false)
  const [isEmailVerified, setIsEmailVerified] = useState(false)
  const [showPassword, setShowPassword] = useState(false)
  const [showPasswordConfirm, setShowPasswordConfirm] = useState(false)

  // 프론트에서 비밀번호 일치 확인 (확인란을 입력했는데 서로 다를 때만 표시)
  const passwordMismatch =
    passwordConfirm.length > 0 && password !== passwordConfirm

  const handleRequestCode = () => {
    // TODO: 이메일 인증번호 발송 API 연동 예정
  }

  const handleVerifyCode = () => {
    // TODO: 인증번호 확인 API 연동 예정
    // 지금은 화면 흐름 확인용으로 인증완료 상태로만 전환한다.
    setIsEmailVerified(true)
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
            autoComplete="email"
          />
          <button
            type="button"
            className="signup-side-button"
            onClick={handleRequestCode}
          >
            인증번호
          </button>
        </div>

        {/* 인증번호 입력 + 확인 */}
        <div className="signup-row">
          <input
            type="text"
            className="signup-input"
            placeholder="인증번호"
            value={code}
            onChange={(event) => setCode(event.target.value)}
            inputMode="numeric"
          />
          <button
            type="button"
            className={
              isEmailVerified
                ? 'signup-side-button signup-side-button--done'
                : 'signup-side-button'
            }
            onClick={handleVerifyCode}
            disabled={isEmailVerified}
          >
            {isEmailVerified ? '인증완료' : '확인하기'}
          </button>
        </div>

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
