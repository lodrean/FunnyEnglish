import { test, expect } from '@chromatic-com/playwright';
import { request } from '@playwright/test';
import * as path from 'path';
import { fileURLToPath } from 'url';
import * as fs from 'fs';
import { GradingInboxPage } from '../pages/GradingInboxPage';
import { GradingDetailPage } from '../pages/GradingDetailPage';

const FIXTURES = path.join(path.dirname(fileURLToPath(import.meta.url)), '..', 'fixtures');
const AUDIO_FIXTURE = path.join(FIXTURES, 'sample-audio.m4a');

const ADMIN_URL = process.env.ADMIN_URL || 'http://localhost:5173';
const API = `${ADMIN_URL}/api`;

const RUN = Date.now();
const TOPIC_NAME = `E2E Grading Topic ${RUN}`;
const DEMO_EMAIL = 'demo@sotospeak.app';

let topicId: string;

/**
 * Сид через публичный practice-flow (решение Open Question №4 Part 3):
 * админ создаёт опубликованный топик → demo-юзер отправляет practice-запись
 * (POST /api/speaking/submissions, multipart — как мобильный клиент).
 */
test.beforeAll(async () => {
  const ctx = await request.newContext();
  const url = (p: string) => `${API}${p}`;

  // 1. Логин админа (rate limit в dev-compose ослаблен, memory.md №23)
  const adminLogin = await ctx.post(url('/auth/login'), {
    data: { email: process.env.TEST_ADMIN_EMAIL || 'admin@sotospeak.com', password: process.env.TEST_ADMIN_PASSWORD || 'admin123' },
  });
  expect(adminLogin.ok()).toBeTruthy();
  const adminToken = (await adminLogin.json()).token;
  const adminHeaders = { Authorization: `Bearer ${adminToken}` };

  // 2. Тема + топик (опубликованный) + видео + вопрос
  const libResp = await ctx.post(url('/admin/speaking/libraries'), {
    headers: adminHeaders,
    data: { title: `E2E Grading Library ${RUN}`, displayOrder: 0, isPublished: true },
  });
  expect(libResp.status()).toBe(201);
  const libraryId = (await libResp.json()).id;

  const topicResp = await ctx.post(url('/admin/speaking/topics'), {
    headers: adminHeaders,
    data: { libraryId, title: TOPIC_NAME, displayOrder: 0, isPublished: true },
  });
  expect(topicResp.status()).toBe(201);
  topicId = (await topicResp.json()).id;

  const videoResp = await ctx.put(url(`/admin/speaking/topics/${topicId}/video`), {
    headers: adminHeaders,
    data: { videoUrl: 'http://localhost:9000/sotospeak/e2e/dummy.mp4', durationSeconds: 10 },
  });
  expect(videoResp.ok()).toBeTruthy();

  const questionResp = await ctx.post(url(`/admin/speaking/topics/${topicId}/questions`), {
    headers: adminHeaders,
    data: { text: 'Tell me about yourself.', displayOrder: 0 },
  });
  expect(questionResp.status()).toBe(201);

  // 3. Логин demo-юзера и practice-запись (multipart, как мобильный клиент)
  const demoLogin = await ctx.post(url('/auth/login'), {
    data: { email: DEMO_EMAIL, password: 'demo123' },
  });
  expect(demoLogin.ok()).toBeTruthy();
  const demoToken = (await demoLogin.json()).token;

  const submissionResp = await ctx.post(url('/speaking/submissions'), {
    headers: { Authorization: `Bearer ${demoToken}` },
    multipart: {
      file: {
        name: 'practice.m4a',
        mimeType: 'audio/mp4',
        buffer: fs.readFileSync(AUDIO_FIXTURE),
      },
      topicId,
      durationSec: '5',
    },
  });
  expect(submissionResp.status()).toBe(201);

  await ctx.dispose();
});

test.describe('Grading: проверка записей', () => {
  test.describe.configure({ mode: 'serial' });

  test('1. дефолтный фильтр NEW — запись видна', async ({ page }) => {
    const inbox = new GradingInboxPage(page);
    await inbox.goto();
    await inbox.expectRowWithStatus(TOPIC_NAME, 'NEW');
  });

  test('2. открытие записи: плеер и вопросы', async ({ page }) => {
    const inbox = new GradingInboxPage(page);
    const detail = new GradingDetailPage(page);

    await inbox.goto();
    await inbox.openSubmission(TOPIC_NAME);
    await detail.expectLoaded();
    await detail.expectAudioPlayer();
    await detail.expectQuestionsVisible();
  });

  test('3. выставление оценки 8/7/9/6 → total 7.5 → REVIEWED', async ({ page }) => {
    const inbox = new GradingInboxPage(page);
    const detail = new GradingDetailPage(page);

    await inbox.goto();
    await inbox.openSubmission(TOPIC_NAME);
    await detail.expectLoaded();

    await detail.setCriterion('grammar', 8);
    await detail.setCriterion('vocabulary', 7);
    await detail.setCriterion('pronunciation', 9);
    await detail.setCriterion('fluency', 6);
    await detail.expectTotal('7.5');

    await detail.fillComment('E2E: хорошая работа');
    await detail.saveGrade();
    await detail.expectStatusReviewed();
  });

  test('4. inbox: при NEW записи нет, при REVIEWED — есть с баллом 7.5', async ({ page }) => {
    const inbox = new GradingInboxPage(page);

    await inbox.goto();
    await inbox.expectNoRow(TOPIC_NAME);

    await inbox.filterStatus('REVIEWED');
    await inbox.expectRowWithStatus(TOPIC_NAME, 'REVIEWED');
    await inbox.expectScoreInRow(TOPIC_NAME, '7.5');
  });

  test('5. редактирование оценки: fluency 10 → total 8.5', async ({ page }) => {
    const inbox = new GradingInboxPage(page);
    const detail = new GradingDetailPage(page);

    await inbox.goto();
    await inbox.filterStatus('REVIEWED');
    await inbox.openSubmission(TOPIC_NAME);
    await detail.expectLoaded();

    // форма предзаполнена и readonly
    await detail.expectFormReadonly();
    await detail.editButton.click();

    await detail.setCriterion('fluency', 10);
    await detail.expectTotal('8.5');
    await detail.saveGrade();
    await detail.expectStatusReviewed();

    // в inbox — 8.5
    await detail.backToInbox();
    await inbox.filterStatus('REVIEWED');
    await inbox.expectScoreInRow(TOPIC_NAME, '8.5');
  });

  test('6. фильтр по студенту (Autocomplete)', async ({ page }) => {
    const inbox = new GradingInboxPage(page);

    await inbox.goto();
    await inbox.filterStatus('All');

    const studentInput = page.locator('[data-testid="filter-student-autocomplete"] input');
    await studentInput.fill('demo');
    const option = page.getByRole('option', { name: new RegExp(DEMO_EMAIL) });
    await option.waitFor({ timeout: 15000 });
    // MUI Autocomplete: Enter на option не срабатывает (выбор обрабатывает input) — кликаем
    await option.click();

    // строки только demo-юзера — наша запись видна
    await inbox.expectRowWithStatus(TOPIC_NAME, 'REVIEWED');
  });
});
