import { useActionState, useId } from 'react'
import { Link, useNavigate } from 'react-router'
import { Alert, Input } from 'antd'
import { SmileOutlined, LockOutlined, UserOutlined } from '@ant-design/icons'
import GlassCard from '@/components/GlassCard'
import AppLogo from '@/components/AppLogo'
import GradientButton from '@/components/GradientButton'
import { authApi } from '@/api/auth'
import { useAuthStore } from '@/store/auth'
import { useDocumentTitle } from '@/hooks/useDocumentTitle'
import '@/styles/auth.scss'

export default function Register() {
  useDocumentTitle('注册 · AI 心理健康助手')
  const navigate = useNavigate()
  const id = useId()

  const [error, submitAction, pending] = useActionState<string | null, FormData>(
    async (_prev, formData) => {
      const username = String(formData.get('username') ?? '').trim()
      const nickname = String(formData.get('nickname') ?? '').trim()
      const password = String(formData.get('password') ?? '')
      const confirm = String(formData.get('confirm') ?? '')
      if (!username || !nickname || !password) return '请填写完整信息'
      if (password.length < 6) return '密码至少 6 位'
      if (password !== confirm) return '两次输入的密码不一致'
      try {
        const res = await authApi.register({ username, password, nickname })
        useAuthStore.getState().setAuth(res.token, res.user)
        navigate('/', { replace: true })
        return null
      } catch (e) {
        return e instanceof Error && e.message ? e.message : '注册失败，请稍后重试'
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
        <p className="auth-subtitle">创建账号，开始你的心灵疗愈之旅 ✨</p>
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
          <label className="field-label" htmlFor={`${id}-nickname`}>
            昵称
          </label>
          <Input
            id={`${id}-nickname`}
            name="nickname"
            size="large"
            prefix={<SmileOutlined />}
            placeholder="给自己取一个温暖的昵称"
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
            placeholder="至少 6 位密码"
            autoComplete="new-password"
          />
          <label className="field-label" htmlFor={`${id}-confirm`}>
            确认密码
          </label>
          <Input
            id={`${id}-confirm`}
            name="confirm"
            type="password"
            size="large"
            prefix={<LockOutlined />}
            placeholder="再次输入密码"
            autoComplete="new-password"
          />
          <GradientButton htmlType="submit" size="large" block loading={pending} className="auth-submit">
            {pending ? '注册中…' : '注 册'}
          </GradientButton>
        </form>
        <div className="auth-footer">
          已有账号？<Link to="/login">直接登录</Link>
        </div>
      </GlassCard>
    </div>
  )
}
