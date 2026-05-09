import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  test: {
    environment: 'jsdom',
    setupFiles: './src/test/setupTests.js',

    // Chỉ cho Vitest chạy unit/integration test trong src
    include: ['src/**/*.{test,spec}.{js,jsx,ts,tsx}'],

    // Không cho Vitest chạy nhầm Playwright E2E
    exclude: ['node_modules', 'dist', 'coverage', 'e2e/**', 'playwright-report/**'],

    coverage: {
      reporter: ['text', 'html', 'lcov'],
      include: ['src/utils/**/*.js', 'src/services/**/*.js', 'src/components/**/*.jsx']
    }
  }
});