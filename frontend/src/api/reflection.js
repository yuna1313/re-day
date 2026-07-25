import client, { throwIfFailed } from './client'

// 회고 관련 API (백엔드: /api/v1/reflections)
export const reflectionApi = {
  // 오늘 회고 + 오늘 완료한 일정 조회
  // 반환: { reflection: { reflectionId, reflectionDate, content } | null,
  //         completedSchedules: [{ scheduleId, title }] }
  getTodayReflection: async () => {
    const { data } = await client.get('/reflections/today')
    return throwIfFailed(data, '오늘 회고 조회에 실패했습니다.').data
  },

  // 오늘의 회고 작성 (회원별 같은 날짜에는 1개만 작성 가능)
  // reflectionDate: 'yyyy-MM-dd' / 반환: { reflectionId }
  createReflection: async ({ reflectionDate, content }) => {
    const { data } = await client.post('/reflections', {
      reflectionDate,
      content,
    })
    return throwIfFailed(data, '회고 작성에 실패했습니다.').data
  },

  // 회고 수정 (내용 변경)
  // ※ 백엔드 스펙 확정 전 관례(PATCH /reflections/{id}, body { content })로 가정
  updateReflection: async ({ reflectionId, content }) => {
    const { data } = await client.patch(`/reflections/${reflectionId}`, {
      content,
    })
    return throwIfFailed(data, '회고 수정에 실패했습니다.').data
  },
}
