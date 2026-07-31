/**
 * Pagination Component
 * Table pagination wrapper with MUI TablePagination
 */
import React from 'react';
import {
  TablePagination,
  styled,
  Box,
} from '@mui/material';

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
  backgroundColor: COLORS.card,
  borderTop: `1px solid ${COLORS.border}`,
  borderRadius: `0 0 ${theme.shape.borderRadius}px ${theme.shape.borderRadius}px`,
}));

const StyledTablePagination = styled(TablePagination)({
  '.MuiTablePagination-selectLabel': {
    color: COLORS.textSecondary,
    fontSize: '0.875rem',
  },
  '.MuiTablePagination-displayedRows': {
    color: COLORS.textPrimary,
    fontSize: '0.875rem',
  },
  '.MuiTablePagination-select': {
    color: COLORS.textPrimary,
    fontSize: '0.875rem',
    paddingRight: '24px',
  },
  '.MuiTablePagination-actions': {
    '& .MuiIconButton-root': {
      color: COLORS.textSecondary,
      '&:hover': {
        backgroundColor: 'rgba(74, 144, 217, 0.08)',
        color: COLORS.primary,
      },
      '&.Mui-disabled': {
        color: 'rgba(0, 0, 0, 0.26)',
      },
    },
  },
  '.MuiSelect-icon': {
    color: COLORS.textSecondary,
  },
});

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
        component="div"
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
  showPageNumbers = true,
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
        component="div"
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
