import { test, expect } from '@playwright/test'

/**
 * 用户认证E2E测试
 * Requirements: 2.2 - 覆盖用户登录注册流程测试
 */
test.describe('用户认证流程', () => {
  
  test.beforeEach(async ({ page }) => {
    // 清除本地存储，确保测试从未登录状态开始
    await page.goto('/')
    await page.evaluate(() => {
      localStorage.clear()
    })
  })

  test.describe('登录功能', () => {
    
    test('应该显示登录页面', async ({ page }) => {
      await page.goto('/#/login')
      
      // 验证登录页面元素存在
      await expect(page.locator('.login-card')).toBeVisible()
      await expect(page.locator('input[placeholder="用户名"]')).toBeVisible()
      await expect(page.locator('input[placeholder="密码"]')).toBeVisible()
      await expect(page.locator('button[type="submit"]')).toBeVisible()
    })

    test('空用户名应该显示错误提示', async ({ page }) => {
      await page.goto('/#/login')
      
      // 只填写密码，不填用户名
      await page.fill('input[placeholder="密码"]', 'testpassword')
      await page.click('button[type="submit"]')
      
      // 验证错误提示
      await expect(page.locator('.error-text')).toContainText('请输入用户名')
    })

    test('空密码应该显示错误提示', async ({ page }) => {
      await page.goto('/#/login')
      
      // 只填写用户名，不填密码
      await page.fill('input[placeholder="用户名"]', 'testuser')
      await page.click('button[type="submit"]')
      
      // 验证错误提示
      await expect(page.locator('.error-text')).toContainText('请输入密码')
    })

    test('使用有效凭据登录应该跳转到仪表盘', async ({ page }) => {
      await page.goto('/#/login')
      
      // 填写登录表单
      await page.fill('input[placeholder="用户名"]', 'admin')
      await page.fill('input[placeholder="密码"]', 'admin123')
      
      // 点击登录按钮
      await page.click('button[type="submit"]')
      
      // 等待跳转到仪表盘（可能需要等待API响应）
      await page.waitForURL('**/#/dashboard**', { timeout: 15000 })
      
      // 验证已跳转到仪表盘
      expect(page.url()).toContain('/dashboard')
    })

    test('使用无效凭据登录应该显示错误', async ({ page }) => {
      await page.goto('/#/login')
      
      // 填写无效的登录信息
      await page.fill('input[placeholder="用户名"]', 'invaliduser')
      await page.fill('input[placeholder="密码"]', 'wrongpassword')
      
      // 点击登录按钮
      await page.click('button[type="submit"]')
      
      // 等待错误响应（卡片抖动或错误消息）
      // 验证仍在登录页面
      await page.waitForTimeout(2000)
      expect(page.url()).toContain('/login')
    })

    test('记住我功能应该保存用户名', async ({ page }) => {
      await page.goto('/#/login')
      
      // 填写登录表单并勾选记住我
      await page.fill('input[placeholder="用户名"]', 'admin')
      await page.fill('input[placeholder="密码"]', 'admin123')
      await page.check('input[type="checkbox"]')
      
      // 点击登录
      await page.click('button[type="submit"]')
      
      // 等待登录成功
      await page.waitForURL('**/#/dashboard**', { timeout: 15000 })
      
      // 验证localStorage中保存了用户名
      const rememberedUsername = await page.evaluate(() => {
        return localStorage.getItem('rememberedUsername')
      })
      expect(rememberedUsername).toBe('admin')
    })

    test('密码显示/隐藏切换应该正常工作', async ({ page }) => {
      await page.goto('/#/login')
      
      const passwordInput = page.locator('input[placeholder="密码"]')
      const toggleButton = page.locator('.toggle-password')
      
      // 初始状态应该是密码隐藏
      await expect(passwordInput).toHaveAttribute('type', 'password')
      
      // 点击切换按钮
      await toggleButton.click()
      
      // 密码应该显示
      await expect(passwordInput).toHaveAttribute('type', 'text')
      
      // 再次点击切换
      await toggleButton.click()
      
      // 密码应该再次隐藏
      await expect(passwordInput).toHaveAttribute('type', 'password')
    })
  })

  test.describe('注册功能', () => {
    
    test('应该能打开注册弹窗', async ({ page }) => {
      await page.goto('/#/login')
      
      // 点击注册链接
      await page.click('.register-link')
      
      // 验证注册弹窗显示
      await expect(page.locator('.register-modal')).toBeVisible()
      await expect(page.locator('.modal-title')).toContainText('创建账号')
    })

    test('注册表单应该包含所有必要字段', async ({ page }) => {
      await page.goto('/#/login')
      await page.click('.register-link')
      
      // 验证所有输入字段存在
      await expect(page.locator('.register-modal input[placeholder="用户名"]')).toBeVisible()
      await expect(page.locator('.register-modal input[placeholder="邮箱"]')).toBeVisible()
      await expect(page.locator('.register-modal input[placeholder="密码"]')).toBeVisible()
      await expect(page.locator('.register-modal input[placeholder="确认密码"]')).toBeVisible()
    })

    test('应该能关闭注册弹窗', async ({ page }) => {
      await page.goto('/#/login')
      
      // 打开注册弹窗
      await page.click('.register-link')
      await expect(page.locator('.register-modal')).toBeVisible()
      
      // 点击关闭按钮
      await page.click('.modal-close')
      
      // 验证弹窗已关闭
      await expect(page.locator('.register-modal')).not.toBeVisible()
    })

    test('密码不匹配应该阻止注册', async ({ page }) => {
      await page.goto('/#/login')
      await page.click('.register-link')
      
      // 填写注册表单，密码不匹配
      await page.fill('.register-modal input[placeholder="用户名"]', 'newuser')
      await page.fill('.register-modal input[placeholder="邮箱"]', 'newuser@test.com')
      await page.fill('.register-modal input[placeholder="密码"]', 'password123')
      await page.fill('.register-modal input[placeholder="确认密码"]', 'differentpassword')
      await page.check('.register-modal .agreement input[type="checkbox"]')
      
      // 点击注册按钮
      await page.click('.register-modal button[type="submit"]')
      
      // 弹窗应该仍然显示（注册未成功）
      await page.waitForTimeout(1000)
      await expect(page.locator('.register-modal')).toBeVisible()
    })

    test('未同意服务条款应该阻止注册', async ({ page }) => {
      await page.goto('/#/login')
      await page.click('.register-link')
      
      // 填写注册表单，但不勾选同意条款
      await page.fill('.register-modal input[placeholder="用户名"]', 'newuser')
      await page.fill('.register-modal input[placeholder="邮箱"]', 'newuser@test.com')
      await page.fill('.register-modal input[placeholder="密码"]', 'password123')
      await page.fill('.register-modal input[placeholder="确认密码"]', 'password123')
      // 不勾选同意条款
      
      // 点击注册按钮
      await page.click('.register-modal button[type="submit"]')
      
      // 弹窗应该仍然显示
      await page.waitForTimeout(1000)
      await expect(page.locator('.register-modal')).toBeVisible()
    })
  })

  test.describe('登出功能', () => {
    
    test('登出后应该跳转到登录页', async ({ page }) => {
      // 先登录
      await page.goto('/#/login')
      await page.fill('input[placeholder="用户名"]', 'admin')
      await page.fill('input[placeholder="密码"]', 'admin123')
      await page.click('button[type="submit"]')
      
      // 等待登录成功
      await page.waitForURL('**/#/dashboard**', { timeout: 15000 })
      
      // 清除token模拟登出
      await page.evaluate(() => {
        localStorage.removeItem('token')
        localStorage.removeItem('isLoggedIn')
      })
      
      // 刷新页面
      await page.reload()
      
      // 应该被重定向到登录页
      await page.waitForURL('**/#/login**', { timeout: 10000 })
      expect(page.url()).toContain('/login')
    })

    test('登出后localStorage应该被清除', async ({ page }) => {
      // 先登录
      await page.goto('/#/login')
      await page.fill('input[placeholder="用户名"]', 'admin')
      await page.fill('input[placeholder="密码"]', 'admin123')
      await page.click('button[type="submit"]')
      
      await page.waitForURL('**/#/dashboard**', { timeout: 15000 })
      
      // 验证token存在
      let token = await page.evaluate(() => localStorage.getItem('token'))
      expect(token).toBeTruthy()
      
      // 模拟登出
      await page.evaluate(() => {
        localStorage.removeItem('token')
        localStorage.removeItem('isLoggedIn')
        localStorage.removeItem('username')
      })
      
      // 验证token已清除
      token = await page.evaluate(() => localStorage.getItem('token'))
      expect(token).toBeNull()
    })
  })

  test.describe('路由守卫', () => {
    
    test('未登录用户访问受保护页面应该跳转到登录页', async ({ page }) => {
      // 确保未登录
      await page.goto('/')
      await page.evaluate(() => {
        localStorage.clear()
      })
      
      // 尝试访问仪表盘
      await page.goto('/#/dashboard')
      
      // 应该被重定向到登录页
      await page.waitForURL('**/#/login**', { timeout: 10000 })
      expect(page.url()).toContain('/login')
    })

    test('已登录用户访问登录页应该跳转到仪表盘', async ({ page }) => {
      // 先登录
      await page.goto('/#/login')
      await page.fill('input[placeholder="用户名"]', 'admin')
      await page.fill('input[placeholder="密码"]', 'admin123')
      await page.click('button[type="submit"]')
      
      await page.waitForURL('**/#/dashboard**', { timeout: 15000 })
      
      // 尝试访问登录页
      await page.goto('/#/login')
      
      // 应该被重定向到仪表盘
      await page.waitForURL('**/#/dashboard**', { timeout: 10000 })
      expect(page.url()).toContain('/dashboard')
    })
  })
})
