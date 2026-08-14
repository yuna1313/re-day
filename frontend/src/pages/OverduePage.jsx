import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { ChevronLeft } from 'lucide-react'
import { useOverdueSchedules } from '../hooks/useOverdueSchedules'
import { useCompleteSchedule } from '../hooks/useCompleteSchedule'
import { useDeferSchedule } from '../hooks/useDeferSchedule'
import { getApiErrorMessage } from '../api/client'
import ScheduleCompleteSheet from '../components/ScheduleCompleteSheet'
import DeferReasonSheet from '../components/DeferReasonSheet'
import './OverduePage.css'

const DAY_MS = 24 * 60 * 60 * 1000

// 'yyyy-MM-dd' → 오늘 기준 며칠 전인지
function daysAgo(dateStr) {
  const [year, month, day] = dateStr.split('-').map(Number)
  const target = new Date(year, month - 1, day)
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return Math.round((today - target) / DAY_MS)
}

function OverduePage() {
  const navigate = useNavigate()
  const { data, isLoading, isError, error } = useOverdueSchedules()
  const items = data?.items ?? []

  const [completingSchedule, setCompletingSchedule] = useState(null)
  const [deferringSchedule, setDeferringSchedule] = useState(null)

  const completeMutation = useCompleteSchedule()
  const deferMutation = useDeferSchedule()

  const closeCompleteSheet = () => {
    setCompletingSchedule(null)
    completeMutation.reset()
  }

  const closeDeferSheet = () => {
    setDeferringSchedule(null)
    deferMutation.reset()
  }

  const handleCompleteSubmit = (actualMinutes) => {
    completeMutation.mutate(
      { scheduleId: completingSchedule.id, actualMinutes },
      { onSuccess: closeCompleteSheet },
    )
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
        newStartAt,
      },
      { onSuccess: closeDeferSheet },
    )
  }

  return (
    <div className="overdue-page">
      <header className="overdue-header">
        <button
          type="button"
          className="overdue-back"
          onClick={() => navigate(-1)}
          aria-label="뒤로"
        >
          <ChevronLeft size={26} />
        </button>
        <h1 className="overdue-title">밀린 일정</h1>
      </header>

      {isLoading ? (
        <OverdueSkeleton />
      ) : isError ? (
        <p className="overdue-error">{getApiErrorMessage(error)}</p>
      ) : items.length === 0 ? (
        <p className="overdue-empty">
          밀린 일정이 없어요.
          <br />
          지금 페이스, 꽤 괜찮아요 🙂
        </p>
      ) : (
        <>
          <p className="overdue-lead">
            아직 끝내지 못한 일정이에요. 지금 할 수 있는 것만 골라도 충분해요.
          </p>
          <ul className="overdue-list">
            {items.map((item) => (
              <OverdueItem
                key={item.id}
                item={item}
                onCompleteClick={setCompletingSchedule}
                onDeferClick={setDeferringSchedule}
                onTitleClick={() =>
                  navigate(`/schedules/${item.id}`, {
                    state: { schedule: item },
                  })
                }
              />
            ))}
          </ul>
          {data.hasMore && (
            <p className="overdue-more-note">
              최근 50개까지만 보여드려요. (전체 {data.totalCount}개)
            </p>
          )}
        </>
      )}

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
    </div>
  )
}

function OverdueItem({ item, onCompleteClick, onDeferClick, onTitleClick }) {
  const passed = daysAgo(item.date)

  return (
    <li className="overdue-item">
      <button type="button" className="overdue-main" onClick={onTitleClick}>
        <span className="overdue-item-title">{item.title}</span>
        <span className="overdue-item-meta">
          <span className="overdue-days">{passed}일 지남</span>
          {item.deferCount > 0 && (
            <span className="overdue-defer-badge">
              {item.deferCount}번 미룸
            </span>
          )}
        </span>
      </button>

      <div className="overdue-actions">
        <button
          type="button"
          className="overdue-btn-defer"
          onClick={() => onDeferClick(item)}
        >
          다시 잡기
        </button>
        <button
          type="button"
          className="overdue-btn-complete"
          onClick={() => onCompleteClick(item)}
        >
          완료
        </button>
      </div>
    </li>
  )
}

// 로딩 중 스켈레톤 (밀린 일정 항목 모양의 회색 블록 + shimmer)
function OverdueSkeleton() {
  return (
    <ul className="overdue-list" aria-hidden="true">
      {[0, 1, 2].map((i) => (
        <li className="overdue-item" key={i}>
          <div className="overdue-main">
            <span className="skeleton overdue-skel-title" />
            <span className="skeleton overdue-skel-meta" />
          </div>
        </li>
      ))}
    </ul>
  )
}

export default OverduePage
