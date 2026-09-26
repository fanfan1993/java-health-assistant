/** 后端统一 ID 类型 */
export type ID = number | string

/** 后端统一响应包装 */
export interface Result<T> {
  code: number
  message: string
  data: T
}

/** 用户信息 */
export interface UserInfo {
  id: ID
  username: string
  nickname: string
  avatar?: string
  role: 'ADMIN' | 'USER' | string
}

/** 登录/注册返回 */
export interface LoginResult {
  token: string
  user: UserInfo
}

/** 聊天会话 */
export interface ChatSession {
  id: ID
  title: string
  updateTime: string
}

/** 聊天消息 */
export interface ChatMessage {
  id: ID
  role: 'user' | 'assistant'
  content: string
  createTime?: string
}

/** 量表选项 */
export interface ScaleOption {
  label: string
  score: number
}

/** 量表题目 */
export interface Question {
  id: ID
  title: string
  options: ScaleOption[]
}

/** 心理量表 */
export interface Scale {
  id: ID
  name: string
  desc: string
  minutes: number
  questions: Question[]
}

/** 测评提交结果 */
export interface AssessmentResult {
  totalScore: number
  level: string
  suggestion: string
  /** 逐题明细（后端返回对象，含 rawScore/totalScore/answers） */
  detail?: string | { rawScore?: number; totalScore?: number } | null
}

/** 测评记录 */
export interface AssessmentRecord {
  id: ID
  scaleName: string
  totalScore: number
  level: string
  createTime: string
}

/** 情绪日记 */
export interface MoodJournal {
  id: ID
  content: string
  /** 心情 1-5 */
  mood: number
  tags: string[]
  createTime: string
}

/** 分页结果 */
export interface PageResult<T> {
  records: T[]
  total: number
}

/** 心情趋势点 */
export interface MoodPoint {
  date: string
  score: number
}

/** 情绪分布项 */
export interface EmotionItem {
  name: string
  value: number
}

/** 仪表盘统计 */
export interface Overview {
  moodTrend: MoodPoint[]
  emotionDistribution: EmotionItem[]
  assessmentHistory: AssessmentRecord[]
  chatCount: number
  journalCount: number
  avgMood: number
  streakDays: number
}

/** 管理后台统计 */
export interface AdminOverview {
  userCount: number
  chatCount: number
  journalCount: number
  assessmentCount: number
}
