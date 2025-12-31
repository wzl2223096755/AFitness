/**
 * E2E测试辅助工具
 * 提供稳定的等待和重试机制
 */

/**
 * 等待元素可见并稳定
 * @param {import('@playwright/test').Page} page 
 * @param {string} selector 
 * @param {number} timeout 
 */
export async function waitForElementStable(page, selector, timeout = 10000) {
  await page.waitForSelector(selector, { state: 'visible', timeout })
  // 等待元素稳定（不再移动）
  await page.waitForFunction(
    (sel) => {
      const el = document.querySelector(sel)
      if (!el) return false
      const rect1 = el.getBoundingClientRect()
      return new Promise(resolve => {
        setTimeout(() => {
          const rect2 = el.getBoundingClientRect()
          resolve(
            rect1.top === rect2.top &&
            rect1.left === rect2.left &&
            rect1.width === rect2.width &&
            rect1.height === rect2.height
          )
        }, 100)
      })
    },
    selector,
    { timeout }
  )
}

/**
 * 安全点击元素（带重试）
 * @param {import('@playwright/test').Page} page 
 * @param {string} selector 
 * @param {number} retries 
 */
export async function safeClick(page, selector, retries = 3) {
  for (let i = 0; i < retries; i++) {
    try {
      await page.waitForSelector(selector, { state: 'visible', timeout: 5000 })
      await page.click(selector)
      return
    } catch (error) {
      if (i === retries - 1) throw error
      await page.waitForTimeout(500)
    }
  }
}

/**
 * 安全填写输入框
 * @param {import('@playwright/test').Page} page 
 * @param {string} selector 
 * @param {string} value 
 */
export async function safeFill(page, selector, value) {
  await page.waitForSelector(selector, { state: 'visible', timeout: 5000 })
  await page.fill(selector, '')
  await page.fill(selector, value)
}

/**
 * 等待网络空闲
 * @param {import('@playwright/test').Page} page 
 * @param {number} timeout 
 */
export async function waitForNetworkIdle(page, timeout = 5000) {
  try {
    await page.waitForLoadState('networkidle', { timeout })
  } catch {
    // 忽略超时，继续执行
  }
}

/**
 * 登录辅助函数
 * @param {import('@playwright/test').Page} page 
 * @param {string} username 
 * @param {string} password 
 */
export async function login(page, username = 'admin', password = 'admin123') {
  await page.goto('/#/login')
  await waitForNetworkIdle(page)
  
  await safeFill(page, 'input[placeholder="用户名"]', username)
  await safeFill(page, 'input[placeholder="密码"]', password)
  await safeClick(page, 'button[type="submit"]')
  
  // 等待登录完成
  await page.waitForURL('**/#/dashboard**', { timeout: 20000 })
}

/**
 * 清除登录状态
 * @param {import('@playwright/test').Page} page 
 */
export async function clearAuth(page) {
  await page.evaluate(() => {
    localStorage.clear()
    sessionStorage.clear()
  })
}
