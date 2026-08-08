import { defineConfig, devices } from '@playwright/test';

/**
 * Playwright configuration for CMP (Compose Multiplatform) E2E testing
 * Tests WASM web target running in browser
 */
export default defineConfig({
  testDir: './tests',
  
  /* Maximum time one test can run */
  timeout: 60 * 1000,
  
  expect: {
    timeout: 10000
  },
  
  /* Run tests sequentially for stability */
  fullyParallel: false,
  
  /* Fail the build on CI if you accidentally left test.only */
  forbidOnly: !!process.env.CI,
  
  /* Retry on CI only */
  retries: process.env.CI ? 2 : 0,
  
  /* Opt out of parallel tests */
  workers: 1,
  
  /* Reporter */
  reporter: [
    ['html', { outputFolder: 'playwright-report' }],
    ['list']
  ],

  /* Гигиена dev-стека (purge E2E-библиотек) перед прогоном */
  globalSetup: './global-setup.ts',
  
  /* Shared settings */
  use: {
    baseURL: process.env.CMP_URL || 'http://localhost:8082',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'on-first-retry',
    viewport: { width: 1280, height: 720 },
    actionTimeout: 15000,
    navigationTimeout: 15000,
  },

  /* Configure projects */
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
    {
      name: 'Mobile Chrome',
      use: { ...devices['Pixel 5'] },
    },
  ],

  /* Start WASM dev server before tests */
  webServer: process.env.SKIP_WEB_SERVER ? undefined : {
    // Cross-platform: на Windows gradlew — это .bat (cmd не понимает ../gradlew)
    command: process.platform === 'win32'
      ? 'cd ..\\composeApp && ..\\gradlew.bat wasmJsBrowserDevelopmentRun --quiet'
      : 'cd ../composeApp && ../gradlew wasmJsBrowserDevelopmentRun --quiet',
    url: 'http://localhost:8082',
    reuseExistingServer: !process.env.CI,
    timeout: 600 * 1000, // 10 minutes for WASM compilation
    env: {
      // Disable configuration cache for stability
      GRADLE_OPTS: '-Dorg.gradle.configuration-cache=false'
    }
  },
});
