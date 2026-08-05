import { Page, Locator, expect } from '@chromatic-com/playwright';

/** Page Object: /speaking/libraries (AW-T14) */
export class SpeakingLibrariesPage {
  readonly page: Page;
  readonly pageTitle: Locator;
  readonly addButton: Locator;
  readonly searchInput: Locator;
  readonly table: Locator;

  constructor(page: Page) {
    this.page = page;
    this.pageTitle = page.locator('[data-testid="page-title"]');
    this.addButton = page.locator('[data-testid="add-library-button"]');
    this.searchInput = page.locator('[data-testid="search-libraries"] input');
    this.table = page.locator('[data-testid="libraries-table"]');
  }

  async goto() {
    await this.page.goto('/speaking/libraries');
    await this.pageTitle.waitFor({ timeout: 15000 });
    // Ждём конца загрузки данных (skeleton → таблица/empty), иначе кнопка Add «не стабильна»
    await this.page
      .locator('[data-testid="libraries-table"], [data-testid="libraries-empty"]')
      .first()
      .waitFor({ timeout: 15000 });
  }

  async clickAdd() {
    // На mobile кнопка в широкой шапке иногда «not stable» (непрерывный reflow
    // из-за горизонтального скролла таблицы) — dispatchEvent надёжнее
    await this.addButton.dispatchEvent('click');
  }

  async search(name: string) {
    await this.searchInput.fill(name);
  }

  row(name: string): Locator {
    return this.table.locator('tr', { hasText: name });
  }

  async togglePublish(rowName: string) {
    const row = this.row(rowName);
    // MUI Switch input скрыт (№22г) + на mobile за пределами viewport — dispatchEvent
    await row.locator('[data-testid^="publish-switch-"] input').dispatchEvent('click');
  }

  async deleteLibrary(rowName: string) {
    const row = this.row(rowName);
    // На mobile кнопка обрезана широкой таблицей — dispatchEvent
    await row.locator('[data-testid^="delete-library-"]').dispatchEvent('click');
    // ConfirmDialog: на mobile анимация диалога перехватывает клик — dispatchEvent
    await this.page.getByRole('button', { name: 'Удалить' }).dispatchEvent('click');
  }

  async expectRowVisible(name: string) {
    await expect(this.row(name)).toBeVisible();
  }
}
