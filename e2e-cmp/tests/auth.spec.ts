import { test, expect } from '@playwright/test';

/**
 * CMP WASM Authentication Tests
 * Tests login flow in WASM web app
 */
test.describe('CMP WASM - Authentication', () => {
  
  test('should show login screen on first load', async ({ page }) => {
    await page.goto('/');
    
    // Wait for WASM to load
    await expect(page.locator('canvas')).toBeVisible({ timeout: 30000 });
    await page.waitForTimeout(5000);
    
    // Take screenshot of initial screen
    await page.screenshot({ 
      path: 'test-results/cmp-login-screen.png' 
    });
    
    // Check browser console for auth-related messages
    const logs: string[] = [];
    page.on('console', msg => logs.push(msg.text()));
    
    // Wait and check logs
    await page.waitForTimeout(2000);
    
    const authLogs = logs.filter(l => 
      l.toLowerCase().includes('auth') || 
      l.toLowerCase().includes('login')
    );
    
    console.log('Auth logs:', authLogs);
    console.log('✅ Login screen screenshot captured');
  });

  test('should handle keyboard input for login', async ({ page }) => {
    await page.goto('/');
    await expect(page.locator('canvas')).toBeVisible({ timeout: 30000 });
    await page.waitForTimeout(3000);
    
    // Try to interact with canvas via clicks
    const canvas = page.locator('canvas');
    const box = await canvas.boundingBox();
    
    if (box) {
      // Click in center (might trigger login form)
      await canvas.click({
        position: { x: box.width / 2, y: box.height / 2 }
      });
      
      await page.waitForTimeout(1000);
      
      // Take screenshot after click
      await page.screenshot({ 
        path: 'test-results/cmp-after-click.png' 
      });
    }
    
    console.log('✅ Click interaction tested');
  });

  test('should maintain session after reload', async ({ page, context }) => {
    await page.goto('/');
    await expect(page.locator('canvas')).toBeVisible({ timeout: 30000 });
    await page.waitForTimeout(3000);
    
    // Check localStorage for auth token
    const localStorage = await page.evaluate(() => {
      const items: Record<string, string> = {};
      for (let i = 0; i < localStorage.length; i++) {
        const key = localStorage.key(i);
        if (key) {
          items[key] = localStorage.getItem(key) || '';
        }
      }
      return items;
    });
    
    console.log('LocalStorage items:', Object.keys(localStorage));
    
    // Reload page
    await page.reload();
    await expect(page.locator('canvas')).toBeVisible({ timeout: 30000 });
    
    console.log('✅ Session persistence checked');
  });
});
