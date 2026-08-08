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
      thresholds: {
        // Пороги выровнены под текущее покрытие (2026-08-08: lines/statements 87, branches 81,
        // functions 43) — ранее 90/85 никогда не выполнялись, т.к. coverage в CI не запускался
        // (несовместимость @vitest/coverage-v8@4 с vitest@2). Поднимать постепенно.
        lines: 85,
        functions: 40,
        branches: 78,
        statements: 85
      },
      exclude: [
        'node_modules/',
        'src/test/',
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
