import { useNavigate, useLocation, useParams } from 'react-router-dom'
import { ChevronLeft } from 'lucide-react'
import { useSchedule } from '../hooks/useSchedule'
import { useDeleteSchedule } from '../hooks/useDeleteSchedule'
import { getApiErrorMessage } from '../api/client'
import './ScheduleDetailPage.css'

function ScheduleDetailPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const { scheduleId } = useParams()

  // 목록에서 넘어온 데이터가 있으면 즉시 표시하고, 상세 조회로 갱신(메모 등)
  const {
    data: schedule,
    isError,
    error,
  } = useSchedule(scheduleId, location.state?.schedule)

  const deleteMutation = useDeleteSchedule()

  const handleDelete = () => {
    if (!window.confirm('이 일정을 삭제할까요?')) return
    deleteMutation.mutate(
      { scheduleId: schedule.id },
      { onSuccess: () => navigate('/', { replace: true }) },
    )
  }

  return (
    <div className="detail-page">
      <header className="detail-header">
        <button
          type="button"
          className="detail-back"
          onClick={() => navigate(-1)}
          aria-label="뒤로"
        >
          <ChevronLeft size={26} />
        </button>
        <h1 className="detail-title">일정 상세</h1>
      </header>

      {!schedule ? (
        isError ? (
          <p className="detail-error">{getApiErrorMessage(error)}</p>
        ) : (
          <DetailSkeleton />
        )
      ) : (
        <ScheduleDetailBody
          schedule={schedule}
          onEdit={() =>
            navigate(`/schedules/${schedule.id}/edit`, { state: { schedule } })
          }
          onDelete={handleDelete}
          deleting={deleteMutation.isPending}
          deleteError={
            deleteMutation.isError
              ? getApiErrorMessage(deleteMutation.error)
              : null
          }
        />
      )}
    </div>
  )
}

function ScheduleDetailBody({
  schedule,
  onEdit,
  onDelete,
  deleting,
  deleteError,
}) {
  const [year, month, day] = schedule.date.split('-')
  const startAtLabel = `${Number(year)}년 ${Number(month)}월 ${Number(day)}일 ${schedule.period} ${schedule.time}`

  return (
    <>
      {/* 일정 정보 */}
      <div className="detail-card">
        <h2 className="detail-name">{schedule.title}</h2>
        <hr className="detail-divider" />

        <div className="detail-row">
          <p className="detail-label">시작 일시</p>
          <p className="detail-value">{startAtLabel}</p>
        </div>
        <div className="detail-row">
          <p className="detail-label">예상 시간</p>
          <p className="detail-value">{schedule.estimatedMin}분</p>
        </div>
        <div className="detail-row">
          <p className="detail-label">상태</p>
          <p
            className={
              schedule.completed
                ? 'detail-value detail-status-done'
                : 'detail-value detail-status'
            }
          >
            {schedule.completed ? '완료' : '미완료'}
          </p>
        </div>
      </div>

      {/* 메모 */}
      <div className="detail-card">
        <h2 className="detail-card-title">메모</h2>
        <hr className="detail-divider" />
        {schedule.memo ? (
          <p className="detail-memo">{schedule.memo}</p>
        ) : (
          <p className="detail-memo-empty">작성된 메모가 없어요.</p>
        )}
      </div>

      {/* 수정 / 삭제 (완료된 일정은 기록이므로 수정 불가, 삭제만 가능) */}
      <div className="detail-actions">
        {!schedule.completed && (
          <button type="button" className="detail-edit" onClick={onEdit}>
            수정하기
          </button>
        )}
        <button
          type="button"
          className="detail-delete"
          onClick={onDelete}
          disabled={deleting}
        >
          {deleting ? '삭제 중...' : '삭제하기'}
        </button>
      </div>

      {deleteError && <p className="detail-error">{deleteError}</p>}
    </>
  )
}

// 로딩 스켈레톤 (정보 카드 모양)
function DetailSkeleton() {
  return (
    <div className="detail-card" aria-hidden="true">
      <span
        className="skeleton"
        style={{ display: 'block', width: '55%', height: 22 }}
      />
      <hr className="detail-divider" />
      {[0, 1, 2].map((i) => (
        <div className="detail-row" key={i}>
          <span className="skeleton" style={{ width: 64, height: 14 }} />
          <span className="skeleton" style={{ width: '40%', height: 14 }} />
        </div>
      ))}
    </div>
  )
}

export default ScheduleDetailPage
