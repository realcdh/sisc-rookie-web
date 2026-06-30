import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// /api 요청을 백엔드(8080)로 프록시한다. 같은 출처로 호출되므로 CORS 설정이 필요 없다.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
});
