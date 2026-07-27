import { X } from 'lucide-react'
import './AgreementSheet.css'

// 약관 상세를 화면 하단에서 올라오는 바텀시트로 보여준다. (모바일 우선)
function AgreementSheet({ agreement, onClose }) {
  if (!agreement) return null

  return (
    <div className="sheet-overlay" onClick={onClose}>
      <div
        className="sheet"
        role="dialog"
        aria-modal="true"
        aria-label={agreement.label}
        onClick={(event) => event.stopPropagation()}
      >
        <div className="sheet-header">
          <h2 className="sheet-title">{agreement.label}</h2>
          <button
            type="button"
            className="sheet-close"
            onClick={onClose}
            aria-label="닫기"
          >
            <X size={22} />
          </button>
        </div>
        <div className="sheet-body">{agreement.content}</div>
      </div>
    </div>
  )
}

export default AgreementSheet
