import { test, expect } from '@chromatic-com/playwright';
import * as path from 'path';
import { fileURLToPath } from 'url';
import { SpeakingLibrariesPage } from '../pages/SpeakingLibrariesPage';
import { SpeakingLibraryEditorPage } from '../pages/SpeakingLibraryEditorPage';
import { SpeakingTopicsPage } from '../pages/SpeakingTopicsPage';
import { SpeakingTopicEditorPage } from '../pages/SpeakingTopicEditorPage';

const FIXTURES = path.join(path.dirname(fileURLToPath(import.meta.url)), '..', 'fixtures');
const VIDEO_FIXTURE = path.join(FIXTURES, 'sample-video.mp4');
const VTT_FIXTURE = path.join(FIXTURES, 'sample-subtitles.vtt');

// Имена с префиксом E2E (конвенция существующих спеков) + timestamp для уникальности
const RUN = Date.now();
const LIBRARY_NAME = `E2E Speaking Library ${RUN}`;
const TOPIC_NAME = `E2E Speaking Topic ${RUN}`;

test.describe('Speaking content: навигация + CRUD-чейн', () => {
  test.describe.configure({ mode: 'serial' });

  test('1. навигационный смоук: пункты Speaking и Grading в сайдбаре', async ({ page }) => {
    const librariesPage = new SpeakingLibrariesPage(page);
    await librariesPage.goto();

    // На desktop сайдбар всегда виден; на mobile/tablet — drawer за кнопкой toggle (№22е)
    let navSpeaking = page.locator('[data-testid="nav-speaking"]').first();
    let navGrading = page.locator('[data-testid="nav-grading"]').first();
    if (!(await navSpeaking.isVisible())) {
      await page.getByRole('button', { name: 'toggle sidebar' }).click();
      const drawer = page.locator('.MuiModal-root');
      navSpeaking = drawer.locator('[data-testid="nav-speaking"]');
      navGrading = drawer.locator('[data-testid="nav-grading"]');
    }
    await expect(navSpeaking).toBeVisible();
    await expect(navGrading).toBeVisible();

    // nav-grading ведёт на /grading
    await navGrading.click();
    await expect(page).toHaveURL(/\/grading/);
    await expect(page.locator('[data-testid="page-title"]')).toBeVisible();
  });

  test('2. создание темы', async ({ page }) => {
    const librariesPage = new SpeakingLibrariesPage(page);
    const editorPage = new SpeakingLibraryEditorPage(page);

    await librariesPage.goto();
    await librariesPage.clickAdd();
    await editorPage.fillForm({ name: LIBRARY_NAME, description: 'E2E chain', order: 0 });
    await editorPage.save();
    await editorPage.expectSaved();

    await librariesPage.expectRowVisible(LIBRARY_NAME);
  });

  test('3. создание топика с видео и субтитрами', async ({ page }) => {
    const topicsPage = new SpeakingTopicsPage(page);
    const editorPage = new SpeakingTopicEditorPage(page);

    await topicsPage.goto();
    await topicsPage.clickAdd();

    await editorPage.selectLibrary(LIBRARY_NAME);
    await editorPage.fillName(TOPIC_NAME);
    await editorPage.uploadVideo(VIDEO_FIXTURE);
    await editorPage.uploadSubtitles(VTT_FIXTURE);
    await editorPage.setDuration(10);
    await editorPage.save();
    await editorPage.expectCreated();
  });

  test('4. вопросы: добавление, reorder, edit, delete', async ({ page }) => {
    const topicsPage = new SpeakingTopicsPage(page);
    const editorPage = new SpeakingTopicEditorPage(page);

    // Открываем созданный топик из списка
    await topicsPage.goto();
    await topicsPage.filterByLibrary(LIBRARY_NAME);
    await topicsPage.openEditor(TOPIC_NAME);
    await expect(page).toHaveURL(/\/speaking\/topics\/[0-9a-f-]+\/edit$/);

    await editorPage.switchToQuestionsTab();
    await editorPage.addQuestion('What is your name?');
    await editorPage.addQuestion('Where are you from?');

    // reorder: первый вопрос вниз → порядок меняется
    await editorPage.moveQuestionDown('What is your name?');
    await editorPage.saveOrder();

    // после reload порядок сохранён
    await page.reload();
    await editorPage.switchToQuestionsTab();
    await editorPage.expectQuestionOrder('Where are you from?', 'What is your name?');

    // delete вопроса
    await editorPage.deleteQuestion('What is your name?');
  });

  test('5. publish toggle топика и темы (force-click Switch)', async ({ page }) => {
    const topicsPage = new SpeakingTopicsPage(page);
    const librariesPage = new SpeakingLibrariesPage(page);

    await topicsPage.goto();
    await topicsPage.filterByLibrary(LIBRARY_NAME);
    await topicsPage.togglePublish(TOPIC_NAME);
    await expect(page.locator('[data-testid="topics-table"]')).toBeVisible();

    await librariesPage.goto();
    await librariesPage.togglePublish(LIBRARY_NAME);
    await expect(page.locator('[data-testid="libraries-table"]')).toBeVisible();
  });

  test('6. архивация топика → chip «Archived»', async ({ page }) => {
    const topicsPage = new SpeakingTopicsPage(page);

    await topicsPage.goto();
    await topicsPage.filterByLibrary(LIBRARY_NAME);
    await topicsPage.archiveTopic(TOPIC_NAME);
    await topicsPage.expectArchivedChip(TOPIC_NAME);
  });

  test('7. cleanup: удаление тестовой темы', async ({ page }) => {
    const librariesPage = new SpeakingLibrariesPage(page);

    await librariesPage.goto();
    await librariesPage.deleteLibrary(LIBRARY_NAME);
    await expect(librariesPage.row(LIBRARY_NAME)).toHaveCount(0);
  });
});
