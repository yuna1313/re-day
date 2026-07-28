import { useState } from 'react'
import { DEFER_REASONS, DEFER_CUSTOM_CODE } from '../constants/deferReasons'
import './DeferReasonSheet.css'

const pad = (n) => String(n).padStart(2, '0')

const toDateStr = (d) =>
  `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`

// 오늘 기준 N일 뒤 날짜 문자열
function addDaysStr(days) {
  const d = new Date()
  d.setHours(0, 0, 0, 0)
  d.setDate(d.getDate() + days)
  return toDateStr(d)
}

// 표시용 12시간(오전/오후 HH:MM) → 24시간 HH:MM (time input 기본값)
function scheduleTime24(schedule) {
  if (!schedule?.time || !schedule?.period) return '09:00'
  const [hour12, minute] = schedule.time.split(':')
  const base = Number(hour12) % 12
  const hour24 = schedule.period === '오전' ? base : base + 12
  return `${pad(hour24)}:${minute}`
}

const RESCHEDULE_PRESETS = [
  { label: '내일', days: 1 },
  { label: '3일 뒤', days: 3 },
  { label: '다음 주', days: 7 },
]

// 일정 "미루기" = 다시 할 시간을 정하고(재약속) 이유를 남기는 바텀시트.
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
  const [newDate, setNewDate] = useState(() => addDaysStr(1))
  const [newTime, setNewTime] = useState(() => scheduleTime24(schedule))

  if (!schedule) return null

  const isCustom = selectedCode === DEFER_CUSTOM_CODE
  const deferCount = schedule.deferCount ?? 0
  // 다시 할 시간을 정하고, 이유를 골라야 함(직접 입력이면 상세 필수)
  const canSubmit =
    Boolean(newDate) &&
    Boolean(newTime) &&
    Boolean(selectedCode) &&
    (!isCustom || customDetail.trim())

  const handleSubmit = () => {
    if (!canSubmit || isSubmitting) return
    onDefer({
      deferReasonCode: selectedCode,
      deferReasonDetail: isCustom ? customDetail.trim() : '',
      newStartAt: `${newDate} ${newTime}:00`,
    })
  }

  return (
    <div className="defer-overlay" onClick={onClose}>
      <div
        className="defer-sheet"
        role="dialog"
        aria-modal="true"
        aria-label="미루기"
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
          <h2 className="defer-title">미루기</h2>
          {deferCount > 0 ? (
            <p className="defer-count-note">
              이 일정을 벌써 {deferCount}번 미뤘어요. 이번엔 언제가 좋을까요?
            </p>
          ) : (
            <p className="defer-subtitle">
              다시 할 시간을 정하고 이유를 골라주세요 :)
            </p>
          )}

          {/* 재약속: 언제 다시 할까요? */}
          <div className="defer-section">
            <p className="defer-section-title">언제 다시 할까요?</p>
            <div className="defer-presets">
              {RESCHEDULE_PRESETS.map((preset) => {
                const dateStr = addDaysStr(preset.days)
                return (
                  <button
                    key={preset.label}
                    type="button"
                    className={`defer-preset${newDate === dateStr ? ' active' : ''}`}
                    onClick={() => setNewDate(dateStr)}
                  >
                    {preset.label}
                  </button>
                )
              })}
            </div>
            <div className="defer-datetime">
              <input
                type="date"
                className="defer-dt-input"
                value={newDate}
                onChange={(event) => setNewDate(event.target.value)}
              />
              <input
                type="time"
                className="defer-dt-input"
                value={newTime}
                onChange={(event) => setNewTime(event.target.value)}
              />
            </div>
          </div>

          {/* 미루는 이유 */}
          <div className="defer-section">
            <p className="defer-section-title">미루는 이유</p>
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
