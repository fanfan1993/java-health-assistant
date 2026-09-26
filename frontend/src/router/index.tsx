import { lazy, Suspense, type ReactNode } from 'react'
import { createBrowserRouter, Navigate } from 'react-router'
import ProtectedRoute from '@/components/ProtectedRoute'
import LoadingScreen from '@/components/LoadingScreen'
import MainLayout from '@/pages/Layout'

// ---------- 懒加载页面 ----------
const Login = lazy(() => import('@/pages/Login'))
const Register = lazy(() => import('@/pages/Register'))
const Dashboard = lazy(() => import('@/pages/Dashboard'))
const Chat = lazy(() => import('@/pages/Chat'))
const Assessment = lazy(() => import('@/pages/Assessment'))
const Journal = lazy(() => import('@/pages/Journal'))
const Profile = lazy(() => import('@/pages/Profile'))
const Admin = lazy(() => import('@/pages/Admin'))

const withSuspense = (node: ReactNode) => (
  <Suspense fallback={<LoadingScreen />}>{node}</Suspense>
)

/** 集中式路由配置 */
export const router = createBrowserRouter([
  { path: '/login', element: withSuspense(<Login />) },
  { path: '/register', element: withSuspense(<Register />) },
  {
    path: '/',
    element: <ProtectedRoute><MainLayout /></ProtectedRoute>,
    children: [
      { index: true, element: withSuspense(<Dashboard />) },
      { path: 'chat', element: withSuspense(<Chat />) },
      { path: 'assessment', element: withSuspense(<Assessment />) },
      { path: 'journal', element: withSuspense(<Journal />) },
      { path: 'profile', element: withSuspense(<Profile />) },
      {
        path: 'admin',
        element: <ProtectedRoute admin>{withSuspense(<Admin />)}</ProtectedRoute>,
      },
      { path: '*', element: <Navigate to="/" replace /> },
    ],
  },
])
