import { format, formatDistanceToNow, parseISO } from 'date-fns'

// Date formatters
export const formatDate = (date: string | Date, formatStr = 'MMM dd, yyyy'): string => {
  if (!date) return '-'
  const d = typeof date === 'string' ? parseISO(date) : date
  return format(d, formatStr)
}

export const formatDateTime = (date: string | Date): string => {
  if (!date) return '-'
  const d = typeof date === 'string' ? parseISO(date) : date
  return format(d, 'MMM dd, yyyy HH:mm')
}

export const formatRelativeTime = (date: string | Date): string => {
  if (!date) return '-'
  const d = typeof date === 'string' ? parseISO(date) : date
  return formatDistanceToNow(d, { addSuffix: true })
}

// Number formatters
export const formatNumber = (num: number, decimals = 0): string => {
  if (num === null || num === undefined) return '-'
  return num.toLocaleString('en-US', {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals,
  })
}

export const formatPercent = (value: number, decimals = 1): string => {
  if (value === null || value === undefined) return '-'
  return `${value.toFixed(decimals)}%`
}

export const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return `${parseFloat((bytes / Math.pow(k, i)).toFixed(2))} ${sizes[i]}`
}

// String formatters
export const truncateString = (str: string, maxLength: number): string => {
  if (!str || str.length <= maxLength) return str
  return `${str.substring(0, maxLength)}...`
}

export const capitalizeFirst = (str: string): string => {
  if (!str) return ''
  return str.charAt(0).toUpperCase() + str.slice(1)
}

export const slugify = (str: string): string => {
  return str
    .toLowerCase()
    .trim()
    .replace(/[^\w\s-]/g, '')
    .replace(/[\s_-]+/g, '-')
    .replace(/^-+|-+$/g, '')
}

// Duration formatter
export const formatDuration = (minutes: number): string => {
  if (minutes < 60) {
    return `${minutes}m`
  }
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  if (mins === 0) {
    return `${hours}h`
  }
  return `${hours}h ${mins}m`
}

// Currency formatter
export const formatCurrency = (amount: number, currency = 'USD'): string => {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency,
  }).format(amount)
}
