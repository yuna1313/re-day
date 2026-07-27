import { useQuery } from '@tanstack/react-query'
import { scheduleApi } from '../api/schedule'
import { toDisplayItem } from './useSchedules'

// 상세 응답을 화면 표시용 형태로 변환 (목록 표시 필드 + 메모/미루기 로그 등)
function toDisplayDetail(detail) {
  return {
    ...toDisplayItem(detail),
    memo: detail.memo ?? '',
    actualMinutes: detail.actualMinutes ?? null,
    completedAt: detail.completedAt ?? null,
    createdAt: detail.createdAt ?? null,
    updatedAt: detail.updatedAt ?? null,
    deferLogs: detail.deferLogs ?? [],
  }
}

// 일정 상세 조회 훅.
// placeholder: 목록에서 넘어온 표시용 item(있으면 즉시 표시, 조회 완료 시 갱신)
export function useSchedule(scheduleId, placeholder) {
  return useQuery({
    queryKey: ['schedule', scheduleId],
    queryFn: async () =>
      toDisplayDetail(await scheduleApi.getSchedule({ scheduleId })),
    enabled: Boolean(scheduleId),
    placeholderData: placeholder,
  })
}
