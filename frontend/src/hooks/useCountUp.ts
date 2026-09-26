import { useEffect, useRef, useState } from 'react'

/**
 * 数字滚动动画：requestAnimationFrame + easeOutCubic。
 */
export function useCountUp(target: number, duration = 1200): number {
  const [value, setValue] = useState(0)
  const currentRef = useRef(0)

  useEffect(() => {
    const from = currentRef.current
    const start = performance.now()
    let raf = 0
    const tick = (now: number) => {
      const progress = Math.min((now - start) / duration, 1)
      const eased = 1 - Math.pow(1 - progress, 3)
      const current = from + (target - from) * eased
      currentRef.current = current
      setValue(current)
      if (progress < 1) {
        raf = requestAnimationFrame(tick)
      }
    }
    raf = requestAnimationFrame(tick)
    return () => cancelAnimationFrame(raf)
  }, [target, duration])

  return value
}
