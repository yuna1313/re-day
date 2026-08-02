import { useState } from 'react'
import './ScheduleFormFields.css'

const pad = (n) => String(n).padStart(2, '0')

// 폼의 날짜 + 시간('HH:mm') → 'yyyy-MM-dd HH:mm:ss'
function buildStartAt(date, time) {
  return `${date} ${time}:00`
}

// 오늘 날짜 'yyyy-MM-dd' (등록 기본값)
function todayStr() {
  const d = new Date()
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

// 다음 30분 단위 'HH:mm' (등록 기본값, 예: 14:10 → 14:30, 14:37 → 15:00)
function nextHalfHourStr() {
  const d = new Date()
  d.setSeconds(0, 0)
  d.setMinutes(d.getMinutes() < 30 ? 30 : 60)
  return `${pad(d.getHours())}:${pad(d.getMinutes())}`
}

// 기존 일정(수정)의 표시용 12시간(오전/오후 HH:mm) → time input용 24시간 'HH:mm'
function toTimeInput(schedule) {
  if (!schedule?.time || !schedule?.period) return nextHalfHourStr()
  const [hour12, minute] = schedule.time.split(':')
  const base = Number(hour12) % 12
  const hour24 = schedule.period === '오전' ? base : base + 12
  return `${pad(hour24)}:${minute}`
}

// 일정 등록/수정 공용 입력 필드 + 제출 버튼.
// 상태는 내부에서 관리하고, 제출 시 onSubmit(payload)만 부모에 전달한다.
function ScheduleFormFields({
  initial,
  onSubmit,
  isPending,
  errorMessage,
  submitLabel,
  submittingLabel,
  suggestions,
}) {
  const [title, setTitle] = useState(initial?.title ?? '')
  // 등록: 오늘 날짜 / 다음 30분 기본값. 수정: 기존 값.
  const [date, setDate] = useState(initial?.date ?? todayStr())
  const [time, setTime] = useState(() =>
    initial ? toTimeInput(initial) : nextHalfHourStr(),
  )
  const [estimatedMinutes, setEstimatedMinutes] = useState(
    initial?.estimatedMin != null ? String(initial.estimatedMin) : '',
  )
  const [memo, setMemo] = useState(initial?.memo ?? '')

  const isDisabled = !title || !date || !time || !estimatedMinutes || isPending

  const handleSubmit = (event) => {
    event.preventDefault()
    if (isDisabled) return
    onSubmit({
      title,
      startAt: buildStartAt(date, time),
      estimatedMinutes: Number(estimatedMinutes),
      memo,
    })
  }

  return (
    <form className="form-body" onSubmit={handleSubmit} noValidate>
      {/* 일정 제목 */}
      <div className="form-field">
        <label className="form-label" htmlFor="schedule-title">
          일정 제목
        </label>
        <input
          id="schedule-title"
          className="form-input"
          placeholder="일정 제목을 입력해주세요"
          value={title}
          onChange={(event) => setTitle(event.target.value)}
        />
        {/* 빠른 추가: 최근 제목/추천 (누르면 제목 채움) */}
        {suggestions?.length > 0 && (
          <div className="form-suggestions">
            {suggestions.map((suggestion) => (
              <button
                key={suggestion}
                type="button"
                className="form-suggestion"
                onClick={() => setTitle(suggestion)}
              >
                {suggestion}
              </button>
            ))}
          </div>
        )}
      </div>

      {/* 시작 일자 */}
      <div className="form-field">
        <label className="form-label" htmlFor="schedule-date">
          시작 일자
        </label>
        <input
          id="schedule-date"
          type="date"
          className="form-input"
          value={date}
          onChange={(event) => setDate(event.target.value)}
        />
      </div>

      {/* 시작 시간 (네이티브 시간 피커) */}
      <div className="form-field">
        <label className="form-label" htmlFor="schedule-time">
          시작 시간
        </label>
        <input
          id="schedule-time"
          type="time"
          className="form-input"
          value={time}
          onChange={(event) => setTime(event.target.value)}
        />
      </div>

      {/* 예상 시간 */}
      <div className="form-field">
        <label className="form-label" htmlFor="schedule-est">
          예상 시간
        </label>
        <div className="form-est-wrap">
          <input
            id="schedule-est"
            className="form-input form-est-input"
            inputMode="numeric"
            placeholder="예: 30"
            value={estimatedMinutes}
            onChange={(event) =>
              setEstimatedMinutes(
                event.target.value.replace(/\D/g, '').slice(0, 4),
              )
            }
          />
          <span className="form-est-unit">분</span>
        </div>
      </div>

      {/* 첫 단계 (실행의도 — 착수 저항 낮추기) */}
      <div className="form-field">
        <label className="form-label" htmlFor="schedule-memo">
          첫 단계 (선택)
        </label>
        <input
          id="schedule-memo"
          className="form-input"
          placeholder="예: 운동화 신기 / 자료 폴더 열기"
          value={memo}
          onChange={(event) => setMemo(event.target.value)}
        />
        <p className="form-hint">
          가장 작은 첫 행동을 적어두면 시작이 훨씬 쉬워져요.
        </p>
      </div>

      {errorMessage && <p className="form-error">{errorMessage}</p>}

      <button type="submit" className="form-submit" disabled={isDisabled}>
        {isPending ? submittingLabel : submitLabel}
      </button>
    </form>
  )
}

export default ScheduleFormFields
