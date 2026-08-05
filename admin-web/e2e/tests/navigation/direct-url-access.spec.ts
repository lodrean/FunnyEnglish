import { test, expect } from '@chromatic-com/playwright';

/**
 * Тесты для проверки прямого доступа по URL
 * 
 * Проблема: при переходе непосредственно к тесту происходит 
 * сброс навигации на дашборд
 * 
 * Фикс: ProtectedRoute теперь ждет завершения auth initialization
 * перед проверкой isAuthenticated
 */
test.describe('Direct URL Access', () => {
  test('должен открывать тест по прямому URL без редиректа на дашборд', async ({ page }) => {
    // Логинимся через UI
    await page.goto('/login');
    await page.waitForLoadState('networkidle');
    await expect(page.locator('text=Sign In')).toBeVisible();
    
    await page.getByLabel('Email').fill(process.env.TEST_ADMIN_EMAIL || 'admin@sotospeak.com');
    await page.getByLabel('Password').fill(process.env.TEST_ADMIN_PASSWORD || 'admin123');
    await page.getByRole('button', { name: 'Sign In' }).click();
    
    // Ждем редиректа на дашборд
    await expect(page).toHaveURL('/', { timeout: 10000 });
    
    // Переходим напрямую на /speaking/topics
    await page.goto('/speaking/topics');
    await page.waitForLoadState('networkidle');
    
    // Проверяем что URL содержит /topics
    await expect(page).toHaveURL(/.*topics.*/);
    
    // Проверяем что URL не просто /
    const url = page.url();
    expect(url).not.toBe('http://localhost:3000/');
    expect(url).not.toBe('http://localhost:3000');
  });

  test('должен сохранять URL при обновлении страницы', async ({ page }) => {
    // Логинимся
    await page.goto('/login');
    await page.waitForLoadState('networkidle');
    await page.getByLabel('Email').fill(process.env.TEST_ADMIN_EMAIL || 'admin@sotospeak.com');
    await page.getByLabel('Password').fill(process.env.TEST_ADMIN_PASSWORD || 'admin123');
    await page.getByRole('button', { name: 'Sign In' }).click();
    await expect(page).toHaveURL('/', { timeout: 10000 });
    
    // Переходим на Speaking topics
    await page.goto('/speaking/topics');
    await page.waitForLoadState('networkidle');
    
    // Проверяем начальный URL
    await expect(page).toHaveURL(/.*topics.*/);
    
    // Обновляем страницу
    await page.reload();
    await page.waitForLoadState('networkidle');
    
    // Проверяем что URL сохранился
    await expect(page).toHaveURL(/.*topics.*/);
  });

  test('не должен редиректить на дашборд для разных защищенных роутов', async ({ page }) => {
    // Логинимся
    await page.goto('/login');
    await page.waitForLoadState('networkidle');
    await page.getByLabel('Email').fill(process.env.TEST_ADMIN_EMAIL || 'admin@sotospeak.com');
    await page.getByLabel('Password').fill(process.env.TEST_ADMIN_PASSWORD || 'admin123');
    await page.getByRole('button', { name: 'Sign In' }).click();
    await expect(page).toHaveURL('/', { timeout: 10000 });
    
    const routes = ['/speaking/topics', '/speaking/libraries', '/users', '/settings'];
    
    for (const route of routes) {
      await page.goto(route);
      await page.waitForLoadState('networkidle');
      
      // Проверяем что мы не на главной странице
      const url = page.url();
      expect(url).not.toBe('http://localhost:3000/');
      expect(url).not.toBe('http://localhost:3000');
      
      // URL должен содержать часть пути
      const pathPart = route.replace('/speaking/', '').replace('/', '');
      expect(url.toLowerCase()).toContain(pathPart.toLowerCase());
    }
  });

  test('должен восстанавливать сессию после перезагрузки браузера', async ({ page }) => {
    // Логинимся
    await page.goto('/login');
    await page.waitForLoadState('networkidle');
    await page.getByLabel('Email').fill(process.env.TEST_ADMIN_EMAIL || 'admin@sotospeak.com');
    await page.getByLabel('Password').fill(process.env.TEST_ADMIN_PASSWORD || 'admin123');
    await page.getByRole('button', { name: 'Sign In' }).click();
    await expect(page).toHaveURL('/', { timeout: 10000 });
    
    // Переходим на Speaking topics
    await page.goto('/speaking/topics');
    await page.waitForLoadState('networkidle');
    await expect(page).toHaveURL(/.*topics.*/);
    
    // Перезагружаем страницу (имитирует закрытие/открытие браузера)
    await page.reload();
    await page.waitForLoadState('networkidle');
    
    // Проверяем что сессия восстановлена и URL сохранился
    await expect(page).toHaveURL(/.*topics.*/);
    
    // Проверяем что мы не на логине
    const url = page.url();
    expect(url).not.toContain('/login');
  });
});
