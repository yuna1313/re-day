import { ChevronLeft } from 'lucide-react'
import './AuthHeader.css'

// 인증 화면 공통 상단 앱바: 뒤로 가기(좌) + 화면 제목(가운데).
// 로그인처럼 뒤로가 필요 없으면 onBack 없이 title만 전달한다.
function AuthHeader({ title, onBack }) {
  return (
    <div className="auth-header">
      {onBack && (
        <button
          type="button"
          className="auth-back"
          onClick={onBack}
          aria-label="뒤로"
        >
          <ChevronLeft size={26} />
        </button>
      )}
      {title && <h1 className="auth-title">{title}</h1>}
    </div>
  )
}

export default AuthHeader
