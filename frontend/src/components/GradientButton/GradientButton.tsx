import { Button } from 'antd'
import type { ButtonProps } from 'antd'
import './GradientButton.scss'

interface GradientButtonProps extends ButtonProps {
  className?: string
}

/** 渐变按钮（默认 primary，hover 渐变位移） */
export default function GradientButton({ className = '', type, ...rest }: GradientButtonProps) {
  return <Button className={`gradient-btn ${className}`} type={type ?? 'primary'} {...rest} />
}
