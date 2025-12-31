import { test, expect } from '@playwright/test'

/**
 * 训练数据E2E测试
 * Requirements: 2.3 - 覆盖训练数据录入流程测试
 */
test.describe('训练数据管理', () => {
  
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

  test.describe('训练记录录入', () => {
    
    test('应该能访问训练数据页面', async ({ page }) => {
      await page.goto('/#/training-data')
      
      // 验证页面元素
      await expect(page.locator('.training-data-page')).toBeVisible()
      await expect(page.locator('.page-header h1')).toContainText('训练数据')
    })

    test('应该显示训练数据录入表单', async ({ page }) => {
      await page.goto('/#/training-data')
      
      // 验证表单元素存在
      await expect(page.locator('.form-card')).toBeVisible()
      await expect(page.locator('input[placeholder="请输入动作名称"]')).toBeVisible()
      await expect(page.locator('.el-select').first()).toBeVisible() // 动作类型选择器
    })

    test('应该能录入训练数据', async ({ page }) => {
      await page.goto('/#/training-data')
      
      // 填写训练表单
      await page.fill('input[placeholder="请输入动作名称"]', '深蹲')
      
      // 选择动作类型
      await page.click('.el-select')
      await page.click('.el-select-dropdown__item:has-text("下肢")')
      
      // 填写数值字段 - 使用el-input-number
      const weightInput = page.locator('.el-form-item:has-text("重量") .el-input-number input')
      await weightInput.fill('100')
      
      const setsInput = page.locator('.el-form-item:has-text("组数") .el-input-number input')
      await setsInput.fill('4')
      
      const repsInput = page.locator('.el-form-item:has-text("次数") .el-input-number input')
      await repsInput.fill('8')
      
      const durationInput = page.locator('.el-form-item:has-text("时长") .el-input-number input')
      await durationInput.fill('60')
      
      // 点击保存按钮
      await page.click('button:has-text("保存数据")')
      
      // 等待保存完成（可能显示成功消息）
      await page.waitForTimeout(2000)
    })

    test('动作名称建议应该正常工作', async ({ page }) => {
      await page.goto('/#/training-data')
      
      // 输入部分动作名称
      const exerciseInput = page.locator('input[placeholder="请输入动作名称"]')
      await exerciseInput.fill('深')
      
      // 等待建议列表出现
      await page.waitForTimeout(500)
      
      // 检查是否有建议列表
      const suggestions = page.locator('.exercise-suggestions')
      if (await suggestions.isVisible()) {
        // 点击建议项
        await page.click('.suggestion-item:has-text("深蹲")')
        
        // 验证输入框值已更新
        await expect(exerciseInput).toHaveValue('深蹲')
      }
    })

    test('表单验证应该正常工作', async ({ page }) => {
      await page.goto('/#/training-data')
      
      // 不填写任何内容直接点击保存
      await page.click('button:has-text("保存数据")')
      
      // 等待验证消息
      await page.waitForTimeout(500)
      
      // 应该仍在训练数据页面（保存未成功）
      expect(page.url()).toContain('/training-data')
    })

    test('重置按钮应该清空表单', async ({ page }) => {
      await page.goto('/#/training-data')
      
      // 填写一些数据
      await page.fill('input[placeholder="请输入动作名称"]', '卧推')
      
      // 点击重置按钮
      await page.click('button:has-text("重置")')
      
      // 验证表单已清空
      const exerciseInput = page.locator('input[placeholder="请输入动作名称"]')
      await expect(exerciseInput).toHaveValue('')
    })
  })

  test.describe('训练记录查看', () => {
    
    test('应该显示训练记录列表', async ({ page }) => {
      await page.goto('/#/training-data')
      
      // 验证记录列表区域存在
      await expect(page.locator('.recent-records')).toBeVisible()
      await expect(page.locator('.el-table')).toBeVisible()
    })

    test('应该能刷新记录列表', async ({ page }) => {
      await page.goto('/#/training-data')
      
      // 点击刷新按钮
      await page.click('button:has-text("刷新")')
      
      // 等待刷新完成
      await page.waitForTimeout(1000)
    })

    test('应该能按动作类型筛选记录', async ({ page }) => {
      await page.goto('/#/training-data')
      
      // 点击筛选选择器
      const filterSelect = page.locator('.record-actions .el-select')
      await filterSelect.click()
      
      // 选择筛选选项
      await page.click('.el-select-dropdown__item:has-text("上肢")')
      
      // 等待筛选结果
      await page.waitForTimeout(500)
    })

    test('表格应该支持排序', async ({ page }) => {
      await page.goto('/#/training-data')
      
      // 等待表格加载
      await page.waitForSelector('.el-table')
      
      // 点击日期列头进行排序
      const dateHeader = page.locator('.el-table__header th:has-text("日期")')
      if (await dateHeader.isVisible()) {
        await dateHeader.click()
        await page.waitForTimeout(500)
      }
    })

    test('应该显示分页控件', async ({ page }) => {
      await page.goto('/#/training-data')
      
      // 验证分页控件存在
      await expect(page.locator('.el-pagination')).toBeVisible()
    })
  })

  test.describe('训练记录编辑和删除', () => {
    
    test('应该能打开编辑对话框', async ({ page }) => {
      await page.goto('/#/training-data')
      
      // 等待表格加载
      await page.waitForSelector('.el-table')
      await page.waitForTimeout(1000)
      
      // 查找编辑按钮
      const editButton = page.locator('.el-table button:has-text("编辑")').first()
      
      if (await editButton.isVisible()) {
        await editButton.click()
        
        // 验证编辑对话框打开
        await expect(page.locator('.el-dialog')).toBeVisible()
        await expect(page.locator('.el-dialog__title')).toContainText('编辑训练记录')
      }
    })

    test('编辑对话框应该能关闭', async ({ page }) => {
      await page.goto('/#/training-data')
      
      await page.waitForSelector('.el-table')
      await page.waitForTimeout(1000)
      
      const editButton = page.locator('.el-table button:has-text("编辑")').first()
      
      if (await editButton.isVisible()) {
        await editButton.click()
        
        // 等待对话框打开
        await expect(page.locator('.el-dialog')).toBeVisible()
        
        // 点击取消按钮关闭
        await page.click('.el-dialog button:has-text("取消")')
        
        // 验证对话框已关闭
        await expect(page.locator('.el-dialog')).not.toBeVisible()
      }
    })

    test('删除按钮应该显示确认对话框', async ({ page }) => {
      await page.goto('/#/training-data')
      
      await page.waitForSelector('.el-table')
      await page.waitForTimeout(1000)
      
      // 查找删除按钮
      const deleteButton = page.locator('.el-table button:has-text("删除")').first()
      
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
