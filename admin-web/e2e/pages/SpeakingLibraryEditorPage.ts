import { Page, Locator, expect } from '@chromatic-com/playwright';

/** Page Object: /speaking/libraries/new и /speaking/libraries/:id/edit (AW-T14) */
export class SpeakingLibraryEditorPage {
  readonly page: Page;
  readonly pageTitle: Locator;
  readonly nameInput: Locator;
  readonly descriptionInput: Locator;
  readonly orderInput: Locator;
  readonly publishedSwitch: Locator;
  readonly saveButton: Locator;

  constructor(page: Page) {
    this.page = page;
    this.pageTitle = page.locator('[data-testid="page-title"]');
    this.nameInput = page.locator('[data-testid="library-name-input"] input');
    this.descriptionInput = page.locator('[data-testid="library-description-input"] textarea').first();
    this.orderInput = page.locator('[data-testid="library-order-input"] input');
    this.publishedSwitch = page.locator('[data-testid="library-published-switch"] input');
    this.saveButton = page.locator('[data-testid="save-library-button"]');
  }

  async fillForm(data: { name: string; description?: string; order?: number; published?: boolean }) {
    await this.nameInput.fill(data.name);
    if (data.description) await this.descriptionInput.fill(data.description);
    if (data.order !== undefined) await this.orderInput.fill(String(data.order));
    if (data.published) await this.publishedSwitch.click({ force: true });
  }

  async save() {
    await this.saveButton.click();
  }

  async expectSaved() {
    // create → редирект на список
    await expect(this.page).toHaveURL(/\/speaking\/libraries$/, { timeout: 15000 });
  }
}
