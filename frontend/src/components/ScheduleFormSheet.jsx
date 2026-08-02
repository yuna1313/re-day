import { useCreateSchedule } from '../hooks/useCreateSchedule'
import { getApiErrorMessage } from '../api/client'
import ScheduleFormFields from './ScheduleFormFields'
import './ScheduleFormSheet.css'

// 일정 등록 바텀시트. (홈 FAB에서 열림 — 전체 화면 이동 대신 팝업으로)
function ScheduleFormSheet({ onClose, suggestions }) {
  const createMutation = useCreateSchedule()

  const handleCreate = (payload) => {
    createMutation.mutate(payload, { onSuccess: onClose })
  }

  return (
    <div className="sheet-overlay" onClick={onClose}>
      <div
        className="schedule-sheet"
        role="dialog"
        aria-modal="true"
        aria-label="일정 등록"
        onClick={(event) => event.stopPropagation()}
      >
        {/* 상단 그래버 (탭하면 닫힘) */}
        <button
          type="button"
          className="sheet-grabber"
          onClick={onClose}
          aria-label="닫기"
        >
          <span className="sheet-grabber-bar" />
        </button>

        <h2 className="sheet-title">일정 등록</h2>

        <ScheduleFormFields
          initial={null}
          onSubmit={handleCreate}
          isPending={createMutation.isPending}
          errorMessage={
            createMutation.isError
              ? getApiErrorMessage(createMutation.error)
              : null
          }
          submitLabel="등록"
          submittingLabel="등록 중..."
          suggestions={suggestions}
        />
      </div>
    </div>
  )
}

export default ScheduleFormSheet
