import { defineConfig, loadEnv } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'
import path from 'path'

export default defineConfig(({ mode }) => {
  // uni-app 编译 mp-weixin 时，dev 模式下 import.meta.env 的静态替换不可靠
  //（小程序运行时不原生支持 import.meta），会用 loadEnv + define 显式注入，
  // 保证 dev / build 都能拿到值。
  const env = loadEnv(mode, process.cwd(), '')
  return {
    plugins: [uni()],
    resolve: {
      alias: {
        '@': path.resolve(__dirname, 'src'),
      },
    },
    define: {
      'import.meta.env.VITE_API_BASE_URL': JSON.stringify(env.VITE_API_BASE_URL),
    },
  }
})
