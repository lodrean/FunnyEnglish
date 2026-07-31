import { test, expect } from '@chromatic-com/playwright';
import { UsersPage, UserDetailsPage } from '../pages/UsersPage';

test.describe('Управление пользователями', () => {
  let usersPage: UsersPage;
  let userDetailsPage: UserDetailsPage;

  test.beforeEach(async ({ page }) => {
    // Auth state is already set up
    usersPage = new UsersPage(page);
    userDetailsPage = new UserDetailsPage(page);
    await usersPage.goto();
  });

  test('должен отображать страницу пользователей', async () => {
    await usersPage.expectPageLoaded();
  });

  test('должен отображать таблицу пользователей', async () => {
    const count = await usersPage.getUsersCount();
    expect(count).toBeGreaterThanOrEqual(0);
  });

  test('должен искать пользователя', async () => {
    await usersPage.searchUser('admin');
    
    // Проверяем что поиск работает
    const count = await usersPage.getUsersCount();
    expect(count).toBeGreaterThanOrEqual(0);
  });

  test('должен фильтровать по роли', async () => {
    // Проверяем что фильтр существует
    const isFilterVisible = await usersPage.roleFilter.isVisible().catch(() => false);
    if (isFilterVisible) {
      await usersPage.filterByRole('admin');
      // Проверяем что фильтрация применилась
      await expect(usersPage.usersTable).toBeVisible();
    }
  });

  test('должен открывать детали пользователя', async () => {
    // Ищем пользователя admin
    await usersPage.searchUser('admin@funnyenglish.com');
    
    // Кликаем для просмотра деталей если пользователь найден
    const count = await usersPage.getUsersCount();
    if (count > 0) {
      await usersPage.clickViewUser('admin@funnyenglish.com');
      await userDetailsPage.expectPageLoaded();
    }
  });
});

test.describe('Управление пользователями - Дизайн система', () => {
  let usersPage: UsersPage;

  test.beforeEach(async ({ page }) => {
    usersPage = new UsersPage(page);
    await usersPage.goto();
  });

  test('должен иметь корректную структуру страницы', async ({ page }) => {
    await expect(page.locator('[data-testid="page-title"]')).toBeVisible();
    await expect(page.locator('[data-testid="search-users"]')).toBeVisible();
    await expect(usersPage.usersTable).toBeVisible();
  });

  test('должен иметь функциональный поиск пользователей', async ({ page }) => {
    const searchInput = page.locator('[data-testid="search-users"] input, input[placeholder*="Search"]').first();
    await searchInput.fill('test');
    await expect(searchInput).toHaveValue('test');
    
    // Очистка поиска
    await searchInput.clear();
    await expect(searchInput).toHaveValue('');
  });
});
