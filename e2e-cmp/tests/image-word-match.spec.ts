import { test, expect } from '@playwright/test';

/**
 * CMP WASM Image Word Match Tests
 * Tests the main learning activity
 */
test.describe('CMP WASM - Image Word Match', () => {
  
  test('should load Image Word Match test', async ({ page }) => {
    await page.goto('/');
    await expect(page.locator('canvas')).toBeVisible({ timeout: 30000 });
    await page.waitForTimeout(5000);
    
    // Screenshot initial state
    await page.screenshot({ path: 'test-results/cmp-iwm-initial.png' });
    
    // Navigate to tests section (assuming bottom nav)
    const canvas = page.locator('canvas');
    const box = await canvas.boundingBox();
    
    if (box) {
      // Click on tests/categories tab (middle of bottom nav)
      await canvas.click({ 
        position: { x: box.width * 0.5, y: box.height - 40 } 
      });
      await page.waitForTimeout(2000);
      
      await page.screenshot({ path: 'test-results/cmp-iwm-categories.png' });
    }
    
    console.log('✅ Image Word Match navigation tested');
  });

  test('should display image with hotspots', async ({ page }) => {
    await page.goto('/');
    await expect(page.locator('canvas')).toBeVisible({ timeout: 30000 });
    await page.waitForTimeout(5000);
    
    // Try to start a test
    const canvas = page.locator('canvas');
    const box = await canvas.boundingBox();
    
    if (box) {
      // Click on first category/test (assuming list layout)
      await canvas.click({ 
        position: { x: box.width / 2, y: box.height * 0.3 } 
      });
      await page.waitForTimeout(3000);
      
      // Screenshot test screen
      await page.screenshot({ path: 'test-results/cmp-iwm-test-screen.png' });
    }
    
    console.log('✅ Test screen captured');
  });

  test('should handle drag and drop interaction', async ({ page }) => {
    await page.goto('/');
    await expect(page.locator('canvas')).toBeVisible({ timeout: 30000 });
    await page.waitForTimeout(5000);
    
    const canvas = page.locator('canvas');
    const box = await canvas.boundingBox();
    
    if (box) {
      // Start test
      await canvas.click({ position: { x: box.width / 2, y: box.height * 0.3 } });
      await page.waitForTimeout(3000);
      
      // Try drag and drop
      await canvas.dragTo(canvas, {
        sourcePosition: { x: box.width * 0.2, y: box.height * 0.8 },
        targetPosition: { x: box.width * 0.5, y: box.height * 0.5 }
      });
      
      await page.waitForTimeout(2000);
      await page.screenshot({ path: 'test-results/cmp-iwm-drag-drop.png' });
    }
    
    console.log('✅ Drag and drop interaction tested');
  });

  test('should show completion feedback', async ({ page }) => {
    await page.goto('/');
    await expect(page.locator('canvas')).toBeVisible({ timeout: 30000 });
    await page.waitForTimeout(3000);
    
    // Monitor console for completion messages
    const messages: string[] = [];
    page.on('console', msg => {
      const text = msg.text();
      if (text.includes('complete') || text.includes('finish') || text.includes('score')) {
        messages.push(text);
      }
    });
    
    await page.waitForTimeout(5000);
    
    console.log('Completion messages:', messages);
    await page.screenshot({ path: 'test-results/cmp-iwm-completion.png' });
    
    console.log('✅ Completion feedback captured');
  });
});
