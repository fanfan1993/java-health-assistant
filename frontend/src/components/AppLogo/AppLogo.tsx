import './AppLogo.scss'

interface AppLogoProps {
  size?: 'sm' | 'lg'
}

/** 渐变 Logo */
export default function AppLogo({ size = 'lg' }: AppLogoProps) {
  return (
    <div className={`app-logo app-logo-${size}`}>
      <span className="app-logo-icon">🌿</span>
      <span className="app-logo-text gradient-text">AI 心灵伙伴</span>
    </div>
  )
}
