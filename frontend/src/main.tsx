import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { RouterProvider } from 'react-router'
import { ConfigProvider, theme as antdTheme, App as AntdApp } from 'antd'
import zhCN from 'antd/locale/zh_CN'
import { router } from './router'
import { ThemeContext } from './context/ThemeContext'
import { useThemeStore } from './store/theme'
import './styles/global.scss'

/** 根组件：antd 主题 + 主题 Context + 路由 */
function ThemedApp() {
  const dark = useThemeStore((s) => s.dark)
  const toggle = useThemeStore((s) => s.toggle)

  return (
    <ThemeContext value={{ dark, toggle }}>
      <ConfigProvider
        locale={zhCN}
        theme={{
          algorithm: dark ? antdTheme.darkAlgorithm : antdTheme.defaultAlgorithm,
          token: {
            colorPrimary: '#6C5CE7',
            borderRadius: 10,
          },
        }}
      >
        <AntdApp>
          <RouterProvider router={router} />
        </AntdApp>
      </ConfigProvider>
    </ThemeContext>
  )
}

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <ThemedApp />
  </StrictMode>,
)
