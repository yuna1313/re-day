import { useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { ChevronLeft } from 'lucide-react'
import { useSchedule } from '../hooks/useSchedule'
import { useCreateSchedule } from '../hooks/useCreateSchedule'
import { useUpdateSchedule } from '../hooks/useUpdateSchedule'
import { getApiErrorMessage } from '../api/client'
import './ScheduleFormPage.css'

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
  // 30분 미만이면 30분으로, 이상이면 다음 정시(60 → 자동으로 다음 시각 00분)
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

// 헤더(뒤로 + 제목)
function FormHeader({ isEdit, onBack }) {
  return (
    <header className="form-header">
      <button
        type="button"
        className="form-back"
        onClick={onBack}
        aria-label="뒤로"
      >
        <ChevronLeft size={26} />
      </button>
      <h1 className="form-title">{isEdit ? '일정 수정' : '일정 등록'}</h1>
    </header>
  )
}

// 컨테이너: 수정이면 상세를 조회해 메모까지 채운 뒤 폼을 띄운다.
function ScheduleFormPage() {
  const navigate = useNavigate()
  const { scheduleId } = useParams()
  const isEdit = Boolean(scheduleId)

  // 수정: 상세 조회(상세를 이미 봤다면 캐시 적중으로 즉시). 등록: 조회 안 함.
  const {
    data: initial,
    isError,
    error,
  } = useSchedule(isEdit ? scheduleId : undefined)

  if (!isEdit) {
    return <ScheduleFormInner isEdit={false} initial={null} />
  }

  // 수정 대상 조회 전/실패 처리
  if (!initial) {
    return (
      <div className="form-page">
        <FormHeader isEdit onBack={() => navigate(-1)} />
        {isError ? (
          <p className="form-error">{getApiErrorMessage(error)}</p>
        ) : (
          <FormSkeleton />
        )}
      </div>
    )
  }

  return <ScheduleFormInner isEdit initial={initial} key={initial.id} />
}

function ScheduleFormInner({ isEdit, initial }) {
  const navigate = useNavigate()

  const [title, setTitle] = useState(initial?.title ?? '')
  // 등록: 오늘 날짜 / 다음 정시 기본값. 수정: 기존 값.
  const [date, setDate] = useState(initial?.date ?? todayStr())
  const [time, setTime] = useState(() =>
    initial ? toTimeInput(initial) : nextHalfHourStr(),
  )
  const [estimatedMinutes, setEstimatedMinutes] = useState(
    initial?.estimatedMin != null ? String(initial.estimatedMin) : '',
  )
  const [memo, setMemo] = useState(initial?.memo ?? '')

  const createMutation = useCreateSchedule()
  const updateMutation = useUpdateSchedule()
  const submitMutation = isEdit ? updateMutation : createMutation

  const isSubmitDisabled =
    !title || !date || !time || !estimatedMinutes || submitMutation.isPending

  const handleSubmit = (event) => {
    event.preventDefault()

    const payload = {
      title,
      startAt: buildStartAt(date, time),
      estimatedMinutes: Number(estimatedMinutes),
      memo,
    }

    if (isEdit) {
      updateMutation.mutate(
        { scheduleId: initial.id, ...payload },
        { onSuccess: () => navigate(-1) },
      )
    } else {
      createMutation.mutate(payload, { onSuccess: () => navigate(-1) })
    }
  }

  return (
    <div className="form-page">
      <FormHeader isEdit={isEdit} onBack={() => navigate(-1)} />

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

        {/* 메모 */}
        <div className="form-field">
          <label className="form-label" htmlFor="schedule-memo">
            메모
          </label>
          <input
            id="schedule-memo"
            className="form-input"
            placeholder="메모를 입력해주세요 (선택)"
            value={memo}
            onChange={(event) => setMemo(event.target.value)}
          />
        </div>

        {submitMutation.isError && (
          <p className="form-error">
            {getApiErrorMessage(submitMutation.error)}
          </p>
        )}

        <button
          type="submit"
          className="form-submit"
          disabled={isSubmitDisabled}
        >
          {submitMutation.isPending
            ? isEdit
              ? '수정 중...'
              : '등록 중...'
            : isEdit
              ? '수정'
              : '등록'}
        </button>
      </form>
    </div>
  )
}

// 수정 대상 조회 중 스켈레톤
function FormSkeleton() {
  return (
    <div className="form-body" aria-hidden="true">
      {[0, 1, 2, 3].map((i) => (
        <div className="form-field" key={i}>
          <span
            className="skeleton"
            style={{
              display: 'block',
              width: 70,
              height: 14,
              marginBottom: 10,
            }}
          />
          <span
            className="skeleton"
            style={{ display: 'block', height: 54, borderRadius: 14 }}
          />
        </div>
      ))}
    </div>
  )
}

export default ScheduleFormPage
