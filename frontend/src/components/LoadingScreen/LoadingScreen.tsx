import './LoadingScreen.scss'

/** 全屏加载动画 */
export default function LoadingScreen() {
  return (
    <div className="loading-screen">
      <div className="loading-ring" />
      <p className="loading-text gradient-text">加载中…</p>
    </div>
  )
}
