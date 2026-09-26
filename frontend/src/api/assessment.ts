import { http } from './request'
import type { AssessmentRecord, AssessmentResult, ID, Scale } from '@/types'

/** 后端原始量表结构（题目共享量表级选项，题目文案字段为 text） */
interface BackendScale {
  id: ID
  name: string
  description: string
  /** 量表级共享选项（按等级从低到高） */
  options: { label: string; score: number }[]
  /** 题目（text 为题干，reverse 为反向计分标记，计分在后端完成） */
  questions: { text: string; reverse: boolean }[]
}

/** 预估答题用时：每题约 20 秒，向上取整到分钟 */
const estimateMinutes = (count: number): number => Math.max(1, Math.ceil((count * 20) / 60))

/** 后端结构 -> 前端渲染契约：题干/选项下放到每道题 */
const toFrontScale = (s: BackendScale): Scale => ({
  id: s.id,
  name: s.name,
  desc: s.description,
  minutes: estimateMinutes(s.questions.length),
  questions: s.questions.map((q, i) => ({
    id: `${s.id}-${i}`,
    title: q.text,
    options: s.options,
  })),
})

export const assessmentApi = {
  /** 量表列表（含题目），提交时传选项下标数组 */
  async getScales(): Promise<Scale[]> {
    const raw = await http<BackendScale[]>({ url: '/assessment/scales', method: 'GET' })
    return raw.map(toFrontScale)
  },
  /** 提交测评：answers 为每题选中选项的下标数组 */
  submit(scaleId: ID, answers: number[]): Promise<AssessmentResult> {
    return http<AssessmentResult>({
      url: '/assessment/submit',
      method: 'POST',
      data: { scaleId, answers },
    })
  },
  /** 历史测评记录 */
  getRecords(): Promise<AssessmentRecord[]> {
    return http<AssessmentRecord[]>({ url: '/assessment/records', method: 'GET' })
  },
}
