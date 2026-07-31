import { defineConfig, devices } from '@playwright/test';

/**
 * Local test configuration - no dependencies, no webServer
 */
export default defineConfig({
  testDir: './e2e',
  timeout: 60 * 1000,
  expect: { timeout: 10000 },
  fullyParallel: false,
  workers: 1,
  reporter: [['list']],
  use: {
    baseURL: process.env.ADMIN_URL || 'http://localhost:3001',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    viewport: { width: 1280, height: 720 },
    actionTimeout: 15000,
    navigationTimeout: 15000,
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'], headless: false },
    },
    {
      name: 'chromium-headless',
      use: { ...devices['Desktop Chrome'], headless: true },
    },
  ],
});
