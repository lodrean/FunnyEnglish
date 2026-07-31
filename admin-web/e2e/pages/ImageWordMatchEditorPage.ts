import { Page, Locator, expect } from '@chromatic-com/playwright';

/**
 * Page Object для редактора Image-Word-Match вопросов
 * 
 * Поддерживает многошаговый процесс создания:
 * Step 1: Upload Image
 * Step 2: Add Words
 * Step 3: Draw Hotspots
 * Step 4: Preview & Save
 */
export class ImageWordMatchEditorPage {
  readonly page: Page;
  
  // Header
  readonly editorContainer: Locator;
  readonly stepIndicator: Locator;
  readonly cancelButton: Locator;
  readonly savedChip: Locator;
  
  // Step 1: Image Upload
  readonly imageStep: Locator;
  readonly imageUploadInput: Locator;
  readonly imageUploadLabel: Locator;
  readonly imagePreview: Locator;
  readonly changeImageButton: Locator;
  readonly continueToWordsButton: Locator;
  
  // Step 2: Words
  readonly wordsStep: Locator;
  readonly wordInput: Locator;
  readonly translationInput: Locator;
  readonly addWordButton: Locator;
  readonly wordList: Locator;
  readonly continueToHotspotsButton: Locator;
  readonly backToImageButton: Locator;
  readonly instructionInput: Locator;
  
  // Step 3: Hotspots
  readonly hotspotsStep: Locator;
  readonly canvasContainer: Locator;
  readonly selectToolButton: Locator;
  readonly rectangleToolButton: Locator;
  readonly circleToolButton: Locator;
  readonly zoomInButton: Locator;
  readonly zoomOutButton: Locator;
  readonly resetZoomButton: Locator;
  readonly zoomLevel: Locator;
  readonly deleteHotspotButton: Locator;
  readonly propertiesPanel: Locator;
  readonly wordLinkSelect: Locator;
  readonly progressBar: Locator;
  readonly progressText: Locator;
  readonly previewButton: Locator;
  readonly backToWordsButton: Locator;
  
  // Step 4: Preview
  readonly previewStep: Locator;
  readonly previewImage: Locator;
  readonly previewHotspots: Locator;
  readonly previewWordList: Locator;
  readonly saveQuestionButton: Locator;
  readonly backToHotspotsButton: Locator;
  readonly saveErrorAlert: Locator;
  readonly saveSuccessAlert: Locator;

  constructor(page: Page) {
    this.page = page;
    
    // Header
    this.editorContainer = page.locator('[data-testid="image-word-match-editor"]');
    this.stepIndicator = page.locator('.step-indicator');
    this.cancelButton = page.locator('.cancel-button');
    this.savedChip = page.locator('.MuiChip-root:has-text("Saved")');
    
    // Step 1
    this.imageStep = page.locator('[data-testid="image-step"]');
    this.imageUploadInput = page.locator('[data-testid="image-upload-input"]');
    this.imageUploadLabel = page.locator('[data-testid="image-upload-label"]');
    this.imagePreview = page.locator('.image-preview');
    this.changeImageButton = page.locator('.change-image-button');
    this.continueToWordsButton = page.locator('.next-button:has-text("Continue to Words")');
    
    // Step 2
    this.wordsStep = page.locator('[data-testid="words-step"]');
    this.wordInput = page.locator('[data-testid="word-input"]');
    this.translationInput = page.locator('[data-testid="translation-input"]');
    this.addWordButton = page.locator('[data-testid="add-word-button"]');
    this.wordList = page.locator('.word-list');
    this.continueToHotspotsButton = page.locator('[data-testid="continue-to-hotspots"]');
    this.backToImageButton = page.locator('.back-button:has-text("Back")');
    this.instructionInput = page.locator('.instruction-input input');
    
    // Step 3
    this.hotspotsStep = page.locator('[data-testid="hotspots-step"]');
    this.canvasContainer = page.locator('.hotspot-canvas-container');
    this.selectToolButton = page.locator('.tool-button[title="Select/Move"]');
    this.rectangleToolButton = page.locator('.tool-button[title="Draw Rectangle"]');
    this.circleToolButton = page.locator('.tool-button[title="Draw Circle"]');
    this.zoomInButton = page.locator('.tool-button[title="Zoom In"]');
    this.zoomOutButton = page.locator('.tool-button[title="Zoom Out"]');
    this.resetZoomButton = page.locator('.tool-button[title="Reset Zoom"]');
    this.zoomLevel = page.locator('.zoom-level');
    this.deleteHotspotButton = page.locator('.tool-button.delete-button');
    this.propertiesPanel = page.locator('.properties-panel');
    this.wordLinkSelect = page.locator('.properties-panel select');
    this.progressBar = page.locator('.progress-fill');
    this.progressText = page.locator('.progress-text');
    this.previewButton = page.locator('[data-testid="preview-button"]');
    this.backToWordsButton = page.locator('.back-button:has-text("Back")');
    
    // Step 4
    this.previewStep = page.locator('[data-testid="preview-step"]');
    this.previewImage = page.locator('.preview-image');
    this.previewHotspots = page.locator('.preview-hotspot');
    this.previewWordList = page.locator('.preview-word-list');
    this.saveQuestionButton = page.locator('[data-testid="save-question-button"]');
    this.backToHotspotsButton = page.locator('.back-button:has-text("Edit Hotspots")');
    this.saveErrorAlert = page.locator('.MuiAlertseverity-error');
    this.saveSuccessAlert = page.locator('.MuiAlertseverity-success');
  }

  // ==================== Navigation ====================

  /**
   * Проверка что редактор загружен
   */
  async expectEditorLoaded() {
    await expect(this.editorContainer).toBeVisible({ timeout: 10000 });
    await expect(this.stepIndicator).toBeVisible();
  }

  /**
   * Получить текущий активный шаг
   */
  async getCurrentStep(): Promise<string> {
    const activeStep = this.page.locator('.step.active .step-name');
    return await activeStep.textContent() || '';
  }

  // ==================== Step 1: Image Upload ====================

  /**
   * Загрузить изображение
   */
  async uploadImage(filePath: string) {
    // File input may be hidden, so we don't check visibility
    await this.imageUploadInput.setInputFiles(filePath);
    // Ждем загрузки preview
    await expect(this.imagePreview).toBeVisible({ timeout: 10000 });
  }

  /**
   * Проверка что изображение загружено
   */
  async expectImageUploaded() {
    await expect(this.imagePreview).toBeVisible();
    await expect(this.changeImageButton).toBeVisible();
    await expect(this.continueToWordsButton).toBeEnabled();
  }

  /**
   * Перейти к шагу Words
   */
  async goToWordsStep() {
    await this.continueToWordsButton.click();
    await expect(this.wordsStep).toBeVisible({ timeout: 5000 });
  }

  // ==================== Step 2: Words ====================

  /**
   * Добавить слово
   */
  async addWord(text: string, translation?: string) {
    await this.wordInput.fill(text);
    if (translation) {
      await this.translationInput.fill(translation);
    }
    await this.addWordButton.click();
    // Ждем появления в списке
    await expect(this.wordList.locator(`.word-text:has-text("${text}")`)).toBeVisible();
  }

  /**
   * Удалить слово по индексу
   */
  async removeWord(index: number) {
    const removeButtons = this.wordList.locator('[data-testid="remove-word-button"]');
    await removeButtons.nth(index).click();
  }

  /**
   * Установить инструкцию
   */
  async setInstruction(instruction: string) {
    await this.instructionInput.fill(instruction);
  }

  /**
   * Получить количество слов
   */
  async getWordCount(): Promise<number> {
    return await this.wordList.locator('.word-card').count();
  }

  /**
   * Перейти к шагу Hotspots
   */
  async goToHotspotsStep() {
    await this.continueToHotspotsButton.click();
    await expect(this.hotspotsStep).toBeVisible({ timeout: 5000 });
  }

  /**
   * Вернуться к шагу Image
   */
  async goBackToImage() {
    await this.backToImageButton.click();
    await expect(this.imageStep).toBeVisible({ timeout: 5000 });
  }

  // ==================== Step 3: Hotspots ====================

  /**
   * Выбрать инструмент рисования
   */
  async selectTool(tool: 'select' | 'rectangle' | 'circle') {
    switch (tool) {
      case 'select':
        await this.selectToolButton.click();
        break;
      case 'rectangle':
        await this.rectangleToolButton.click();
        break;
      case 'circle':
        await this.circleToolButton.click();
        break;
    }
  }

  /**
   * Нарисовать hotspot прямоугольник
   */
  async drawRectangleHotspot(x: number, y: number, width: number, height: number) {
    await this.selectTool('rectangle');
    const canvas = this.page.locator('.hotspot-canvas');
    
    // Начинаем рисование
    await canvas.hover({ position: { x, y } });
    await this.page.mouse.down();
    await canvas.hover({ position: { x: x + width, y: y + height } });
    await this.page.mouse.up();
    
    // Ждем создания hotspot
    await this.page.waitForTimeout(300);
  }

  /**
   * Нарисовать hotspot круг
   */
  async drawCircleHotspot(centerX: number, centerY: number, radius: number) {
    await this.selectTool('circle');
    const canvas = this.page.locator('.hotspot-canvas');
    
    await canvas.hover({ position: { x: centerX - radius, y: centerY - radius } });
    await this.page.mouse.down();
    await canvas.hover({ position: { x: centerX + radius, y: centerY + radius } });
    await this.page.mouse.up();
    
    await this.page.waitForTimeout(300);
  }

  /**
   * Выбрать hotspot по координатам
   */
  async selectHotspotAt(x: number, y: number) {
    const canvas = this.page.locator('.hotspot-canvas');
    await canvas.click({ position: { x, y } });
    await expect(this.propertiesPanel).toBeVisible({ timeout: 3000 });
  }

  /**
   * Связать выбранный hotspot со словом
   */
  async linkHotspotToWord(wordText: string) {
    await expect(this.propertiesPanel).toBeVisible();
    await this.wordLinkSelect.selectOption({ label: wordText });
  }

  /**
   * Удалить выбранный hotspot
   */
  async deleteSelectedHotspot() {
    await this.deleteHotspotButton.click();
    await this.page.waitForTimeout(300);
  }

  /**
   * Zoom операции
   */
  async zoomIn() {
    await this.zoomInButton.click();
  }

  async zoomOut() {
    await this.zoomOutButton.click();
  }

  async resetZoom() {
    await this.resetZoomButton.click();
  }

  /**
   * Получить текущий zoom level
   */
  async getZoomLevel(): Promise<number> {
    const text = await this.zoomLevel.textContent() || '100%';
    return parseInt(text.replace('%', ''));
  }

  /**
   * Получить progress linked words
   */
  async getProgressText(): Promise<string> {
    return await this.progressText.textContent() || '';
  }

  /**
   * Перейти к шагу Preview
   */
  async goToPreviewStep() {
    await this.previewButton.click();
    await expect(this.previewStep).toBeVisible({ timeout: 5000 });
  }

  /**
   * Вернуться к шагу Words
   */
  async goBackToWords() {
    await this.backToWordsButton.click();
    await expect(this.wordsStep).toBeVisible({ timeout: 5000 });
  }

  // ==================== Step 4: Preview & Save ====================

  /**
   * Проверка preview отображения
   */
  async expectPreviewDisplayed() {
    await expect(this.previewImage).toBeVisible();
    await expect(this.previewWordList).toBeVisible();
  }

  /**
   * Получить количество preview hotspots
   */
  async getPreviewHotspotCount(): Promise<number> {
    return await this.previewHotspots.count();
  }

  /**
   * Сохранить вопрос
   */
  async saveQuestion() {
    await this.saveQuestionButton.click();
    // Ждем завершения сохранения
    await expect(this.savedChip.or(this.saveSuccessAlert)).toBeVisible({ timeout: 15000 });
  }

  /**
   * Проверка успешного сохранения
   */
  async expectQuestionSaved() {
    await expect(this.savedChip).toBeVisible();
  }

  /**
   * Проверка ошибки сохранения
   */
  async expectSaveError(errorText?: string) {
    await expect(this.saveErrorAlert).toBeVisible();
    if (errorText) {
      await expect(this.saveErrorAlert).toContainText(errorText);
    }
  }

  // ==================== Full Workflow ====================

  /**
   * Полный флоу создания Image-Word-Match вопроса
   */
  async createFullQuestion(
    imagePath: string,
    words: Array<{ text: string; translation?: string }>,
    hotspots: Array<{ x: number; y: number; width: number; height: number; wordIndex: number }>
  ) {
    // Step 1: Upload Image
    await this.uploadImage(imagePath);
    await this.goToWordsStep();

    // Step 2: Add Words
    for (const word of words) {
      await this.addWord(word.text, word.translation);
    }
    await this.goToHotspotsStep();

    // Step 3: Create Hotspots
    await this.selectTool('rectangle');
    for (const hotspot of hotspots) {
      await this.drawRectangleHotspot(hotspot.x, hotspot.y, hotspot.width, hotspot.height);
      await this.selectHotspotAt(hotspot.x + hotspot.width / 2, hotspot.y + hotspot.height / 2);
      await this.linkHotspotToWord(words[hotspot.wordIndex].text);
    }
    await this.goToPreviewStep();

    // Step 4: Save
    await this.saveQuestion();
  }
}
