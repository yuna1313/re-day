import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      // 개발·미리보기에서 /api 요청을 백엔드로 전달한다.
      // 브라우저의 Origin과 맞도록 원래 Host를 유지한다.
      '/api': {
        target: 'http://localhost:8080',
      },
    },
  },
})
