import { useQuery, keepPreviousData } from '@tanstack/react-query'
import { scheduleApi } from '../api/schedule'

// 서버 응답의 일정 하나를 화면 표시용 형태로 변환한다.
// startAt: "2026-01-09 08:00:00" → 오전/오후 + 12시간 표기
export function toDisplayItem(schedule) {
  const dateStr = schedule.startAt.slice(0, 10) // yyyy-MM-dd
  const hour24 = Number(schedule.startAt.slice(11, 13))
  const minute = schedule.startAt.slice(14, 16)
  const period = hour24 < 12 ? '오전' : '오후'
  const hour12 = hour24 % 12 === 0 ? 12 : hour24 % 12

  return {
    id: schedule.scheduleId,
    title: schedule.title,
    period,
    time: `${String(hour12).padStart(2, '0')}:${minute}`,
    estimatedMin: schedule.estimatedMinutes,
    completed: schedule.status === 'DONE',
    status: schedule.status,
    deferCount: schedule.deferCount,
    date: dateStr,
  }
}

// 일정 배열을 날짜별로 묶는다. { 'yyyy-MM-dd': [item, ...] }
function groupByDate(schedules) {
  const map = {}
  for (const schedule of schedules ?? []) {
    const item = toDisplayItem(schedule)
    ;(map[item.date] ??= []).push(item)
  }
  return map
}

// 주간/월간 일정 목록 조회 훅.
// 범위(startDate~endDate)별로 캐싱되고, 주/월 이동 시 이전 데이터를 유지해 깜빡임을 줄인다.
export function useSchedules({ viewType, startDate, endDate }) {
  return useQuery({
    queryKey: ['schedules', viewType, startDate, endDate],
    queryFn: () => scheduleApi.getSchedules({ viewType, startDate, endDate }),
    // 응답을 날짜별 맵으로 변환해서 반환
    select: (data) => groupByDate(data.schedules),
    placeholderData: keepPreviousData,
  })
}
