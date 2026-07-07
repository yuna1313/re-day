import { useState } from 'react'
import {
  startOfToday,
  startOfWeek,
  startOfMonth,
  addDays,
  subDays,
  addMonths,
  subMonths,
  isSameDay,
  isSameMonth,
  isToday,
} from 'date-fns'
import { Plus, ChevronLeft, ChevronRight, CheckCircle2 } from 'lucide-react'
import './HomePage.css'

const WEEKDAYS = ['월', '화', '수', '목', '금', '토', '일']

const dateKey = (d) =>
  `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(
    d.getDate(),
  ).padStart(2, '0')}`

const monthDay = (d) => `${d.getMonth() + 1}월 ${d.getDate()}일`

// TODO: 일정 조회 API 연동 예정. 지금은 오늘 근처 며칠에 예시 일정을 넣어둔다.
const initialSchedules = () => {
  const today = startOfToday()
  return {
    [dateKey(today)]: [
      {
        id: 1,
        period: '오전',
        time: '08:00',
        title: '운동하기',
        estimatedMin: 15,
        completed: true,
      },
      {
        id: 2,
        period: '오후',
        time: '01:30',
        title: 'NCS 문제 풀기',
        estimatedMin: 60,
        completed: false,
      },
    ],
    [dateKey(addDays(today, 4))]: [
      {
        id: 3,
        period: '오전',
        time: '10:00',
        title: '이력서 수정',
        estimatedMin: 30,
        completed: false,
      },
      {
        id: 4,
        period: '오후',
        time: '02:00',
        title: '알고리즘 문제',
        estimatedMin: 45,
        completed: false,
      },
      {
        id: 5,
        period: '오후',
        time: '07:00',
        title: '독서',
        estimatedMin: 20,
        completed: false,
      },
    ],
    [dateKey(addDays(today, 5))]: [
      {
        id: 6,
        period: '오전',
        time: '09:00',
        title: '스터디',
        estimatedMin: 90,
        completed: false,
      },
    ],
  }
}

function HomePage() {
  const [view, setView] = useState('week') // 'week' | 'month'
  const [selectedDate, setSelectedDate] = useState(() => startOfToday())
  const [schedulesByDate, setSchedulesByDate] = useState(initialSchedules)

  // 주간용
  const weekStart = startOfWeek(selectedDate, { weekStartsOn: 1 })
  const weekDays = Array.from({ length: 7 }, (_, i) => addDays(weekStart, i))
  const rangeLabel = `${monthDay(weekStart)} ~ ${monthDay(addDays(weekStart, 6))}`

  // 월간용: 해당 월의 1일이 포함된 주의 월요일부터 6주(42칸)
  const monthStart = startOfMonth(selectedDate)
  const gridStart = startOfWeek(monthStart, { weekStartsOn: 1 })
  const monthDays = Array.from({ length: 42 }, (_, i) => addDays(gridStart, i))
  const monthLabel = `${selectedDate.getFullYear()}년 ${selectedDate.getMonth() + 1}월`

  // < > : 주간이면 한 주, 월간이면 한 달 이동
  const handlePrev = () =>
    setSelectedDate((d) => (view === 'week' ? subDays(d, 7) : subMonths(d, 1)))
  const handleNext = () =>
    setSelectedDate((d) => (view === 'week' ? addDays(d, 7) : addMonths(d, 1)))

  const items = schedulesByDate[dateKey(selectedDate)] ?? []

  const completeItem = (id) => {
    const key = dateKey(selectedDate)
    setSchedulesByDate((prev) => ({
      ...prev,
      [key]: prev[key].map((it) =>
        it.id === id ? { ...it, completed: true } : it,
      ),
    }))
  }

  return (
    <div className="home">
      {/* 헤더 */}
      <header className="home-header">
        <h1 className="home-logo">RE:DAY</h1>
      </header>

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
              const count = (schedulesByDate[dateKey(day)] ?? []).length
              const isOther = !isSameMonth(day, monthStart)
              const dow = day.getDay() // 0=일, 6=토
              // 이번 달이 아니면 회색, 아니면 일=빨강/토=파랑/평일=검정
              const numClass = [
                'month-day-num',
                isToday(day) && 'today', // 오늘 = 하늘색 배경
                isSameDay(day, selectedDate) && 'selected', // 선택 = 파란 테두리
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
                >
                  <span className={numClass}>{day.getDate()}</span>
                  {count > 0 && (
                    <span className="month-day-count">{count}개</span>
                  )}
                </button>
              )
            })}
          </div>
        </>
      )}

      {/* 공통: 선택 날짜 일정 (주간/월간 공유) */}
      <section className="schedule">
        <h2 className="schedule-title">{monthDay(selectedDate)} 일정</h2>
        {items.length === 0 ? (
          <p className="schedule-empty">등록된 일정이 없어요.</p>
        ) : (
          <ul className="schedule-list">
            {items.map((item) => (
              <ScheduleItem
                key={item.id}
                item={item}
                onComplete={() => completeItem(item.id)}
              />
            ))}
          </ul>
        )}
      </section>

      {/* 일정 추가 버튼 (추후 팝업 연결) */}
      <div className="home-fab-area">
        <button type="button" className="home-fab" aria-label="일정 추가">
          <Plus size={28} />
        </button>
      </div>
    </div>
  )
}

function ScheduleItem({ item, onComplete }) {
  return (
    <li className="schedule-item">
      <div className="schedule-time">
        <span className="schedule-period">{item.period}</span>
        <span className="schedule-clock">{item.time}</span>
      </div>

      <div className="schedule-body">
        <p className="schedule-name">{item.title}</p>
        <span className="schedule-est">예상 {item.estimatedMin}분</span>
      </div>

      <div className="schedule-actions">
        {item.completed ? (
          <span className="schedule-done">
            <CheckCircle2 size={20} />
            완료됨
          </span>
        ) : (
          <>
            {/* TODO: 미루기 API 연동 예정 */}
            <button type="button" className="btn-defer">
              미루기
            </button>
            <button type="button" className="btn-complete" onClick={onComplete}>
              완료
            </button>
          </>
        )}
      </div>
    </li>
  )
}

export default HomePage
