import { test, expect } from '@playwright/test';

test.describe('API Limiter Dashboard Smoke Tests', () => {
  const username = `user_${Date.now()}`;
  const password = 'testpassword123';

  test('should register, login, view dashboard, and test rate limit', async ({ page }) => {
    // 1. Register
    await page.goto('/register');
    await page.fill('input[placeholder="Choose a username"]', username);
    await page.fill('input[placeholder="Choose a password"]', password);
    // Click FREE plan
    await page.locator('text=Free').click();
    await page.click('button:has-text("Register")');

    // Should redirect to login
    await expect(page).toHaveURL(/.*\/login/);

    // 2. Login
    await page.fill('input[placeholder="Enter username"]', username);
    await page.fill('input[placeholder="Enter password"]', password);
    await page.click('button:has-text("Login")');

    // Should redirect to dashboard
    await expect(page).toHaveURL(/.*\/dashboard/);
    await expect(page.locator('text=API Dashboard')).toBeVisible();
    await expect(page.locator(`text=Welcome, ${username}`)).toBeVisible();

    // 3. Verify Stats
    await expect(page.locator('text=FREE PLAN')).toBeVisible();
    // Wait for tokens to load
    await expect(page.locator('text=Tokens')).not.toBeEmpty();

    // 4. Logout
    await page.click('button:has-text("Logout")');
    await expect(page).toHaveURL(/.*\/login/);
  });
});
