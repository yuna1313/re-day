import client, { throwIfFailed } from './client'

// 회원 관련 API (백엔드: /api/v1/members)
export const memberApi = {
  // 내 정보 조회 / 반환: { memberId, nickname, email }
  getMe: async () => {
    const { data } = await client.get('/members/me')
    return throwIfFailed(data, '내 정보 조회에 실패했습니다.').data
  },
}
