import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

// Plugin to fix Element Plus module resolution issues
function globalThisResolverPlugin() {
  return {
    name: 'globalThis-resolver',
    resolveId(source, importer) {
      if (importer && importer.includes('element-plus')) {
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
  plugins: [
    globalThisResolverPlugin(),
    vue(),
    AutoImport({
      resolvers: [ElementPlusResolver()],
      dts: 'src/auto-imports.d.ts',
    }),
    Components({
      resolvers: [ElementPlusResolver()],
      dts: 'src/components.d.ts',
    }),
  ],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
      '@shared': resolve(__dirname, '../shared'),
    },
    // Ensure dependencies from shared folder are resolved from admin's node_modules
    dedupe: ['vue', 'vue-router', 'pinia', 'axios', 'element-plus'],
  },
  define: {
    global: 'globalThis',
  },
  server: {
    host: '0.0.0.0',
    port: 3002,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        timeout: 60000, // 代理超时60秒
        proxyTimeout: 60000
      },
    },
  },
  build: {
    outDir: 'dist',
    assetsDir: 'assets',
    sourcemap: false,
    cssCodeSplit: true,
    assetsInlineLimit: 4096,
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (id.includes('node_modules/vue/') || 
              id.includes('node_modules/@vue/') ||
              id.includes('node_modules/vue-router/') || 
              id.includes('node_modules/pinia/')) {
            return 'vue-vendor'
          }
          if (id.includes('node_modules/element-plus') || 
              id.includes('node_modules/@element-plus')) {
            return 'element-plus'
          }
          if (id.includes('node_modules/echarts') || 
              id.includes('node_modules/vue-echarts') ||
              id.includes('node_modules/zrender')) {
            return 'echarts'
          }
          if (id.includes('node_modules/axios') || 
              id.includes('node_modules/dayjs') ||
              id.includes('node_modules/lodash-es')) {
            return 'utils'
          }
        },
        chunkFileNames: 'assets/js/[name]-[hash].js',
        entryFileNames: 'assets/js/[name]-[hash].js',
      }
    },
    chunkSizeWarningLimit: 1000,
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
  css: {
    preprocessorOptions: {
      scss: {
        additionalData: `@use "@shared/styles/variables.scss" as *;`
      }
    }
  }
})
