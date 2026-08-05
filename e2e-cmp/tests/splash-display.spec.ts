import { test, expect, Page } from '@playwright/test';
import { PNG } from 'pngjs';
import { collectCriticalErrors } from './helpers';

const SPLASH_BG = {
  light: { r: 238, g: 243, b: 255 },
  dark: { r: 22, g: 26, b: 46 },
} as const;

const TOLERANCE = 8;

function hexDistance(a: { r: number; g: number; b: number }, b: { r: number; g: number; b: number }) {
  return Math.sqrt((a.r - b.r) ** 2 + (a.g - b.g) ** 2 + (a.b - b.b) ** 2);
}

async function setTheme(page: Page, mode: 'dark' | 'light' | 'system') {
  await page.addInitScript((themeMode: string) => {
    // WASM Settings uses a prefix: Settings("sotospeak.preferences") → key "sotospeak.preferences_theme_mode".
    // The HTML shell also falls back to "theme_mode" for convenience.
    localStorage.setItem('sotospeak.preferences_theme_mode', themeMode);
    localStorage.setItem('theme_mode', themeMode);
  }, mode);
}

interface Pixel { r: number; g: number; b: number; a: number }

function readScreenshotPixels(buffer: Buffer): { width: number; height: number; pixels: Pixel[] } {
  const png = PNG.sync.read(buffer);
  const pixels: Pixel[] = [];
  for (let i = 0; i < png.data.length; i += 4) {
    pixels.push({
      r: png.data[i],
      g: png.data[i + 1],
      b: png.data[i + 2],
      a: png.data[i + 3],
    });
  }
  return { width: png.width, height: png.height, pixels };
}

function pixelAt(pixels: Pixel[], width: number, x: number, y: number): Pixel {
  return pixels[y * width + x];
}

async function assertSplashBackground(
  page: Page,
  screenshot: Buffer,
  expected: 'light' | 'dark'
) {
  const expectedColor = SPLASH_BG[expected];
  const { width, height, pixels } = readScreenshotPixels(screenshot);

  const insetX = Math.max(1, Math.round(width * 0.05));
  const insetY = Math.max(1, Math.round(height * 0.05));

  // Sample points around the edges and corners. They must match the theme background.
  const edgePoints: { x: number; y: number }[] = [
    { x: 0, y: 0 },
    { x: width - 1, y: 0 },
    { x: 0, y: height - 1 },
    { x: width - 1, y: height - 1 },
    { x: Math.round(width / 2), y: insetY },
    { x: Math.round(width / 2), y: height - 1 - insetY },
    { x: insetX, y: Math.round(height / 2) },
    { x: width - 1 - insetX, y: Math.round(height / 2) },
  ];

  const nonBackgroundEdges = edgePoints
    .map((p) => ({ ...p, color: pixelAt(pixels, width, p.x, p.y) }))
    .filter((p) => hexDistance(p.color, expectedColor) > TOLERANCE);

  expect(
    nonBackgroundEdges.length,
    `splash edges should be ${expected} background; offending pixels: ${JSON.stringify(
      nonBackgroundEdges.slice(0, 5)
    )}`
  ).toBe(0);

  // Center region should contain logo pixels, not just the background.
  // Sample a generous central box with a small step — the logo can be slightly offset.
  const centerMinX = Math.round(width * 0.35);
  const centerMaxX = Math.round(width * 0.65);
  const centerMinY = Math.round(height * 0.35);
  const centerMaxY = Math.round(height * 0.65);
  const step = Math.max(2, Math.round(Math.min(width, height) * 0.02));
  let logoPixels = 0;
  for (let x = centerMinX; x <= centerMaxX; x += step) {
    for (let y = centerMinY; y <= centerMaxY; y += step) {
      if (hexDistance(pixelAt(pixels, width, x, y), expectedColor) > TOLERANCE) {
        logoPixels++;
      }
    }
  }
  expect(logoPixels, 'splash center should contain the logo (not just background)').toBeGreaterThan(0);
}

/**
 * E2E проверка отображения SplashScreen на разных viewport и темах.
 * CMP WASM рендерит splash в canvas — проверяем:
 *  - canvas виден;
 *  - фон по краям соответствует теме (нет светлой вспышки в dark mode);
 *  - логотип не упирается в края (отступы через padding);
 *  - нет критических ошибок.
 */
test.describe('SplashScreen display', () => {
  async function gotoSplash(page: Page, url = '/') {
    await page.goto(url);
    await page.locator('canvas').waitFor({ state: 'visible', timeout: 60000 });
    // Даём WASM/Skiko один кадр на отрисовку фона.
    await page.waitForTimeout(300);
  }

  test('renders splash on desktop light mode', async ({ page }) => {
    const errors = collectCriticalErrors(page);
    await page.setViewportSize({ width: 1280, height: 720 });
    await setTheme(page, 'light');
    await gotoSplash(page);

    const shot = await page.screenshot({ path: 'test-results/splash-desktop-light.png' });
    await assertSplashBackground(page, shot, 'light');

    expect(errors).toHaveLength(0);
  });

  test('renders splash on desktop dark mode', async ({ page }) => {
    const errors = collectCriticalErrors(page);
    await page.setViewportSize({ width: 1280, height: 720 });
    await setTheme(page, 'dark');
    await gotoSplash(page);

    const shot = await page.screenshot({ path: 'test-results/splash-desktop-dark.png' });
    await assertSplashBackground(page, shot, 'dark');

    expect(errors).toHaveLength(0);
  });

  test('renders splash on mobile portrait dark mode', async ({ page }) => {
    const errors = collectCriticalErrors(page);
    await page.setViewportSize({ width: 390, height: 844 });
    await setTheme(page, 'dark');
    await gotoSplash(page);

    const shot = await page.screenshot({ path: 'test-results/splash-mobile-dark.png' });
    await assertSplashBackground(page, shot, 'dark');

    expect(errors).toHaveLength(0);
  });

  test('renders splash on mobile landscape light mode', async ({ page }) => {
    const errors = collectCriticalErrors(page);
    await page.setViewportSize({ width: 844, height: 390 });
    await setTheme(page, 'light');
    await gotoSplash(page);

    const shot = await page.screenshot({ path: 'test-results/splash-mobile-landscape-light.png' });
    await assertSplashBackground(page, shot, 'light');

    expect(errors).toHaveLength(0);
  });

  test('renders splash on tablet viewport', async ({ page }) => {
    const errors = collectCriticalErrors(page);
    await page.setViewportSize({ width: 768, height: 1024 });
    await setTheme(page, 'dark');
    await gotoSplash(page);

    const shot = await page.screenshot({ path: 'test-results/splash-tablet-dark.png' });
    await assertSplashBackground(page, shot, 'dark');

    expect(errors).toHaveLength(0);
  });

  test('renders splash on wide desktop viewport', async ({ page, isMobile }) => {
    test.skip(!!isMobile, 'Широкий viewport тестируем только на десктопе (避免 Mobile Chrome DPR issues)');
    const errors = collectCriticalErrors(page);
    await page.setViewportSize({ width: 1920, height: 1080 });
    await setTheme(page, 'light');
    await gotoSplash(page);

    const shot = await page.screenshot({ path: 'test-results/splash-wide-light.png' });
    await assertSplashBackground(page, shot, 'light');

    expect(errors).toHaveLength(0);
  });
});
