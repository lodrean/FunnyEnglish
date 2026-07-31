import { defineConfig, devices } from '@playwright/test';

/**
 * Playwright конфигурация для визуального регрессионного тестирования через Chromatic
 * @see https://www.chromatic.com/docs/playwright/
 */
export default defineConfig({
  testDir: './e2e',
  
  /* Максимальное время выполнения одного теста */
  timeout: 60 * 1000,
  
  /* Ожидание перед выполнением каждого действия */
  expect: {
    timeout: 10000
  },
  
  /* Запускать тесты в файлах последовательно для стабильности */
  fullyParallel: false,
  
  /* Запрещать повторный запуск тестов в CI */
  forbidOnly: !!process.env.CI,
  
  /* Количество повторных попыток в CI */
  retries: process.env.CI ? 1 : 0,
  
  /* Количество параллельных worker-ов */
  workers: 1,
  
  /* Output directory for test results */
  outputDir: 'test-results',
  
  /* Reporter для вывода результатов */
  reporter: [
    ['html', { outputFolder: 'playwright-report' }],
    ['list']
  ],
  
  /* Глобальные настройки для всех проектов */
  use: {
    /* Базовый URL для тестов */
    baseURL: process.env.ADMIN_URL || 'http://localhost:5173',
    
    /* Скриншоты для каждого теста (необходимо для Chromatic) */
    screenshot: 'on',
    
    /* Trace для отладки */
    trace: 'on-first-retry',
    
    /* Окно браузера */
    viewport: { width: 1280, height: 720 },
    
    /* Действия с пользовательской перспективой */
    actionTimeout: 15000,
    
    /* Навигация с таймаутом */
    navigationTimeout: 15000,
    
    /* Задержка перед скриншотами для стабильности */
    delay: 500,
  },

  /* Configure projects for major browsers */
  projects: [
    // Setup project for authentication
    {
      name: 'setup',
      testMatch: /.*\.setup\.ts/,
    },
    
    // Chromatic visual tests - desktop
    {
      name: 'chromatic-desktop',
      testIgnore: /tests\/auth\.spec\.ts/, // Auth tests run separately
      use: { 
        ...devices['Desktop Chrome'],
        headless: true,
        storageState: 'e2e/.auth/admin.json',
      },
      dependencies: ['setup'],
    },

    // Chromatic visual tests - mobile
    {
      name: 'chromatic-mobile',
      testIgnore: /tests\/auth\.spec\.ts/,
      use: { 
        ...devices['Pixel 5'],
        headless: true,
        storageState: 'e2e/.auth/admin.json',
      },
      dependencies: ['setup'],
    },
  ],

  /* Запуск локального dev сервера перед тестами */
  webServer: process.env.SKIP_WEB_SERVER ? undefined : {
    command: 'npm run dev',
    url: 'http://localhost:5173',
    reuseExistingServer: !process.env.CI,
    timeout: 120 * 1000,
  },
});
