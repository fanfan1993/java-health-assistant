import { Component, type ReactNode } from 'react'
import { Result, Button } from 'antd'

interface ErrorBoundaryProps {
  children: ReactNode
}

interface ErrorBoundaryState {
  error: Error | null
}

/** 简单错误边界：配合 use(Promise) 的 Suspense 使用 */
export default class ErrorBoundary extends Component<ErrorBoundaryProps, ErrorBoundaryState> {
  state: ErrorBoundaryState = { error: null }

  static getDerivedStateFromError(error: Error): ErrorBoundaryState {
    return { error }
  }

  render() {
    if (this.state.error) {
      return (
        <Result
          status="error"
          title="数据加载失败"
          subTitle={this.state.error.message}
          extra={
            <Button type="primary" onClick={() => window.location.reload()}>
              刷新重试
            </Button>
          }
        />
      )
    }
    return this.props.children
  }
}
