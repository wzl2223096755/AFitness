import { test as base } from '@playwright/test'

/**
 * 扩展的测试fixtures
 * 提供登录状态和常用测试数据
 */
export const test = base.extend({
  // 已登录用户的页面
  authenticatedPage: async ({ page }, use) => {
    // 访问登录页
    await page.goto('/login')
    
    // 执行登录
    await page.fill('input[placeholder*="用户名"], input[type="text"]', 'admin')
    await page.fill('input[placeholder*="密码"], input[type="password"]', 'admin123')
    await page.click('button[type="submit"], .login-btn')
    
    // 等待登录成功跳转
    await page.waitForURL('**/dashboard**', { timeout: 10000 })
    
    await use(page)
  },
  
  // 测试用户数据
  testUser: async ({}, use) => {
    await use({
      username: 'admin',
      password: 'admin123',
      invalidUsername: 'invaliduser',
      invalidPassword: 'wrongpassword'
    })
  },
  
  // 测试训练数据
  testTrainingData: async ({}, use) => {
    await use({
      exerciseName: '深蹲',
      weight: 100,
      reps: 10,
      sets: 3,
      duration: 60
    })
  },
  
  // 测试营养数据
  testNutritionData: async ({}, use) => {
    await use({
      foodName: '鸡胸肉',
      calories: 165,
      protein: 31,
      carbs: 0,
      fat: 3.6
    })
  }
})

export { expect } from '@playwright/test'
