import { use, useTransition } from 'react'
import { Outlet, useLocation, useNavigate } from 'react-router'
import { Avatar, Button, Dropdown, Menu, type MenuProps } from 'antd'
import {
  BookOutlined,
  DashboardOutlined,
  FileDoneOutlined,
  LogoutOutlined,
  MoonOutlined,
  RobotOutlined,
  SunOutlined,
  TeamOutlined,
  UserOutlined,
} from '@ant-design/icons'
import AppLogo from '@/components/AppLogo'
import { ThemeContext } from '@/context/ThemeContext'
import { useAuthStore } from '@/store/auth'
import './Layout.scss'

/** 主题切换按钮：演示 use(Context) 读取 Context */
function ThemeToggle() {
  const { dark, toggle } = use(ThemeContext)
  return (
    <Button
      type="text"
      shape="circle"
      icon={dark ? <SunOutlined /> : <MoonOutlined />}
      onClick={toggle}
      title={dark ? '切换亮色' : '切换暗色'}
    />
  )
}

const PAGE_TITLES: Record<string, string> = {
  '/': '仪表盘',
  '/chat': 'AI 心灵伙伴',
  '/assessment': '心理测评',
  '/journal': '情绪日记',
  '/profile': '个人中心',
  '/admin': '管理后台',
}

/** 主布局：渐变玻璃侧边栏 + 顶部栏 */
export default function Layout() {
  const location = useLocation()
  const navigate = useNavigate()
  const user = useAuthStore((s) => s.user)
  const logout = useAuthStore((s) => s.logout)
  const [navigating, startNavigation] = useTransition()

  const items: MenuProps['items'] = [
    { key: '/', icon: <DashboardOutlined />, label: '仪表盘' },
    { key: '/chat', icon: <RobotOutlined />, label: 'AI 心灵伙伴' },
    { key: '/assessment', icon: <FileDoneOutlined />, label: '心理测评' },
    { key: '/journal', icon: <BookOutlined />, label: '情绪日记' },
    { key: '/profile', icon: <UserOutlined />, label: '个人中心' },
    ...(user?.role === 'ADMIN'
      ? [{ key: '/admin', icon: <TeamOutlined />, label: '管理后台' }]
      : []),
  ]

  const onMenuClick: MenuProps['onClick'] = ({ key }) => {
    // useTransition 非阻塞导航
    startNavigation(() => navigate(key))
  }

  const handleLogout = () => {
    logout()
    navigate('/login', { replace: true })
  }

  return (
    <div className="app-layout">
      <aside className="app-sidebar">
        <div className="sidebar-logo">
          <AppLogo size="sm" />
        </div>
        <Menu
          className="sidebar-menu"
          mode="inline"
          items={items}
          selectedKeys={[location.pathname]}
          onClick={onMenuClick}
        />
        <div className="sidebar-footer">万物皆有裂痕，那是光照进来的地方</div>
      </aside>
      <div className="app-main">
        <header className="app-header">
          <h1 className="app-header-title">{PAGE_TITLES[location.pathname] ?? ''}</h1>
          <div className="app-header-actions">
            <ThemeToggle />
            <Dropdown
              menu={{
                items: [
                  {
                    key: 'logout',
                    icon: <LogoutOutlined />,
                    label: '退出登录',
                    onClick: handleLogout,
                  },
                ],
              }}
            >
              <div className="user-chip">
                <Avatar style={{ background: 'linear-gradient(135deg,#6C5CE7,#00CEC9)' }} icon={<UserOutlined />} />
                <span className="user-chip-name">{user?.nickname ?? user?.username ?? '用户'}</span>
              </div>
            </Dropdown>
          </div>
        </header>
        <main className="app-content" data-navigating={navigating}>
          <Outlet />
        </main>
      </div>
    </div>
  )
}
