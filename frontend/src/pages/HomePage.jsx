import { useState } from 'react'
import {
  startOfToday,
  startOfWeek,
  addDays,
  subDays,
  isSameDay,
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

// TODO: 일정 조회 API 연동 예정. 지금은 오늘 날짜에 예시 일정을 넣어둔다.
const initialSchedules = () => ({
  [dateKey(startOfToday())]: [
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
})

function HomePage() {
  const [view, setView] = useState('week') // 'week' | 'month'
  const [selectedDate, setSelectedDate] = useState(() => startOfToday())
  const [schedulesByDate, setSchedulesByDate] = useState(initialSchedules)

  const weekStart = startOfWeek(selectedDate, { weekStartsOn: 1 })
  const weekDays = Array.from({ length: 7 }, (_, i) => addDays(weekStart, i))
  const rangeLabel = `${monthDay(weekStart)} ~ ${monthDay(addDays(weekStart, 6))}`

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

      {view === 'month' ? (
        <p className="home-month-placeholder">월간 보기는 준비 중입니다.</p>
      ) : (
        <>
          {/* 주 이동 */}
          <div className="week-nav">
            <div className="week-range-group">
              <button
                type="button"
                className="week-arrow"
                onClick={() => setSelectedDate((d) => subDays(d, 7))}
                aria-label="이전 주"
              >
                <ChevronLeft size={20} />
              </button>
              <span className="week-range">{rangeLabel}</span>
              <button
                type="button"
                className="week-arrow"
                onClick={() => setSelectedDate((d) => addDays(d, 7))}
                aria-label="다음 주"
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

          {/* 요일 스트립 */}
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

          {/* 선택 날짜 일정 */}
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
        </>
      )}

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
