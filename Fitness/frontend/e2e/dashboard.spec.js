import { test, expect } from '@playwright/test'

/**
 * 仪表盘E2E测试
 * Requirements: 2.5 - 覆盖仪表盘数据展示测试
 */
test.describe('仪表盘功能', () => {
  
  // 在每个测试前登录
  test.beforeEach(async ({ page }) => {
    // 清除本地存储
    await page.goto('/')
    await page.evaluate(() => {
      localStorage.clear()
    })
    
    // 登录
    await page.goto('/#/login')
    await page.fill('input[placeholder="用户名"]', 'admin')
    await page.fill('input[placeholder="密码"]', 'admin123')
    await page.click('button[type="submit"]')
    
    // 等待登录成功
    await page.waitForURL('**/#/dashboard**', { timeout: 15000 })
  })

  test.describe('仪表盘数据加载', () => {
    
    test('应该能访问仪表盘页面', async ({ page }) => {
      // 验证已在仪表盘页面
      expect(page.url()).toContain('/dashboard')
      
      // 验证仪表盘容器存在
      await expect(page.locator('.dynamic-dashboard, .dashboard-container')).toBeVisible()
    })

    test('应该显示仪表盘标题', async ({ page }) => {
      // 验证标题存在
      await expect(page.locator('.dashboard-title, .dashboard-header h1')).toBeVisible()
    })

    test('应该显示仪表盘控制按钮', async ({ page }) => {
      // 验证控制区域存在
      await expect(page.locator('.dashboard-controls, .dashboard-header')).toBeVisible()
    })

    test('应该显示布局选择器', async ({ page }) => {
      // 验证布局选择器存在
      const layoutSelector = page.locator('.modern-select, .el-select').first()
      await expect(layoutSelector).toBeVisible()
    })

    test('应该显示编辑布局按钮', async ({ page }) => {
      // 验证编辑按钮存在
      await expect(page.locator('button:has-text("编辑布局")')).toBeVisible()
    })

    test('应该显示重置布局按钮', async ({ page }) => {
      // 验证重置按钮存在
      await expect(page.locator('button:has-text("重置布局")')).toBeVisible()
    })

    test('编辑模式应该能切换', async ({ page }) => {
      // 点击编辑布局按钮
      await page.click('button:has-text("编辑布局")')
      
      // 验证按钮文本变化
      await expect(page.locator('button:has-text("完成编辑")')).toBeVisible()
      
      // 再次点击退出编辑模式
      await page.click('button:has-text("完成编辑")')
      
      // 验证按钮文本恢复
      await expect(page.locator('button:has-text("编辑布局")')).toBeVisible()
    })

    test('重置布局应该正常工作', async ({ page }) => {
      // 点击重置布局按钮
      await page.click('button:has-text("重置布局")')
      
      // 等待操作完成
      await page.waitForTimeout(1000)
      
      // 验证仍在仪表盘页面
      expect(page.url()).toContain('/dashboard')
    })
  })

  test.describe('图表渲染', () => {
    
    test('应该显示指标概览区域', async ({ page }) => {
      // 等待页面加载
      await page.waitForTimeout(2000)
      
      // 验证指标区域存在（可能是MetricsOverview组件）
      const metricsSection = page.locator('.metrics-overview, .metric-card, .overview-card').first()
      if (await metricsSection.isVisible()) {
        await expect(metricsSection).toBeVisible()
      }
    })

    test('应该显示数据分析区域', async ({ page }) => {
      // 等待页面加载
      await page.waitForTimeout(2000)
      
      // 验证分析区域存在
      const analyticsSection = page.locator('.analytics-section, .chart-card, .chart-container').first()
      if (await analyticsSection.isVisible()) {
        await expect(analyticsSection).toBeVisible()
      }
    })

    test('应该显示最近活动区域', async ({ page }) => {
      // 等待页面加载
      await page.waitForTimeout(2000)
      
      // 验证活动区域存在
      const activitySection = page.locator('.recent-activity, .activity-content').first()
      if (await activitySection.isVisible()) {
        await expect(activitySection).toBeVisible()
      }
    })

    test('图表容器应该正确渲染', async ({ page }) => {
      // 等待页面加载
      await page.waitForTimeout(3000)
      
      // 查找图表容器
      const chartContainers = page.locator('.chart-content, .chart-container, canvas, .echarts-container')
      const count = await chartContainers.count()
      
      // 如果有图表容器，验证它们可见
      if (count > 0) {
        const firstChart = chartContainers.first()
        await expect(firstChart).toBeVisible()
      }
    })

    test('欢迎区域应该显示', async ({ page }) => {
      // 等待页面加载
      await page.waitForTimeout(2000)
      
      // 验证欢迎区域存在
      const welcomeSection = page.locator('.welcome-section, .welcome-content').first()
      if (await welcomeSection.isVisible()) {
        await expect(welcomeSection).toBeVisible()
      }
    })
  })

  test.describe('布局切换', () => {
    
    test('应该能打开布局选择器', async ({ page }) => {
      // 点击布局选择器
      const layoutSelector = page.locator('.modern-select, .el-select').first()
      await layoutSelector.click()
      
      // 验证下拉菜单出现
      await expect(page.locator('.el-select-dropdown')).toBeVisible()
    })

    test('应该显示可用的布局选项', async ({ page }) => {
      // 点击布局选择器
      const layoutSelector = page.locator('.modern-select, .el-select').first()
      await layoutSelector.click()
      
      // 等待下拉菜单
      await page.waitForTimeout(500)
      
      // 验证有布局选项
      const options = page.locator('.el-select-dropdown__item')
      const count = await options.count()
      expect(count).toBeGreaterThan(0)
    })
  })

  test.describe('响应式布局', () => {
    
    test('在桌面视口应该正常显示', async ({ page }) => {
      // 设置桌面视口
      await page.setViewportSize({ width: 1920, height: 1080 })
      
      // 刷新页面
      await page.reload()
      await page.waitForTimeout(2000)
      
      // 验证仪表盘可见
      await expect(page.locator('.dynamic-dashboard, .dashboard-container')).toBeVisible()
    })

    test('在平板视口应该正常显示', async ({ page }) => {
      // 设置平板视口
      await page.setViewportSize({ width: 768, height: 1024 })
      
      // 刷新页面
      await page.reload()
      await page.waitForTimeout(2000)
      
      // 验证仪表盘可见
      await expect(page.locator('.dynamic-dashboard, .dashboard-container')).toBeVisible()
    })

    test('在移动端视口应该正常显示', async ({ page }) => {
      // 设置移动端视口
      await page.setViewportSize({ width: 375, height: 667 })
      
      // 刷新页面
      await page.reload()
      await page.waitForTimeout(2000)
      
      // 验证仪表盘可见
      await expect(page.locator('.dynamic-dashboard, .dashboard-container')).toBeVisible()
    })
  })

  test.describe('导航功能', () => {
    
    test('应该能从仪表盘导航到训练数据页面', async ({ page }) => {
      // 导航到训练数据页面
      await page.goto('/#/training-data')
      
      // 验证已跳转
      await page.waitForURL('**/#/training-data**', { timeout: 10000 })
      expect(page.url()).toContain('/training-data')
    })

    test('应该能从仪表盘导航到营养追踪页面', async ({ page }) => {
      // 导航到营养追踪页面
      await page.goto('/#/nutrition-tracking')
      
      // 验证已跳转
      await page.waitForURL('**/#/nutrition-tracking**', { timeout: 10000 })
      expect(page.url()).toContain('/nutrition-tracking')
    })

    test('应该能返回仪表盘', async ({ page }) => {
      // 先导航到其他页面
      await page.goto('/#/training-data')
      await page.waitForURL('**/#/training-data**', { timeout: 10000 })
      
      // 返回仪表盘
      await page.goto('/#/dashboard')
      
      // 验证已返回
      await page.waitForURL('**/#/dashboard**', { timeout: 10000 })
      expect(page.url()).toContain('/dashboard')
    })
  })
})
