import client, { throwIfFailed } from './client'

// 회고 관련 API (백엔드: /api/v1/reflections)
export const reflectionApi = {
  // 오늘의 회고 작성 (회원별 같은 날짜에는 1개만 작성 가능)
  // reflectionDate: 'yyyy-MM-dd' / 반환: { reflectionId }
  createReflection: async ({ reflectionDate, content }) => {
    const { data } = await client.post('/reflections', {
      reflectionDate,
      content,
    })
    return throwIfFailed(data, '회고 작성에 실패했습니다.').data
  },
}
