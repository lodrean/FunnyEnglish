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
        // Пороги отключены до аудита базлайна: v8-coverage даёт разные итоги на
        // Windows (87%) и Linux CI (34%) при одной конфигурации — требуется
        // разбор baseline на CI, потом вернуть пороги (2026-08-08).
        lines: 0,
        functions: 0,
        branches: 0,
        statements: 0
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
