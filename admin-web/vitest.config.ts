import { defineConfig } from 'vitest/config';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: ['./src/test/setup.ts'],
    css: true,
    exclude: [
      'node_modules',
      'e2e/**/*',
      '**/e2e/**/*',
      'playwright.config.ts',
      'playwright.*.config.ts'
    ],
    coverage: {
      provider: 'v8',
      reporter: ['text', 'json', 'html'],
      // Пороги по факту зелёного CI-прогона (артефакт coverage-final, run 33985994607):
      // без e2e/ в отчёте — statements 47.7%, functions 49.2%, branches 82.8%.
      // Консервативный запас вниз (грабля №88: пороги = 0 → мёртвые; расхождение
      // Windows/CI давал случайный coverage- include e2e-скриптов Playwright).
      thresholds: {
        lines: 40,
        functions: 40,
        branches: 75,
        statements: 40
      },
      exclude: [
        'node_modules/',
        'src/test/',
        'e2e/**',
        '**/*.d.ts',
        '**/*.config.*',
        '**/*.stories.tsx',
        '**/index.ts'
      ]
    }
  },
  resolve: {
    alias: {
      '@': '/src'
    }
  }
});
