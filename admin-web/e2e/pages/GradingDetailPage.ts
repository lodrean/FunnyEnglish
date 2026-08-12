import { Page, Locator, expect } from '@chromatic-com/playwright';

/** Page Object: /grading/submissions/:id (AW-T14/15) */
export class GradingDetailPage {
  readonly page: Page;
  readonly pageTitle: Locator;
  readonly audioPlayer: Locator;
  readonly questions: Locator;
  readonly total: Locator;
  readonly comment: Locator;
  readonly saveButton: Locator;
  readonly editButton: Locator;
  readonly backButton: Locator;

  constructor(page: Page) {
    this.page = page;
    this.pageTitle = page.locator('[data-testid="student-card"]');
    this.audioPlayer = page.locator('[data-testid="submission-audio-player"]');
    this.questions = page.locator('[data-testid="submission-questions"]');
    this.total = page.locator('[data-testid="rubric-total"]');
    this.comment = page.locator('[data-testid="rubric-comment"] textarea').first();
    this.saveButton = page.locator('[data-testid="save-grade-button"]');
    this.editButton = page.locator('[data-testid="edit-grade-button"]');
    this.backButton = page.locator('[data-testid="header-back-button"]');
  }

  async expectLoaded() {
    await this.pageTitle.waitFor({ timeout: 15000 });
  }

  async expectAudioPlayer() {
    await expect(this.audioPlayer).toBeVisible();
    await expect(this.audioPlayer.locator('audio')).toHaveAttribute('src', /.+/);
  }

  async expectQuestionsVisible() {
    await expect(this.questions).toBeVisible();
  }

  /** Оценки выставляем через скрытый input MUI Slider (драг слайдера — флаки) */
  async setCriterion(criterion: 'grammar' | 'vocabulary' | 'pronunciation' | 'fluency', value: number) {
    const slider = this.page.locator(`[data-testid="rubric-slider-${criterion}"] input`);
    await slider.focus();
    await slider.press('Home'); // min = 1
    for (let i = 1; i < value; i++) await slider.press('ArrowRight');
  }

  async expectTotal(total: string) {
    await expect(this.total).toHaveText(total);
  }

  async fillComment(text: string) {
    await this.comment.fill(text);
  }

  async saveGrade() {
    await this.saveButton.click();
  }

  async expectStatusReviewed() {
    await expect(this.page.locator('[data-testid="submission-status-chip"]')).toHaveText('REVIEWED');
  }

  async expectFormReadonly() {
    await expect(this.page.locator('[data-testid="rubric-slider-grammar"] input')).toBeDisabled();
  }

  async backToInbox() {
    await this.backButton.click();
    await expect(this.page).toHaveURL(/\/grading(\?.*)?$/, { timeout: 15000 });
  }
}
