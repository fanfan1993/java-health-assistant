import { useCountUp } from '@/hooks/useCountUp'

interface AnimatedNumberProps {
  value: number
  precision?: number
  className?: string
}

/** 数字滚动组件 */
export default function AnimatedNumber({ value, precision = 0, className }: AnimatedNumberProps) {
  const current = useCountUp(value)
  return <span className={className}>{current.toFixed(precision)}</span>
}
