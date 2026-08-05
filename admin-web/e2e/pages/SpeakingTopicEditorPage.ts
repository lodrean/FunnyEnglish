import { Page, Locator, expect } from '@chromatic-com/playwright';

/** Page Object: /speaking/topics/new и /speaking/topics/:id/edit (AW-T14) */
export class SpeakingTopicEditorPage {
  readonly page: Page;
  readonly pageTitle: Locator;
  readonly librarySelect: Locator;
  readonly nameInput: Locator;
  readonly durationInput: Locator;
  readonly orderInput: Locator;
  readonly saveButton: Locator;
  readonly questionsTab: Locator;

  constructor(page: Page) {
    this.page = page;
    this.pageTitle = page.locator('[data-testid="page-title"]');
    this.librarySelect = page.locator('[data-testid="topic-library-select"]');
    this.nameInput = page.locator('[data-testid="topic-name-input"] input');
    this.durationInput = page.locator('[data-testid="topic-duration-input"] input');
    this.orderInput = page.locator('[data-testid="topic-order-input"] input');
    this.saveButton = page.locator('[data-testid="save-topic-button"]');
    this.questionsTab = page.locator('[data-testid="tab-questions"]');
  }

  /** MUI Select — выбор клавиатурой (грабля №22в) */
  async selectLibrary(name: string) {
    await this.librarySelect.locator('[role="combobox"]').click();
    const option = this.page.getByRole('option', { name });
    await option.waitFor();
    await this.page.waitForTimeout(350);
    await option.press('Enter');
  }

  async fillName(name: string) {
    await this.nameInput.fill(name);
  }

  /** Загрузка через скрытый input в dropzone (НЕ drag&drop — флаки) */
  async uploadVideo(path: string) {
    await this.page
      .locator('[data-testid="topic-video-uploader"] input[type="file"]')
      .setInputFiles(path);
    // превью видео после загрузки
    await expect(
      this.page.locator('[data-testid="topic-video-uploader"] video')
    ).toBeVisible({ timeout: 30000 });
  }

  async uploadSubtitles(path: string) {
    await this.page
      .locator('[data-testid="topic-subtitles-uploader"] input[type="file"]')
      .setInputFiles(path);
    await expect(
      this.page.locator('[data-testid="topic-subtitles-uploader"] a')
    ).toBeVisible({ timeout: 30000 });
  }

  async setDuration(seconds: number) {
    await this.durationInput.fill(String(seconds));
  }

  async save() {
    await this.saveButton.click();
  }

  /** create → редирект на :id/edit, вкладка Questions активна */
  async expectCreated() {
    await expect(this.page).toHaveURL(/\/speaking\/topics\/[0-9a-f-]+\/edit$/, { timeout: 20000 });
    await expect(this.questionsTab).toBeEnabled();
  }

  async switchToQuestionsTab() {
    // MUI Tab: click иногда не переключает (фокус без select) — focus + Enter надёжнее
    await this.questionsTab.focus();
    await this.questionsTab.press('Enter');
    await expect(this.page.locator('[data-testid="tab-questions"]')).toHaveAttribute(
      'aria-selected',
      'true'
    );
  }

  async addQuestion(text: string) {
    const input = this.page.locator('[placeholder="New question text…"]');
    await input.fill(text);
    await this.page.locator('[data-testid="add-question-button"]').click();
    // Ждём именно question-item (не textarea, которая ещё содержит текст)
    await expect(this.questionItem(text)).toBeVisible();
    // Ждём очистку input в onSuccess — иначе следующее добавление гоняется с ним
    await expect(input).toHaveValue('');
  }

  questionItem(text: string): Locator {
    return this.page.locator('[data-testid^="question-item-"]', { hasText: text });
  }

  async moveQuestionDown(text: string) {
    await this.questionItem(text).locator('[data-testid^="question-down-"]').click();
  }

  async saveOrder() {
    await this.page.locator('[data-testid="save-order-button"]').click();
    // Ждём завершение цепочки PUT (иначе reload отменит в полёте)
    await expect(this.page.getByText('Порядок сохранён')).toBeVisible({ timeout: 15000 });
  }

  async expectQuestionOrder(first: string, second: string) {
    const items = this.page.locator('[data-testid^="question-item-"]');
    await expect(items.nth(0)).toContainText(first);
    await expect(items.nth(1)).toContainText(second);
  }

  async deleteQuestion(text: string) {
    const item = this.questionItem(text);
    await item.locator('button').last().click();
    await this.page.getByRole('button', { name: 'Удалить' }).click();
    await expect(this.page.locator('[data-testid^="question-item-"]', { hasText: text })).toHaveCount(0);
  }
}
