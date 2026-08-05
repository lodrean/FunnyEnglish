import { Page, Locator, expect } from '@chromatic-com/playwright';

/** Page Object: /grading (AW-T14/15) */
export class GradingInboxPage {
  readonly page: Page;
  readonly pageTitle: Locator;
  readonly statusFilter: Locator;
  readonly table: Locator;

  constructor(page: Page) {
    this.page = page;
    this.pageTitle = page.locator('[data-testid="page-title"]');
    this.statusFilter = page.locator('[data-testid="filter-status-select"]');
    this.table = page.locator('[data-testid="submissions-table"]');
  }

  async goto() {
    await this.page.goto('/grading');
    await this.pageTitle.waitFor({ timeout: 15000 });
    // Ждём конца загрузки данных (стабильность layout на mobile)
    await this.page
      .locator('[data-testid="submissions-table"] tr, [data-testid="submissions-empty"]')
      .first()
      .waitFor({ timeout: 15000 });
  }

  /** MUI Select — клавиатурой (грабля №22в) */
  async filterStatus(status: 'All' | 'NEW' | 'REVIEWED') {
    await this.statusFilter.locator('[role="combobox"]').click();
    const option = this.page.getByRole('option', { name: status, exact: true });
    await option.waitFor();
    await this.page.waitForTimeout(350);
    await option.press('Enter');
  }

  row(studentEmail: string): Locator {
    return this.table.locator('tr', { hasText: studentEmail });
  }

  async openSubmission(studentEmail: string) {
    // На mobile таблица шире viewport — обычный click таймаутится (кнопка обрезана),
    // dispatchEvent срабатывает на React onClick напрямую
    await this.row(studentEmail).locator('[data-testid^="review-submission-"]').dispatchEvent('click');
  }

  async expectRowWithStatus(studentEmail: string, status: 'NEW' | 'REVIEWED') {
    await expect(this.row(studentEmail).getByText(status, { exact: true })).toBeVisible();
  }

  async expectNoRow(studentEmail: string) {
    await expect(this.row(studentEmail)).toHaveCount(0);
  }

  async expectScoreInRow(studentEmail: string, score: string) {
    await expect(this.row(studentEmail).getByText(score)).toBeVisible();
  }
}
