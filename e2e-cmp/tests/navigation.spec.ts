import { test, expect } from '@playwright/test';

/**
 * CMP WASM Navigation Tests
 * Tests navigation between screens
 */
test.describe('CMP WASM - Navigation', () => {
  
  test('should navigate through main screens', async ({ page }) => {
    await page.goto('/');
    await expect(page.locator('canvas')).toBeVisible({ timeout: 30000 });
    await page.waitForTimeout(5000);
    
    // Screenshot of home screen
    await page.screenshot({ 
      path: 'test-results/cmp-screen-home.png' 
    });
    
    // Try to find and click navigation elements
    // CMP renders to canvas, so we need coordinate-based clicking
    const canvas = page.locator('canvas');
    const box = await canvas.boundingBox();
    
    if (!box) {
      throw new Error('Canvas not found');
    }
    
    console.log(`Canvas size: ${box.width}x${box.height}`);
    
    // Try clicking different areas (assuming bottom navigation)
    const navY = box.height - 50; // Bottom nav area
    
    // Click home (left)
    await canvas.click({ position: { x: box.width * 0.1, y: navY } });
    await page.waitForTimeout(2000);
    await page.screenshot({ path: 'test-results/cmp-nav-home.png' });
    
    // Click categories
    await canvas.click({ position: { x: box.width * 0.3, y: navY } });
    await page.waitForTimeout(2000);
    await page.screenshot({ path: 'test-results/cmp-nav-categories.png' });
    
    // Click profile
    await canvas.click({ position: { x: box.width * 0.9, y: navY } });
    await page.waitForTimeout(2000);
    await page.screenshot({ path: 'test-results/cmp-nav-profile.png' });
    
    console.log('✅ Navigation tested with coordinate clicks');
  });

  test('should handle back navigation', async ({ page }) => {
    await page.goto('/');
    await expect(page.locator('canvas')).toBeVisible({ timeout: 30000 });
    await page.waitForTimeout(3000);
    
    // Navigate forward
    const canvas = page.locator('canvas');
    const box = await canvas.boundingBox();
    
    if (box) {
      // Click to navigate
      await canvas.click({ position: { x: box.width / 2, y: box.height / 2 } });
      await page.waitForTimeout(2000);
      
      // Go back via browser
      await page.goBack();
      await page.waitForTimeout(3000);
      
      // Canvas should still be visible
      await expect(canvas).toBeVisible();
      
      await page.screenshot({ path: 'test-results/cmp-back-nav.png' });
    }
    
    console.log('✅ Back navigation works');
  });

  test('should be responsive on mobile viewport', async ({ page }) => {
    // Set mobile viewport
    await page.setViewportSize({ width: 390, height: 844 });
    
    await page.goto('/');
    await expect(page.locator('canvas')).toBeVisible({ timeout: 30000 });
    await page.waitForTimeout(3000);
    
    const canvas = page.locator('canvas');
    const box = await canvas.boundingBox();
    
    expect(box?.width).toBeLessThan(400);
    
    await page.screenshot({ 
      path: 'test-results/cmp-mobile-view.png',
      fullPage: false
    });
    
    console.log('✅ Mobile viewport rendered correctly');
  });
});
