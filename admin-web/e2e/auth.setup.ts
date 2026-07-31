import { test as setup, expect } from '@chromatic-com/playwright';

const authFile = 'e2e/.auth/admin.json';

/**
 * Setup test for authentication
 * This runs before all tests to create a logged-in state
 */
setup('authenticate as admin', async ({ page }) => {
  // Navigate to login page
  await page.goto('/login');
  await page.waitForLoadState('networkidle');
  
  // Wait for login form - use resilient selectors
  const emailInput = page.locator('input').first();
  await expect(emailInput).toBeVisible({ timeout: 10000 });
  
  // Fill email
  await emailInput.fill('admin@funnyenglish.com');
  
  // Fill password
  await page.locator('input[type="password"]').fill('admin123');
  
  // Click submit
  await page.locator('button[type="submit"]').click();
  
  // Wait for navigation to dashboard (various possible paths)
  await page.waitForURL(/\/(dashboard)?$/, { timeout: 15000 });
  
  // Verify we're logged in - check for any dashboard element
  await expect(page.locator('body')).toContainText('Dashboard', { timeout: 10000 });
  
  // Save the authentication state
  await page.context().storageState({ path: authFile });
  
  console.log('✅ Auth setup completed successfully');
});
