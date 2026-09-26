import { useEffect, useState, useOptimistic, useTransition } from 'react'
import { DatePicker, Drawer, Empty, Form, Input, Popconfirm, Select, Tag } from 'antd'
import { DeleteOutlined, PlusOutlined, SearchOutlined } from '@ant-design/icons'
import dayjs from 'dayjs'
import PageTransition from '@/components/PageTransition'
import GlassCard from '@/components/GlassCard'
import GradientButton from '@/components/GradientButton'
import { journalApi } from '@/api/journal'
import { useDebounce } from '@/hooks/useDebounce'
import { useDocumentTitle } from '@/hooks/useDocumentTitle'
import type { MoodJournal } from '@/types'
import './Journal.scss'

const MOOD_EMOJIS = ['😢', '😞', '😐', '🙂', '😄']
const MOOD_LABELS = ['很难过', '低落', '一般', '不错', '很开心']

interface JournalAction {
  type: 'del' | 'add'
  id?: string | number
  item?: MoodJournal
}

function moodEmoji(mood: number): string {
  return MOOD_EMOJIS[Math.min(Math.max(mood, 1), 5) - 1]
}

export default function Journal() {
  useDocumentTitle('情绪日记 · AI 心理健康助手')
  const [records, setRecords] = useState<MoodJournal[]>([])
  const [total, setTotal] = useState(0)
  const [page, setPage] = useState(1)
  const [loading, setLoading] = useState(false)
  const [drawerOpen, setDrawerOpen] = useState(false)
  const [keyword, setKeyword] = useState('')
  const [form] = Form.useForm()
  const [, startTransition] = useTransition()

  const debouncedKeyword = useDebounce(keyword, 300)

  // useOptimistic：删除/新增乐观更新
  const [optimisticRecords, applyOptimistic] = useOptimistic(
    records,
    (state, action: JournalAction) => {
      if (action.type === 'del') return state.filter((r) => r.id !== action.id)
      return action.item ? [action.item, ...state] : state
    },
  )

  const load = async (targetPage: number) => {
    setLoading(true)
    try {
      const res = await journalApi.getJournals(targetPage, 10)
      setRecords(res.records)
      setTotal(res.total)
      setPage(targetPage)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load(1)
  }, [])

  const handleDelete = (id: string | number) => {
    startTransition(async () => {
      applyOptimistic({ type: 'del', id })
      await journalApi.removeJournal(id)
      setRecords((prev) => prev.filter((r) => r.id !== id))
      setTotal((t) => Math.max(t - 1, 0))
    })
  }

  const handleCreate = (values: { content: string; mood: number; tags?: string[] }) => {
    startTransition(async () => {
      const payload = {
        content: values.content,
        mood: values.mood,
        tags: values.tags ?? [],
      }
      const optimisticItem: MoodJournal = {
        id: `tmp-${Date.now()}`,
        createTime: dayjs().format('YYYY-MM-DD HH:mm:ss'),
        ...payload,
      }
      applyOptimistic({ type: 'add', item: optimisticItem })
      await journalApi.addJournal(payload)
      setDrawerOpen(false)
      form.resetFields()
      await load(1)
    })
  }

  const filtered = debouncedKeyword
    ? optimisticRecords.filter((r) => r.content.includes(debouncedKeyword))
    : optimisticRecords

  return (
    <PageTransition>
      <div className="journal-page">
        <div className="journal-toolbar">
          <Input
            allowClear
            prefix={<SearchOutlined />}
            placeholder="搜索日记内容"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            className="journal-search"
          />
          <GradientButton icon={<PlusOutlined />} onClick={() => setDrawerOpen(true)}>
            写日记
          </GradientButton>
        </div>

        {filtered.length === 0 && !loading ? (
          <Empty description={keyword ? '没有匹配的日记' : '还没有日记，写下今天的心情吧'} className="journal-empty" />
        ) : (
          <div className="journal-grid">
            {filtered.map((item, i) => (
              <GlassCard
                key={item.id}
                className="journal-card"
                style={{ animationDelay: `${i * 0.05}s` }}
              >
                <div className="journal-head">
                  <span className="journal-mood" title={MOOD_LABELS[item.mood - 1]}>
                    {moodEmoji(item.mood)}
                  </span>
                  <Popconfirm title="确定删除这篇日记吗？" onConfirm={() => handleDelete(item.id)}>
                    <DeleteOutlined className="journal-delete" />
                  </Popconfirm>
                </div>
                <p className="journal-content">{item.content}</p>
                <div className="journal-tags">
                  {item.tags.map((tag) => (
                    <Tag key={tag} color="processing">
                      {tag}
                    </Tag>
                  ))}
                </div>
                <div className="journal-time">{item.createTime}</div>
              </GlassCard>
            ))}
          </div>
        )}

        <Drawer
          title="写一篇新日记"
          open={drawerOpen}
          onClose={() => setDrawerOpen(false)}
          width={460}
        >
          <Form form={form} layout="vertical" onFinish={handleCreate} initialValues={{ mood: 3 }}>
            <Form.Item name="content" label="今天的心情和想法" rules={[{ required: true, message: '写点什么吧' }]}>
              <Input.TextArea rows={6} placeholder="记录此刻的感受…" />
            </Form.Item>
            <Form.Item name="mood" label="现在的心情">
              <Select
                options={MOOD_EMOJIS.map((emoji, i) => ({
                  value: i + 1,
                  label: `${emoji} ${MOOD_LABELS[i]}`,
                }))}
              />
            </Form.Item>
            <Form.Item name="tags" label="标签">
              <Select mode="tags" placeholder="输入后回车添加，如：工作、运动" />
            </Form.Item>
            <GradientButton htmlType="submit" block size="large">
              保存日记
            </GradientButton>
          </Form>
        </Drawer>

        {total > 10 && !keyword && (
          <div className="journal-pagination">
            <a
              onClick={() => load(Math.max(page - 1, 1))}
              style={{ opacity: page === 1 ? 0.4 : 1 }}
            >
              上一页
            </a>
            <span>
              第 {page} / {Math.ceil(total / 10)} 页
            </span>
            <a
              onClick={() => load(Math.min(page + 1, Math.ceil(total / 10)))}
              style={{ opacity: page >= Math.ceil(total / 10) ? 0.4 : 1 }}
            >
              下一页
            </a>
          </div>
        )}
      </div>
    </PageTransition>
  )
}
