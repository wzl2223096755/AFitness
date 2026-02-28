import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import { VantResolver } from 'unplugin-vue-components/resolvers'
import { VitePWA } from 'vite-plugin-pwa'

// GitHub Pages配置
const repoName = process.env.GITHUB_REPOSITORY?.split('/')?.[1] || 'AFitness'
const isGitHubPages = process.env.GITHUB_PAGES === 'true'
const base = isGitHubPages ? `/${repoName}/` : '/'

console.log(' Build Configuration:')
console.log(`- Repository: ${process.env.GITHUB_REPOSITORY}`)
console.log(`- GitHub Pages: ${isGitHubPages}`)
console.log(`- Base path: ${base}`)
console.log(`- Repo name: ${repoName}`)

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
  base,
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
    // PWA配置 - 在GitHub Pages上禁用Service Worker
    ...(isGitHubPages ? [] : [
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
          start_url: base,
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
          enabled: !isGitHubPages // 开发环境启用，GitHub Pages禁用
        }
      })
    ]),
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
    // GitHub Pages环境变量
    __GITHUB_PAGES__: isGitHubPages,
    __BASE_PATH__: JSON.stringify(base)
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
    sourcemap: false, // GitHub Pages不需要sourcemap
    minify: 'terser',
    target: 'es2015',
    // 启用CSS代码分割
    cssCodeSplit: true,
    // 资源内联阈值（小于4KB的资源内联为base64）
    assetsInlineLimit: 4096,
    // 构建优化
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
          // 工具库
          if (id.includes('node_modules/axios') ||
              id.includes('node_modules/dayjs') ||
              id.includes('node_modules/lodash')) {
            return 'utils'
          }
        },
        // 文件命名优化
        chunkFileNames: (chunkInfo) => {
          const facadeModuleId = chunkInfo.facadeModuleId ? chunkInfo.facadeModuleId.split('/').pop() : 'chunk'
          return `js/[name]-[hash].js`
        },
        entryFileNames: 'js/[name]-[hash].js',
        assetFileNames: (assetInfo) => {
          const info = assetInfo.name.split('.')
          const ext = info[info.length - 1]
          if (/\.(mp4|webm|ogg|mp3|wav|flac|aac)(\?.*)?$/i.test(assetInfo.name)) {
            return `media/[name]-[hash].[ext]`
          }
          if (/\.(png|jpe?g|gif|svg|webp|avif)(\?.*)?$/i.test(assetInfo.name)) {
            return `images/[name]-[hash].[ext]`
          }
          if (/\.(woff2?|eot|ttf|otf)(\?.*)?$/i.test(assetInfo.name)) {
            return `fonts/[name]-[hash].[ext]`
          }
          return `${ext}/[name]-[hash].[ext]`
        }
      }
    },
    // 压缩配置
    terserOptions: {
      compress: {
        drop_console: true, // 移除console.log
        drop_debugger: true, // 移除debugger
        pure_funcs: ['console.log', 'console.info', 'console.debug', 'console.warn']
      },
      mangle: {
        safari10: true
      }
    }
  },
  // CSS预处理器配置
  css: {
    preprocessorOptions: {
      scss: {
        additionalData: `@use "@/assets/styles/variables.scss" as *;`
      }
    }
  },
  // 依赖优化
  optimizeDeps: {
    include: [
      'vue',
      'vue-router',
      'pinia',
      'axios',
      'element-plus',
      'vant'
    ],
    exclude: ['@shared']
  }
})
