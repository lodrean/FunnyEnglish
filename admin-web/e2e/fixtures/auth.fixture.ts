import { test as base } from '@playwright/test';

/**
 * Типы для фикстур аутентификации
 */
type AuthFixtures = {
  authenticatedPage: {
    login: (email: string, password: string) => Promise<void>;
    logout: () => Promise<void>;
    isAuthenticated: () => Promise<boolean>;
  };
  adminCredentials: {
    email: string;
    password: string;
  };
};

/**
 * Расширенный test с фикстурами для аутентификации
 * 
 * @example
 * ```typescript
 * import { test } from '../fixtures/auth.fixture';
 * 
 * test('admin can view dashboard', async ({ page, authenticatedPage }) => {
 *   await authenticatedPage.login('admin@example.com', 'password');
 *   await expect(page).toHaveURL('/dashboard');
 * });
 * ```
 */
export const test = base.extend<AuthFixtures>({
  // Фикстура с учетными данными админа
  adminCredentials: {
    email: process.env.ADMIN_EMAIL || 'admin@funnyenglish.com',
    password: process.env.ADMIN_PASSWORD || 'admin123',
  },

  // Фикстура для аутентифицированных действий
  authenticatedPage: async ({ page }, use) => {
    const auth = {
      async login(email: string, password: string) {
        await page.goto('/login');
        await page.waitForSelector('input[type="email"]', { state: 'visible' });
        
        await page.fill('input[type="email"]', email);
        await page.fill('input[type="password"]', password);
        
        await page.click('button[type="submit"]');
        
        // Ждем редиректа после успешного логина
        await page.waitForURL(/\/(dashboard|home)/, { timeout: 10000 });
      },

      async logout() {
        // Ищем кнопку выхода - может быть в меню профиля
        const logoutButton = page.locator('text=Выйти, text=Logout, [data-testid="logout"]').first();
        
        if (await logoutButton.isVisible().catch(() => false)) {
          await logoutButton.click();
        } else {
          // Открываем меню профиля если нужно
          const profileMenu = page.locator('[data-testid="profile-menu"], [aria-label="profile"]').first();
          if (await profileMenu.isVisible().catch(() => false)) {
            await profileMenu.click();
            await page.click('text=Выйти');
          }
        }
        
        await page.waitForURL('/login', { timeout: 10000 });
      },

      async isAuthenticated() {
        const currentUrl = page.url();
        return !currentUrl.includes('/login');
      }
    };

    await use(auth);
  },
});

export { expect } from '@playwright/test';
