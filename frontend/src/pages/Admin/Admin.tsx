import { Suspense, useEffect, useState, use } from 'react'
import { Avatar, Card, Skeleton, Table, type TableProps } from 'antd'
import {
  BookOutlined,
  CommentOutlined,
  FileDoneOutlined,
  TeamOutlined,
} from '@ant-design/icons'
import PageTransition from '@/components/PageTransition'
import ErrorBoundary from '@/components/ErrorBoundary'
import GlassCard from '@/components/GlassCard'
import AnimatedNumber from '@/components/AnimatedNumber'
import { userApi } from '@/api/user'
import { useDocumentTitle } from '@/hooks/useDocumentTitle'
import type { AdminOverview, UserInfo } from '@/types'
import './Admin.scss'

/** 统计卡片：use(Promise) 解包 */
function AdminStats({ promise }: { promise: Promise<AdminOverview> }) {
  const data = use(promise)
  const cards = [
    { icon: <TeamOutlined />, label: '用户总数', value: data.userCount, color: 'linear-gradient(135deg,#6C5CE7,#A29BFE)' },
    { icon: <CommentOutlined />, label: '对话总数', value: data.chatCount, color: 'linear-gradient(135deg,#00CEC9,#55EFC4)' },
    { icon: <BookOutlined />, label: '日记总数', value: data.journalCount, color: 'linear-gradient(135deg,#FDCB6E,#FF7675)' },
    { icon: <FileDoneOutlined />, label: '测评总数', value: data.assessmentCount, color: 'linear-gradient(135deg,#FD79A8,#A29BFE)' },
  ]
  return (
    <div className="admin-stats">
      {cards.map((c) => (
        <GlassCard key={c.label} className="stat-card">
          <div className="stat-icon" style={{ background: c.color }}>
            {c.icon}
          </div>
          <div>
            <div className="stat-value">
              <AnimatedNumber value={c.value} />
            </div>
            <div className="stat-label">{c.label}</div>
          </div>
        </GlassCard>
      ))}
    </div>
  )
}

/** 用户表格：use(Promise) 解包 */
function UserTable({ promise }: { promise: Promise<UserInfo[]> }) {
  const users = use(promise)
  const columns: TableProps<UserInfo>['columns'] = [
    {
      title: '用户',
      dataIndex: 'nickname',
      render: (_, record) => (
        <div className="user-cell">
          <Avatar style={{ background: 'linear-gradient(135deg,#6C5CE7,#00CEC9)' }}>
            {record.nickname?.[0] ?? record.username[0]}
          </Avatar>
          <div>
            <div className="user-nickname">{record.nickname}</div>
            <div className="user-username">@{record.username}</div>
          </div>
        </div>
      ),
    },
    { title: '用户 ID', dataIndex: 'id' },
    {
      title: '角色',
      dataIndex: 'role',
      render: (role: string) => (
        <span className={`role-tag ${role === 'ADMIN' ? 'admin' : ''}`}>
          {role === 'ADMIN' ? '管理员' : '用户'}
        </span>
      ),
    },
  ]
  return (
    <GlassCard className="admin-table">
      <Table rowKey="id" columns={columns} dataSource={users} pagination={{ pageSize: 8 }} />
    </GlassCard>
  )
}

export default function Admin() {
  useDocumentTitle('管理后台 · AI 心理健康助手')
  const [usersPromise] = useState(() => userApi.getAdminUsers())
  const [overviewPromise] = useState(() => userApi.getAdminOverview())

  return (
    <PageTransition>
      <ErrorBoundary>
        <Suspense fallback={<Card loading style={{ minHeight: 140 }} />}>
          <AdminStats promise={overviewPromise} />
        </Suspense>
        <ErrorBoundary>
          <Suspense fallback={<Card loading style={{ minHeight: 360 }} />}>
            <UserTable promise={usersPromise} />
          </Suspense>
        </ErrorBoundary>
      </ErrorBoundary>
    </PageTransition>
  )
}
