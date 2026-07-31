import { useState, useCallback, useMemo } from 'react'

interface UseTableProps<T> {
  data: T[]
  initialSort?: { key: keyof T; direction: 'asc' | 'desc' }
  initialPage?: number
  initialPageSize?: number
}

interface UseTableReturn<T> {
  // Sorting
  sortKey: keyof T | null
  sortDirection: 'asc' | 'desc'
  handleSort: (key: keyof T) => void
  
  // Pagination
  page: number
  pageSize: number
  handlePageChange: (page: number) => void
  handlePageSizeChange: (pageSize: number) => void
  
  // Selection
  selectedRows: T[]
  selectedIds: string[]
  handleSelectAll: () => void
  handleSelectRow: (row: T, idKey: keyof T) => void
  isSelected: (id: string) => boolean
  
  // Filtered and paginated data
  processedData: T[]
  totalPages: number
}

export function useTable<T>({
  data,
  initialSort,
  initialPage = 0,
  initialPageSize = 10,
}: UseTableProps<T>): UseTableReturn<T> {
  // Sorting state
  const [sortKey, setSortKey] = useState<keyof T | null>(initialSort?.key || null)
  const [sortDirection, setSortDirection] = useState<'asc' | 'desc'>(initialSort?.direction || 'asc')
  
  // Pagination state
  const [page, setPage] = useState(initialPage)
  const [pageSize, setPageSize] = useState(initialPageSize)
  
  // Selection state
  const [selectedIds, setSelectedIds] = useState<string[]>([])

  // Handle sort
  const handleSort = useCallback((key: keyof T) => {
    setSortDirection((prev) => {
      if (sortKey === key) {
        return prev === 'asc' ? 'desc' : 'asc'
      }
      return 'asc'
    })
    setSortKey(key)
    setPage(0)
  }, [sortKey])

  // Handle pagination
  const handlePageChange = useCallback((newPage: number) => {
    setPage(newPage)
  }, [])

  const handlePageSizeChange = useCallback((newPageSize: number) => {
    setPageSize(newPageSize)
    setPage(0)
  }, [])

  // Handle selection
  const handleSelectAll = useCallback(() => {
    setSelectedIds((prev) => {
      if (prev.length === data.length) {
        return []
      }
      return data.map((_, index) => String(index))
    })
  }, [data])

  const handleSelectRow = useCallback((row: T, idKey: keyof T) => {
    const id = String(row[idKey])
    setSelectedIds((prev) => {
      if (prev.includes(id)) {
        return prev.filter((i) => i !== id)
      }
      return [...prev, id]
    })
  }, [])

  const isSelected = useCallback((id: string) => {
    return selectedIds.includes(id)
  }, [selectedIds])

  // Process data (sort and paginate)
  const processedData = useMemo(() => {
    let result = [...data]
    
    // Sort
    if (sortKey) {
      result.sort((a, b) => {
        const aValue = a[sortKey]
        const bValue = b[sortKey]
        
        if (aValue < bValue) return sortDirection === 'asc' ? -1 : 1
        if (aValue > bValue) return sortDirection === 'asc' ? 1 : -1
        return 0
      })
    }
    
    return result
  }, [data, sortKey, sortDirection])

  // Paginated data
  const paginatedData = useMemo(() => {
    const start = page * pageSize
    return processedData.slice(start, start + pageSize)
  }, [processedData, page, pageSize])

  const totalPages = Math.ceil(processedData.length / pageSize)

  // Selected rows
  const selectedRows = useMemo(() => {
    return data.filter((row, index) => selectedIds.includes(String(index)))
  }, [data, selectedIds])

  return {
    sortKey,
    sortDirection,
    handleSort,
    page,
    pageSize,
    handlePageChange,
    handlePageSizeChange,
    selectedRows,
    selectedIds,
    handleSelectAll,
    handleSelectRow,
    isSelected,
    processedData: paginatedData,
    totalPages,
  }
}
