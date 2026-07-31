# Admin Web Components - Detailed Specifications

## Quick Reference for Implementation

---

## 1. Layout Components

### AdminLayout
```typescript
// src/components/layout/AdminLayout.tsx
import { Box, CssBaseline, ThemeProvider } from '@mui/material';
import { Outlet } from 'react-router-dom';
import { Header } from './Header';
import { Sidebar } from './Sidebar';
import { useState } from 'react';

const SIDEBAR_WIDTH = 240;
const SIDEBAR_COLLAPSED_WIDTH = 64;
const HEADER_HEIGHT = 64;

export const AdminLayout: React.FC = () => {
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
  
  return (
    <Box sx={{ display: 'flex', minHeight: '100vh' }}>
      <CssBaseline />
      <Header height={HEADER_HEIGHT} onMenuToggle={() => setSidebarCollapsed(!sidebarCollapsed)} />
      <Sidebar 
        width={sidebarCollapsed ? SIDEBAR_COLLAPSED_WIDTH : SIDEBAR_WIDTH}
        collapsed={sidebarCollapsed}
      />
      <Box
        component="main"
        sx={{
          flexGrow: 1,
          mt: `${HEADER_HEIGHT}px`,
          ml: `${sidebarCollapsed ? SIDEBAR_COLLAPSED_WIDTH : SIDEBAR_WIDTH}px`,
          p: 3,
          bgcolor: 'background.default',
          transition: 'margin 0.3s ease'
        }}
      >
        <Outlet />
      </Box>
    </Box>
  );
};
```

### Sidebar Navigation Items
```typescript
// src/components/navigation/navItems.ts
export const navItems = [
  {
    id: 'dashboard',
    label: 'Dashboard',
    icon: DashboardIcon,
    path: '/'
  },
  {
    id: 'content',
    label: 'Content',
    icon: FolderIcon,
    children: [
      { id: 'categories', label: 'Categories', path: '/categories', icon: CategoryIcon },
      { id: 'tests', label: 'Tests', path: '/tests', icon: QuizIcon },
      { id: 'questions', label: 'Questions', path: '/questions', icon: HelpIcon }
    ]
  },
  {
    id: 'users',
    label: 'Users',
    icon: PeopleIcon,
    children: [
      { id: 'students', label: 'Students', path: '/users/students', icon: SchoolIcon },
      { id: 'admins', label: 'Admins', path: '/users/admins', icon: AdminPanelSettingsIcon },
      { id: 'groups', label: 'Groups', path: '/users/groups', icon: GroupsIcon }
    ]
  },
  {
    id: 'analytics',
    label: 'Analytics',
    icon: AnalyticsIcon,
    children: [
      { id: 'reports', label: 'Reports', path: '/analytics/reports', icon: AssessmentIcon },
      { id: 'statistics', label: 'Statistics', path: '/analytics/statistics', icon: BarChartIcon }
    ]
  },
  {
    id: 'settings',
    label: 'Settings',
    icon: SettingsIcon,
    path: '/settings'
  }
];
```

---

## 2. Data Display Components

### DataTable with All Features
```typescript
// src/components/data/DataTable.tsx
import {
  Table, TableBody, TableCell, TableContainer, TableHead, TableRow,
  TablePagination, Checkbox, IconButton, Menu, MenuItem, Skeleton,
  Box, Typography
} from '@mui/material';
import { MoreVert as MoreVertIcon } from '@mui/icons-material';
import { useState } from 'react';

export interface Column<T> {
  key: string;
  header: string;
  accessor: (row: T) => React.ReactNode;
  sortable?: boolean;
  width?: string | number;
  align?: 'left' | 'center' | 'right';
}

export interface DataTableProps<T> {
  data: T[];
  columns: Column<T>[];
  keyExtractor: (row: T) => string;
  loading?: boolean;
  selectable?: boolean;
  pagination?: {
    page: number;
    pageSize: number;
    total: number;
    onPageChange: (page: number) => void;
    onPageSizeChange: (pageSize: number) => void;
  };
  onRowClick?: (row: T) => void;
  rowActions?: {
    label: string;
    icon?: React.ReactNode;
    onClick: (row: T) => void;
    danger?: boolean;
  }[];
}

export function DataTable<T>({
  data,
  columns,
  keyExtractor,
  loading = false,
  selectable = false,
  pagination,
  onRowClick,
  rowActions
}: DataTableProps<T>) {
  const [selected, setSelected] = useState<string[]>([]);
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [actionRow, setActionRow] = useState<T | null>(null);

  const handleSelectAll = (event: React.ChangeEvent<HTMLInputElement>) => {
    if (event.target.checked) {
      setSelected(data.map(keyExtractor));
    } else {
      setSelected([]);
    }
  };

  const handleSelect = (id: string) => {
    const selectedIndex = selected.indexOf(id);
    let newSelected: string[] = [];

    if (selectedIndex === -1) {
      newSelected = [...selected, id];
    } else {
      newSelected = selected.filter((item) => item !== id);
    }

    setSelected(newSelected);
  };

  if (loading) {
    return <TableSkeleton rows={5} columns={columns.length} />;
  }

  return (
    <Box>
      <TableContainer>
        <Table size="small">
          <TableHead>
            <TableRow>
              {selectable && (
                <TableCell padding="checkbox">
                  <Checkbox
                    indeterminate={selected.length > 0 && selected.length < data.length}
                    checked={data.length > 0 && selected.length === data.length}
                    onChange={handleSelectAll}
                  />
                </TableCell>
              )}
              {columns.map((col) => (
                <TableCell 
                  key={col.key}
                  align={col.align}
                  style={{ width: col.width }}
                >
                  {col.header}
                </TableCell>
              ))}
              {rowActions && <TableCell align="right">Actions</TableCell>}
            </TableRow>
          </TableHead>
          <TableBody>
            {data.map((row) => {
              const id = keyExtractor(row);
              const isSelected = selected.indexOf(id) !== -1;

              return (
                <TableRow
                  hover
                  key={id}
                  selected={isSelected}
                  onClick={() => onRowClick?.(row)}
                  sx={{ cursor: onRowClick ? 'pointer' : 'default' }}
                >
                  {selectable && (
                    <TableCell padding="checkbox" onClick={(e) => e.stopPropagation()}>
                      <Checkbox
                        checked={isSelected}
                        onChange={() => handleSelect(id)}
                      />
                    </TableCell>
                  )}
                  {columns.map((col) => (
                    <TableCell key={col.key} align={col.align}>
                      {col.accessor(row)}
                    </TableCell>
                  ))}
                  {rowActions && (
                    <TableCell align="right" onClick={(e) => e.stopPropagation()}>
                      <IconButton
                        onClick={(e) => {
                          setAnchorEl(e.currentTarget);
                          setActionRow(row);
                        }}
                      >
                        <MoreVertIcon />
                      </IconButton>
                    </TableCell>
                  )}
                </TableRow>
              );
            })}
          </TableBody>
        </Table>
      </TableContainer>

      {pagination && (
        <TablePagination
          component="div"
          count={pagination.total}
          page={pagination.page}
          onPageChange={(_, page) => pagination.onPageChange(page)}
          rowsPerPage={pagination.pageSize}
          onRowsPerPageChange={(e) => pagination.onPageSizeChange(parseInt(e.target.value, 10))}
          rowsPerPageOptions={[10, 25, 50, 100]}
        />
      )}

      {/* Row Actions Menu */}
      <Menu
        anchorEl={anchorEl}
        open={Boolean(anchorEl)}
        onClose={() => setAnchorEl(null)}
      >
        {rowActions?.map((action) => (
          <MenuItem
            key={action.label}
            onClick={() => {
              if (actionRow) action.onClick(actionRow);
              setAnchorEl(null);
            }}
            sx={action.danger ? { color: 'error.main' } : undefined}
          >
            {action.icon && <Box sx={{ mr: 1 }}>{action.icon}</Box>}
            {action.label}
          </MenuItem>
        ))}
      </Menu>
    </Box>
  );
}

// Skeleton loader
function TableSkeleton({ rows, columns }: { rows: number; columns: number }) {
  return (
    <TableContainer>
      <Table size="small">
        <TableHead>
          <TableRow>
            {Array.from({ length: columns + 1 }).map((_, i) => (
              <TableCell key={i}>
                <Skeleton width="80%" />
              </TableCell>
            ))}
          </TableRow>
        </TableHead>
        <TableBody>
          {Array.from({ length: rows }).map((_, rowIndex) => (
            <TableRow key={rowIndex}>
              {Array.from({ length: columns + 1 }).map((_, colIndex) => (
                <TableCell key={colIndex}>
                  <Skeleton />
                </TableCell>
              ))}
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  );
}
```

### Stats Card
```typescript
// src/components/data/StatsCard.tsx
import { Card, CardContent, Box, Typography, SvgIconProps } from '@mui/material';
import { TrendingUp, TrendingDown } from '@mui/icons-material';
import { LineChart, Line, ResponsiveContainer } from 'recharts';

interface StatsCardProps {
  title: string;
  value: string | number;
  change?: {
    value: number;
    isPositive: boolean;
  };
  icon: React.ComponentType<SvgIconProps>;
  color?: 'primary' | 'success' | 'warning' | 'error' | 'info';
  chartData?: number[];
}

export const StatsCard: React.FC<StatsCardProps> = ({
  title,
  value,
  change,
  icon: Icon,
  color = 'primary',
  chartData
}) => {
  const colors = {
    primary: { main: '#4A90D9', light: '#E3F2FD' },
    success: { main: '#43A047', light: '#E8F5E9' },
    warning: { main: '#FB8C00', light: '#FFF3E0' },
    error: { main: '#E53935', light: '#FFEBEE' },
    info: { main: '#2196F3', light: '#E3F2FD' }
  };

  const themeColor = colors[color];

  const sparklineData = chartData?.map((val, idx) => ({ idx, val })) || [];

  return (
    <Card sx={{ height: '100%', position: 'relative', overflow: 'hidden' }}>
      <CardContent>
        <Box sx={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between' }}>
          <Box>
            <Typography variant="body2" color="text.secondary" gutterBottom>
              {title}
            </Typography>
            <Typography variant="h4" component="div" fontWeight="600">
              {value}
            </Typography>
            {change && (
              <Box sx={{ display: 'flex', alignItems: 'center', mt: 1 }}>
                {change.isPositive ? (
                  <TrendingUp fontSize="small" color="success" />
                ) : (
                  <TrendingDown fontSize="small" color="error" />
                )}
                <Typography
                  variant="body2"
                  color={change.isPositive ? 'success.main' : 'error.main'}
                  sx={{ ml: 0.5 }}
                >
                  {change.isPositive ? '+' : ''}{change.value}%
                </Typography>
                <Typography variant="body2" color="text.secondary" sx={{ ml: 0.5 }}>
                  vs last month
                </Typography>
              </Box>
            )}
          </Box>
          <Box
            sx={{
              p: 1.5,
              borderRadius: 2,
              bgcolor: themeColor.light,
              color: themeColor.main
            }}
          >
            <Icon fontSize="large" />
          </Box>
        </Box>

        {chartData && (
          <Box sx={{ height: 60, mt: 2, ml: -2, mr: -2 }}>
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={sparklineData}>
                <Line
                  type="monotone"
                  dataKey="val"
                  stroke={themeColor.main}
                  strokeWidth={2}
                  dot={false}
                />
              </LineChart>
            </ResponsiveContainer>
          </Box>
        )}
      </CardContent>
    </Card>
  );
};
```

---

## 3. Form Components

### FormField with Validation
```typescript
// src/components/forms/FormField.tsx
import {
  TextField, TextFieldProps, FormHelperText, FormControl,
  InputLabel, Select, MenuItem, OutlinedInput
} from '@mui/material';
import { Controller, useFormContext } from 'react-hook-form';

interface FormFieldProps extends Omit<TextFieldProps, 'name'> {
  name: string;
  label: string;
  type?: 'text' | 'email' | 'password' | 'number' | 'select' | 'multiline' | 'date';
  options?: { value: string; label: string }[];
  validation?: object;
}

export const FormField: React.FC<FormFieldProps> = ({
  name,
  label,
  type = 'text',
  options,
  validation,
  ...props
}) => {
  const { control, formState: { errors } } = useFormContext();
  const error = errors[name];

  if (type === 'select' && options) {
    return (
      <Controller
        name={name}
        control={control}
        rules={validation}
        render={({ field }) => (
          <FormControl fullWidth error={!!error}>
            <InputLabel>{label}</InputLabel>
            <Select {...field} label={label}>
              {options.map((opt) => (
                <MenuItem key={opt.value} value={opt.value}>
                  {opt.label}
                </MenuItem>
              ))}
            </Select>
            {error && <FormHelperText>{error.message as string}</FormHelperText>}
          </FormControl>
        )}
      />
    );
  }

  return (
    <Controller
      name={name}
      control={control}
      rules={validation}
      render={({ field }) => (
        <TextField
          {...field}
          {...props}
          label={label}
          type={type}
          fullWidth
          error={!!error}
          helperText={error ? (error.message as string) : props.helperText}
          multiline={type === 'multiline'}
          rows={type === 'multiline' ? 4 : undefined}
        />
      )}
    />
  );
};
```

### Image Uploader
```typescript
// src/components/forms/ImageUploader.tsx
import { Box, Button, Typography, IconButton } from '@mui/material';
import { CloudUpload, Delete, Image as ImageIcon } from '@mui/icons-material';
import { useDropzone } from 'react-dropzone';
import { useState, useCallback } from 'react';

interface ImageUploaderProps {
  value?: File | string;
  onChange: (file: File | null) => void;
  accept?: string;
  maxSize?: number; // MB
  label?: string;
}

export const ImageUploader: React.FC<ImageUploaderProps> = ({
  value,
  onChange,
  accept = 'image/*',
  maxSize = 5,
  label = 'Upload Image'
}) => {
  const [preview, setPreview] = useState<string | null>(
    typeof value === 'string' ? value : null
  );

  const onDrop = useCallback((acceptedFiles: File[]) => {
    const file = acceptedFiles[0];
    if (file) {
      onChange(file);
      const reader = new FileReader();
      reader.onloadend = () => {
        setPreview(reader.result as string);
      };
      reader.readAsDataURL(file);
    }
  }, [onChange]);

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    accept: { [accept]: [] },
    maxSize: maxSize * 1024 * 1024,
    multiple: false
  });

  const handleRemove = () => {
    onChange(null);
    setPreview(null);
  };

  if (preview) {
    return (
      <Box sx={{ position: 'relative', display: 'inline-block' }}>
        <img
          src={preview}
          alt="Preview"
          style={{
            width: 200,
            height: 200,
            objectFit: 'cover',
            borderRadius: 8
          }}
        />
        <IconButton
          onClick={handleRemove}
          sx={{
            position: 'absolute',
            top: -10,
            right: -10,
            bgcolor: 'error.main',
            color: 'white',
            '&:hover': { bgcolor: 'error.dark' }
          }}
          size="small"
        >
          <Delete />
        </IconButton>
      </Box>
    );
  }

  return (
    <Box
      {...getRootProps()}
      sx={{
        border: '2px dashed',
        borderColor: isDragActive ? 'primary.main' : 'grey.300',
        borderRadius: 2,
        p: 4,
        textAlign: 'center',
        cursor: 'pointer',
        bgcolor: isDragActive ? 'primary.light' : 'grey.50',
        transition: 'all 0.2s',
        '&:hover': {
          borderColor: 'primary.main',
          bgcolor: 'primary.light'
        }
      }}
    >
      <input {...getInputProps()} />
      <CloudUpload sx={{ fontSize: 48, color: 'primary.main', mb: 1 }} />
      <Typography variant="h6" gutterBottom>
        {isDragActive ? 'Drop the image here' : label}
      </Typography>
      <Typography variant="body2" color="text.secondary">
        Drag & drop or click to select
      </Typography>
      <Typography variant="caption" color="text.secondary" display="block" sx={{ mt: 1 }}>
        Max size: {maxSize}MB
      </Typography>
    </Box>
  );
};
```

---

## 4. Screen Templates

### Dashboard Screen
```typescript
// src/screens/Dashboard.tsx
import { Grid, Box } from '@mui/material';
import {
  People as PeopleIcon,
  Quiz as QuizIcon,
  TrendingUp as TrendingUpIcon,
  AccessTime as AccessTimeIcon
} from '@mui/icons-material';
import { StatsCard } from '../components/data/StatsCard';
import { useQuery } from '@tanstack/react-query';

export const Dashboard: React.FC = () => {
  const { data: stats } = useQuery({
    queryKey: ['dashboardStats'],
    queryFn: fetchDashboardStats
  });

  return (
    <Box>
      <Grid container spacing={3}>
        <Grid item xs={12} sm={6} md={3}>
          <StatsCard
            title="Total Users"
            value={stats?.users || 0}
            change={{ value: 12.5, isPositive: true }}
            icon={PeopleIcon}
            color="primary"
            chartData={[65, 78, 90, 81, 96, 105, 120]}
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatsCard
            title="Total Tests"
            value={stats?.tests || 0}
            change={{ value: 8.2, isPositive: true }}
            icon={QuizIcon}
            color="success"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatsCard
            title="Completion Rate"
            value={`${stats?.completionRate || 0}%`}
            change={{ value: 3.1, isPositive: false }}
            icon={TrendingUpIcon}
            color="warning"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatsCard
            title="Avg. Session"
            value={`${stats?.avgSession || 0}m`}
            change={{ value: 15.3, isPositive: true }}
            icon={AccessTimeIcon}
            color="info"
          />
        </Grid>
      </Grid>

      {/* Add charts and recent activity here */}
    </Box>
  );
};
```

---

## 5. Theme Configuration

### Complete Theme Setup
```typescript
// src/theme/Theme.ts
import { createTheme, ThemeOptions } from '@mui/material';

export const themeOptions: ThemeOptions = {
  palette: {
    primary: {
      main: '#4A90D9',
      light: '#6BA5E7',
      dark: '#1E5AA8',
      contrastText: '#FFFFFF'
    },
    secondary: {
      main: '#9C27B0',
      light: '#BA68C8',
      dark: '#7B1FA2',
      contrastText: '#FFFFFF'
    },
    success: {
      main: '#43A047',
      light: '#66BB6A',
      dark: '#2E7D32'
    },
    error: {
      main: '#E53935',
      light: '#FF897D',
      dark: '#C62828'
    },
    warning: {
      main: '#FB8C00',
      light: '#FFB74D',
      dark: '#EF6C00'
    },
    info: {
      main: '#2196F3',
      light: '#90CAF9',
      dark: '#1565C0'
    },
    background: {
      default: '#F5F5F5',
      paper: '#FFFFFF'
    },
    text: {
      primary: '#212121',
      secondary: '#757575'
    }
  },
  typography: {
    fontFamily: '"Inter", "Roboto", "Helvetica", "Arial", sans-serif',
    h1: { fontSize: '2.5rem', fontWeight: 600, lineHeight: 1.2 },
    h2: { fontSize: '2rem', fontWeight: 600, lineHeight: 1.3 },
    h3: { fontSize: '1.5rem', fontWeight: 600, lineHeight: 1.4 },
    h4: { fontSize: '1.25rem', fontWeight: 600, lineHeight: 1.4 },
    h5: { fontSize: '1.125rem', fontWeight: 500, lineHeight: 1.5 },
    h6: { fontSize: '1rem', fontWeight: 500, lineHeight: 1.5 },
    body1: { fontSize: '1rem', lineHeight: 1.5 },
    body2: { fontSize: '0.875rem', lineHeight: 1.5 },
    button: { textTransform: 'none', fontWeight: 500 },
    caption: { fontSize: '0.75rem', lineHeight: 1.5 }
  },
  shape: {
    borderRadius: 8
  },
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          borderRadius: 8,
          padding: '8px 16px',
          fontWeight: 500
        },
        contained: {
          boxShadow: 'none',
          '&:hover': {
            boxShadow: '0 2px 8px rgba(0,0,0,0.15)'
          }
        }
      }
    },
    MuiCard: {
      styleOverrides: {
        root: {
          borderRadius: 12,
          boxShadow: '0 2px 8px rgba(0,0,0,0.08)'
        }
      }
    },
    MuiTableCell: {
      styleOverrides: {
        root: {
          borderBottom: '1px solid #E0E0E0'
        },
        head: {
          fontWeight: 600,
          backgroundColor: '#F5F5F5'
        }
      }
    }
  }
};

export const theme = createTheme(themeOptions);
```

---

## Implementation Checklist

### Phase 1: Foundation
- [ ] Theme configuration (colors, typography, spacing)
- [ ] Layout components (AdminLayout, Header, Sidebar)
- [ ] Navigation setup with react-router
- [ ] Breadcrumb component

### Phase 2: Data Display
- [ ] DataTable with pagination, sorting, selection
- [ ] StatsCard component
- [ ] StatusBadge component
- [ ] Skeleton loaders

### Phase 3: Forms
- [ ] FormField wrapper for react-hook-form
- [ ] ImageUploader with drag-drop
- [ ] Rich text editor integration
- [ ] Validation schema with Zod

### Phase 4: Content Management
- [ ] CategoryTree with drag-drop
- [ ] TestList component
- [ ] QuestionBuilder form
- [ ] Question type components

### Phase 5: User Management
- [ ] UserTable with filters
- [ ] UserCard component
- [ ] PermissionEditor
- [ ] Group management

### Phase 6: Analytics
- [ ] Chart components (recharts)
- [ ] Report builder
- [ ] Export functionality

### Phase 7: Polish
- [ ] Toast notifications
- [ ] Confirm dialogs
- [ ] Loading states
- [ ] Error boundaries
- [ ] Empty states

---

**Estimated Lines of Code:** 7,500+
**Estimated Files:** 34
**Estimated Time:** 40-50 hours
