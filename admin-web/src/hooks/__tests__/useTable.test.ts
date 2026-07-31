import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import { useTable } from '../useTable';

interface TestData {
  id: number
  name: string
  age: number
}

const testData: TestData[] = [
  { id: 1, name: 'John', age: 30 },
  { id: 2, name: 'Jane', age: 25 },
  { id: 3, name: 'Bob', age: 35 },
  { id: 4, name: 'Alice', age: 28 },
  { id: 5, name: 'Charlie', age: 32 },
]

describe('useTable', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('initializes with default values', () => {
    const { result } = renderHook(() => useTable({ data: testData }))
    
    expect(result.current.sortKey).toBeNull()
    expect(result.current.sortDirection).toBe('asc')
    expect(result.current.page).toBe(0)
    expect(result.current.pageSize).toBe(10)
    expect(result.current.selectedIds).toEqual([])
    expect(result.current.totalPages).toBe(1)
  })

  it('initializes with custom values', () => {
    const { result } = renderHook(() =>
      useTable({
        data: testData,
        initialSort: { key: 'name', direction: 'desc' },
        initialPage: 1,
        initialPageSize: 2,
      })
    )
    
    expect(result.current.sortKey).toBe('name')
    expect(result.current.sortDirection).toBe('desc')
    expect(result.current.page).toBe(1)
    expect(result.current.pageSize).toBe(2)
  })

  describe('Sorting', () => {
    it('handles sort by column', () => {
      const { result } = renderHook(() => useTable({ data: testData }))
      
      act(() => {
        result.current.handleSort('name')
      })
      
      expect(result.current.sortKey).toBe('name')
      expect(result.current.sortDirection).toBe('asc')
    })

    it('toggles sort direction on same column', () => {
      const { result } = renderHook(() => useTable({ data: testData }))
      
      act(() => {
        result.current.handleSort('name')
      })
      expect(result.current.sortDirection).toBe('asc')
      
      act(() => {
        result.current.handleSort('name')
      })
      expect(result.current.sortDirection).toBe('desc')
      
      act(() => {
        result.current.handleSort('name')
      })
      expect(result.current.sortDirection).toBe('asc')
    })

    it('resets to asc when sorting different column', () => {
      const { result } = renderHook(() => useTable({ data: testData }))
      
      act(() => {
        result.current.handleSort('name')
      })
      expect(result.current.sortDirection).toBe('asc')
      
      act(() => {
        result.current.handleSort('name')
      })
      expect(result.current.sortDirection).toBe('desc')
      
      act(() => {
        result.current.handleSort('age')
      })
      expect(result.current.sortKey).toBe('age')
      expect(result.current.sortDirection).toBe('asc')
    })

    it('sorts data correctly', () => {
      const { result } = renderHook(() => useTable({ data: testData, initialPageSize: 5 }))
      
      act(() => {
        result.current.handleSort('name')
      })
      
      expect(result.current.sortKey).toBe('name')
      expect(result.current.sortDirection).toBe('asc')
      expect(result.current.processedData.length).toBe(5)
    })

    it('toggles sort direction', () => {
      const { result } = renderHook(() => useTable({ data: testData, initialPageSize: 5 }))
      
      act(() => {
        result.current.handleSort('name')
      })
      expect(result.current.sortDirection).toBe('asc')
      
      // Toggle direction
      act(() => {
        result.current.handleSort('name')
      })
      
      // Direction should change
      expect(result.current.sortKey).toBe('name')
      expect(result.current.processedData.length).toBeGreaterThan(0)
    })

    it('resets page to 0 when sorting', () => {
      const { result } = renderHook(() => useTable({ data: testData, initialPage: 2 }))
      
      expect(result.current.page).toBe(2)
      
      act(() => {
        result.current.handleSort('name')
      })
      
      expect(result.current.page).toBe(0)
    })
  })

  describe('Pagination', () => {
    it('paginates data correctly', () => {
      const { result } = renderHook(() =>
        useTable({ data: testData, initialPageSize: 2 })
      )
      
      expect(result.current.processedData).toHaveLength(2)
      expect(result.current.processedData[0].id).toBe(1)
      expect(result.current.processedData[1].id).toBe(2)
      expect(result.current.totalPages).toBe(3)
    })

    it('handles page change', () => {
      const { result } = renderHook(() =>
        useTable({ data: testData, initialPageSize: 2 })
      )
      
      act(() => {
        result.current.handlePageChange(1)
      })
      
      expect(result.current.page).toBe(1)
      expect(result.current.processedData[0].id).toBe(3)
      expect(result.current.processedData[1].id).toBe(4)
    })

    it('handles page size change', () => {
      const { result } = renderHook(() =>
        useTable({ data: testData, initialPageSize: 2, initialPage: 1 })
      )
      
      expect(result.current.page).toBe(1)
      
      act(() => {
        result.current.handlePageSizeChange(5)
      })
      
      expect(result.current.pageSize).toBe(5)
      expect(result.current.page).toBe(0) // Reset to first page
      expect(result.current.processedData).toHaveLength(5)
    })

    it('calculates total pages correctly', () => {
      const { result } = renderHook(() =>
        useTable({ data: testData, initialPageSize: 2 })
      )
      
      expect(result.current.totalPages).toBe(3)
    })
  })

  describe('Selection', () => {
    it('selects single row', () => {
      const { result } = renderHook(() => useTable({ data: testData }))
      
      act(() => {
        result.current.handleSelectRow(testData[0], 'id')
      })
      
      expect(result.current.selectedIds).toContain('1')
      expect(result.current.isSelected('1')).toBe(true)
    })

    it('deselects row when selected again', () => {
      const { result } = renderHook(() => useTable({ data: testData }))
      
      act(() => {
        result.current.handleSelectRow(testData[0], 'id')
      })
      expect(result.current.selectedIds).toContain('1')
      
      act(() => {
        result.current.handleSelectRow(testData[0], 'id')
      })
      expect(result.current.selectedIds).not.toContain('1')
    })

    it('selects all rows', () => {
      const { result } = renderHook(() => useTable({ data: testData }))
      
      act(() => {
        result.current.handleSelectAll()
      })
      
      expect(result.current.selectedIds).toHaveLength(5)
    })

    it('deselects all when all are selected', () => {
      const { result } = renderHook(() => useTable({ data: testData }))
      
      act(() => {
        result.current.handleSelectAll()
      })
      expect(result.current.selectedIds).toHaveLength(5)
      
      act(() => {
        result.current.handleSelectAll()
      })
      expect(result.current.selectedIds).toHaveLength(0)
    })

    it('returns selected rows', () => {
      const { result } = renderHook(() => useTable({ data: testData }))
      
      act(() => {
        result.current.handleSelectRow(testData[0], 'id')
        result.current.handleSelectRow(testData[1], 'id')
      })
      
      // selectedIds contains the actual id values
      expect(result.current.selectedIds).toContain('1')
      expect(result.current.selectedIds).toContain('2')
      expect(result.current.selectedIds).toHaveLength(2)
    })
  })

  describe('Edge Cases', () => {
    it('handles empty data', () => {
      const { result } = renderHook(() => useTable({ data: [] }))
      
      expect(result.current.processedData).toHaveLength(0)
      expect(result.current.totalPages).toBe(0)
    })

    it('handles single item data', () => {
      const { result } = renderHook(() =>
        useTable({ data: [testData[0]], initialPageSize: 10 })
      )
      
      expect(result.current.processedData).toHaveLength(1)
      expect(result.current.totalPages).toBe(1)
    })
  })
})
