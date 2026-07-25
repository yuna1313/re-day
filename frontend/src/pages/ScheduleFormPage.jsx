import { useState } from 'react'
import { useNavigate, useLocation, useParams, Navigate } from 'react-router-dom'
import { ChevronLeft, ChevronUp, ChevronDown } from 'lucide-react'
import { useCreateSchedule } from '../hooks/useCreateSchedule'
import { useUpdateSchedule } from '../hooks/useUpdateSchedule'
import { getApiErrorMessage } from '../api/client'
import './ScheduleFormPage.css'

// 폼의 날짜 + 시간(12h) → 'yyyy-MM-dd HH:mm:ss'
function buildStartAt(date, time) {
  const hour24 = time.ampm === 'AM' ? time.hour % 12 : (time.hour % 12) + 12
  const pad = (n) => String(n).padStart(2, '0')
  return `${date} ${pad(hour24)}:${pad(time.minute)}:00`
}

// 기존 일정(수정)에서 초기 시간 파싱. 없으면 오전 12:00.
function parseInitialTime(schedule) {
  if (schedule?.time && schedule?.period) {
    const [h, m] = schedule.time.split(':').map(Number)
    return {
      hour: h,
      minute: m,
      ampm: schedule.period === '오전' ? 'AM' : 'PM',
    }
  }
  return { hour: 12, minute: 0, ampm: 'AM' }
}

function ScheduleFormPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const { scheduleId } = useParams()
  const isEdit = Boolean(scheduleId)
  const schedule = location.state?.schedule

  const [title, setTitle] = useState(schedule?.title ?? '')
  const [date, setDate] = useState(schedule?.date ?? '')
  const [time, setTime] = useState(() => parseInitialTime(schedule))
  const [estimatedMinutes, setEstimatedMinutes] = useState(
    schedule?.estimatedMin != null ? String(schedule.estimatedMin) : '',
  )
  const [memo, setMemo] = useState(schedule?.memo ?? '')
  const [showTimePicker, setShowTimePicker] = useState(false)

  const createMutation = useCreateSchedule()
  const updateMutation = useUpdateSchedule()
  const submitMutation = isEdit ? updateMutation : createMutation

  // 수정인데 데이터가 없으면(직접 접근/새로고침) 홈으로
  // TODO: 상세 조회 API(GET /schedules/{id}) 연동 시 직접 조회로 대체
  if (isEdit && !schedule) {
    return <Navigate to="/" replace />
  }

  const timeLabel = `${time.ampm === 'AM' ? '오전' : '오후'} ${String(
    time.hour,
  ).padStart(2, '0')}:${String(time.minute).padStart(2, '0')}`

  const isSubmitDisabled =
    !title || !date || !estimatedMinutes || submitMutation.isPending

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
        { scheduleId: schedule.id, ...payload },
        { onSuccess: () => navigate(-1) },
      )
    } else {
      createMutation.mutate(payload, { onSuccess: () => navigate(-1) })
    }
  }

  return (
    <div className="form-page">
      <header className="form-header">
        <button
          type="button"
          className="form-back"
          onClick={() => navigate(-1)}
          aria-label="뒤로"
        >
          <ChevronLeft size={26} />
        </button>
        <h1 className="form-title">{isEdit ? '일정 수정' : '일정 등록'}</h1>
      </header>

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

        {/* 시작 시간 (커스텀 스테퍼) */}
        <div className="form-field">
          <label className="form-label">시작 시간</label>
          <button
            type="button"
            className="form-input form-time-field"
            onClick={() => setShowTimePicker((v) => !v)}
          >
            {timeLabel}
          </button>
          {showTimePicker && <TimeStepper time={time} onChange={setTime} />}
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

// 시:분 AM/PM 스테퍼
function TimeStepper({ time, onChange }) {
  const { hour, minute, ampm } = time
  const changeHour = (delta) =>
    onChange({ ...time, hour: ((hour - 1 + delta + 12) % 12) + 1 })
  const changeMinute = (delta) =>
    onChange({ ...time, minute: (minute + delta + 60) % 60 })
  const toggleAmpm = () =>
    onChange({ ...time, ampm: ampm === 'AM' ? 'PM' : 'AM' })

  return (
    <div className="time-stepper">
      <div className="ts-left">
        <div className="ts-col">
          <button
            type="button"
            className="ts-btn"
            onClick={() => changeHour(1)}
            aria-label="시 올리기"
          >
            <ChevronUp size={18} />
          </button>
          <span className="ts-value">{String(hour).padStart(2, '0')}</span>
          <button
            type="button"
            className="ts-btn"
            onClick={() => changeHour(-1)}
            aria-label="시 내리기"
          >
            <ChevronDown size={18} />
          </button>
        </div>

        <span className="ts-colon">:</span>

        <div className="ts-col">
          <button
            type="button"
            className="ts-btn"
            onClick={() => changeMinute(1)}
            aria-label="분 올리기"
          >
            <ChevronUp size={18} />
          </button>
          <span className="ts-value">{String(minute).padStart(2, '0')}</span>
          <button
            type="button"
            className="ts-btn"
            onClick={() => changeMinute(-1)}
            aria-label="분 내리기"
          >
            <ChevronDown size={18} />
          </button>
        </div>
      </div>

      <div className="ts-col">
        <button
          type="button"
          className="ts-btn"
          onClick={toggleAmpm}
          aria-label="오전/오후 올리기"
        >
          <ChevronUp size={18} />
        </button>
        <span className="ts-value">{ampm}</span>
        <button
          type="button"
          className="ts-btn"
          onClick={toggleAmpm}
          aria-label="오전/오후 내리기"
        >
          <ChevronDown size={18} />
        </button>
      </div>
    </div>
  )
}

export default ScheduleFormPage
