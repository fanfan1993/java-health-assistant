import type { ReactNode } from 'react'
import { Navigate, useLocation } from 'react-router'
import { useAuthStore } from '@/store/auth'

interface ProtectedRouteProps {
  children: ReactNode
  /** 是否要求 ADMIN 角色 */
  admin?: boolean
}

/** 路由守卫：未登录跳 /login，ADMIN 路由校验角色 */
export default function ProtectedRoute({ children, admin = false }: ProtectedRouteProps) {
  const token = useAuthStore((s) => s.token)
  const role = useAuthStore((s) => s.user?.role)
  const location = useLocation()

  if (!token) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />
  }
  if (admin && role !== 'ADMIN') {
    return <Navigate to="/" replace />
  }
  return children
}
