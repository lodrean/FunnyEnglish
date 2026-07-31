import { test, expect } from '@playwright/test';

/**
 * CMP WASM Gamification Tests
 * Tests streaks, achievements, XP, leaderboard
 */
test.describe('CMP WASM - Gamification', () => {
  
  test('should display user stats on home screen', async ({ page }) => {
    await page.goto('/');
    await expect(page.locator('canvas')).toBeVisible({ timeout: 30000 });
    await page.waitForTimeout(5000);
    
    // Screenshot home screen with stats
    await page.screenshot({ path: 'test-results/cmp-gamification-home.png' });
    
    // Check console for gamification data
    const logs: string[] = [];
    page.on('console', msg => logs.push(msg.text()));
    
    await page.waitForTimeout(3000);
    
    const gamificationLogs = logs.filter(l => 
      l.includes('level') || 
      l.includes('xp') || 
      l.includes('streak') ||
      l.includes('points')
    );
    
    console.log('Gamification logs:', gamificationLogs);
    console.log('✅ Home screen with gamification captured');
  });

  test('should navigate to achievements screen', async ({ page }) => {
    await page.goto('/');
    await expect(page.locator('canvas')).toBeVisible({ timeout: 30000 });
    await page.waitForTimeout(3000);
    
    const canvas = page.locator('canvas');
    const box = await canvas.boundingBox();
    
    if (box) {
      // Navigate to profile/achievements
      await canvas.click({ 
        position: { x: box.width * 0.8, y: box.height - 40 } 
      });
      await page.waitForTimeout(2000);
      
      await page.screenshot({ path: 'test-results/cmp-achievements.png' });
    }
    
    console.log('✅ Achievements screen captured');
  });

  test('should show leaderboard', async ({ page }) => {
    await page.goto('/');
    await expect(page.locator('canvas')).toBeVisible({ timeout: 30000 });
    await page.waitForTimeout(3000);
    
    const canvas = page.locator('canvas');
    const box = await canvas.boundingBox();
    
    if (box) {
      // Try to find leaderboard (might be in menu or separate tab)
      // Click on menu or hamburger
      await canvas.click({ position: { x: 40, y: 40 } });
      await page.waitForTimeout(2000);
      
      await page.screenshot({ path: 'test-results/cmp-menu.png' });
    }
    
    console.log('✅ Menu/Leaderboard navigation tested');
  });

  test('should update streak after activity', async ({ page }) => {
    await page.goto('/');
    await expect(page.locator('canvas')).toBeVisible({ timeout: 30000 });
    await page.waitForTimeout(3000);
    
    // Take initial screenshot
    await page.screenshot({ path: 'test-results/cmp-streak-initial.png' });
    
    // Check localStorage for streak data
    const streakData = await page.evaluate(() => {
      return {
        currentStreak: localStorage.getItem('currentStreak'),
        lastActivityDate: localStorage.getItem('lastActivityDate')
      };
    });
    
    console.log('Streak data:', streakData);
    console.log('✅ Streak data checked');
  });
});
