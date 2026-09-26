import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const __dirname = path.dirname(fileURLToPath(import.meta.url))

// 后端端口：默认 8080，可用环境变量 BACKEND_PORT 覆盖（沙箱/容器等端口映射场景）
const BACKEND_PORT = process.env.BACKEND_PORT || '8080'

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src'),
    },
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: `http://[::1]:${BACKEND_PORT}`,
        changeOrigin: true,
      },
    },
  },
  css: {
    preprocessorOptions: {
      scss: {
        // 向每个编译入口注入变量与 mixin；跳过 partial 文件自身，避免重复加载冲突
        additionalData: (source: string, filename: string) =>
          /_[a-zA-Z0-9-]+\.scss$/.test(filename)
            ? source
            : `@use "@/styles/variables" as *; @use "@/styles/mixins" as *;\n${source}`,
      },
    },
  },
})
