import { useCallback, useEffect, useRef } from 'react'
import * as echarts from 'echarts'

/**
 * ECharts 封装：自动 init / resize / dispose，返回容器 ref 与 setOption。
 */
export function useECharts() {
  const containerRef = useRef<HTMLDivElement | null>(null)
  const chartRef = useRef<echarts.ECharts | null>(null)

  useEffect(() => {
    if (!containerRef.current) return
    const chart = echarts.init(containerRef.current)
    chartRef.current = chart
    const onResize = () => chart.resize()
    window.addEventListener('resize', onResize)
    return () => {
      window.removeEventListener('resize', onResize)
      chart.dispose()
      chartRef.current = null
    }
  }, [])

  const setOption = useCallback((option: echarts.EChartsOption, notMerge = false) => {
    chartRef.current?.setOption(option, notMerge)
  }, [])

  return { containerRef, setOption }
}
