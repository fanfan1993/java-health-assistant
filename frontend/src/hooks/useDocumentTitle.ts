import { useEffect } from 'react'

/** 设置页面标题 */
export function useDocumentTitle(title: string): void {
  useEffect(() => {
    document.title = title
  }, [title])
}
