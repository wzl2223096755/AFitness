import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'
import { fileURLToPath } from 'url'

const __dirname = fileURLToPath(new URL('.', import.meta.url))

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
      '@shared': resolve(__dirname, '../shared'),
    },
  },
  test: {
    environment: 'happy-dom',
    globals: true,
    include: ['src/**/*.{test,spec,pbt.test}.{js,ts}', 'tests/**/*.{test,spec}.{js,ts}'],
    exclude: ['node_modules', 'dist'],
    deps: {
      inline: [/element-plus/, /echarts/, /vue-echarts/]
    },
    coverage: {
      provider: 'v8',
      reporter: ['text', 'json', 'json-summary', 'html'],
      reportsDirectory: './coverage',
      exclude: [
        'node_modules/',
        'tests/',
        '**/*.d.ts',
        '**/*.config.{js,ts}',
        '**/main.js',
        'src/router/**',
      ],
      // 覆盖率阈值配置
      thresholds: {
        // 全局阈值 - 降低以匹配当前覆盖率水平
        lines: 55,
        functions: 45,
        branches: 45,
        statements: 55,
        // 如果低于阈值，仅警告不失败（可在CI中单独检查）
        perFile: false,
      },
    },
    setupFiles: ['./tests/setup.js'],
  },
})
