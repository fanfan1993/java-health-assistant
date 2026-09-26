import { useActionState, useId } from 'react'
import { Link, useNavigate } from 'react-router'
import { Alert, Input } from 'antd'
import { LockOutlined, UserOutlined } from '@ant-design/icons'
import GlassCard from '@/components/GlassCard'
import AppLogo from '@/components/AppLogo'
import GradientButton from '@/components/GradientButton'
import { authApi } from '@/api/auth'
import { useAuthStore } from '@/store/auth'
import { useDocumentTitle } from '@/hooks/useDocumentTitle'
import '@/styles/auth.scss'

export default function Login() {
  useDocumentTitle('登录 · AI 心理健康助手')
  const navigate = useNavigate()
  const id = useId()

  const [error, submitAction, pending] = useActionState<string | null, FormData>(
    async (_prev, formData) => {
      const username = String(formData.get('username') ?? '').trim()
      const password = String(formData.get('password') ?? '')
      if (!username || !password) return '请输入用户名和密码'
      try {
        const res = await authApi.login({ username, password })
        useAuthStore.getState().setAuth(res.token, res.user)
        navigate('/', { replace: true })
        return null
      } catch (e) {
        return e instanceof Error && e.message ? e.message : '登录失败，请稍后重试'
      }
    },
    null,
  )

  return (
    <div className="auth-page">
      <div className="blob blob-1" />
      <div className="blob blob-2" />
      <div className="blob blob-3" />
      <div className="blob blob-4" />
      <GlassCard className="auth-card anim-fadeUp">
        <AppLogo />
        <p className="auth-subtitle">欢迎回来，今天也要好好照顾自己的情绪 🌙</p>
        {error && (
          <Alert type="error" message={error} showIcon className="auth-alert" />
        )}
        <form action={submitAction} className="auth-form">
          <label className="field-label" htmlFor={`${id}-username`}>
            用户名
          </label>
          <Input
            id={`${id}-username`}
            name="username"
            size="large"
            prefix={<UserOutlined />}
            placeholder="请输入用户名"
            autoComplete="username"
          />
          <label className="field-label" htmlFor={`${id}-password`}>
            密码
          </label>
          <Input
            id={`${id}-password`}
            name="password"
            type="password"
            size="large"
            prefix={<LockOutlined />}
            placeholder="请输入密码"
            autoComplete="current-password"
          />
          <GradientButton htmlType="submit" size="large" block loading={pending} className="auth-submit">
            {pending ? '登录中…' : '登 录'}
          </GradientButton>
        </form>
        <div className="auth-footer">
          还没有账号？<Link to="/register">立即注册</Link>
        </div>
      </GlassCard>
    </div>
  )
}
