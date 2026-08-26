import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    // Forward API calls to the Spring Boot backend during development, so the
    // browser never sees a different origin and we don't need to deal with CORS
    // at all in this phase. In production (Phase 5) the frontend build and the
    // backend will need a different arrangement - decided when we get there.
    proxy: {
      '/admin': 'http://localhost:8080',
      '/auth': 'http://localhost:8080',
    },
  },
})
