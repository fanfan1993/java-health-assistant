import type { ReactNode } from 'react'
import './PageTransition.scss'

/** 路由/内容切换过渡（淡入 + 上移） */
export default function PageTransition({ children }: { children: ReactNode }) {
  return <div className="page-transition">{children}</div>
}
