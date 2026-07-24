import { useNavigate, useLocation, Navigate } from 'react-router-dom'
import { ChevronLeft } from 'lucide-react'
import './ScheduleDetailPage.css'

function ScheduleDetailPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const schedule = location.state?.schedule

  // 목록에서 넘어온 데이터가 없으면(직접 접근/새로고침) 홈으로
  // TODO: 상세 조회 API(GET /schedules/{id}) 연동 시 이 경우에도 직접 조회
  if (!schedule) {
    return <Navigate to="/" replace />
  }

  const [year, month, day] = schedule.date.split('-')
  const startAtLabel = `${Number(year)}년 ${Number(month)}월 ${Number(day)}일 ${schedule.period} ${schedule.time}`

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
        {/* TODO: 상세 API 연동 시 실제 메모 표시 */}
        <p className="detail-memo-empty">작성된 메모가 없어요.</p>
      </div>

      {/* 수정 / 삭제 */}
      <div className="detail-actions">
        {/* TODO: 일정 수정 화면 연결 */}
        <button type="button" className="detail-edit">
          수정하기
        </button>
        {/* TODO: 일정 삭제 API 연동 */}
        <button type="button" className="detail-delete">
          삭제하기
        </button>
      </div>
    </div>
  )
}

export default ScheduleDetailPage
