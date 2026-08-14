import { useNavigate, useLocation, useParams } from 'react-router-dom'
import { ChevronLeft } from 'lucide-react'
import { useSchedule } from '../hooks/useSchedule'
import { useDeleteSchedule } from '../hooks/useDeleteSchedule'
import { getApiErrorMessage } from '../api/client'
import { DEFER_CUSTOM_CODE, deferReasonLabel } from '../constants/deferReasons'
import './ScheduleDetailPage.css'

// 처리 로그 시각 "2026-08-12 15:20:00" → "8월 12일 오후 03:20"
function actionTimeLabel(actionAt) {
  if (!actionAt) return ''

  const month = Number(actionAt.slice(5, 7))
  const day = Number(actionAt.slice(8, 10))
  const hour24 = Number(actionAt.slice(11, 13))
  const minute = actionAt.slice(14, 16)
  const period = hour24 < 12 ? '오전' : '오후'
  const hour12 = hour24 % 12 === 0 ? 12 : hour24 % 12

  return `${month}월 ${day}일 ${period} ${String(hour12).padStart(2, '0')}:${minute}`
}

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
  // 목록에서 넘어온 placeholder 에는 로그가 없으므로 상세 조회 전까지는 빈 배열
  const actionLogs = schedule.deferLogs ?? []

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
        {schedule.deferCount > 0 && (
          <div className="detail-row">
            <p className="detail-label">미룬 횟수</p>
            <p className="detail-value">{schedule.deferCount}번</p>
          </div>
        )}
      </div>

      {/* 첫 단계 */}
      <div className="detail-card">
        <h2 className="detail-card-title">첫 단계</h2>
        <hr className="detail-divider" />
        {schedule.memo ? (
          <p className="detail-memo">{schedule.memo}</p>
        ) : (
          <p className="detail-memo-empty">
            첫 단계를 적어두면 시작이 쉬워져요.
          </p>
        )}
      </div>

      {/* 처리 기록 (미루기/완료 이력이 있을 때만) */}
      {actionLogs.length > 0 && <ScheduleActionLogs logs={actionLogs} />}

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

// 미루기/완료 이력 타임라인. logs 는 백엔드가 처리 시각 오름차순으로 내려준다.
function ScheduleActionLogs({ logs }) {
  return (
    <div className="detail-card">
      <h2 className="detail-card-title">처리 기록</h2>
      <hr className="detail-divider" />

      <ol className="log-list">
        {logs.map((log) => {
          const isDone = log.actionType === 'DONE'
          const isCustom = log.deferReasonCode === DEFER_CUSTOM_CODE
          const detail = log.deferReasonDetail

          return (
            <li className="log-item" key={log.actionLogId}>
              <span className={isDone ? 'log-dot done' : 'log-dot'} />
              <div className="log-body">
                <p className="log-head">
                  <span className={isDone ? 'log-action done' : 'log-action'}>
                    {isDone ? '완료함' : '미룸'}
                  </span>
                  <span className="log-time">
                    {actionTimeLabel(log.actionAt)}
                  </span>
                </p>

                {/* 직접 입력한 사유는 라벨 대신 적어둔 내용을 그대로 보여준다 */}
                {!isDone && (
                  <p className="log-reason">
                    {isCustom && detail
                      ? detail
                      : deferReasonLabel(log.deferReasonCode)}
                  </p>
                )}
                {!isDone && !isCustom && detail && (
                  <p className="log-detail">{detail}</p>
                )}
              </div>
            </li>
          )
        })}
      </ol>
    </div>
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
