/**
 * DataTable Component
 * Full-featured table with sorting, selection, pagination, and virtualization
 */
import React, { useState, useCallback, useMemo } from 'react';
import {
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TableSortLabel,
  Checkbox,
  Paper,
  Box,
  Typography,
  IconButton,
  Menu,
  MenuItem,
  Skeleton,
  styled,
  useTheme,
} from '@mui/material';
import {
  MoreVert as MoreVertIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
} from '@mui/icons-material';
import { FixedSizeList as List, ListChildComponentProps } from 'react-window';
import Pagination from './Pagination';

// Design System Colors
const COLORS = {
  primary: '#4A90D9',
  background: '#F5F5F5',
  card: '#FFFFFF',
  textPrimary: '#212121',
  textSecondary: '#757575',
  border: '#E0E0E0',
};

// Type Definitions
export interface ColumnDef<T> {
  key: string;
  header: string;
  accessor: (row: T) => React.ReactNode;
  sortable?: boolean;
  width?: string | number;
  align?: 'left' | 'center' | 'right';
  sortAccessor?: (row: T) => string | number;
}

export interface DataTableProps<T> {
  columns: ColumnDef<T>[];
  data: T[];
  keyExtractor: (row: T) => string;
  loading?: boolean;
  selectable?: boolean;
  onSelectionChange?: (selectedIds: string[]) => void;
  onRowClick?: (row: T) => void;
  onEdit?: (row: T) => void;
  onDelete?: (row: T) => void;
  emptyMessage?: string;
  rowsPerPageOptions?: number[];
  defaultRowsPerPage?: number;
  stickyHeader?: boolean;
  maxHeight?: number;
  enableVirtualization?: boolean;
  virtualizationHeight?: number;
}

type SortDirection = 'asc' | 'desc';

interface SortConfig {
  key: string;
  direction: SortDirection;
}

// Styled Components
const StyledTableContainer = styled(TableContainer)(({ theme }) => ({
  backgroundColor: COLORS.card,
  borderRadius: theme.shape.borderRadius,
  boxShadow: '0 1px 3px rgba(0,0,0,0.12)',
  overflow: 'hidden',
}));

const StyledTableHead = styled(TableHead)({
  backgroundColor: COLORS.background,
});

const StyledTableCell = styled(TableCell)<{ width?: string | number; align?: string }>(
  ({ width, align }) => ({
    width: width,
    textAlign: align || 'left',
    fontWeight: 600,
    color: COLORS.textPrimary,
    borderBottom: `1px solid ${COLORS.border}`,
    whiteSpace: 'nowrap',
    overflow: 'hidden',
    textOverflow: 'ellipsis',
  })
);

const StyledTableRow = styled(TableRow)<{ clickable?: boolean }>(({ clickable }) => ({
  cursor: clickable ? 'pointer' : 'default',
  '&:hover': {
    backgroundColor: clickable ? 'rgba(74, 144, 217, 0.04)' : 'transparent',
  },
  '&:nth-of-type(even)': {
    backgroundColor: 'rgba(0, 0, 0, 0.02)',
  },
}));

const EmptyStateContainer = styled(Box)({
  display: 'flex',
  flexDirection: 'column',
  alignItems: 'center',
  justifyContent: 'center',
  padding: '48px 24px',
  color: COLORS.textSecondary,
});

// Virtualized Row Component
interface VirtualRowData<T> {
  data: T[];
  columns: ColumnDef<T>[];
  selectedIds: Set<string>;
  keyExtractor: (row: T) => string;
  onRowClick?: (row: T) => void;
  onEdit?: (row: T) => void;
  onDelete?: (row: T) => void;
  selectable?: boolean;
  onToggleRow: (id: string) => void;
}

function VirtualTableRow<T>({
  index,
  style,
  data: rowData,
}: ListChildComponentProps<VirtualRowData<T>>) {
  const {
    data,
    columns,
    selectedIds,
    keyExtractor,
    onRowClick,
    onEdit,
    onDelete,
    selectable,
    onToggleRow,
  } = rowData;

  const row = data[index];
  const rowId = keyExtractor(row);
  const isSelected = selectedIds.has(rowId);
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);

  const handleMenuOpen = (event: React.MouseEvent<HTMLElement>) => {
    event.stopPropagation();
    setAnchorEl(event.currentTarget);
  };

  const handleMenuClose = (event: React.MouseEvent) => {
    event.stopPropagation();
    setAnchorEl(null);
  };

  const handleEdit = (event: React.MouseEvent) => {
    event.stopPropagation();
    onEdit?.(row);
    setAnchorEl(null);
  };

  const handleDelete = (event: React.MouseEvent) => {
    event.stopPropagation();
    onDelete?.(row);
    setAnchorEl(null);
  };

  return (
    <TableRow
      style={{
        ...style,
        display: 'flex',
        alignItems: 'center',
      }}
      hover={!!onRowClick}
      selected={isSelected}
      onClick={() => onRowClick?.(row)}
    >
      {selectable && (
        <TableCell padding="checkbox" style={{ width: 50, flexShrink: 0 }}>
          <Checkbox
            checked={isSelected}
            onChange={() => onToggleRow(rowId)}
            onClick={(e) => e.stopPropagation()}
          />
        </TableCell>
      )}
      {columns.map((column) => (
        <TableCell
          key={column.key}
          align={column.align}
          style={{
            width: column.width,
            flex: column.width ? undefined : 1,
            minWidth: column.width,
            overflow: 'hidden',
            textOverflow: 'ellipsis',
            whiteSpace: 'nowrap',
          }}
        >
          {column.accessor(row)}
        </TableCell>
      ))}
      {(onEdit || onDelete) && (
        <TableCell align="right" style={{ width: 60, flexShrink: 0 }}>
          <IconButton size="small" onClick={handleMenuOpen}>
            <MoreVertIcon />
          </IconButton>
          <Menu
            anchorEl={anchorEl}
            open={Boolean(anchorEl)}
            onClose={handleMenuClose}
            onClick={(e) => e.stopPropagation()}
          >
            {onEdit && (
              <MenuItem onClick={handleEdit}>
                <EditIcon fontSize="small" sx={{ mr: 1 }} />
                Edit
              </MenuItem>
            )}
            {onDelete && (
              <MenuItem onClick={handleDelete} sx={{ color: '#E53935' }}>
                <DeleteIcon fontSize="small" sx={{ mr: 1 }} />
                Delete
              </MenuItem>
            )}
          </Menu>
        </TableCell>
      )}
    </TableRow>
  );
}

// Main Component
function DataTable<T>({
  columns,
  data,
  keyExtractor,
  loading = false,
  selectable = false,
  onSelectionChange,
  onRowClick,
  onEdit,
  onDelete,
  emptyMessage = 'No data available',
  rowsPerPageOptions = [10, 25, 50, 100],
  defaultRowsPerPage = 10,
  stickyHeader = true,
  maxHeight,
  enableVirtualization = false,
  virtualizationHeight = 400,
}: DataTableProps<T>) {
  const theme = useTheme();
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(defaultRowsPerPage);
  const [sortConfig, setSortConfig] = useState<SortConfig | null>(null);
  const [selectedIds, setSelectedIds] = useState<Set<string>>(new Set());

  // Sorting Logic
  const handleSort = useCallback(
    (columnKey: string) => {
      const column = columns.find((c) => c.key === columnKey);
      if (!column?.sortable) return;

      setSortConfig((current) => {
        if (current?.key === columnKey) {
          return {
            key: columnKey,
            direction: current.direction === 'asc' ? 'desc' : 'asc',
          };
        }
        return { key: columnKey, direction: 'asc' };
      });
    },
    [columns]
  );

  const sortedData = useMemo(() => {
    if (!sortConfig) return data;

    const column = columns.find((c) => c.key === sortConfig.key);
    if (!column) return data;

    return [...data].sort((a, b) => {
      const aValue = column.sortAccessor
        ? column.sortAccessor(a)
        : (column.accessor(a) as string | number);
      const bValue = column.sortAccessor
        ? column.sortAccessor(b)
        : (column.accessor(b) as string | number);

      if (aValue < bValue) return sortConfig.direction === 'asc' ? -1 : 1;
      if (aValue > bValue) return sortConfig.direction === 'asc' ? 1 : -1;
      return 0;
    });
  }, [data, sortConfig, columns]);

  // Pagination Logic
  const paginatedData = useMemo(() => {
    const start = page * rowsPerPage;
    return sortedData.slice(start, start + rowsPerPage);
  }, [sortedData, page, rowsPerPage]);

  // Selection Logic
  const handleSelectAll = useCallback(
    (event: React.ChangeEvent<HTMLInputElement>) => {
      const newSelected = new Set(selectedIds);
      if (event.target.checked) {
        paginatedData.forEach((row) => newSelected.add(keyExtractor(row)));
      } else {
        paginatedData.forEach((row) => newSelected.delete(keyExtractor(row)));
      }
      setSelectedIds(newSelected);
      onSelectionChange?.(Array.from(newSelected));
    },
    [paginatedData, selectedIds, keyExtractor, onSelectionChange]
  );

  const handleToggleRow = useCallback(
    (id: string) => {
      const newSelected = new Set(selectedIds);
      if (newSelected.has(id)) {
        newSelected.delete(id);
      } else {
        newSelected.add(id);
      }
      setSelectedIds(newSelected);
      onSelectionChange?.(Array.from(newSelected));
    },
    [selectedIds, onSelectionChange]
  );

  const isAllSelected =
    paginatedData.length > 0 &&
    paginatedData.every((row) => selectedIds.has(keyExtractor(row)));

  const isIndeterminate =
    paginatedData.some((row) => selectedIds.has(keyExtractor(row))) &&
    !isAllSelected;

  // Loading Skeleton
  if (loading) {
    return (
      <Box>
        {[...Array(5)].map((_, i) => (
          <Box key={i} sx={{ display: 'flex', gap: 2, p: 2 }}>
            {selectable && (
              <Skeleton variant="rectangular" width={40} height={40} />
            )}
            {columns.map((_, j) => (
              <Skeleton
                key={j}
                variant="text"
                width={j === 0 ? 200 : 120}
                height={40}
              />
            ))}
          </Box>
        ))}
      </Box>
    );
  }

  // Empty State
  if (data.length === 0) {
    return (
      <Paper>
        <EmptyStateContainer>
          <Typography variant="h6" color="textSecondary">
            {emptyMessage}
          </Typography>
        </EmptyStateContainer>
      </Paper>
    );
  }

  return (
    <Box>
      <StyledTableContainer sx={{ maxHeight }}>
        <Table stickyHeader={stickyHeader} size="medium">
          <StyledTableHead>
            <TableRow>
              {selectable && (
                <StyledTableCell padding="checkbox" style={{ width: 50 }}>
                  <Checkbox
                    checked={isAllSelected}
                    indeterminate={isIndeterminate}
                    onChange={handleSelectAll}
                  />
                </StyledTableCell>
              )}
              {columns.map((column) => (
                <StyledTableCell
                  key={column.key}
                  width={column.width}
                  align={column.align}
                >
                  {column.sortable ? (
                    <TableSortLabel
                      active={sortConfig?.key === column.key}
                      direction={
                        sortConfig?.key === column.key
                          ? sortConfig.direction
                          : 'asc'
                      }
                      onClick={() => handleSort(column.key)}
                    >
                      {column.header}
                    </TableSortLabel>
                  ) : (
                    column.header
                  )}
                </StyledTableCell>
              ))}
              {(onEdit || onDelete) && (
                <StyledTableCell align="right" style={{ width: 60 }}>
                  Actions
                </StyledTableCell>
              )}
            </TableRow>
          </StyledTableHead>
          {!enableVirtualization ? (
            <TableBody>
              {paginatedData.map((row) => {
                const rowId = keyExtractor(row);
                const isSelected = selectedIds.has(rowId);
                const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(
                  null
                );

                return (
                  <StyledTableRow
                    key={rowId}
                    clickable={!!onRowClick}
                    selected={isSelected}
                    onClick={() => onRowClick?.(row)}
                  >
                    {selectable && (
                      <TableCell padding="checkbox">
                        <Checkbox
                          checked={isSelected}
                          onChange={() => handleToggleRow(rowId)}
                          onClick={(e) => e.stopPropagation()}
                        />
                      </TableCell>
                    )}
                    {columns.map((column) => (
                      <TableCell
                        key={column.key}
                        align={column.align}
                        style={{
                          maxWidth: column.width,
                          overflow: 'hidden',
                          textOverflow: 'ellipsis',
                          whiteSpace: 'nowrap',
                        }}
                      >
                        {column.accessor(row)}
                      </TableCell>
                    ))}
                    {(onEdit || onDelete) && (
                      <TableCell align="right">
                        <IconButton
                          size="small"
                          onClick={(e) => {
                            e.stopPropagation();
                            setAnchorEl(e.currentTarget);
                          }}
                        >
                          <MoreVertIcon />
                        </IconButton>
                        <Menu
                          anchorEl={anchorEl}
                          open={Boolean(anchorEl)}
                          onClose={(e: React.MouseEvent) => {
                            e.stopPropagation();
                            setAnchorEl(null);
                          }}
                          onClick={(e) => e.stopPropagation()}
                        >
                          {onEdit && (
                            <MenuItem
                              onClick={(e) => {
                                e.stopPropagation();
                                onEdit(row);
                                setAnchorEl(null);
                              }}
                            >
                              <EditIcon fontSize="small" sx={{ mr: 1 }} />
                              Edit
                            </MenuItem>
                          )}
                          {onDelete && (
                            <MenuItem
                              onClick={(e) => {
                                e.stopPropagation();
                                onDelete(row);
                                setAnchorEl(null);
                              }}
                              sx={{ color: '#E53935' }}
                            >
                              <DeleteIcon fontSize="small" sx={{ mr: 1 }} />
                              Delete
                            </MenuItem>
                          )}
                        </Menu>
                      </TableCell>
                    )}
                  </StyledTableRow>
                );
              })}
            </TableBody>
          ) : (
            <TableBody>
              <TableRow>
                <TableCell
                  colSpan={
                    columns.length +
                    (selectable ? 1 : 0) +
                    (onEdit || onDelete ? 1 : 0)
                  }
                  style={{ padding: 0, border: 'none' }}
                >
                  <List
                    height={virtualizationHeight}
                    itemCount={paginatedData.length}
                    itemSize={52}
                    itemData={{
                      data: paginatedData,
                      columns,
                      selectedIds,
                      keyExtractor,
                      onRowClick,
                      onEdit,
                      onDelete,
                      selectable,
                      onToggleRow: handleToggleRow,
                    }}
                    width="100%"
                  >
                    {VirtualTableRow}
                  </List>
                </TableCell>
              </TableRow>
            </TableBody>
          )}
        </Table>
      </StyledTableContainer>

      <Pagination
        count={data.length}
        page={page}
        rowsPerPage={rowsPerPage}
        rowsPerPageOptions={rowsPerPageOptions}
        onPageChange={setPage}
        onRowsPerPageChange={setRowsPerPage}
      />
    </Box>
  );
}

export default DataTable;
