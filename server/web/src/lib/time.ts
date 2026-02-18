/**
 * 格式化日期时间为本地化字符串
 * @param dateTime - Date对象或ISO字符串
 * @returns 格式化后的日期时间字符串
 */
export const formatDateTime = (dateTime: Date | string): string => {
  try {
    const date = typeof dateTime === 'string' ? new Date(dateTime) : dateTime
    if (isNaN(date.getTime())) {
      return '无效日期'
    }
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    })
  } catch (error) {
    return '无效日期'
  }
}

/**
 * 格式化日期（不包含时间）
 * @param dateTime - Date对象或ISO字符串
 * @returns 格式化后的日期字符串
 */
export const formatDate = (dateTime: Date | string): string => {
  try {
    const date = typeof dateTime === 'string' ? new Date(dateTime) : dateTime
    if (isNaN(date.getTime())) {
      return '无效日期'
    }
    return date.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit'
    })
  } catch (error) {
    return '无效日期'
  }
}

/**
 * 格式化时间（不包含日期）
 * @param dateTime - Date对象或ISO字符串
 * @returns 格式化后的时间字符串
 */
export const formatTime = (dateTime: Date | string): string => {
  try {
    const date = typeof dateTime === 'string' ? new Date(dateTime) : dateTime
    if (isNaN(date.getTime())) {
      return '无效时间'
    }
    return date.toLocaleTimeString('zh-CN', {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    })
  } catch (error) {
    return '无效时间'
  }
}

/**
 * 获取相对时间（如："刚刚"、"5分钟前"、"2小时前"）
 * @param dateTime - Date对象或ISO字符串
 * @returns 相对时间字符串
 */
export const getRelativeTime = (dateTime: Date | string): string => {
  try {
    const date = typeof dateTime === 'string' ? new Date(dateTime) : dateTime
    if (isNaN(date.getTime())) {
      return '无效日期'
    }
    
    const now = new Date()
    const diff = now.getTime() - date.getTime()
    const seconds = Math.floor(diff / 1000)
    const minutes = Math.floor(seconds / 60)
    const hours = Math.floor(minutes / 60)
    const days = Math.floor(hours / 24)
    
    if (seconds < 60) {
      return '刚刚'
    } else if (minutes < 60) {
      return `${minutes}分钟前`
    } else if (hours < 24) {
      return `${hours}小时前`
    } else if (days < 7) {
      return `${days}天前`
    } else {
      return formatDate(date)
    }
  } catch (error) {
    return '无效日期'
  }
}
