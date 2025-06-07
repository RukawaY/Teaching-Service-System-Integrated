import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    // send all requests with /api to the backend
    proxy: {
      '/test': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/arrange': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      }
    },
    // front end port
    port: 3000,
  },
})
