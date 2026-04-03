import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    // Proxy API requests to the Spring Boot backend
    // Frontend: http://localhost:3000
    // Backend:  http://localhost:8080
    // Any request to /api/* gets forwarded to the backend
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
