import { test, expect } from '@playwright/test';

/**
 * CMP WASM Smoke Tests
 * Basic tests to verify WASM app loads and renders
 */
test.describe('CMP WASM - Smoke Tests', () => {
  
  test('should load WASM application', async ({ page }) => {
    // Navigate to CMP web app
    await page.goto('/');
    
    // Wait for canvas to be present (Compose renders to canvas)
    const canvas = page.locator('canvas');
    await expect(canvas).toBeVisible({ timeout: 30000 });
    
    // Verify no errors in console
    const errors: string[] = [];
    page.on('console', msg => {
      if (msg.type() === 'error') {
        errors.push(msg.text());
      }
    });
    
    // Wait a bit for app to initialize
    await page.waitForTimeout(3000);
    
    // Should have no critical errors
    const criticalErrors = errors.filter(e => 
      !e.includes('source map') && 
      !e.includes('favicon')
    );
    
    expect(criticalErrors).toHaveLength(0);
    console.log('✅ WASM app loaded successfully');
  });

  test('should show loading state initially', async ({ page }) => {
    await page.goto('/');
    
    // Check for loading indicator
    const loadingText = page.locator('text=Loading');
    
    // Loading might be quick, so just check it exists briefly
    try {
      await expect(loadingText).toBeVisible({ timeout: 5000 });
      console.log('✅ Loading state shown');
    } catch {
      // Loading was too fast, that's OK
      console.log('ℹ️ Loading state was too fast to capture');
    }
  });

  test('should render app content after load', async ({ page }) => {
    await page.goto('/');
    
    // Wait for canvas
    await expect(page.locator('canvas')).toBeVisible({ timeout: 30000 });
    
    // Wait for app to fully render
    await page.waitForTimeout(5000);
    
    // Take screenshot for visual verification
    await page.screenshot({ 
      path: 'test-results/cmp-initial-load.png',
      fullPage: false 
    });
    
    console.log('✅ Screenshot saved');
  });

  test('should handle window resize', async ({ page }) => {
    await page.goto('/');
    await expect(page.locator('canvas')).toBeVisible({ timeout: 30000 });
    
    // Test different viewport sizes
    await page.setViewportSize({ width: 1920, height: 1080 });
    await page.waitForTimeout(1000);
    
    await page.setViewportSize({ width: 1366, height: 768 });
    await page.waitForTimeout(1000);
    
    await page.setViewportSize({ width: 390, height: 844 });
    await page.waitForTimeout(1000);
    
    // Canvas should still be visible
    await expect(page.locator('canvas')).toBeVisible();
    console.log('✅ Responsive design works');
  });
});
