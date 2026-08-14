import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  startOfToday,
  startOfWeek,
  startOfMonth,
  endOfMonth,
  addDays,
  subDays,
  addMonths,
  subMonths,
  isSameDay,
  isSameMonth,
  isToday,
} from 'date-fns'
import {
  Plus,
  ChevronLeft,
  ChevronRight,
  CheckCircle2,
  Search,
} from 'lucide-react'
import { useSchedules } from '../hooks/useSchedules'
import { useOverdueSchedules } from '../hooks/useOverdueSchedules'
import { useCompleteSchedule } from '../hooks/useCompleteSchedule'
import { useDeferSchedule } from '../hooks/useDeferSchedule'
import { getApiErrorMessage } from '../api/client'
import ScheduleCompleteSheet from '../components/ScheduleCompleteSheet'
import DeferReasonSheet from '../components/DeferReasonSheet'
import ScheduleFormSheet from '../components/ScheduleFormSheet'
import './HomePage.css'

const WEEKDAYS = ['월', '화', '수', '목', '금', '토', '일']

const dateKey = (d) =>
  `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(
    d.getDate(),
  ).padStart(2, '0')}`

const monthDay = (d) => `${d.getMonth() + 1}월 ${d.getDate()}일`

// 빠른 추가 제안: 최근 사용한 제목 우선, 부족하면 기본 예시로 채움
const SUGGESTION_LIMIT = 6
const DEFAULT_SUGGESTIONS = ['운동', '공부', '독서', '산책']

function buildTitleSuggestions(byDate) {
  const items = Object.values(byDate).flat()
  // 최근 날짜 순
  const sorted = [...items].sort((a, b) => (a.date < b.date ? 1 : -1))
  const seen = new Set()
  const result = []
  for (const item of sorted) {
    const title = item.title?.trim()
    if (title && !seen.has(title)) {
      seen.add(title)
      result.push(title)
    }
    if (result.length >= SUGGESTION_LIMIT) break
  }
  for (const preset of DEFAULT_SUGGESTIONS) {
    if (result.length >= SUGGESTION_LIMIT) break
    if (!seen.has(preset)) {
      seen.add(preset)
      result.push(preset)
    }
  }
  return result
}

function HomePage() {
  const navigate = useNavigate()
  const [view, setView] = useState('week') // 'week' | 'month'
  const [selectedDate, setSelectedDate] = useState(() => startOfToday())
  // '완료' 클릭한 일정 (null 이면 완료 시트 닫힘)
  const [completingSchedule, setCompletingSchedule] = useState(null)
  // '미루기' 클릭한 일정 (null 이면 미루기 시트 닫힘)
  const [deferringSchedule, setDeferringSchedule] = useState(null)
  // 일정 등록 시트 열림 여부
  const [showCreateSheet, setShowCreateSheet] = useState(false)

  // 주간용
  const weekStart = startOfWeek(selectedDate, { weekStartsOn: 1 })
  const weekDays = Array.from({ length: 7 }, (_, i) => addDays(weekStart, i))
  const rangeLabel = `${monthDay(weekStart)} ~ ${monthDay(addDays(weekStart, 6))}`

  // 월간용: 해당 월의 1일이 포함된 주의 월요일부터 6주(42칸)
  const monthStart = startOfMonth(selectedDate)
  const gridStart = startOfWeek(monthStart, { weekStartsOn: 1 })
  const monthDays = Array.from({ length: 42 }, (_, i) => addDays(gridStart, i))
  const monthLabel = `${selectedDate.getFullYear()}년 ${selectedDate.getMonth() + 1}월`

  // 일정 조회 범위 (주간=그 주 / 월간=그 달)
  const rangeStart = view === 'week' ? weekStart : monthStart
  const rangeEnd =
    view === 'week' ? addDays(weekStart, 6) : endOfMonth(selectedDate)
  const {
    data: schedulesByDate = {},
    isLoading,
    isError,
    error,
  } = useSchedules({
    viewType: view === 'week' ? 'WEEK' : 'MONTH',
    startDate: dateKey(rangeStart),
    endDate: dateKey(rangeEnd),
  })

  // < > : 주간이면 한 주, 월간이면 한 달 이동
  const handlePrev = () =>
    setSelectedDate((d) => (view === 'week' ? subDays(d, 7) : subMonths(d, 1)))
  const handleNext = () =>
    setSelectedDate((d) => (view === 'week' ? addDays(d, 7) : addMonths(d, 1)))

  const items = schedulesByDate[dateKey(selectedDate)] ?? []

  // 밀린 일정: 매일 오늘 화면만 보다 보면 놓치게 되므로 홈 상단에서 알려준다.
  const { data: overdue } = useOverdueSchedules()
  const overdueCount = overdue?.totalCount ?? 0

  const completeMutation = useCompleteSchedule()

  const closeCompleteSheet = () => {
    setCompletingSchedule(null)
    completeMutation.reset() // 다음에 열 때 이전 에러가 남지 않도록
  }

  const handleCompleteSubmit = (actualMinutes) => {
    completeMutation.mutate(
      { scheduleId: completingSchedule.id, actualMinutes },
      { onSuccess: closeCompleteSheet },
    )
  }

  const deferMutation = useDeferSchedule()

  const closeDeferSheet = () => {
    setDeferringSchedule(null)
    deferMutation.reset()
  }

  const handleDeferSubmit = ({
    deferReasonCode,
    deferReasonDetail,
    newStartAt,
  }) => {
    deferMutation.mutate(
      {
        scheduleId: deferringSchedule.id,
        deferReasonCode,
        deferReasonDetail,
        newStartAt, // 미루기 = 다시 할 시간 재약속
      },
      { onSuccess: closeDeferSheet },
    )
  }

  return (
    <div className="home">
      {/* 헤더 */}
      <header className="home-header">
        <h1 className="home-logo">RE:DAY</h1>
        <button
          type="button"
          className="home-search-btn"
          onClick={() => navigate('/search')}
          aria-label="일정 검색"
        >
          <Search size={22} />
        </button>
      </header>

      {/* 밀린 일정 배너 (없으면 아예 표시하지 않는다) */}
      {overdueCount > 0 && (
        <button
          type="button"
          className="home-overdue"
          onClick={() => navigate('/overdue')}
        >
          <span className="home-overdue-dot" />
          <span className="home-overdue-text">
            아직 끝내지 못한 일정 {overdueCount}개
          </span>
          <ChevronRight size={18} className="home-overdue-chevron" />
        </button>
      )}

      {/* 주간/월간 탭 */}
      <div className="home-tabs">
        <button
          type="button"
          className={view === 'week' ? 'home-tab active' : 'home-tab'}
          onClick={() => setView('week')}
        >
          주간
        </button>
        <button
          type="button"
          className={view === 'month' ? 'home-tab active' : 'home-tab'}
          onClick={() => setView('month')}
        >
          월간
        </button>
      </div>

      {/* 공통: 이동 네비게이션 (주간=주 범위, 월간=년월) */}
      <div className="week-nav">
        <div className="week-range-group">
          <button
            type="button"
            className="week-arrow"
            onClick={handlePrev}
            aria-label={view === 'week' ? '이전 주' : '이전 달'}
          >
            <ChevronLeft size={20} />
          </button>
          <span className="week-range">
            {view === 'week' ? rangeLabel : monthLabel}
          </span>
          <button
            type="button"
            className="week-arrow"
            onClick={handleNext}
            aria-label={view === 'week' ? '다음 주' : '다음 달'}
          >
            <ChevronRight size={20} />
          </button>
        </div>
        <button
          type="button"
          className="today-btn"
          onClick={() => setSelectedDate(startOfToday())}
        >
          오늘
        </button>
      </div>

      {view === 'week' ? (
        /* 요일 스트립 */
        <div className="day-strip">
          {weekDays.map((day, i) => {
            // 오늘 = 파란 테두리, 선택 = 하늘색 배경 + 파란 글씨
            const cellClass = [
              'day-cell',
              isToday(day) && 'today',
              isSameDay(day, selectedDate) && 'selected',
            ]
              .filter(Boolean)
              .join(' ')
            return (
              <button
                key={dateKey(day)}
                type="button"
                className={cellClass}
                onClick={() => setSelectedDate(day)}
              >
                <span className="day-label">{WEEKDAYS[i]}</span>
                <span className="day-num">{day.getDate()}</span>
              </button>
            )
          })}
        </div>
      ) : (
        /* 월간 달력 */
        <>
          <div className="month-weekdays">
            {WEEKDAYS.map((w) => (
              <span key={w} className="month-weekday">
                {w}
              </span>
            ))}
          </div>
          <div className="month-grid">
            {monthDays.map((day) => {
              const dayItems = schedulesByDate[dateKey(day)] ?? []
              const isOther = !isSameMonth(day, monthStart)
              const dow = day.getDay() // 0=일, 6=토
              // 이번 달이 아니면 회색, 아니면 일=빨강/토=파랑/평일=검정
              const numClass = [
                'month-day-num',
                isToday(day) && 'today', // 오늘 = 파란 테두리
                isSameDay(day, selectedDate) && 'selected', // 선택 = 하늘색 배경 + 파란 글씨
                isOther
                  ? 'other-month'
                  : dow === 0
                    ? 'sun'
                    : dow === 6
                      ? 'sat'
                      : '',
              ]
                .filter(Boolean)
                .join(' ')
              return (
                <button
                  key={dateKey(day)}
                  type="button"
                  className="month-cell"
                  onClick={() => setSelectedDate(day)}
                  // 점은 시각 표현이라, 개수는 읽어줄 수 있게 라벨로 남긴다.
                  aria-label={
                    dayItems.length > 0
                      ? `${monthDay(day)}, 일정 ${dayItems.length}개`
                      : undefined
                  }
                >
                  <span className={numClass}>{day.getDate()}</span>
                  {dayItems.length > 0 && <MonthDayDots items={dayItems} />}
                </button>
              )
            })}
          </div>
        </>
      )}

      {/* 공통: 선택 날짜 일정 (주간/월간 공유) */}
      <section className="schedule">
        <h2 className="schedule-title">{monthDay(selectedDate)} 일정</h2>
        {isLoading ? (
          <ScheduleSkeleton />
        ) : isError ? (
          <p className="schedule-error">{getApiErrorMessage(error)}</p>
        ) : items.length === 0 ? (
          <p className="schedule-empty">등록된 일정이 없어요.</p>
        ) : (
          <ul className="schedule-list">
            {items.map((item) => (
              <ScheduleItem
                key={item.id}
                item={item}
                onCompleteClick={setCompletingSchedule}
                onDeferClick={setDeferringSchedule}
                onItemClick={(it) =>
                  navigate(`/schedules/${it.id}`, { state: { schedule: it } })
                }
              />
            ))}
          </ul>
        )}
      </section>

      {/* 일정 추가 버튼 → 등록 화면 */}
      <div className="home-fab-area">
        <button
          type="button"
          className="home-fab"
          aria-label="일정 추가"
          onClick={() => setShowCreateSheet(true)}
        >
          <Plus size={28} />
        </button>
      </div>

      {/* 일정 완료 시트 (완료 버튼 클릭 시) */}
      {completingSchedule && (
        <ScheduleCompleteSheet
          schedule={completingSchedule}
          onClose={closeCompleteSheet}
          onComplete={handleCompleteSubmit}
          isSubmitting={completeMutation.isPending}
          errorMessage={
            completeMutation.isError
              ? getApiErrorMessage(completeMutation.error)
              : null
          }
        />
      )}

      {/* 일정 미루기 시트 (미루기 버튼 클릭 시) */}
      {deferringSchedule && (
        <DeferReasonSheet
          schedule={deferringSchedule}
          onClose={closeDeferSheet}
          onDefer={handleDeferSubmit}
          isSubmitting={deferMutation.isPending}
          errorMessage={
            deferMutation.isError
              ? getApiErrorMessage(deferMutation.error)
              : null
          }
        />
      )}

      {/* 일정 등록 시트 (FAB 클릭 시) */}
      {showCreateSheet && (
        <ScheduleFormSheet
          suggestions={buildTitleSuggestions(schedulesByDate)}
          onClose={() => setShowCreateSheet(false)}
        />
      )}
    </div>
  )
}

// 월간 셀 아래 점 표시.
// 셀이 좁아 최대 3개까지만 찍고 나머지는 표시하지 않는다.
// (점은 "무엇이 있는지"를 알리는 신호일 뿐이고, 정확한 개수·내용은 날짜를 누르면 아래 목록에 나온다.
//  모바일 캘린더의 통용 방식 — Google 캘린더도 월간에서 점 3개까지만 찍고 나머지는 생략한다.)
const MAX_DOTS = 3

// 미룬 일정 > 남은 일정 > 완료 순. 점이 잘려도 미룬 날은 반드시 눈에 띄게 한다.
const DOT_KINDS = ['deferred', 'pending', 'done']

function dotKind(item) {
  if (item.completed) return 'done'
  return item.deferCount > 0 ? 'deferred' : 'pending'
}

function MonthDayDots({ items }) {
  // 3개가 넘으면 신호가 강한 것부터 고르고,
  // 고른 뒤에는 아래 일정 목록과 순서가 어긋나지 않도록 다시 시간순으로 되돌린다.
  const picked = [...items]
    .sort(
      (a, b) => DOT_KINDS.indexOf(dotKind(a)) - DOT_KINDS.indexOf(dotKind(b)),
    )
    .slice(0, MAX_DOTS)
  const visible = items.filter((item) => picked.includes(item))

  return (
    <span className="month-dots" aria-hidden="true">
      {visible.map((item) => (
        <span key={item.id} className={`month-dot ${dotKind(item)}`} />
      ))}
    </span>
  )
}

// 로딩 중 스켈레톤 (일정 항목 모양의 회색 블록 + shimmer)
function ScheduleSkeleton() {
  return (
    <ul className="schedule-list" aria-hidden="true">
      {[0, 1, 2].map((i) => (
        <li className="schedule-item" key={i}>
          <div className="schedule-time">
            <span className="skeleton skel-period" />
            <span className="skeleton skel-clock" />
          </div>
          <div className="schedule-body">
            <span className="skeleton skel-title" />
            <span className="skeleton skel-badge" />
          </div>
          <div className="schedule-actions">
            <span className="skeleton skel-action" />
          </div>
        </li>
      ))}
    </ul>
  )
}

function ScheduleItem({ item, onCompleteClick, onDeferClick, onItemClick }) {
  return (
    <li className="schedule-item">
      {/* 시간+제목 영역을 누르면 상세로 이동 (버튼과 분리) */}
      <button
        type="button"
        className="schedule-main"
        onClick={() => onItemClick(item)}
      >
        <div className="schedule-time">
          <span className="schedule-period">{item.period}</span>
          <span className="schedule-clock">{item.time}</span>
        </div>

        <div className="schedule-body">
          <p className="schedule-name">{item.title}</p>
          <div className="schedule-meta">
            <span className="schedule-est">예상 {item.estimatedMin}분</span>
            {item.deferCount > 0 && (
              <span className="schedule-defer-badge">
                {item.deferCount}번 미룸
              </span>
            )}
          </div>
        </div>
      </button>

      <div className="schedule-actions">
        {item.completed ? (
          <span className="schedule-done">
            <CheckCircle2 size={20} />
            완료됨
          </span>
        ) : (
          <>
            <button
              type="button"
              className="btn-defer"
              onClick={() => onDeferClick(item)}
            >
              미루기
            </button>
            <button
              type="button"
              className="btn-complete"
              onClick={() => onCompleteClick(item)}
            >
              완료
            </button>
          </>
        )}
      </div>
    </li>
  )
}

export default HomePage
