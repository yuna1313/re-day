import { useState } from 'react'
import './ScheduleCompleteSheet.css'

// 일정 "완료" 시 실제 소요 시간을 입력받는 바텀시트.
// schedule 이 있으면 열림, null 이면 닫힘(상위에서 조건부 렌더).
function ScheduleCompleteSheet({
  schedule,
  onClose,
  onComplete,
  isSubmitting,
  errorMessage,
}) {
  const [actualMinutes, setActualMinutes] = useState('')

  if (!schedule) return null

  const handleSubmit = () => {
    if (!actualMinutes || isSubmitting) return
    onComplete(Number(actualMinutes))
  }

  return (
    <div className="complete-overlay" onClick={onClose}>
      <div
        className="complete-sheet"
        role="dialog"
        aria-modal="true"
        aria-label="일정 완료"
        onClick={(event) => event.stopPropagation()}
      >
        {/* 상단 그래버 (탭하면 닫힘) */}
        <button
          type="button"
          className="complete-grabber"
          onClick={onClose}
          aria-label="닫기"
        >
          <span className="complete-grabber-bar" />
        </button>

        <div className="complete-body">
          <h2 className="complete-title">일정 완료</h2>

          <p className="complete-name">{schedule.title}</p>
          <p className="complete-est">예상 시간 {schedule.estimatedMin}분</p>

          <hr className="complete-divider" />

          <p className="complete-label">실제 소요 시간을 입력해주세요.</p>
          <div className="complete-input-row">
            <input
              className="complete-input"
              type="text"
              inputMode="numeric"
              placeholder="예: 25"
              value={actualMinutes}
              maxLength={4}
              // 숫자만, 최대 4자리(9999분)
              onChange={(event) =>
                setActualMinutes(
                  event.target.value.replace(/\D/g, '').slice(0, 4),
                )
              }
            />
            <span className="complete-unit">분</span>
          </div>
        </div>

        {errorMessage && <p className="complete-error">{errorMessage}</p>}

        <button
          type="button"
          className="complete-submit"
          onClick={handleSubmit}
          disabled={!actualMinutes || isSubmitting}
        >
          {isSubmitting ? '완료 처리 중...' : '완료하기'}
        </button>
      </div>
    </div>
  )
}

export default ScheduleCompleteSheet
