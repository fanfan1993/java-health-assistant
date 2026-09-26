import { useEffect, useState, Suspense, use } from 'react'
import { Card, Col, Empty, List, Row, Skeleton, Tag } from 'antd'
import {
  BookOutlined,
  CommentOutlined,
  FireOutlined,
  SmileOutlined,
} from '@ant-design/icons'
import * as echarts from 'echarts'
import PageTransition from '@/components/PageTransition'
import ErrorBoundary from '@/components/ErrorBoundary'
import AnimatedNumber from '@/components/AnimatedNumber'
import GlassCard from '@/components/GlassCard'
import { statsApi } from '@/api/stats'
import { useECharts } from '@/hooks/useECharts'
import { useDocumentTitle } from '@/hooks/useDocumentTitle'
import type { Overview } from '@/types'
import './Dashboard.scss'

const LEVEL_COLORS: Record<string, string> = {
  正常: 'green',
  轻度: 'blue',
  中度: 'orange',
  重度: 'red',
}

/** 统计卡片 */
function StatCard(props: { icon: React.ReactNode; label: string; value: number; precision?: number; color: string; index: number }) {
  return (
    <GlassCard className="stat-card" style={{ animationDelay: `${props.index * 0.08}s` }}>
      <div className="stat-icon" style={{ background: props.color }}>
        {props.icon}
      </div>
      <div className="stat-meta">
        <div className="stat-value">
          <AnimatedNumber value={props.value} precision={props.precision ?? 0} />
        </div>
        <div className="stat-label">{props.label}</div>
      </div>
    </GlassCard>
  )
}

/** 仪表盘内容：use(Promise) 直接解包统计数据 */
function DashboardContent({ promise }: { promise: Promise<Overview> }) {
  const data = use(promise)
  const line = useECharts()
  const rose = useECharts()

  useEffect(() => {
    line.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: 40, right: 24, top: 30, bottom: 30 },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: data.moodTrend.map((p) => p.date),
      },
      yAxis: { type: 'value', max: 5, min: 0 },
      series: [
        {
          name: '心情指数',
          type: 'line',
          smooth: true,
          symbolSize: 7,
          data: data.moodTrend.map((p) => p.score),
          lineStyle: { width: 3, color: '#6C5CE7' },
          itemStyle: { color: '#6C5CE7' },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(108,92,231,0.45)' },
              { offset: 1, color: 'rgba(0,206,201,0.05)' },
            ]),
          },
        },
      ],
    })
    rose.setOption({
      tooltip: { trigger: 'item' },
      series: [
        {
          name: '情绪分布',
          type: 'pie',
          roseType: 'area',
          radius: ['28%', '72%'],
          itemStyle: { borderRadius: 8 },
          label: { color: '#636e72' },
          data: data.emotionDistribution.map((item, i) => ({
            ...item,
            itemStyle: { color: ['#6C5CE7', '#00CEC9', '#A29BFE', '#55EFC4', '#FDCB6E', '#FF7675'][i % 6] },
          })),
        },
      ],
    })
  }, [data, line, rose])

  return (
    <div className="dashboard">
      <Row gutter={[16, 16]}>
        {[
          { icon: <CommentOutlined />, label: '对话次数', value: data.chatCount, color: 'linear-gradient(135deg,#6C5CE7,#A29BFE)' },
          { icon: <BookOutlined />, label: '日记数量', value: data.journalCount, color: 'linear-gradient(135deg,#00CEC9,#55EFC4)' },
          { icon: <SmileOutlined />, label: '平均心情', value: data.avgMood, precision: 1, color: 'linear-gradient(135deg,#FDCB6E,#FF7675)' },
          { icon: <FireOutlined />, label: '连续打卡', value: data.streakDays, color: 'linear-gradient(135deg,#FF7675,#FDCB6E)' },
        ].map((item, i) => (
          <Col xs={24} sm={12} lg={6} key={item.label}>
            <div className="stagger" style={{ animationDelay: `${i * 0.08}s` }}>
              <StatCard icon={item.icon} label={item.label} value={item.value} precision={item.precision} color={item.color} index={i} />
            </div>
          </Col>
        ))}
      </Row>

      <Row gutter={[16, 16]} className="charts-row">
        <Col xs={24} lg={14}>
          <GlassCard className="chart-card">
            <h3 className="card-title gradient-text">近期心情趋势</h3>
            <div ref={line.containerRef} className="chart-container" />
          </GlassCard>
        </Col>
        <Col xs={24} lg={10}>
          <GlassCard className="chart-card">
            <h3 className="card-title gradient-text">情绪分布</h3>
            <div ref={rose.containerRef} className="chart-container" />
          </GlassCard>
        </Col>
      </Row>

      <GlassCard className="history-card">
        <h3 className="card-title gradient-text">最近测评记录</h3>
        {data.assessmentHistory.length === 0 ? (
          <Empty description="暂无测评记录" />
        ) : (
          <List
            dataSource={data.assessmentHistory.slice(0, 6)}
            renderItem={(item) => (
              <List.Item className="history-item">
                <span>{item.scaleName}</span>
                <span className="history-score">{item.totalScore} 分</span>
                <Tag color={LEVEL_COLORS[item.level] ?? 'default'}>{item.level}</Tag>
                <span className="history-time">{item.createTime}</span>
              </List.Item>
            )}
          />
        )}
      </GlassCard>
    </div>
  )
}

export default function Dashboard() {
  useDocumentTitle('仪表盘 · AI 心理健康助手')
  // Promise 只创建一次，供 use() 解包
  const [overviewPromise] = useState(() => statsApi.getOverview())

  return (
    <PageTransition>
      <ErrorBoundary>
        <Suspense
          fallback={
            <Card loading style={{ minHeight: 400 }} />
          }
        >
          <SkeletonOrContent promise={overviewPromise} />
        </Suspense>
      </ErrorBoundary>
    </PageTransition>
  )
}

function SkeletonOrContent({ promise }: { promise: Promise<Overview> }) {
  // 占位：真正内容在 DashboardContent 里 use 解包
  return <DashboardContent promise={promise} />
}
