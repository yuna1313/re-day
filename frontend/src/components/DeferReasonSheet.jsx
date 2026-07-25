import { useState } from 'react'
import { DEFER_REASONS, DEFER_CUSTOM_CODE } from '../constants/deferReasons'
import './DeferReasonSheet.css'

// 일정 "미루기" 시 이유를 선택받는 바텀시트.
// schedule 이 있으면 열림, null 이면 닫힘(상위에서 조건부 렌더).
function DeferReasonSheet({
  schedule,
  onClose,
  onDefer,
  isSubmitting,
  errorMessage,
}) {
  const [selectedCode, setSelectedCode] = useState(null)
  const [customDetail, setCustomDetail] = useState('')

  if (!schedule) return null

  const isCustom = selectedCode === DEFER_CUSTOM_CODE
  // 이유를 골라야 하고, 직접 입력이면 상세 내용이 있어야 함
  const canSubmit = Boolean(selectedCode) && (!isCustom || customDetail.trim())

  const handleSubmit = () => {
    if (!canSubmit || isSubmitting) return
    onDefer({
      deferReasonCode: selectedCode,
      deferReasonDetail: isCustom ? customDetail.trim() : '',
    })
  }

  return (
    <div className="defer-overlay" onClick={onClose}>
      <div
        className="defer-sheet"
        role="dialog"
        aria-modal="true"
        aria-label="미루기 이유"
        onClick={(event) => event.stopPropagation()}
      >
        {/* 상단 그래버 (탭하면 닫힘) */}
        <button
          type="button"
          className="defer-grabber"
          onClick={onClose}
          aria-label="닫기"
        >
          <span className="defer-grabber-bar" />
        </button>

        <div className="defer-body">
          <h2 className="defer-title">미루기 이유</h2>
          <p className="defer-subtitle">미루기 이유를 한 개 선택해주세요 :)</p>

          <div className="defer-options">
            {DEFER_REASONS.map((reason) => (
              <label key={reason.code} className="defer-option">
                <input
                  type="radio"
                  name="defer-reason"
                  className="defer-radio"
                  checked={selectedCode === reason.code}
                  onChange={() => setSelectedCode(reason.code)}
                />
                <span>{reason.label}</span>
              </label>
            ))}

            {/* 직접 입력 */}
            <label className="defer-option">
              <input
                type="radio"
                name="defer-reason"
                className="defer-radio"
                checked={isCustom}
                onChange={() => setSelectedCode(DEFER_CUSTOM_CODE)}
              />
              <span>직접 입력</span>
            </label>

            {isCustom && (
              <input
                className="defer-custom-input"
                placeholder="미루기 이유를 입력해주세요"
                value={customDetail}
                onChange={(event) => setCustomDetail(event.target.value)}
              />
            )}
          </div>
        </div>

        {errorMessage && <p className="defer-error">{errorMessage}</p>}

        <button
          type="button"
          className="defer-submit"
          onClick={handleSubmit}
          disabled={!canSubmit || isSubmitting}
        >
          {isSubmitting ? '미루는 중...' : '미루기'}
        </button>
      </div>
    </div>
  )
}

export default DeferReasonSheet
