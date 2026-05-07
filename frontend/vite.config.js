import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  test: {
    environment: 'jsdom',
    setupFiles: './src/test/setupTests.js',
    coverage: {
      reporter: ['text', 'html', 'lcov'],
      include: ['src/utils/**/*.js', 'src/services/**/*.js', 'src/components/**/*.jsx']
    }
  }
});
