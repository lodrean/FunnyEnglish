/**
 * Pagination Component
 * Table pagination wrapper with MUI TablePagination
 */
import React from 'react';
import {
  TablePagination,
  styled,
  Box,
  alpha,
} from '@mui/material';

// Цвета — из темы (bd ku2: уход от хардкодов #4A90D9/MUI-grey)

// Type Definitions
export interface PaginationProps {
  count: number;
  page: number;
  rowsPerPage: number;
  rowsPerPageOptions?: number[];
  onPageChange: (page: number) => void;
  onRowsPerPageChange: (rowsPerPage: number) => void;
  labelDisplayedRows?: (props: {
    from: number;
    to: number;
    count: number;
  }) => React.ReactNode;
  labelRowsPerPage?: string;
  showFirstButton?: boolean;
  showLastButton?: boolean;
}

// Styled Components
const PaginationContainer = styled(Box)(({ theme }) => ({
  display: 'flex',
  justifyContent: 'flex-end',
  alignItems: 'center',
  padding: theme.spacing(1.5, 2),
  backgroundColor: theme.palette.background.paper,
  borderTop: `1px solid ${theme.palette.divider}`,
  borderRadius: `0 0 ${theme.shape.borderRadius}px ${theme.shape.borderRadius}px`,
}));

const StyledTablePagination = styled(TablePagination)(({ theme }) => ({
  '.MuiTablePagination-selectLabel': {
    color: theme.palette.text.secondary,
    fontSize: '0.875rem',
  },
  '.MuiTablePagination-displayedRows': {
    color: theme.palette.text.primary,
    fontSize: '0.875rem',
  },
  '.MuiTablePagination-select': {
    color: theme.palette.text.primary,
    fontSize: '0.875rem',
    paddingRight: '24px',
  },
  '.MuiTablePagination-actions': {
    '& .MuiIconButton-root': {
      color: theme.palette.text.secondary,
      '&:hover': {
        backgroundColor: alpha(theme.palette.primary.main, 0.08),
        color: theme.palette.primary.main,
      },
      '&.Mui-disabled': {
        color: alpha(theme.palette.text.primary, 0.26),
      },
    },
  },
  '.MuiSelect-icon': {
    color: theme.palette.text.secondary,
  },
}));

// Default label displayed rows function
const defaultLabelDisplayedRows = ({
  from,
  to,
  count,
}: {
  from: number;
  to: number;
  count: number;
}) => {
  return `${from}-${to} of ${count}`;
};

// Main Component
const Pagination: React.FC<PaginationProps> = ({
  count,
  page,
  rowsPerPage,
  rowsPerPageOptions = [10, 25, 50, 100],
  onPageChange,
  onRowsPerPageChange,
  labelDisplayedRows = defaultLabelDisplayedRows,
  labelRowsPerPage = 'Rows per page:',
  showFirstButton = false,
  showLastButton = false,
}) => {
  const handlePageChange = (
    _event: React.MouseEvent<HTMLButtonElement> | null,
    newPage: number
  ) => {
    onPageChange(newPage);
  };

  const handleRowsPerPageChange = (
    event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ) => {
    onRowsPerPageChange(parseInt(event.target.value, 10));
    onPageChange(0); // Reset to first page when changing rows per page
  };

  return (
    <PaginationContainer>
      <StyledTablePagination
        count={count}
        page={page}
        rowsPerPage={rowsPerPage}
        rowsPerPageOptions={rowsPerPageOptions}
        onPageChange={handlePageChange}
        onRowsPerPageChange={handleRowsPerPageChange}
        labelDisplayedRows={labelDisplayedRows}
        labelRowsPerPage={labelRowsPerPage}
        showFirstButton={showFirstButton}
        showLastButton={showLastButton}
      />
    </PaginationContainer>
  );
};

// Compact Pagination variant for smaller spaces
export interface CompactPaginationProps {
  count: number;
  page: number;
  onPageChange: (page: number) => void;
  showPageNumbers?: boolean;
}

export const CompactPagination: React.FC<CompactPaginationProps> = ({
  count,
  page,
  onPageChange,
  // showPageNumbers = true, // TODO: implement page numbers display
}) => {
  return (
    <Box
      sx={{
        display: 'flex',
        alignItems: 'center',
        gap: 1,
        padding: 1,
      }}
    >
      <StyledTablePagination
        count={count}
        page={page}
        rowsPerPage={1}
        rowsPerPageOptions={[]}
        onPageChange={(_e, newPage) => onPageChange(newPage)}
        labelRowsPerPage=""
        labelDisplayedRows={() => ''}
      />
    </Box>
  );
};

export default Pagination;
