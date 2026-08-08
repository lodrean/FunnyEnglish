import { test, expect } from '@playwright/test';
import * as fs from 'fs';
import * as path from 'path';

/**
 * CMP E2E Configuration Tests
 * Verifies test setup without needing running server
 */
test.describe('CMP E2E - Configuration', () => {
  
  test('should have valid playwright config', async () => {
    const configPath = path.join(__dirname, '..', 'playwright.config.ts');
    expect(fs.existsSync(configPath)).toBe(true);
    
    const config = fs.readFileSync(configPath, 'utf-8');
    expect(config).toContain('testDir');
    expect(config).toContain('baseURL');
    expect(config).toContain('wasmJsBrowserDevelopmentRun');
    
    console.log('✅ Playwright config is valid');
  });

  test('should have all test files', async () => {
    const testDir = path.join(__dirname);
    const expectedFiles = [
      'smoke.spec.ts',
      'auth.spec.ts',
      'navigation.spec.ts',
      'performance.spec.ts',
      'config.spec.ts'
    ];
    
    for (const file of expectedFiles) {
      const filePath = path.join(testDir, file);
      expect(fs.existsSync(filePath)).toBe(true);
      console.log(`✅ ${file} exists`);
    }
  });

  test('should have package.json with dependencies', async () => {
    const packagePath = path.join(__dirname, '..', 'package.json');
    expect(fs.existsSync(packagePath)).toBe(true);
    
    const packageJson = JSON.parse(fs.readFileSync(packagePath, 'utf-8'));
    expect(packageJson.devDependencies).toBeDefined();
    expect(packageJson.devDependencies['@playwright/test']).toBeDefined();
    
    console.log('✅ package.json is valid');
  });

  test('should verify test structure', async () => {
    // Count test files
    const testDir = path.join(__dirname);
    const testFiles = fs.readdirSync(testDir)
      .filter(f => f.endsWith('.spec.ts'));
    
    expect(testFiles.length).toBeGreaterThanOrEqual(5);
    console.log(`✅ Found ${testFiles.length} test files`);
    
    // Verify each file has tests
    for (const file of testFiles) {
      const content = fs.readFileSync(path.join(testDir, file), 'utf-8');
      expect(content).toContain('test(');
      expect(content).toContain('expect(');
    }
    
    console.log('✅ All test files have valid structure');
  });

  test('should have README documentation', async () => {
    const readmePath = path.join(__dirname, '..', 'README.md');
    expect(fs.existsSync(readmePath)).toBe(true);
    
    const readme = fs.readFileSync(readmePath, 'utf-8');
    expect(readme).toContain('CMP');
    expect(readme).toContain('Playwright');
    expect(readme).toContain('WASM');
    
    console.log('✅ README documentation exists');
  });
});

test.describe('CMP E2E - Environment Check', () => {
  
  test('should have working Playwright installation', async () => {
    // This test will fail if Playwright is not installed
    // But it validates the setup
    const { chromium } = require('@playwright/test');
    expect(chromium).toBeDefined();
    
    console.log('✅ Playwright is installed');
  });

  test('should have required environment variables info', async () => {
    console.log('Environment:');
    console.log(`  CI: ${process.env.CI || 'not set'}`);
    console.log(`  SKIP_WEB_SERVER: ${process.env.SKIP_WEB_SERVER || 'not set'}`);
    console.log(`  CMP_URL: ${process.env.CMP_URL || 'http://localhost:8082 (default)'}`);
    
    // Just info, always passes
    expect(true).toBe(true);
  });
});
