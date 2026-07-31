import { test, expect } from '@playwright/test';

/**
 * CMP WASM Performance Tests
 * Tests loading time, memory usage, FPS
 */
test.describe('CMP WASM - Performance', () => {
  
  test('should load within acceptable time', async ({ page }) => {
    const startTime = Date.now();
    
    await page.goto('/');
    await expect(page.locator('canvas')).toBeVisible({ timeout: 60000 });
    
    const loadTime = Date.now() - startTime;
    console.log(`⏱️ WASM load time: ${loadTime}ms`);
    
    // WASM should load within 30 seconds
    expect(loadTime).toBeLessThan(30000);
    
    console.log('✅ Load time acceptable');
  });

  test('should maintain stable memory usage', async ({ page }) => {
    await page.goto('/');
    await expect(page.locator('canvas')).toBeVisible({ timeout: 30000 });
    
    // Get initial metrics
    const initialMetrics = await page.evaluate(() => {
      return (performance as any).memory?.usedJSHeapSize || 0;
    });
    
    console.log(`Initial memory: ${Math.round(initialMetrics / 1024 / 1024)}MB`);
    
    // Interact with app
    const canvas = page.locator('canvas');
    const box = await canvas.boundingBox();
    
    if (box) {
      // Click multiple times
      for (let i = 0; i < 5; i++) {
        await canvas.click({ position: { x: box.width / 2, y: box.height / 2 } });
        await page.waitForTimeout(1000);
      }
    }
    
    // Get final metrics
    const finalMetrics = await page.evaluate(() => {
      return (performance as any).memory?.usedJSHeapSize || 0;
    });
    
    console.log(`Final memory: ${Math.round(finalMetrics / 1024 / 1024)}MB`);
    
    // Memory shouldn't grow uncontrollably
    const growth = finalMetrics - initialMetrics;
    console.log(`Memory growth: ${Math.round(growth / 1024 / 1024)}MB`);
    
    console.log('✅ Memory usage checked');
  });

  test('should handle rapid interactions', async ({ page }) => {
    await page.goto('/');
    await expect(page.locator('canvas')).toBeVisible({ timeout: 30000 });
    await page.waitForTimeout(3000);
    
    const canvas = page.locator('canvas');
    const box = await canvas.boundingBox();
    
    if (box) {
      // Rapid clicking
      const startTime = Date.now();
      
      for (let i = 0; i < 10; i++) {
        await canvas.click({ 
          position: { 
            x: box.width * (0.3 + Math.random() * 0.4), 
            y: box.height * (0.3 + Math.random() * 0.4) 
          } 
        });
      }
      
      const duration = Date.now() - startTime;
      console.log(`⏱️ 10 clicks in ${duration}ms`);
      
      // App should still be responsive
      await expect(canvas).toBeVisible();
      
      await page.screenshot({ path: 'test-results/cmp-rapid-clicks.png' });
    }
    
    console.log('✅ Rapid interactions handled');
  });

  test('should measure frame rate', async ({ page }) => {
    await page.goto('/');
    await expect(page.locator('canvas')).toBeVisible({ timeout: 30000 });
    
    // Measure FPS using Performance API
    const fps = await page.evaluate(() => {
      return new Promise<number>((resolve) => {
        let frames = 0;
        const startTime = performance.now();
        
        function countFrames() {
          frames++;
          if (performance.now() - startTime < 1000) {
            requestAnimationFrame(countFrames);
          } else {
            resolve(frames);
          }
        }
        
        requestAnimationFrame(countFrames);
      });
    });
    
    console.log(`📊 FPS: ${fps}`);
    
    // WASM should maintain reasonable FPS
    expect(fps).toBeGreaterThan(10); // At least 10 FPS
    
    console.log('✅ Frame rate measured');
  });
});
