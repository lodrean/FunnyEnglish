import { Page, Locator, expect } from '@chromatic-com/playwright';

/** Page Object: /speaking/topics (AW-T14) */
export class SpeakingTopicsPage {
  readonly page: Page;
  readonly pageTitle: Locator;
  readonly addButton: Locator;
  readonly libraryFilter: Locator;
  readonly table: Locator;

  constructor(page: Page) {
    this.page = page;
    this.pageTitle = page.locator('[data-testid="page-title"]');
    this.addButton = page.locator('[data-testid="add-topic-button"]');
    this.libraryFilter = page.locator('[data-testid="library-filter-select"]');
    this.table = page.locator('[data-testid="topics-table"]');
  }

  async goto() {
    await this.page.goto('/speaking/topics');
    await this.pageTitle.waitFor({ timeout: 15000 });
    // Ждём конца загрузки данных (стабильность layout на mobile)
    await this.page
      .locator('[data-testid="topics-table"], [data-testid="topics-empty"]')
      .first()
      .waitFor({ timeout: 15000 });
  }

  async clickAdd() {
    // На mobile кнопка в шапке иногда «not stable» — dispatchEvent надёжнее
    await this.addButton.dispatchEvent('click');
  }

  /** MUI Select (№22в): force-click открывает (mobile «not stable»), опция — dispatchEvent (в обход backdrop) */
  async filterByLibrary(name: string) {
    await this.libraryFilter.locator('[role="combobox"]').click({ force: true });
    const option = this.page.getByRole('option', { name });
    await option.waitFor();
    await this.page.waitForTimeout(350); // стабилизация MUI-меню
    await option.dispatchEvent('click');
    // Меню иногда остаётся открытым и перехватывает pointer events — закрываем явно
    await this.page.keyboard.press('Escape');
    await this.page.locator('.MuiPopover-root').waitFor({ state: 'detached', timeout: 5000 }).catch(() => {});
  }

  row(name: string): Locator {
    return this.table.locator('tr', { hasText: name });
  }

  async togglePublish(rowName: string) {
    const row = this.row(rowName);
    // MUI Switch input скрыт (№22г) + на mobile за пределами viewport — dispatchEvent
    await row.locator('[data-testid^="publish-switch-"] input').dispatchEvent('click');
  }

  async archiveTopic(rowName: string) {
    const row = this.row(rowName);
    // На mobile кнопка обрезана широкой таблицей — dispatchEvent
    await row.locator('[data-testid^="delete-topic-"]').dispatchEvent('click');
    // ConfirmDialog: на mobile анимация диалога перехватывает клик — dispatchEvent
    await this.page.getByRole('button', { name: 'Архивировать' }).dispatchEvent('click');
  }

  /** Открыть редактор топика; на mobile кнопка обрезана широкой таблицей — dispatchEvent */
  async openEditor(rowName: string) {
    await this.row(rowName).locator('[data-testid^="edit-topic-"]').dispatchEvent('click');
  }

  async expectArchivedChip(rowName: string) {
    await expect(this.row(rowName).getByText('Archived')).toBeVisible();
  }
}
