import { test, expect } from '@chromatic-com/playwright';

test.describe('Debug API Calls', () => {
  test('should log all API calls and errors', async ({ page }) => {
    // Capture console logs
    const consoleLogs: string[] = [];
    page.on('console', (msg) => {
      consoleLogs.push(`[${msg.type()}] ${msg.text()}`);
    });

    // Capture network errors
    const networkErrors: string[] = [];
    page.on('response', async (response) => {
      const url = response.url();
      if (url.includes('/api/') || url.includes('/admin/')) {
        const status = response.status();
        console.log(`Network: ${url} - Status: ${status}`);
        
        if (status >= 400) {
          try {
            const body = await response.text();
            networkErrors.push(`${url} - ${status}: ${body.substring(0, 200)}`);
          } catch (e) {
            networkErrors.push(`${url} - ${status}: [Could not read body]`);
          }
        }
      }
    });

    // Auth — через storageState (setup-проект); ручной логин убран:
    // он редиректил с /login на / и падал по timeout на input[type=email].
    await page.goto('/');
    
    // Navigate to tests
    console.log('Navigating to tests...');
    await page.goto('/content/tests');
    await page.locator('[data-testid="page-title"]').waitFor({ timeout: 15000 });
    
    // Wait a bit for all API calls
    await page.waitForTimeout(3000);
    
    // Take screenshot
    await page.screenshot({ path: 'test-results/debug-api-calls.png' });
    
    // Log all captured data
    console.log('\n=== Console Logs ===');
    consoleLogs.forEach(log => console.log(log));
    
    console.log('\n=== Network Errors ===');
    networkErrors.forEach(err => console.log(err));
    
    // Check if we can see the error message
    const errorVisible = await page.locator('text=Failed to load').isVisible().catch(() => false);
    console.log('\nError visible on page:', errorVisible);
    
    // The test should show us what's happening
    expect(true).toBe(true);
  });
});
