import { useMutation, useQueryClient } from '@tanstack/react-query'
import { Navigate, useLocation, useNavigate } from 'react-router-dom'
import { authApi } from '../api/auth'
import { authStorage, getApiErrorMessage } from '../api/client'
import AuthHeader from '../components/AuthHeader'
import './LoginPage.css'
import './GuestRecordImportPage.css'

function GuestRecordImportPage() {
  const { state } = useLocation()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const destination = state?.fromMyPage ? '/mypage' : '/'
  const guestToken = authStorage.getGuestImportToken()
  const importMutation = useMutation({
    mutationFn: authApi.importGuestRecords,
    onSuccess: (_, importedToken) => {
      authStorage.removeSavedGuest(importedToken)
      void queryClient.invalidateQueries()
      navigate(destination, { replace: true })
    },
  })

  if (!guestToken) {
    return <Navigate to={destination} replace />
  }

  return (
    <main className="login-page">
      <AuthHeader title="게스트 기록을 추가할까요?" />
      <p className="guest-record-import-description">
        이 브라우저에서 게스트로 작성한 일정, 미루기 기록, 회고를 계정으로
        가져와요. 같은 날짜의 회고는 기존 글 뒤에 덧붙여요.
      </p>
      {importMutation.isError && (
        <p className="login-error" role="alert">
          {getApiErrorMessage(importMutation.error)}
        </p>
      )}
      <button
        className="login-button"
        type="button"
        disabled={importMutation.isPending}
        onClick={() => importMutation.mutate(guestToken)}
      >
        {importMutation.isPending ? '가져오는 중…' : '내 계정에 추가'}
      </button>
      <button
        className="guest-record-import-skip"
        type="button"
        disabled={importMutation.isPending}
        onClick={() => navigate(destination, { replace: true })}
      >
        지금은 건너뛰기
      </button>
      <p className="guest-record-import-note">
        마이페이지에서 나중에 가져올 수 있어요.
      </p>
    </main>
  )
}

export default GuestRecordImportPage
