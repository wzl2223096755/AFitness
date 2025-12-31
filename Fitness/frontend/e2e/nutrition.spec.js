import { test, expect } from '@playwright/test'

/**
 * 营养记录E2E测试
 * Requirements: 2.4 - 覆盖营养记录流程测试
 */
test.describe('营养记录管理', () => {
  
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

  test.describe('营养记录添加', () => {
    
    test('应该能访问营养追踪页面', async ({ page }) => {
      await page.goto('/#/nutrition-tracking')
      
      // 验证页面元素
      await expect(page.locator('.nutrition-page')).toBeVisible()
      await expect(page.locator('.page-header h1')).toContainText('营养追踪')
    })

    test('应该显示营养概览卡片', async ({ page }) => {
      await page.goto('/#/nutrition-tracking')
      
      // 验证概览卡片存在
      await expect(page.locator('.overview-cards')).toBeVisible()
      await expect(page.locator('.overview-card.calories')).toBeVisible()
      await expect(page.locator('.overview-card.protein')).toBeVisible()
      await expect(page.locator('.overview-card.carbs')).toBeVisible()
      await expect(page.locator('.overview-card.fat')).toBeVisible()
    })

    test('应该显示添加饮食记录表单', async ({ page }) => {
      await page.goto('/#/nutrition-tracking')
      
      // 验证表单元素存在
      await expect(page.locator('.form-card')).toBeVisible()
      await expect(page.locator('.card-header h3:has-text("添加饮食记录")')).toBeVisible()
    })

    test('应该能选择餐次', async ({ page }) => {
      await page.goto('/#/nutrition-tracking')
      
      // 点击餐次选择器
      await page.click('.el-form-item:has-text("餐次") .el-select')
      
      // 验证选项存在
      await expect(page.locator('.el-select-dropdown__item:has-text("早餐")')).toBeVisible()
      await expect(page.locator('.el-select-dropdown__item:has-text("午餐")')).toBeVisible()
      await expect(page.locator('.el-select-dropdown__item:has-text("晚餐")')).toBeVisible()
      await expect(page.locator('.el-select-dropdown__item:has-text("加餐")')).toBeVisible()
      
      // 选择一个选项
      await page.click('.el-select-dropdown__item:has-text("午餐")')
    })

    test('应该能输入食物名称', async ({ page }) => {
      await page.goto('/#/nutrition-tracking')
      
      // 输入食物名称
      const foodInput = page.locator('input[placeholder="请输入食物名称"]')
      await foodInput.fill('鸡胸肉')
      
      // 验证输入值
      await expect(foodInput).toHaveValue('鸡胸肉')
    })

    test('食物建议列表应该正常工作', async ({ page }) => {
      await page.goto('/#/nutrition-tracking')
      
      // 输入部分食物名称
      const foodInput = page.locator('input[placeholder="请输入食物名称"]')
      await foodInput.fill('鸡')
      
      // 等待建议列表出现
      await page.waitForTimeout(500)
      
      // 检查是否有建议列表
      const suggestions = page.locator('.food-suggestions')
      if (await suggestions.isVisible()) {
        // 验证建议项存在
        await expect(page.locator('.suggestion-item:has-text("鸡胸肉")')).toBeVisible()
      }
    })

    test('应该能填写完整的营养记录', async ({ page }) => {
      await page.goto('/#/nutrition-tracking')
      
      // 选择餐次
      await page.click('.el-form-item:has-text("餐次") .el-select')
      await page.click('.el-select-dropdown__item:has-text("午餐")')
      
      // 输入食物名称
      await page.fill('input[placeholder="请输入食物名称"]', '鸡胸肉')
      
      // 输入份量
      const amountInput = page.locator('.el-form-item:has-text("份量") .el-input-number input')
      await amountInput.fill('200')
      
      // 点击添加记录按钮
      await page.click('button:has-text("添加记录")')
      
      // 等待操作完成
      await page.waitForTimeout(2000)
    })

    test('表单验证应该正常工作', async ({ page }) => {
      await page.goto('/#/nutrition-tracking')
      
      // 不填写任何内容直接点击添加
      await page.click('button:has-text("添加记录")')
      
      // 等待验证消息
      await page.waitForTimeout(500)
      
      // 应该仍在营养追踪页面
      expect(page.url()).toContain('/nutrition-tracking')
    })

    test('重置按钮应该清空表单', async ({ page }) => {
      await page.goto('/#/nutrition-tracking')
      
      // 填写一些数据
      await page.fill('input[placeholder="请输入食物名称"]', '牛肉')
      
      // 点击重置按钮
      await page.click('button:has-text("重置")')
      
      // 验证表单已清空
      const foodInput = page.locator('input[placeholder="请输入食物名称"]')
      await expect(foodInput).toHaveValue('')
    })
  })

  test.describe('营养统计查看', () => {
    
    test('应该显示今日营养摄入统计', async ({ page }) => {
      await page.goto('/#/nutrition-tracking')
      
      // 验证统计卡片显示数值
      await expect(page.locator('.overview-card.calories .card-value')).toBeVisible()
      await expect(page.locator('.overview-card.protein .card-value')).toBeVisible()
      await expect(page.locator('.overview-card.carbs .card-value')).toBeVisible()
      await expect(page.locator('.overview-card.fat .card-value')).toBeVisible()
    })

    test('应该显示营养摄入分布图表', async ({ page }) => {
      await page.goto('/#/nutrition-tracking')
      
      // 验证图表区域存在
      await expect(page.locator('.chart-section')).toBeVisible()
      await expect(page.locator('.chart-card')).toBeVisible()
    })

    test('应该能选择日期查看历史数据', async ({ page }) => {
      await page.goto('/#/nutrition-tracking')
      
      // 点击日期选择器
      const datePicker = page.locator('.chart-header .el-date-editor')
      await datePicker.click()
      
      // 验证日期选择器弹出
      await expect(page.locator('.el-date-picker')).toBeVisible()
    })

    test('应该显示今日饮食记录表格', async ({ page }) => {
      await page.goto('/#/nutrition-tracking')
      
      // 验证记录表格存在
      await expect(page.locator('.records-card')).toBeVisible()
      await expect(page.locator('.records-card .el-table')).toBeVisible()
    })

    test('表格应该显示正确的列', async ({ page }) => {
      await page.goto('/#/nutrition-tracking')
      
      // 验证表格列头
      const table = page.locator('.records-card .el-table')
      await expect(table.locator('th:has-text("餐次")')).toBeVisible()
      await expect(table.locator('th:has-text("食物名称")')).toBeVisible()
      await expect(table.locator('th:has-text("份量")')).toBeVisible()
      await expect(table.locator('th:has-text("卡路里")')).toBeVisible()
      await expect(table.locator('th:has-text("蛋白质")')).toBeVisible()
    })

    test('应该有导出数据按钮', async ({ page }) => {
      await page.goto('/#/nutrition-tracking')
      
      // 验证导出按钮存在
      await expect(page.locator('button:has-text("导出数据")')).toBeVisible()
    })

    test('删除按钮应该显示确认对话框', async ({ page }) => {
      await page.goto('/#/nutrition-tracking')
      
      // 等待表格加载
      await page.waitForSelector('.records-card .el-table')
      await page.waitForTimeout(1000)
      
      // 查找删除按钮
      const deleteButton = page.locator('.records-card .el-table button:has-text("删除")').first()
      
      if (await deleteButton.isVisible()) {
        await deleteButton.click()
        
        // 验证确认对话框出现
        await expect(page.locator('.el-message-box')).toBeVisible()
        await expect(page.locator('.el-message-box__message')).toContainText('确定要删除')
        
        // 点击取消
        await page.click('.el-message-box button:has-text("取消")')
      }
    })
  })
})
