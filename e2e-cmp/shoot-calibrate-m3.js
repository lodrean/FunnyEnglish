// Калибровка координат e2e-cmp под M3-UI (2026-08-07): скриншоты ключевых экранов 1280x720.
// Запуск: SKIP_WEB_SERVER=1 CMP_URL=http://192.168.1.148:8082 node shoot-calibrate-m3.js
const { chromium } = require('@playwright/test');
const path = require('path');

const BASE = process.env.CMP_URL || 'http://localhost:8082';
const OUT = path.join(__dirname, 'test-results', 'calib-m3');
const VP = { width: 1280, height: 720 };

async function main() {
  const fs = require('fs');
  fs.mkdirSync(OUT, { recursive: true });
  const browser = await chromium.launch();
  const page = await browser.newPage({ viewport: VP });
  page.on('response', r => { if (r.url().includes('/api/') && r.status() >= 400) console.log('[api]', r.status(), r.url().slice(-60)); });

  const canvas = page.locator('canvas');
  const click = async (x, y, settle = 1200) => { await canvas.click({ position: { x, y } }); await page.waitForTimeout(settle); };
  const shot = async (name) => { await page.mouse.move(640, 60); await page.waitForTimeout(200); await page.screenshot({ path: path.join(OUT, name + '.png') }); console.log('shot:', name); };

  await page.goto(BASE, { waitUntil: 'load', timeout: 60000 });
  await canvas.waitFor({ state: 'visible', timeout: 60000 });
  await page.waitForTimeout(10000); // splash + init

  // Онбординг (3 слайда)
  await shot('00-onboarding-1');
  await click(640, 648, 800); await shot('01-onboarding-2');
  await click(640, 648, 800); await shot('02-onboarding-3');
  await click(640, 648, 6000); // «Начать»
  await shot('03-library');

  // Скролл списка вниз до seed-библиотеки (wheel; стоп — когда скриншот стабилизируется)
  let prev = null;
  for (let i = 0; i < 20; i++) {
    await page.mouse.move(640, 400);
    await page.mouse.wheel(0, 700);
    await page.waitForTimeout(500);
    const cur = await page.screenshot();
    if (prev && cur.equals(prev)) break; // дно списка
    prev = cur;
  }
  await shot('04-library-bottom');

  // Seed «Разговорный английский» — ПОСЛЕДНЯЯ карточка списка (клик по нижней видимой)
  await click(640, 655, 3500);
  await shot('08-topics-seed');

  // Первый топик → Video (error-стаб) → «К вопросам» → Questions
  await click(640, 115, 3000);
  await shot('09-video');
  await click(707, 441, 4000);
  await shot('10-questions');

  // Training (гость — кнопка выше гейта)
  await click(640, 340, 4000);
  await shot('11-training');
  await page.goBack().catch(() => {});

  // Профиль (NavigationRail — левый столбец)
  await click(40, 425, 3000); // rail «Профиль»
  await shot('05-profile-guest');

  // «Уже есть аккаунт? Войти» → Login
  await click(743, 499, 2500);
  await shot('06-login');

  // Register через ссылку на Login (accent «Регистрация»)
  await click(690, 413, 2500);
  await shot('07-register');

  await browser.close();
  console.log('DONE');
}
main().catch(e => { console.error(e); process.exit(1); });
