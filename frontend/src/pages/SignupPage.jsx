import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useMutation } from '@tanstack/react-query'
import { Eye, EyeOff } from 'lucide-react'
import AuthHeader from '../components/AuthHeader'
import { authApi } from '../api/auth'
import { getApiErrorMessage } from '../api/client'
import { AGREEMENTS } from '../constants/agreements'
import { isValidEmail } from '../utils/validators'
import AgreementSheet from '../components/AgreementSheet'
import './SignupPage.css'

function SignupPage() {
  const navigate = useNavigate()
  const [nickname, setNickname] = useState('')
  const [email, setEmail] = useState('')
  const [code, setCode] = useState('')
  const [password, setPassword] = useState('')
  const [passwordConfirm, setPasswordConfirm] = useState('')
  const [isEmailVerified, setIsEmailVerified] = useState(false)
  const [emailTouched, setEmailTouched] = useState(false)
  const [showPassword, setShowPassword] = useState(false)
  const [showPasswordConfirm, setShowPasswordConfirm] = useState(false)
  // 약관별 동의 상태 { terms: false, privacy: false, marketing: false }
  const [agreements, setAgreements] = useState(() =>
    Object.fromEntries(AGREEMENTS.map((item) => [item.key, false])),
  )
  // '보기'로 상세를 띄울 약관 (null 이면 닫힘)
  const [viewingAgreement, setViewingAgreement] = useState(null)

  const isAllAgreed = AGREEMENTS.every((item) => agreements[item.key])
  const isAllRequiredAgreed = AGREEMENTS.filter((item) => item.required).every(
    (item) => agreements[item.key],
  )

  const handleToggleAll = (event) => {
    const { checked } = event.target
    setAgreements(
      Object.fromEntries(AGREEMENTS.map((item) => [item.key, checked])),
    )
  }

  const handleToggleAgreement = (key) => {
    setAgreements((prev) => ({ ...prev, [key]: !prev[key] }))
  }

  // 이메일 형식 검증 (인증번호 발송 버튼 활성화 조건)
  const isEmailValid = isValidEmail(email)
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

  // 회원가입
  const signupMutation = useMutation({
    mutationFn: authApi.signup,
    onSuccess: () => {
      // 가입 완료 후 로그인 화면으로 이동 (가입 응답에는 토큰이 없음)
      navigate('/login', { replace: true })
    },
  })

  const handleSubmit = (event) => {
    event.preventDefault()
    signupMutation.mutate({
      nickname,
      email,
      password,
      passwordConfirm,
      // API 의 agreeTerms 는 단일 boolean → 필수 약관 동의 여부를 전달
      agreeTerms: isAllRequiredAgreed,
    })
  }

  const isSubmitDisabled =
    !nickname ||
    !email ||
    !isEmailVerified ||
    !password ||
    !passwordConfirm ||
    passwordMismatch ||
    !isAllRequiredAgreed

  return (
    <div className="signup-page">
      <AuthHeader title="회원가입" onBack={() => navigate(-1)} />

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
            {sendCodeMutation.isPending
              ? '전송 중...'
              : sendCodeMutation.isSuccess
                ? '재전송'
                : '인증번호 전송'}
          </button>
        </div>

        {/* 이메일 형식 안내 (입력란을 벗어났을 때만) */}
        {showEmailError && (
          <p className="signup-error">이메일 형식을 다시 한번 확인해주세요.</p>
        )}

        {/* 인증코드 발송 상태 안내 (전송 중 / 성공 / 실패) */}
        {sendCodeMutation.isPending && (
          <p className="signup-hint">인증번호를 보내는 중이에요...</p>
        )}
        {sendCodeMutation.isSuccess && (
          <p className="signup-hint">
            인증코드를 메일로 보냈어요. 안 보이면 스팸함도 확인해주세요.
          </p>
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
            {isEmailVerified
              ? '인증완료'
              : verifyCodeMutation.isPending
                ? '확인 중...'
                : '확인하기'}
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

        {/* 약관 동의 */}
        <div className="signup-agreements">
          {/* 전체 동의 */}
          <label className="signup-agree-all">
            <input
              type="checkbox"
              className="signup-checkbox"
              checked={isAllAgreed}
              onChange={handleToggleAll}
            />
            <span>전체 동의</span>
          </label>

          {/* 개별 항목 */}
          {AGREEMENTS.map((item) => (
            <div className="signup-agree-item" key={item.key}>
              <label className="signup-agree-label">
                <input
                  type="checkbox"
                  className="signup-checkbox"
                  checked={agreements[item.key]}
                  onChange={() => handleToggleAgreement(item.key)}
                />
                <span>
                  <span className="signup-agree-tag">
                    [{item.required ? '필수' : '선택'}]
                  </span>{' '}
                  {item.label}
                </span>
              </label>
              <button
                type="button"
                className="signup-agree-view"
                onClick={() => setViewingAgreement(item)}
              >
                보기
              </button>
            </div>
          ))}
        </div>

        {/* 회원가입 실패 안내 */}
        {signupMutation.isError && (
          <p className="signup-error">
            {getApiErrorMessage(signupMutation.error)}
          </p>
        )}

        <button
          type="submit"
          className="signup-submit"
          disabled={isSubmitDisabled || signupMutation.isPending}
        >
          {signupMutation.isPending ? '회원가입 중...' : '회원가입'}
        </button>
      </form>

      <AgreementSheet
        agreement={viewingAgreement}
        onClose={() => setViewingAgreement(null)}
      />
    </div>
  )
}

export default SignupPage
