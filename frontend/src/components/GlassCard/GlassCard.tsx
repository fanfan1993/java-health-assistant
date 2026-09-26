import type { CSSProperties, ReactNode, Ref } from 'react'

interface GlassCardProps {
  children: ReactNode
  className?: string
  style?: CSSProperties
  /** React 19：ref 直接作为 prop 透传 */
  ref?: Ref<HTMLDivElement>
  onClick?: () => void
}

/** 玻璃拟态卡片 */
export default function GlassCard({ children, className = '', style, ref, onClick }: GlassCardProps) {
  return (
    <div ref={ref} className={`glass-card ${className}`} style={style} onClick={onClick}>
      {children}
    </div>
  )
}
