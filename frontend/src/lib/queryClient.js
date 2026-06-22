import { QueryClient } from '@tanstack/react-query'

// 앱 전역에서 공유하는 단일 QueryClient.
// 서버 상태(캐시)는 여기 설정된 기본 옵션을 따른다.
export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      // 이 시간 동안은 캐시를 "신선"하다고 보고 재요청하지 않는다. (1분)
      staleTime: 60 * 1000,
      // 창에 다시 포커스할 때마다 자동 재요청하지 않는다.
      refetchOnWindowFocus: false,
      // 실패 시 1회만 재시도.
      retry: 1,
    },
  },
})
