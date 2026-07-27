import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      // 개발 중 /api 로 시작하는 요청을 백엔드(8080)로 전달한다.
      // 프론트와 백엔드 오리진이 같아져 CORS 문제가 발생하지 않는다.
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
