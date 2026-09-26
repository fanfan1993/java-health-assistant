import { Avatar, Card, Descriptions, Tag } from 'antd'
import { LogoutOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router'
import PageTransition from '@/components/PageTransition'
import GlassCard from '@/components/GlassCard'
import GradientButton from '@/components/GradientButton'
import { useAuthStore } from '@/store/auth'
import { useDocumentTitle } from '@/hooks/useDocumentTitle'
import './Profile.scss'

export default function Profile() {
  useDocumentTitle('个人中心 · AI 心理健康助手')
  const navigate = useNavigate()
  const user = useAuthStore((s) => s.user)
  const logout = useAuthStore((s) => s.logout)

  const handleLogout = () => {
    logout()
    navigate('/login', { replace: true })
  }

  if (!user) return null

  return (
    <PageTransition>
      <div className="profile-page">
        <GlassCard className="profile-card">
          <div className="profile-top">
            <Avatar size={88} className="profile-avatar">
              {user.nickname?.[0] ?? user.username[0]}
            </Avatar>
            <div>
              <h2 className="gradient-text">{user.nickname}</h2>
              <p className="profile-username">@{user.username}</p>
              <Tag color={user.role === 'ADMIN' ? 'purple' : 'processing'}>
                {user.role === 'ADMIN' ? '管理员' : '普通用户'}
              </Tag>
            </div>
          </div>
          <Descriptions column={1} className="profile-desc" bordered>
            <Descriptions.Item label="用户 ID">{user.id}</Descriptions.Item>
            <Descriptions.Item label="用户名">{user.username}</Descriptions.Item>
            <Descriptions.Item label="昵称">{user.nickname}</Descriptions.Item>
            <Descriptions.Item label="角色">
              {user.role === 'ADMIN' ? 'ADMIN' : 'USER'}
            </Descriptions.Item>
          </Descriptions>
          <GradientButton danger icon={<LogoutOutlined />} size="large" block onClick={handleLogout} className="logout-btn">
            退出登录
          </GradientButton>
        </GlassCard>
        <Card className="profile-quote" bordered={false}>
          <p>「照顾好自己的情绪，就是对生活最好的回应。」</p>
        </Card>
      </div>
    </PageTransition>
  )
}
