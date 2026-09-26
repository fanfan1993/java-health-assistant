import { Suspense, useEffect, useState, use, useTransition } from 'react'
import { Button, Card, Empty, Progress, Tag } from 'antd'
import { ArrowLeftOutlined, CheckOutlined, ClockCircleOutlined } from '@ant-design/icons'
import PageTransition from '@/components/PageTransition'
import ErrorBoundary from '@/components/ErrorBoundary'
import GlassCard from '@/components/GlassCard'
import GradientButton from '@/components/GradientButton'
import AnimatedNumber from '@/components/AnimatedNumber'
import { assessmentApi } from '@/api/assessment'
import { useCountUp } from '@/hooks/useCountUp'
import { useDocumentTitle } from '@/hooks/useDocumentTitle'
import type { AssessmentResult, Scale } from '@/types'
import './Assessment.scss'

type View =
  | { mode: 'list' }
  | { mode: 'quiz'; scale: Scale }
  | { mode: 'result'; scaleName: string; result: AssessmentResult }

/** 量表列表：use(Promise) 解包量表数据 */
function ScaleList({ promise, onPick }: { promise: Promise<Scale[]>; onPick: (scale: Scale) => void }) {
  const scales = use(promise)
  if (scales.length === 0) return <Empty description="暂无可用量表" />
  return (
    <div className="scale-grid">
      {scales.map((scale, i) => (
        <GlassCard
          key={scale.id}
          className="scale-card"
          style={{ animationDelay: `${i * 0.08}s` }}
          onClick={() => onPick(scale)}
        >
          <h3 className="scale-name">{scale.name}</h3>
          <p className="scale-desc">{scale.desc}</p>
          <div className="scale-footer">
            <Tag icon={<ClockCircleOutlined />} color="processing">
              约 {scale.minutes} 分钟
            </Tag>
            <Tag color="default">{scale.questions.length} 题</Tag>
            <span className="scale-start">开始测评 →</span>
          </div>
        </GlassCard>
      ))}
    </div>
  )
}

/** 答题视图 */
function Quiz({ scale, onFinish, onBack }: { scale: Scale; onFinish: (result: AssessmentResult) => void; onBack: () => void }) {
  const [answers, setAnswers] = useState<Record<string, number>>({})
  const [submitting, startSubmit] = useTransition()

  const answeredCount = Object.keys(answers).length
  const percent = Math.round((answeredCount / scale.questions.length) * 100)
  const allAnswered = answeredCount === scale.questions.length

  const handleSubmit = () => {
    startSubmit(async () => {
      // 后端按「选项下标」计分（含反向题处理），传入每题选中选项的下标
      const result = await assessmentApi.submit(
        scale.id,
        scale.questions.map((q) => answers[String(q.id)] ?? 0),
      )
      onFinish(result)
    })
  }

  return (
    <div className="quiz">
      <div className="quiz-header">
        <Button icon={<ArrowLeftOutlined />} onClick={onBack}>
          返回列表
        </Button>
        <h2 className="gradient-text">{scale.name}</h2>
      </div>
      <Progress percent={percent} strokeColor={{ '0%': '#6C5CE7', '100%': '#00CEC9' }} className="quiz-progress" />
      <div className="quiz-questions">
        {scale.questions.map((q, qi) => (
          <GlassCard key={String(q.id)} className="question-card">
            <p className="question-title">
              {qi + 1}. {q.title}
            </p>
            <div className="option-list">
              {q.options.map((opt, oi) => (
                <div
                  key={oi}
                  className={`option-item ${answers[String(q.id)] === oi ? 'selected' : ''}`}
                  onClick={() => setAnswers((prev) => ({ ...prev, [String(q.id)]: oi }))}
                >
                  <span className="option-radio" />
                  {opt.label}
                </div>
              ))}
            </div>
          </GlassCard>
        ))}
      </div>
      <div className="quiz-footer">
        <GradientButton size="large" disabled={!allAnswered} loading={submitting} icon={<CheckOutlined />} onClick={handleSubmit}>
          {submitting ? '提交中…' : allAnswered ? '提交测评' : `还剩 ${scale.questions.length - answeredCount} 题`}
        </GradientButton>
      </div>
    </div>
  )
}

/** 结果视图：环形分数动画 + 等级徽章 + 建议 */
function ResultView({ scaleName, result, onBack }: { scaleName: string; result: AssessmentResult; onBack: () => void }) {
  const score = useCountUp(result.totalScore, 1500)
  const percent = Math.min(result.totalScore / 100, 1)
  const circumference = 2 * Math.PI * 80

  const levelColor =
    result.level.includes('重') ? '#FF7675' : result.level.includes('中') ? '#FDCB6E' : result.level.includes('轻') ? '#00CEC9' : '#00B894'

  return (
    <div className="result-view anim-fadeUp">
      <GlassCard className="result-card">
        <h2 className="gradient-text">{scaleName} · 测评结果</h2>
        <div className="score-ring">
          <svg width="200" height="200" viewBox="0 0 200 200">
            <circle cx="100" cy="100" r="80" fill="none" stroke="#E8E4FF" strokeWidth="12" />
            <circle
              cx="100"
              cy="100"
              r="80"
              fill="none"
              stroke="url(#ringGradient)"
              strokeWidth="12"
              strokeLinecap="round"
              strokeDasharray={circumference}
              strokeDashoffset={circumference * (1 - percent)}
              transform="rotate(-90 100 100)"
              style={{ transition: 'stroke-dashoffset 1.5s ease' }}
            />
            <defs>
              <linearGradient id="ringGradient" x1="0%" y1="0%" x2="100%" y2="100%">
                <stop offset="0%" stopColor="#6C5CE7" />
                <stop offset="100%" stopColor="#00CEC9" />
              </linearGradient>
            </defs>
          </svg>
          <div className="score-center">
            <div className="score-num">
              <AnimatedNumber value={result.totalScore} />
            </div>
            <div className="score-unit">总分</div>
          </div>
        </div>
        <Tag className="level-badge" color={levelColor}>
          {result.level}
        </Tag>
        <div className="suggestion">
          <h4>专业建议</h4>
          <p>{result.suggestion}</p>
          {result.detail && (
            <p className="detail">
              {typeof result.detail === 'string'
                ? result.detail
                : `原始分 ${result.detail.rawScore ?? '-'} · 标准分 ${result.detail.totalScore ?? result.totalScore}`}
            </p>
          )}
        </div>
        <Button type="primary" ghost onClick={onBack}>
          返回量表列表
        </Button>
      </GlassCard>
    </div>
  )
}

export default function Assessment() {
  useDocumentTitle('心理测评 · AI 心理健康助手')
  const [scalesPromise] = useState(() => assessmentApi.getScales())
  const [view, setView] = useState<View>({ mode: 'list' })

  if (view.mode === 'quiz') {
    return (
      <PageTransition>
        <Quiz scale={view.scale} onBack={() => setView({ mode: 'list' })} onFinish={(result) => setView({ mode: 'result', scaleName: view.scale.name, result })} />
      </PageTransition>
    )
  }
  if (view.mode === 'result') {
    return (
      <PageTransition>
        <ResultView scaleName={view.scaleName} result={view.result} onBack={() => setView({ mode: 'list' })} />
      </PageTransition>
    )
  }
  return (
    <PageTransition>
      <ErrorBoundary>
        <Suspense fallback={<Card loading style={{ minHeight: 300 }} />}>
          <ScaleList promise={scalesPromise} onPick={(scale) => setView({ mode: 'quiz', scale })} />
        </Suspense>
      </ErrorBoundary>
    </PageTransition>
  )
}
