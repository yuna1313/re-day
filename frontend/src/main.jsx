import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import { QueryClientProvider } from '@tanstack/react-query'
import { ReactQueryDevtools } from '@tanstack/react-query-devtools'
import { queryClient } from './lib/queryClient'
import { AuthProvider } from './contexts/AuthProvider'
import './index.css'
import App from './App.jsx'

// VITE_USE_MOCK=true 일 때만 MSW 목서버를 켠다. (npm run dev:mock)
async function enableMocking() {
  if (import.meta.env.VITE_USE_MOCK !== 'true') return

  const { worker } = await import('./mocks/browser')
  // mock 하지 않은 요청은 실제 네트워크로 그대로 통과시킨다.
  return worker.start({ onUnhandledRequest: 'bypass' })
}

enableMocking().then(() => {
  createRoot(document.getElementById('root')).render(
    <StrictMode>
      <QueryClientProvider client={queryClient}>
        <AuthProvider>
          <BrowserRouter>
            <App />
          </BrowserRouter>
        </AuthProvider>
        {/* 개발 모드에서만 노출되는 React Query 디버깅 패널 */}
        <ReactQueryDevtools initialIsOpen={false} />
      </QueryClientProvider>
    </StrictMode>,
  )
})
