import { Page, Locator, expect } from '@chromatic-com/playwright';

/**
 * Page Object для Dashboard страницы
 */
export class DashboardPage {
  readonly page: Page;
  readonly pageTitle: Locator;
  readonly sidebar: Locator;
  readonly statsCards: Locator;
  readonly navigationLinks: Locator;

  constructor(page: Page) {
    this.page = page;
    this.pageTitle = page.locator('[data-testid="page-title"]');
    // Mobile drawer — temporary MUI Drawer в Modal-портале; закрытый drawer
    // имеет translateX(-w), поэтому :visible по paper не работает —
    // признак открытия: видимый .MuiBackdrop-root. Desktop — nav/aside.
    this.sidebar = page.locator('.MuiModal-root .MuiDrawer-paper, nav:visible, aside:visible, [role="navigation"]:visible').first();
    this.statsCards = page.locator('[data-testid="stat-card"], .MuiCard-root');
    // Новый AdminLayout: пункты меню — MUI Button (не anchor <a>)
    // Пункты меню — MUI ListItemButton (div[role=button], НЕ <button>)
    this.navigationLinks = page.locator('nav:visible a, nav:visible [role="button"], aside:visible a, aside:visible [role="button"], .MuiModal-root .MuiDrawer-paper a, .MuiModal-root .MuiDrawer-paper [role="button"], [data-testid^="nav-"]');
  }

  /**
   * Переход на дашборд
   */
  async goto() {
    await this.page.goto('/');
    // networkidle нестабилен — ждём заголовок страницы
    await this.pageTitle.waitFor({ timeout: 15000 });
  }

  /**
   * На mobile/tablet sidebar — drawer (скрыт по умолчанию). Открываем его,
   * если скрыт, перед проверками навигации.
   */
  async ensureSidebarOpen() {
    // Desktop: виден постоянный sidebar с пунктами меню (nav-* testid)
    const desktopItems = this.page.locator('nav [data-testid^="nav-"]:visible, aside [data-testid^="nav-"]:visible');
    if (await desktopItems.count() > 0) return;
    // Mobile drawer уже открыт?
    const drawerItem = this.page.locator('.MuiModal-root .MuiDrawer-paper [data-testid^="nav-"]').first();
    if (await drawerItem.isVisible().catch(() => false)) return;
    // Открываем drawer и ждём видимые пункты меню
    await this.page.getByRole('button', { name: 'toggle sidebar' }).first().click();
    await expect(drawerItem).toBeVisible({ timeout: 5000 });
  }

  /**
   * Проверка загрузки страницы
   */
  async expectPageLoaded() {
    await expect(this.pageTitle).toContainText('Dashboard', { timeout: 10000 });
  }

  /**
   * Получить заголовок страницы
   */
  async getPageTitle(): Promise<string> {
    return await this.pageTitle.textContent() || '';
  }

  /**
   * Открыть меню пользователя
   */
  async openUserMenu() {
    await this.page.click('[data-testid="user-menu-button"]');
    await this.page.waitForSelector('[data-testid="logout-menu-item"]', { state: 'visible' });
  }

  /**
   * Выход из системы
   */
  async logout() {
    await this.openUserMenu();
    await this.page.click('[data-testid="logout-menu-item"]');
    await this.page.waitForURL(/.*login.*/, { timeout: 10000 });
  }

  /**
   * Получить количество стат-карточек
   */
  async getStatsCardsCount(): Promise<number> {
    return await this.statsCards.count();
  }

  /**
   * Проверка наличия элемента навигации
   */
  async hasNavigationItem(text: string): Promise<boolean> {
    // Ищем элемент меню по тексту в sidebar/nav/mobile-drawer.
    // В DOM текст есть и в СКРЫТОМ desktop-drawer (display:none на mobile),
    // поэтому проверяем видимость КАЖДОГО совпадения, а не только first().
    const scopes = this.page.locator('nav, aside, .MuiModal-root .MuiDrawer-paper');
    const items = scopes.locator(`text=${text}`);
    const count = await items.count();
    for (let i = 0; i < count; i++) {
      if (await items.nth(i).isVisible().catch(() => false)) return true;
    }
    return false;
  }
}
