import { defineConfig, devices } from '@playwright/test';

/**
 * Playwright конфигурация для E2E тестирования So to Speak Admin Panel
 * @see https://playwright.dev/docs/test-configuration
 */
export default defineConfig({
  testDir: './e2e',
  
  /* Максимальное время выполнения одного теста */
  timeout: 30 * 1000,
  
  /* Ожидание перед выполнением каждого действия */
  expect: {
    timeout: 5000
  },
  
  /* Запускать тесты в файлах последовательно для стабильности */
  fullyParallel: false,
  
  /* Запрещать повторный запуск тестов в CI */
  forbidOnly: !!process.env.CI,
  
  /* Количество повторных попыток в CI */
  retries: process.env.CI ? 2 : 0,
  
  /* Количество параллельных worker-ов */
  workers: 1, // Sequential for stability
  
  /* Reporter для вывода результатов */
  reporter: [
    ['html', { outputFolder: 'playwright-report' }],
    ['list']
  ],
  
  /* Глобальные настройки для всех проектов */
  use: {
    /* Базовый URL для тестов */
    baseURL: process.env.ADMIN_URL || 'http://localhost:5173',
    
    /* Сохранять trace при первом повторе */
    trace: 'on-first-retry',
    
    /* Скриншоты при ошибках */
    screenshot: 'only-on-failure',
    
    /* Видео записи тестов */
    video: 'on-first-retry',
    
    /* Окно браузера */
    viewport: { width: 1280, height: 720 },
    
    /* Действия с пользовательской перспективой */
    actionTimeout: 15000,
    
    /* Навигация с таймаутом */
    navigationTimeout: 15000,
  },

  /* Configure projects for major browsers */
  projects: [
    // Setup project for authentication
    {
      name: 'setup',
      testMatch: /.*\.setup\.ts/,
    },
    
    // Auth tests - no storage state, clean browser for login/logout tests
    {
      name: 'auth-tests',
      testMatch: /tests\/auth\.spec\.ts/,
      use: { 
        ...devices['Desktop Chrome'],
        headless: true,
        // No storageState - clean browser for auth tests
      },
    },
    
    // Main test project with authenticated state
    {
      name: 'chromium',
      // auth и navigation (включая tests/navigation/*) имеют свои проекты — не дублируем
      testIgnore: [/tests\/auth\.spec\.ts/, /tests\/navigation\//],
      use: { 
        ...devices['Desktop Chrome'],
        // Use prepared auth state
        storageState: 'e2e/.auth/admin.json',
      },
      dependencies: ['setup'],
    },
    
    // Headless mode for CI (excludes auth tests)
    {
      name: 'chromium-headless',
      // Auth and navigation tests run separately (navigation/ — каталог, не только navigation.spec.ts)
      testIgnore: [/tests\/auth\.spec\.ts/, /tests\/navigation\//],
      use: { 
        ...devices['Desktop Chrome'],
        headless: true,
        storageState: 'e2e/.auth/admin.json',
      },
      dependencies: ['setup'],
    },
    
    // Navigation tests - handle their own auth
    {
      name: 'navigation-tests',
      testMatch: /tests\/navigation\/.*\.spec\.ts/,
      use: { 
        ...devices['Desktop Chrome'],
        headless: true,
        // No storageState - tests handle auth themselves
      },
    },

    // Mobile testing
    {
      name: 'Mobile Chrome',
      testIgnore: [/tests\/auth\.spec\.ts/, /tests\/navigation\//],
      use: { 
        ...devices['Pixel 5'],
        storageState: 'e2e/.auth/admin.json',
      },
      dependencies: ['setup'],
    },

    // Tablet testing
    {
      name: 'tablet',
      testIgnore: [/tests\/auth\.spec\.ts/, /tests\/navigation\//],
      use: {
        viewport: { width: 768, height: 1024 },
        storageState: 'e2e/.auth/admin.json',
      },
      dependencies: ['setup'],
    }
  ],

  /* Запуск локального dev сервера перед тестами */
  webServer: process.env.SKIP_WEB_SERVER ? undefined : {
    command: 'npm run dev',
    url: 'http://localhost:5173',
    reuseExistingServer: !process.env.CI,
    timeout: 120 * 1000,
  },
});
