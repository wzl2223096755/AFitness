import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import { VantResolver } from 'unplugin-vue-components/resolvers'
import { VitePWA } from 'vite-plugin-pwa'

// Plugin to fix Element Plus and Vant module resolution issues
function globalThisResolverPlugin() {
  return {
    name: 'globalThis-resolver',
    resolveId(source, importer) {
      if (importer && (importer.includes('element-plus') || importer.includes('vant'))) {
        // Fix globalThis naming issues in both libraries
        if (source.includes('globalThis')) {
          const fixed = source.replace('globalThis', 'global')
          return { id: resolve(importer, '..', fixed), external: false }
        }
      }
      return null
    }
  }
}

export default defineConfig({
  base: '/',
  plugins: [
    globalThisResolverPlugin(),
    vue(),
    // 自动导入Element Plus和Vant的API
    AutoImport({
      resolvers: [ElementPlusResolver()],
      dts: 'src/auto-imports.d.ts',
    }),
    // 自动注册Element Plus和Vant组件
    Components({
      resolvers: [
        ElementPlusResolver(),
        VantResolver(),
      ],
      dts: 'src/components.d.ts',
    }),
    // PWA配置
    VitePWA({
      registerType: 'autoUpdate',
      includeAssets: ['favicon.ico', 'robots.txt', 'apple-touch-icon.svg'],
      manifest: {
        name: '健身管理系统',
        short_name: '健身助手',
        description: '力量训练负荷与恢复监控系统',
        theme_color: '#409EFF',
        background_color: '#ffffff',
        display: 'standalone',
        start_url: '/',
        icons: [
          {
            src: 'pwa-192x192.svg',
            sizes: '192x192',
            type: 'image/svg+xml'
          },
          {
            src: 'pwa-512x512.svg',
            sizes: '512x512',
            type: 'image/svg+xml'
          },
          {
            src: 'pwa-512x512.svg',
            sizes: '512x512',
            type: 'image/svg+xml',
            purpose: 'any maskable'
          }
        ]
      },
      workbox: {
        // 缓存静态资源
        globPatterns: ['**/*.{js,css,html,ico,png,svg,woff2}'],
        // 运行时缓存策略
        runtimeCaching: [
          {
            // API请求缓存 - NetworkFirst策略
            urlPattern: /^https?:\/\/.*\/api\/.*/i,
            handler: 'NetworkFirst',
            options: {
              cacheName: 'api-cache',
              expiration: {
                maxEntries: 100,
                maxAgeSeconds: 60 * 60 * 24 // 24小时
              },
              cacheableResponse: {
                statuses: [0, 200]
              },
              networkTimeoutSeconds: 10
            }
          },
          {
            // 图片缓存 - CacheFirst策略
            urlPattern: /\.(?:png|jpg|jpeg|svg|gif|webp)$/i,
            handler: 'CacheFirst',
            options: {
              cacheName: 'image-cache',
              expiration: {
                maxEntries: 50,
                maxAgeSeconds: 60 * 60 * 24 * 30 // 30天
              }
            }
          },
          {
            // 字体缓存 - CacheFirst策略
            urlPattern: /\.(?:woff|woff2|ttf|eot)$/i,
            handler: 'CacheFirst',
            options: {
              cacheName: 'font-cache',
              expiration: {
                maxEntries: 20,
                maxAgeSeconds: 60 * 60 * 24 * 365 // 1年
              }
            }
          }
        ]
      },
      devOptions: {
        enabled: true // 开发环境启用PWA便于测试
      }
    }),
  ],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
      '@shared': resolve(__dirname, '../shared'),
    },
    // Ensure dependencies from shared folder are resolved from frontend's node_modules
    dedupe: ['vue', 'vue-router', 'pinia', 'axios', 'element-plus'],
  },
  define: {
    global: 'globalThis',
  },
  server: {
    host: '0.0.0.0',
    port: 3001,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        timeout: 60000, // 代理超时60秒
        proxyTimeout: 60000
      },
      '/ws': {
        target: 'ws://localhost:8080',
        ws: true,
      },
    },
  },
  build: {
    outDir: 'dist',
    assetsDir: 'assets',
    sourcemap: false,
    // 启用CSS代码分割
    cssCodeSplit: true,
    // 资源内联阈值（小于4KB的资源内联为base64）
    assetsInlineLimit: 4096,
    rollupOptions: {
      output: {
        // 优化代码分割策略
        manualChunks(id) {
          // Vue核心库 - 首屏必需
          if (id.includes('node_modules/vue/') || 
              id.includes('node_modules/@vue/') ||
              id.includes('node_modules/vue-router/') || 
              id.includes('node_modules/pinia/')) {
            return 'vue-vendor'
          }
          // Element Plus - 按需加载，不在首屏
          if (id.includes('node_modules/element-plus') || 
              id.includes('node_modules/@element-plus')) {
            return 'element-plus'
          }
          // ECharts - 仅在需要图表时加载
          if (id.includes('node_modules/echarts') || 
              id.includes('node_modules/vue-echarts') ||
              id.includes('node_modules/zrender')) {
            return 'echarts'
          }
          // Vant移动端组件 - 按需加载
          if (id.includes('node_modules/vant')) {
            return 'vant'
          }
          // 工具库 - 首屏必需
          if (id.includes('node_modules/axios') || 
              id.includes('node_modules/dayjs') ||
              id.includes('node_modules/lodash-es')) {
            return 'utils'
          }
        },
        // 优化chunk文件名
        chunkFileNames: 'assets/js/[name]-[hash].js',
        entryFileNames: 'assets/js/[name]-[hash].js',
        assetFileNames: (assetInfo) => {
          // 根据文件类型分类存放
          const info = assetInfo.name.split('.')
          const ext = info[info.length - 1]
          
          // 图片资源
          if (/\.(png|jpe?g|gif|svg|webp|ico)$/i.test(assetInfo.name)) {
            return 'assets/images/[name]-[hash].[ext]'
          }
          // 字体资源
          if (/\.(woff2?|eot|ttf|otf)$/i.test(assetInfo.name)) {
            return 'assets/fonts/[name]-[hash].[ext]'
          }
          // CSS资源
          if (/\.css$/i.test(assetInfo.name)) {
            return 'assets/css/[name]-[hash].[ext]'
          }
          // 其他资源
          return 'assets/[ext]/[name]-[hash].[ext]'
        }
      }
    },
    chunkSizeWarningLimit: 1000,
    // 压缩选项
    minify: 'terser',
    terserOptions: {
      compress: {
        drop_console: true,
        drop_debugger: true
      }
    }
  },
  optimizeDeps: {
    include: [
      'vue', 
      'vue-router', 
      'pinia', 
      'axios', 
      'dayjs', 
      'echarts',
      'vue-echarts',
      'lodash-es'
    ]
  },
  // CSS预处理器选项
  css: {
    preprocessorOptions: {
      scss: {
        additionalData: `@use "@/assets/styles/variables.scss" as *;`
      }
    }
  }
})
