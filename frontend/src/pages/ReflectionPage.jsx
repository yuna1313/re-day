import { useState } from 'react'
import { format } from 'date-fns'
import { CheckCircle2 } from 'lucide-react'
import { useSchedules } from '../hooks/useSchedules'
import './ReflectionPage.css'

const MAX_LENGTH = 1000

function ReflectionPage() {
  const [content, setContent] = useState('')

  // 오늘의 완료한 일정 (일정 목록에서 오늘 + 완료만)
  const todayKey = format(new Date(), 'yyyy-MM-dd')
  const { data: byDate = {}, isLoading } = useSchedules({
    viewType: 'WEEK',
    startDate: todayKey,
    endDate: todayKey,
  })
  const completed = (byDate[todayKey] ?? []).filter((item) => item.completed)

  const handleSubmit = () => {
    // TODO: 오늘의 회고 작성 API 연동 예정 (POST /reflections)
  }

  return (
    <div className="reflection-page">
      <header className="reflection-header">
        <h1 className="reflection-title">오늘의 회고</h1>
      </header>

      {/* 오늘 완료한 일정 */}
      <section className="reflection-section">
        <h2 className="reflection-heading">오늘 완료한 일정</h2>

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
          <p className="reflection-empty">완료한 일정이 없어요.</p>
        ) : (
          <ul className="completed-list">
            {completed.map((item) => (
              <li className="completed-item" key={item.id}>
                <CheckCircle2 size={20} fill="#3b82f6" color="#fff" />
                <span className="completed-title">{item.title}</span>
              </li>
            ))}
          </ul>
        )}
      </section>

      {/* 오늘의 회고 작성 */}
      <section className="reflection-section">
        <h2 className="reflection-heading">오늘의 회고</h2>
        <p className="reflection-subtext">오늘의 회고를 작성해볼까요? 😎</p>

        <div className="reflection-textarea-wrap">
          <textarea
            className="reflection-textarea"
            placeholder="오늘 느낀 점이나 배운 점을 자유롭게 작성해보세요."
            value={content}
            maxLength={MAX_LENGTH}
            onChange={(event) => setContent(event.target.value)}
          />
          <span className="reflection-counter">
            {content.length} / {MAX_LENGTH}
          </span>
        </div>

        <button
          type="button"
          className="reflection-submit"
          onClick={handleSubmit}
          disabled={!content.trim()}
        >
          작성하기
        </button>
      </section>
    </div>
  )
}

export default ReflectionPage
