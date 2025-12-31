import { defineConfig, devices } from '@playwright/test'

/**
 * Playwright E2E测试配置
 * @see https://playwright.dev/docs/test-configuration
 */
export default defineConfig({
  // 测试目录
  testDir: './e2e',
  
  // 并行运行测试
  fullyParallel: true,
  
  // CI环境禁止.only
  forbidOnly: !!process.env.CI,
  
  // CI环境重试次数
  retries: process.env.CI ? 2 : 0,
  
  // 并行worker数量
  workers: process.env.CI ? 1 : undefined,
  
  // 测试报告配置
  reporter: [
    ['html', { outputFolder: 'playwright-report' }],
    ['json', { outputFile: 'test-results/results.json' }],
    ['list']
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
    
    // 操作超时
    actionTimeout: 10000,
    
    // 导航超时
    navigationTimeout: 30000,
  },
  
  // 测试项目配置
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
    {
      name: 'Mobile Chrome',
      use: { ...devices['Pixel 5'] },
    },
  ],
  
  // 测试输出目录
  outputDir: 'test-results',
  
  // 开发服务器配置（可选，用于自动启动前端服务）
  webServer: {
    command: 'npm run dev',
    url: 'http://localhost:3001',
    reuseExistingServer: true,
    timeout: 120000,
  },
})
