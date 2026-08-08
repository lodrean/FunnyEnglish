// Global setup e2e-cmp: гигиена dev-стека перед прогоном.
// Удаляет библиотеки «E2E Grading*» (мусор admin-web Playwright-прогонов),
// чтобы seed «Разговорный английский» была ПЕРВОЙ карточкой — координатные
// клики canvas-only, поиск по тексту невозможен (грабля №54 для WASM).
import { request } from '@playwright/test';

const API = process.env.E2E_API_URL || 'http://localhost:8080/api';
const ADMIN_EMAIL = process.env.E2E_USER_EMAIL || 'admin@sotospeak.com';
const ADMIN_PASSWORD = process.env.E2E_USER_PASSWORD || 'admin123';

export default async function globalSetup() {
  const ctx = await request.newContext();
  const login = await ctx.post(`${API}/auth/login`, {
    data: { email: ADMIN_EMAIL, password: ADMIN_PASSWORD },
  });
  if (!login.ok()) {
    console.warn('[global-setup] admin login failed — purge skipped:', login.status());
    return;
  }
  const { token } = await login.json();
  const headers = { Authorization: `Bearer ${token}` };

  const libs = await (await ctx.get(`${API}/admin/speaking/libraries`, { headers })).json();
  const e2eLibs = libs.filter((l: any) => String(l.title).startsWith('E2E '));
  let failed = 0;
  for (const lib of e2eLibs) {
    const res = await ctx.delete(`${API}/admin/speaking/libraries/${lib.id}`, { headers });
    if (!res.ok()) failed++; // 400 «Library has submissions» — чистим каскадом через БД ниже
  }
  console.log(`[global-setup] API purge: ${e2eLibs.length - failed} ok, ${failed} blocked by submissions`);
  if (failed > 0) {
    // Fallback: каскадное удаление через psql в docker (dev-стек)
    const { execSync } = await import('child_process');
    const sql = `
      DELETE FROM grades WHERE submission_id IN (SELECT id FROM practice_submissions WHERE topic_id IN (SELECT id FROM topics WHERE library_id IN (SELECT id FROM libraries WHERE title LIKE 'E2E %')));
      DELETE FROM practice_submissions WHERE topic_id IN (SELECT id FROM topics WHERE library_id IN (SELECT id FROM libraries WHERE title LIKE 'E2E %'));
      DELETE FROM speaking_questions WHERE topic_id IN (SELECT id FROM topics WHERE library_id IN (SELECT id FROM libraries WHERE title LIKE 'E2E %'));
      DELETE FROM videos WHERE topic_id IN (SELECT id FROM topics WHERE library_id IN (SELECT id FROM libraries WHERE title LIKE 'E2E %'));
      DELETE FROM topics WHERE library_id IN (SELECT id FROM libraries WHERE title LIKE 'E2E %');
      DELETE FROM libraries WHERE title LIKE 'E2E %';`;
    try {
      execSync(`docker exec sotospeak-postgres psql -U postgres -d sotospeak -c "${sql.replace(/\n/g, ' ')}"`);
      console.log('[global-setup] DB cascade purge done');
    } catch (e) {
      console.warn('[global-setup] DB purge failed (docker down?):', String(e).slice(0, 200));
    }
  }
  await ctx.dispose();
}
