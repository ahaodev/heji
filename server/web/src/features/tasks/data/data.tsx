import {
  ArrowDown,
  ArrowRight,
  ArrowUp,
  Circle,
  CheckCircle,
  AlertCircle,
  Timer,
  HelpCircle,
  CircleOff,
} from 'lucide-react'

export const labels = [
  {
    value: 'bug',
    label: '错误',
  },
  {
    value: 'feature',
    label: '功能',
  },
  {
    value: 'documentation',
    label: '文档',
  },
]

export const statuses = [
  {
    label: '待办',
    value: 'backlog' as const,
    icon: HelpCircle,
  },
  {
    label: '待做',
    value: 'todo' as const,
    icon: Circle,
  },
  {
    label: '进行中',
    value: 'in progress' as const,
    icon: Timer,
  },
  {
    label: '已完成',
    value: 'done' as const,
    icon: CheckCircle,
  },
  {
    label: '已取消',
    value: 'canceled' as const,
    icon: CircleOff,
  },
]

export const priorities = [
  {
    label: '低',
    value: 'low' as const,
    icon: ArrowDown,
  },
  {
    label: '中',
    value: 'medium' as const,
    icon: ArrowRight,
  },
  {
    label: '高',
    value: 'high' as const,
    icon: ArrowUp,
  },
  {
    label: '紧急',
    value: 'critical' as const,
    icon: AlertCircle,
  },
]
