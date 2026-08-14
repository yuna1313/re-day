import { useState } from 'react'
import { format, startOfToday, addDays, subDays, isSameDay } from 'date-fns'
import {
  CalendarDays,
  CheckCircle2,
  ChevronLeft,
  ChevronRight,
} from 'lucide-react'
import { useReflection } from '../hooks/useReflection'
import { useCreateReflection } from '../hooks/useCreateReflection'
import { useUpdateReflection } from '../hooks/useUpdateReflection'
import { getApiErrorMessage } from '../api/client'
import './ReflectionPage.css'

const MAX_LENGTH = 1000

// 자기연민 + 미래지향 회고를 돕는 가이드 프롬프트
const REFLECTION_PROMPTS = ['오늘 잘한 점', '아쉬웠던 점', '내일 딱 하나']

const WEEKDAYS = ['일', '월', '화', '수', '목', '금', '토']

const dateLabel = (d) =>
  `${d.getMonth() + 1}월 ${d.getDate()}일 (${WEEKDAYS[d.getDay()]})`

// 'yyyy-MM-dd' → Date (date-fns parse 는 포맷 파서 전체를 끌고 와서 직접 만든다)
function fromDateKey(key) {
  const [year, month, day] = key.split('-').map(Number)
  return new Date(year, month - 1, day)
}

function ReflectionPage() {
  const today = startOfToday()
  // 일기처럼 지난 날짜를 넘겨볼 수 있게 조회 날짜를 상태로 둔다. (앞으로는 오늘까지)
  const [selectedDate, setSelectedDate] = useState(() => startOfToday())
  const selectedKey = format(selectedDate, 'yyyy-MM-dd')
  const todayKey = format(today, 'yyyy-MM-dd')
  const isToday = isSameDay(selectedDate, today)

  // 선택한 날짜의 회고 + 그날 완료한 일정 조회
  const { data, isLoading, isError, error } = useReflection(selectedKey)
  const reflection = data?.reflection ?? null
  const completed = data?.completedSchedules ?? []
  const hasReflection = Boolean(reflection)

  const [isEditing, setIsEditing] = useState(false)
  const [content, setContent] = useState('')
  // 작성 내역이 없으면 바로 작성, 있으면 조회(보기) → 수정하기 눌러야 편집
  const showEditor = !hasReflection || isEditing

  const createMutation = useCreateReflection()
  const updateMutation = useUpdateReflection()
  const submitMutation = hasReflection ? updateMutation : createMutation

  // 날짜를 옮기면 이전 날짜에 쓰던 입력이 남지 않도록 편집 상태를 초기화한다.
  const goToDate = (nextDate) => {
    setSelectedDate(nextDate)
    setIsEditing(false)
    setContent('')
    createMutation.reset()
    updateMutation.reset()
  }

  const startEditing = () => {
    setContent(reflection?.content ?? '')
    updateMutation.reset()
    setIsEditing(true)
  }

  const cancelEditing = () => {
    setIsEditing(false)
    updateMutation.reset()
  }

  // 프롬프트 칩을 누르면 현재 내용 끝에 시작 문장을 붙여준다.
  const insertPrompt = (label) => {
    setContent((prev) => {
      const base = prev.replace(/\s*$/, '')
      return base ? `${base}\n${label}: ` : `${label}: `
    })
  }

  const handleSubmit = () => {
    if (!content.trim() || submitMutation.isPending) return

    if (hasReflection) {
      updateMutation.mutate(
        { reflectionId: reflection.reflectionId, content: content.trim() },
        { onSuccess: () => setIsEditing(false) },
      )
    } else {
      createMutation.mutate(
        { reflectionDate: selectedKey, content: content.trim() },
        {
          onSuccess: () => {
            setIsEditing(false)
            setContent('')
          },
        },
      )
    }
  }

  return (
    <div className="reflection-page">
      <header className="reflection-header">
        <h1 className="reflection-title">회고</h1>
      </header>

      {/* 날짜 이동 (다음 날은 오늘까지만) */}
      <div className="reflection-nav">
        <div className="reflection-date-group">
          <button
            type="button"
            className="reflection-arrow"
            onClick={() => goToDate(subDays(selectedDate, 1))}
            aria-label="이전 날"
          >
            <ChevronLeft size={20} />
          </button>
          {/* 날짜를 누르면 기본 날짜 선택기가 열린다.
              (라벨 위에 투명한 input 을 겹쳐 두고, 눌렀을 때 선택기를 직접 띄운다) */}
          <span className="reflection-date-picker">
            <span className="reflection-date">
              <CalendarDays size={15} className="reflection-date-icon" />
              {isToday ? '오늘' : dateLabel(selectedDate)}
            </span>
            <input
              type="date"
              className="reflection-date-input"
              value={selectedKey}
              max={todayKey}
              onClick={(event) => {
                // 데스크톱은 날짜 입력을 눌러도 달력이 열리지 않아 직접 띄운다.
                // (모바일은 탭만으로 열리므로 이미 열려 있으면 예외가 날 수 있어 무시)
                try {
                  event.currentTarget.showPicker?.()
                } catch {
                  /* 선택기를 띄울 수 없는 환경은 그대로 둔다 */
                }
              }}
              onChange={(event) => {
                const nextKey = event.target.value
                if (!nextKey) return
                goToDate(fromDateKey(nextKey))
              }}
              aria-label="날짜 선택"
            />
          </span>
          <button
            type="button"
            className="reflection-arrow"
            onClick={() => goToDate(addDays(selectedDate, 1))}
            disabled={isToday}
            aria-label="다음 날"
          >
            <ChevronRight size={20} />
          </button>
        </div>
        <button
          type="button"
          className="reflection-today-btn"
          onClick={() => goToDate(today)}
          disabled={isToday}
        >
          오늘
        </button>
      </div>

      {/* 그날 완료한 일정 */}
      <section className="reflection-section">
        <h2 className="reflection-heading">
          {isToday ? '오늘 완료한 일정' : '이날 완료한 일정'}
        </h2>

        {isLoading ? (
          <ul className="completed-list" aria-hidden="true">
            {[0, 1].map((i) => (
              <li className="completed-item" key={i}>
                <span className="skeleton refl-skel-check" />
                <span className="skeleton refl-skel-title" />
              </li>
            ))}
          </ul>
        ) : completed.length === 0 ? (
          <p className="reflection-empty">
            {isToday
              ? '오늘은 천천히 가도 괜찮아요. 내일 다시 시작하면 돼요 🙂'
              : '이날 완료한 일정이 없어요.'}
          </p>
        ) : (
          <ul className="completed-list">
            {completed.map((item) => (
              <li className="completed-item" key={item.scheduleId}>
                <CheckCircle2 size={20} fill="#3b82f6" color="#fff" />
                <span className="completed-title">{item.title}</span>
              </li>
            ))}
          </ul>
        )}
      </section>

      {/* 그날의 회고 */}
      <section className="reflection-section">
        <h2 className="reflection-heading">
          {isToday ? '오늘의 회고' : '이날의 회고'}
        </h2>

        {isLoading ? (
          <div className="skeleton refl-skel-editor" />
        ) : isError ? (
          <p className="reflection-error">{getApiErrorMessage(error)}</p>
        ) : showEditor ? (
          <>
            {!hasReflection && (
              <p className="reflection-subtext">
                {isToday
                  ? '오늘 하루, 스스로에게 편하게 한마디 남겨볼까요? 😊'
                  : '지나간 날이지만, 지금 떠오르는 대로 남겨도 괜찮아요 😊'}
              </p>
            )}

            {/* 자기연민+미래지향 가이드 프롬프트 (누르면 시작 문장 삽입) */}
            <div className="reflection-prompts">
              {REFLECTION_PROMPTS.map((label) => (
                <button
                  key={label}
                  type="button"
                  className="reflection-prompt"
                  onClick={() => insertPrompt(label)}
                >
                  {label}
                </button>
              ))}
            </div>

            <div className="reflection-textarea-wrap">
              <textarea
                className="reflection-textarea"
                placeholder="잘한 점, 아쉬운 점, 내일 시도할 것을 편하게 적어보세요. 완벽하지 않아도 괜찮아요."
                value={content}
                maxLength={MAX_LENGTH}
                onChange={(event) => setContent(event.target.value)}
              />
              <span className="reflection-counter">
                {content.length} / {MAX_LENGTH}
              </span>
            </div>

            {submitMutation.isError && (
              <p className="reflection-error">
                {getApiErrorMessage(submitMutation.error)}
              </p>
            )}

            {hasReflection ? (
              <div className="reflection-btn-row">
                <button
                  type="button"
                  className="reflection-cancel"
                  onClick={cancelEditing}
                  disabled={submitMutation.isPending}
                >
                  취소
                </button>
                <button
                  type="button"
                  className="reflection-submit"
                  onClick={handleSubmit}
                  disabled={!content.trim() || submitMutation.isPending}
                >
                  {submitMutation.isPending ? '수정 중...' : '수정 완료'}
                </button>
              </div>
            ) : (
              <button
                type="button"
                className="reflection-submit"
                onClick={handleSubmit}
                disabled={!content.trim() || submitMutation.isPending}
              >
                {submitMutation.isPending ? '작성 중...' : '작성하기'}
              </button>
            )}
          </>
        ) : (
          /* 작성 내역이 있으면 내용만 보여주고 '수정하기' */
          <>
            <div className="reflection-view">{reflection.content}</div>
            <button
              type="button"
              className="reflection-submit"
              onClick={startEditing}
            >
              수정하기
            </button>
          </>
        )}
      </section>
    </div>
  )
}

export default ReflectionPage
