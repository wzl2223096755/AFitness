import { defineConfig, devices } from '@playwright/test'

/**
 * Playwright E2E测试配置
 * @see https://playwright.dev/docs/test-configuration
 * 
 * CI稳定性优化:
 * - 增加超时时间
 * - 添加重试机制
 * - 配置等待策略
 */
export default defineConfig({
  // 测试目录
  testDir: './e2e',
  
  // 并行运行测试 - CI环境禁用并行以提高稳定性
  fullyParallel: !process.env.CI,
  
  // CI环境禁止.only
  forbidOnly: !!process.env.CI,
  
  // 重试次数 - CI环境增加重试
  retries: process.env.CI ? 3 : 1,
  
  // 并行worker数量 - CI环境使用单worker
  workers: process.env.CI ? 1 : undefined,
  
  // 全局测试超时
  timeout: process.env.CI ? 60000 : 30000,
  
  // 期望超时
  expect: {
    timeout: process.env.CI ? 15000 : 10000,
  },
  
  // 测试报告配置
  reporter: [
    ['html', { outputFolder: 'playwright-report' }],
    ['json', { outputFile: 'test-results/results.json' }],
    ['list'],
    // CI环境添加JUnit报告
    ...(process.env.CI ? [['junit', { outputFile: 'test-results/junit.xml' }]] : [])
  ],
  
  // 全局配置
  use: {
    // 基础URL
    baseURL: 'http://localhost:3001',
    
    // 失败时追踪
    trace: 'on-first-retry',
    
    // 失败时截图
    screenshot: 'only-on-failure',
    
    // 失败时录制视频
    video: 'retain-on-failure',
    
    // 操作超时 - CI环境增加
    actionTimeout: process.env.CI ? 20000 : 10000,
    
    // 导航超时 - CI环境增加
    navigationTimeout: process.env.CI ? 45000 : 30000,
    
    // 等待元素可见的默认策略
    launchOptions: {
      slowMo: process.env.CI ? 100 : 0,
    },
  },
  
  // 测试项目配置
  projects: [
    {
      name: 'chromium',
      use: { 
        ...devices['Desktop Chrome'],
        // CI环境使用无头模式
        headless: true,
      },
    },
    {
      name: 'Mobile Chrome',
      use: { 
        ...devices['Pixel 5'],
        headless: true,
      },
    },
  ],
  
  // 测试输出目录
  outputDir: 'test-results',
  
  // 开发服务器配置
  webServer: {
    command: 'npm run dev',
    url: 'http://localhost:3001',
    reuseExistingServer: true,
    timeout: 180000, // 增加启动超时
    // CI环境等待服务器完全就绪
    ...(process.env.CI && { 
      stdout: 'pipe',
      stderr: 'pipe',
    }),
  },
})
