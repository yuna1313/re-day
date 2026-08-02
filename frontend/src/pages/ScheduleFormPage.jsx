import { useNavigate, useParams } from 'react-router-dom'
import { ChevronLeft } from 'lucide-react'
import { useSchedule } from '../hooks/useSchedule'
import { useUpdateSchedule } from '../hooks/useUpdateSchedule'
import { getApiErrorMessage } from '../api/client'
import ScheduleFormFields from '../components/ScheduleFormFields'
import './ScheduleFormPage.css'

// 헤더(뒤로 + 제목)
function FormHeader({ onBack }) {
  return (
    <header className="form-header">
      <button
        type="button"
        className="form-back"
        onClick={onBack}
        aria-label="뒤로"
      >
        <ChevronLeft size={26} />
      </button>
      <h1 className="form-title">일정 수정</h1>
    </header>
  )
}

// 일정 수정 페이지. (등록은 홈의 바텀시트로 처리)
function ScheduleFormPage() {
  const navigate = useNavigate()
  const { scheduleId } = useParams()

  // 상세 조회(상세를 이미 봤다면 캐시 적중으로 즉시)
  const { data: initial, isError, error } = useSchedule(scheduleId)
  const updateMutation = useUpdateSchedule()

  const handleUpdate = (payload) => {
    updateMutation.mutate(
      { scheduleId: initial.id, ...payload },
      { onSuccess: () => navigate(-1) },
    )
  }

  return (
    <div className="form-page">
      <FormHeader onBack={() => navigate(-1)} />

      {!initial ? (
        isError ? (
          <p className="form-error">{getApiErrorMessage(error)}</p>
        ) : (
          <FormSkeleton />
        )
      ) : (
        <ScheduleFormFields
          key={initial.id}
          initial={initial}
          onSubmit={handleUpdate}
          isPending={updateMutation.isPending}
          errorMessage={
            updateMutation.isError
              ? getApiErrorMessage(updateMutation.error)
              : null
          }
          submitLabel="수정"
          submittingLabel="수정 중..."
        />
      )}
    </div>
  )
}

// 수정 대상 조회 중 스켈레톤
function FormSkeleton() {
  return (
    <div className="form-body" aria-hidden="true">
      {[0, 1, 2, 3].map((i) => (
        <div className="form-field" key={i}>
          <span
            className="skeleton"
            style={{
              display: 'block',
              width: 70,
              height: 14,
              marginBottom: 10,
            }}
          />
          <span
            className="skeleton"
            style={{ display: 'block', height: 54, borderRadius: 14 }}
          />
        </div>
      ))}
    </div>
  )
}

export default ScheduleFormPage
