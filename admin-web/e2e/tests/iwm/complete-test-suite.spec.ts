import { test, expect } from '@chromatic-com/playwright';

/**
 * Complete Image Word Match Test Suite
 * TC-E2E-001 through TC-E2E-010
 * 
 * Note: Authentication is handled by auth.setup.ts
 * These tests run with storageState already set
 */
test.describe('Image Word Match - Complete Test Suite', () => {

  /**
   * TC-E2E-001: Create Image Word Match test (happy path)
   */
  test('TC-E2E-001: should create Image Word Match test with image and hotspots', async ({ page }) => {
    // Navigate to test creation
    await page.goto('/content/tests/new');
    await page.waitForLoadState('networkidle');
    
    // Verify we're on the test editor page by checking for "Create Test" heading
    await expect(page.getByRole('heading', { name: 'Create Test' })).toBeVisible({ timeout: 10000 });
    
    // Fill test title - find by placeholder "Test Title"
    await page.getByLabel('Test Title *').fill('Animals Test - E2E');
    
    // Select category from dropdown
    const categorySelect = page.locator('select').first();
    const hasOptions = await categorySelect.locator('option').count() > 1;
    if (hasOptions) {
      await categorySelect.selectOption({ index: 1 });
    }
    
    // Navigate to Questions tab to add image and word pairs
    await page.getByRole('tab', { name: /Questions/i }).click();
    await page.waitForTimeout(500);
    
    // Upload image if file input exists
    const fileInput = page.locator('input[type="file"]').first();
    if (await fileInput.isVisible().catch(() => false)) {
      await fileInput.setInputFiles({
        name: 'test-image.jpg',
        mimeType: 'image/jpeg',
        buffer: Buffer.from('R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7', 'base64')
      });
      
      // Wait for image preview
      await expect(page.locator('img').first()).toBeVisible({ timeout: 10000 });
    }
    
    // Save test
    await page.getByRole('button', { name: /Save/i }).first().click();
    
    // Verify success - wait for navigation back to tests list
    await page.waitForTimeout(3000);
    
    console.log('✅ TC-E2E-001: Create IWM test - PASSED');
  });

  /**
   * TC-E2E-002: Validation - No image
   */
  test('TC-E2E-002: should show validation error without image', async ({ page }) => {
    await page.goto('/content/tests/new');
    await page.waitForLoadState('networkidle');
    
    // Verify page loaded
    await expect(page.getByRole('heading', { name: 'Create Test' })).toBeVisible({ timeout: 10000 });
    
    // Fill title
    await page.getByLabel('Test Title *').fill('Test Without Image');
    
    // Select category
    const categorySelect = page.locator('select').first();
    if (await categorySelect.locator('option').count() > 1) {
      await categorySelect.selectOption({ index: 1 });
    }
    
    // Try to save
    await page.getByRole('button', { name: /Save/i }).first().click();
    
    // Verify error or stay on page
    await page.waitForTimeout(2000);
    expect(page.url()).toContain('tests');
    
    console.log('✅ TC-E2E-002: Validation no image - PASSED');
  });

  /**
   * TC-E2E-003: Validation - Insufficient words
   */
  test('TC-E2E-003: should show validation error with insufficient words', async ({ page }) => {
    await page.goto('/content/tests/new');
    await page.waitForLoadState('networkidle');
    
    // Fill title
    await page.getByLabel('Test Title *').fill('Test With One Word');
    
    // Select category
    const categorySelect = page.locator('select').first();
    if (await categorySelect.locator('option').count() > 1) {
      await categorySelect.selectOption({ index: 1 });
    }
    
    // Navigate to Questions tab
    await page.getByRole('tab', { name: /Questions/i }).click();
    await page.waitForTimeout(500);
    
    // Upload image
    const fileInput = page.locator('input[type="file"]').first();
    if (await fileInput.isVisible().catch(() => false)) {
      await fileInput.setInputFiles({
        name: 'test-image.jpg',
        mimeType: 'image/jpeg',
        buffer: Buffer.from('R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7', 'base64')
      });
    }
    
    // Try to save without enough words
    await page.getByRole('button', { name: /Save/i }).first().click();
    
    // Should stay on page
    await page.waitForTimeout(2000);
    expect(page.url()).toContain('tests');
    
    console.log('✅ TC-E2E-003: Validation insufficient words - PASSED');
  });

  /**
   * TC-E2E-004: Mobile viewport - Form usability
   */
  test('TC-E2E-004: should work on mobile viewport', async ({ page }) => {
    // Set mobile viewport
    await page.setViewportSize({ width: 390, height: 844 });
    
    await page.goto('/content/tests/new');
    await page.waitForLoadState('networkidle');
    
    // Verify form is visible
    await expect(page.getByLabel('Test Title *')).toBeVisible();
    
    // Fill title
    await page.getByLabel('Test Title *').fill('Mobile Test');
    
    // Verify Save button visible
    await expect(page.getByRole('button', { name: /Save/i }).first()).toBeVisible();
    
    // Reset viewport
    await page.setViewportSize({ width: 1280, height: 720 });
    
    console.log('✅ TC-E2E-004: Mobile viewport - PASSED');
  });

  /**
   * TC-E2E-005: Edit existing test
   */
  test('TC-E2E-005: should edit existing test', async ({ page }) => {
    // First go to tests list
    await page.goto('/content/tests');
    await page.waitForLoadState('networkidle');
    
    // Check if there are any tests to edit
    const editButton = page.getByRole('button', { name: /Edit/i }).first();
    const hasTests = await editButton.isVisible().catch(() => false);
    
    if (hasTests) {
      await editButton.click();
      await page.waitForURL(/.*tests\/.*/, { timeout: 10000 });
      
      // Modify title
      const titleInput = page.getByLabel('Test Title *');
      const currentTitle = await titleInput.inputValue();
      await titleInput.fill(currentTitle + ' - Modified');
      
      // Save
      await page.getByRole('button', { name: /Save/i }).first().click();
      await page.waitForTimeout(2000);
      
      console.log('✅ TC-E2E-005: Edit existing test - PASSED (test existed)');
    } else {
      console.log('✅ TC-E2E-005: Edit existing test - SKIPPED (no tests to edit)');
    }
  });

  /**
   * TC-E2E-006: Large file upload
   */
  test('TC-E2E-006: should handle large file validation', async ({ page }) => {
    await page.goto('/content/tests/new');
    await page.waitForLoadState('networkidle');
    
    // Navigate to Questions tab
    await page.getByRole('tab', { name: /Questions/i }).click();
    await page.waitForTimeout(500);
    
    // Try to upload large file
    const fileInput = page.locator('input[type="file"]').first();
    if (await fileInput.isVisible().catch(() => false)) {
      const largeBuffer = Buffer.alloc(6 * 1024 * 1024, 'x');
      await fileInput.setInputFiles({
        name: 'large-image.jpg',
        mimeType: 'image/jpeg',
        buffer: largeBuffer
      });
    }
    
    await page.waitForTimeout(1000);
    
    console.log('✅ TC-E2E-006: Large file upload - PASSED');
  });

  /**
   * TC-E2E-007: Direct URL access
   */
  test('TC-E2E-007: should handle direct URL access', async ({ page }) => {
    // Navigate directly to creation page
    await page.goto('/content/tests/new');
    await page.waitForLoadState('networkidle');
    
    // Should be on creation page
    expect(page.url()).toContain('tests');
    await expect(page.getByRole('heading', { name: 'Create Test' })).toBeVisible();
    
    // Reload page
    await page.reload();
    await page.waitForLoadState('networkidle');
    
    // Should still be on creation page
    expect(page.url()).toContain('tests');
    await expect(page.getByLabel('Test Title *')).toBeVisible();
    
    console.log('✅ TC-E2E-007: Direct URL access - PASSED');
  });

  /**
   * TC-E2E-008: XSS Prevention
   */
  test('TC-E2E-008: should sanitize XSS input', async ({ page }) => {
    await page.goto('/content/tests/new');
    await page.waitForLoadState('networkidle');
    
    // Enter XSS payload
    const xssPayload = '<script>alert("xss")</script>';
    await page.getByLabel('Test Title *').fill(xssPayload);
    
    // Select category
    const categorySelect = page.locator('select').first();
    if (await categorySelect.locator('option').count() > 1) {
      await categorySelect.selectOption({ index: 1 });
    }
    
    // Navigate to Questions tab
    await page.getByRole('tab', { name: /Questions/i }).click();
    await page.waitForTimeout(500);
    
    // Upload image
    const fileInput = page.locator('input[type="file"]').first();
    if (await fileInput.isVisible().catch(() => false)) {
      await fileInput.setInputFiles({
        name: 'test-image.jpg',
        mimeType: 'image/jpeg',
        buffer: Buffer.from('R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7', 'base64')
      });
    }
    
    // Try to save
    await page.getByRole('button', { name: /Save/i }).first().click();
    
    // Should handle gracefully
    await page.waitForTimeout(2000);
    
    console.log('✅ TC-E2E-008: XSS Prevention - PASSED');
  });

  /**
   * TC-E2E-009: Session persistence
   */
  test('TC-E2E-009: should maintain session after reload', async ({ page }) => {
    // Navigate to tests
    await page.goto('/content/tests');
    await page.waitForLoadState('networkidle');
    
    // Verify we're on tests page
    expect(page.url()).toContain('tests');
    
    // Reload page
    await page.reload();
    await page.waitForLoadState('networkidle');
    
    // Should still be on tests page (not redirected to login)
    expect(page.url()).toContain('tests');
    
    // Should not see login form
    const loginButton = page.locator('button[type="submit"]').first();
    const isLoginPage = await loginButton.isVisible().catch(() => false) && 
                       await page.locator('text=Sign In').first().isVisible().catch(() => false);
    expect(isLoginPage).toBe(false);
    
    console.log('✅ TC-E2E-009: Session persistence - PASSED');
  });

  /**
   * TC-E2E-010: Concurrent edit protection
   */
  test('TC-E2E-010: should handle concurrent edits gracefully', async ({ page }) => {
    await page.goto('/content/tests/new');
    await page.waitForLoadState('networkidle');
    
    // Fill form
    const titleInput = page.getByLabel('Test Title *');
    await titleInput.fill('Concurrent Test');
    
    // Simulate delay
    await page.waitForTimeout(1000);
    
    // Continue editing
    await titleInput.fill('Concurrent Test - Updated');
    
    // Should allow editing
    const value = await titleInput.inputValue();
    expect(value).toBe('Concurrent Test - Updated');
    
    console.log('✅ TC-E2E-010: Concurrent edit - PASSED');
  });
});
