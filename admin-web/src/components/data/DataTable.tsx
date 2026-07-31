/**
 * DataTable Component - Full-featured data table with sorting, pagination, selection
 * Design System 2.0
 */

import React, { useState, useCallback } from 'react';
import {
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TablePagination,
  TableSortLabel,
  Checkbox,
  IconButton,
  Menu,
  MenuItem,
  Box,
  Typography,
  Skeleton,
  Paper,
  Toolbar,
  Tooltip,
  alpha,
  useTheme,
} from '@mui/material';
import {
  MoreVert as MoreVertIcon,
  FilterList as FilterListIcon,
} from '@mui/icons-material';

// =============================================================================
// TYPES
// =============================================================================

export type SortDirection = 'asc' | 'desc';

export interface Column<T> {
  key: string;
  header: string;
  accessor: (row: T) => React.ReactNode;
  sortable?: boolean;
  width?: string | number;
  align?: 'left' | 'center' | 'right';
  sortAccessor?: (row: T) => string | number;
}

export interface PaginationConfig {
  page: number;
  pageSize: number;
  total: number;
  onPageChange: (page: number) => void;
  onPageSizeChange: (pageSize: number) => void;
}

export interface RowAction<T> {
  label: string;
  icon?: React.ReactNode;
  onClick: (row: T) => void;
  danger?: boolean;
  disabled?: (row: T) => boolean;
}

export interface DataTableProps<T> {
  data: T[];
  columns: Column<T>[];
  keyExtractor: (row: T) => string;
  loading?: boolean;
  selectable?: boolean;
  pagination?: PaginationConfig;
  onRowClick?: (row: T) => void;
  rowActions?: RowAction<T>[];
  emptyState?: React.ReactNode;
  title?: string;
  toolbarActions?: React.ReactNode;
  stickyHeader?: boolean;
  size?: 'small' | 'medium';
}

// =============================================================================
// TABLE SKELETON
// =============================================================================

interface TableSkeletonProps {
  rows: number;
  columns: number;
  selectable?: boolean;
}

const TableSkeleton: React.FC<TableSkeletonProps> = ({ rows, columns, selectable }) => {
  return (
    <Table>
      <TableHead>
        <TableRow>
          {selectable && (
            <TableCell padding="checkbox">
              <Skeleton variant="rectangular" width={20} height={20} />
            </TableCell>
          )}
          {Array.from({ length: columns }).map((_, i) => (
            <TableCell key={i}>
              <Skeleton width="80%" />
            </TableCell>
          ))}
          <TableCell align="right">
            <Skeleton width={24} />
          </TableCell>
        </TableRow>
      </TableHead>
      <TableBody>
        {Array.from({ length: rows }).map((_, rowIndex) => (
          <TableRow key={rowIndex}>
            {selectable && (
              <TableCell padding="checkbox">
                <Skeleton variant="rectangular" width={20} height={20} />
              </TableCell>
            )}
            {Array.from({ length: columns }).map((_, colIndex) => (
              <TableCell key={colIndex}>
                <Skeleton />
              </TableCell>
            ))}
            <TableCell align="right">
              <Skeleton width={24} />
            </TableCell>
          </TableRow>
        ))}
      </TableBody>
    </Table>
  );
};

// =============================================================================
// EMPTY STATE
// =============================================================================

const DefaultEmptyState: React.FC = () => (
  <Box
    sx={{
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      justifyContent: 'center',
      py: 8,
      px: 4,
    }}
  >
    <FilterListIcon
      sx={{
        fontSize: 64,
        color: 'text.disabled',
        mb: 2,
      }}
    />
    <Typography variant="h6" color="text.secondary" gutterBottom>
      No data available
    </Typography>
    <Typography variant="body2" color="text.disabled">
      There are no records to display at this time
    </Typography>
  </Box>
);

// =============================================================================
// MAIN COMPONENT
// =============================================================================

export function DataTable<T>({
  data,
  columns,
  keyExtractor,
  loading = false,
  selectable = false,
  pagination,
  onRowClick,
  rowActions,
  emptyState,
  title,
  toolbarActions,
  stickyHeader = true,
  size = 'medium',
}: DataTableProps<T>) {
  const theme = useTheme();
  const [selected, setSelected] = useState<string[]>([]);
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [actionRow, setActionRow] = useState<T | null>(null);
  const [sortConfig, setSortConfig] = useState<{
    key: string;
    direction: SortDirection;
  } | null>(null);

  // Sort data
  const sortedData = React.useMemo(() => {
    if (!sortConfig) return data;

    const column = columns.find((c) => c.key === sortConfig.key);
    if (!column || !column.sortAccessor) return data;

    return [...data].sort((a, b) => {
      const aValue = column.sortAccessor!(a);
      const bValue = column.sortAccessor!(b);

      if (aValue < bValue) return sortConfig.direction === 'asc' ? -1 : 1;
      if (aValue > bValue) return sortConfig.direction === 'asc' ? 1 : -1;
      return 0;
    });
  }, [data, sortConfig, columns]);

  // Handle select all
  const handleSelectAll = useCallback(
    (event: React.ChangeEvent<HTMLInputElement>) => {
      if (event.target.checked) {
        setSelected(data.map(keyExtractor));
      } else {
        setSelected([]);
      }
    },
    [data, keyExtractor]
  );

  // Handle select row
  const handleSelect = useCallback(
    (id: string) => {
      const selectedIndex = selected.indexOf(id);
      let newSelected: string[] = [];

      if (selectedIndex === -1) {
        newSelected = [...selected, id];
      } else {
        newSelected = selected.filter((item) => item !== id);
      }

      setSelected(newSelected);
    },
    [selected]
  );

  // Handle sort
  const handleSort = useCallback(
    (key: string) => {
      setSortConfig((current) => {
        if (!current || current.key !== key) {
          return { key, direction: 'asc' };
        }
        if (current.direction === 'asc') {
          return { key, direction: 'desc' };
        }
        return null;
      });
    },
    []
  );

  // Handle actions menu
  const handleActionsOpen = useCallback(
    (event: React.MouseEvent<HTMLElement>, row: T) => {
      event.stopPropagation();
      setAnchorEl(event.currentTarget);
      setActionRow(row);
    },
    []
  );

  const handleActionsClose = useCallback(() => {
    setAnchorEl(null);
    setActionRow(null);
  }, []);

  // Check if all selected
  const isAllSelected = data.length > 0 && selected.length === data.length;
  const isIndeterminate = selected.length > 0 && selected.length < data.length;

  if (loading) {
    return (
      <Paper elevation={0} sx={{ borderRadius: 2, overflow: 'hidden' }} data-testid="data-table-loading">
        <TableContainer>
          <TableSkeleton
            rows={pagination?.pageSize || 5}
            columns={columns.length}
            selectable={selectable}
          />
        </TableContainer>
      </Paper>
    );
  }

  if (data.length === 0) {
    return (
      <Paper elevation={0} sx={{ borderRadius: 2, overflow: 'hidden' }} data-testid="data-table-empty">
        {title && (
          <Toolbar sx={{ borderBottom: `1px solid ${theme.palette.divider}` }}>
            <Typography variant="h6" component="div" sx={{ flex: 1 }}>
              {title}
            </Typography>
            {toolbarActions}
          </Toolbar>
        )}
        {emptyState || <DefaultEmptyState />}
      </Paper>
    );
  }

  return (
    <Paper
      elevation={0}
      sx={{
        borderRadius: 2,
        overflow: 'hidden',
        border: `1px solid ${alpha(theme.palette.divider, 0.5)}`,
      }}
      data-testid="data-table"
    >
      {/* Toolbar */}
      {(title || toolbarActions || selected.length > 0) && (
        <Toolbar
          sx={{
            borderBottom: `1px solid ${theme.palette.divider}`,
            minHeight: 56,
            ...(selected.length > 0 && {
              backgroundColor: alpha(theme.palette.primary.main, 0.04),
            }),
          }}
        >
          {selected.length > 0 ? (
            <Typography color="primary.main" variant="subtitle1" component="div">
              {selected.length} selected
            </Typography>
          ) : (
            title && (
              <Typography variant="h6" component="div" sx={{ flex: 1 }}>
                {title}
              </Typography>
            )
          )}
          {toolbarActions}
        </Toolbar>
      )}

      {/* Table */}
      <TableContainer>
        <Table stickyHeader={stickyHeader} size={size}>
          <TableHead>
            <TableRow>
              {selectable && (
                <TableCell padding="checkbox" sx={{ backgroundColor: 'background.paper' }}>
                  <Checkbox
                    indeterminate={isIndeterminate}
                    checked={isAllSelected}
                    onChange={handleSelectAll}
                    inputProps={{ 'aria-label': 'select all' }}
                  />
                </TableCell>
              )}
              {columns.map((col) => (
                <TableCell
                  key={col.key}
                  align={col.align}
                  sx={{
                    width: col.width,
                    backgroundColor: 'background.paper',
                    fontWeight: 600,
                  }}
                >
                  {col.sortable ? (
                    <TableSortLabel
                      active={sortConfig?.key === col.key}
                      direction={sortConfig?.key === col.key ? sortConfig.direction : 'asc'}
                      onClick={() => handleSort(col.key)}
                    >
                      {col.header}
                    </TableSortLabel>
                  ) : (
                    col.header
                  )}
                </TableCell>
              ))}
              {rowActions && (
                <TableCell
                  align="right"
                  sx={{ backgroundColor: 'background.paper', width: 48 }}
                >
                  Actions
                </TableCell>
              )}
            </TableRow>
          </TableHead>
          <TableBody>
            {sortedData.map((row) => {
              const id = keyExtractor(row);
              const isSelected = selected.indexOf(id) !== -1;

              return (
                <TableRow
                  hover
                  key={id}
                  selected={isSelected}
                  onClick={() => onRowClick?.(row)}
                  sx={{
                    cursor: onRowClick ? 'pointer' : 'default',
                    '&:last-child td, &:last-child th': { border: 0 },
                  }}
                  data-testid={`table-row-${id}`}
                >
                  {selectable && (
                    <TableCell
                      padding="checkbox"
                      onClick={(e) => e.stopPropagation()}
                      sx={{ backgroundColor: 'background.paper' }}
                    >
                      <Checkbox
                        checked={isSelected}
                        onChange={() => handleSelect(id)}
                        inputProps={{ 'aria-label': `select row ${id}` }}
                      />
                    </TableCell>
                  )}
                  {columns.map((col) => (
                    <TableCell key={col.key} align={col.align}>
                      {col.accessor(row)}
                    </TableCell>
                  ))}
                  {rowActions && (
                    <TableCell
                      align="right"
                      onClick={(e) => e.stopPropagation()}
                      sx={{ backgroundColor: 'background.paper' }}
                    >
                      <Tooltip title="Actions">
                        <IconButton
                          size="small"
                          onClick={(e) => handleActionsOpen(e, row)}
                          sx={{ opacity: 0.7, '&:hover': { opacity: 1 } }}
                          data-testid={`row-actions-${id}`}
                        >
                          <MoreVertIcon fontSize="small" />
                        </IconButton>
                      </Tooltip>
                    </TableCell>
                  )}
                </TableRow>
              );
            })}
          </TableBody>
        </Table>
      </TableContainer>

      {/* Pagination */}
      {pagination && (
        <TablePagination
          component="div"
          count={pagination.total}
          page={pagination.page}
          onPageChange={(_, page) => pagination.onPageChange(page)}
          rowsPerPage={pagination.pageSize}
          onRowsPerPageChange={(e) =>
            pagination.onPageSizeChange(parseInt(e.target.value, 10))
          }
          rowsPerPageOptions={[10, 25, 50, 100]}
          sx={{
            borderTop: `1px solid ${theme.palette.divider}`,
          }}
        />
      )}

      {/* Row Actions Menu */}
      <Menu
        anchorEl={anchorEl}
        open={Boolean(anchorEl)}
        onClose={handleActionsClose}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
        transformOrigin={{ vertical: 'top', horizontal: 'right' }}
        PaperProps={{
          elevation: 3,
          sx: { minWidth: 160, borderRadius: 2 },
        }}
      >
        {rowActions?.map((action) => {
          const disabled = actionRow ? action.disabled?.(actionRow) : false;
          return (
            <MenuItem
              key={action.label}
              onClick={() => {
                if (actionRow) action.onClick(actionRow);
                handleActionsClose();
              }}
              disabled={disabled}
              sx={{
                borderRadius: 1,
                mx: 0.5,
                my: 0.25,
                ...(action.danger && {
                  color: 'error.main',
                  '&:hover': { backgroundColor: alpha('#E53935', 0.08) },
                }),
              }}
            >
              {action.icon && <Box sx={{ mr: 1.5 }}>{action.icon}</Box>}
              {action.label}
            </MenuItem>
          );
        })}
      </Menu>
    </Paper>
  );
}

export default DataTable;
