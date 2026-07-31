import React, { useState, useMemo } from 'react';
import {
  DndContext,
  closestCenter,
  KeyboardSensor,
  PointerSensor,
  useSensor,
  useSensors,
  DragEndEvent,
  DragOverlay,
  DragStartEvent,
  defaultDropAnimationSideEffects,
  DropAnimation,
} from '@dnd-kit/core';
import {
  SortableContext,
  sortableKeyboardCoordinates,
  verticalListSortingStrategy,
  useSortable,
} from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import {
  Box,
  Paper,
  Typography,
  IconButton,
  Chip,
  LinearProgress,
  Tooltip,
  Menu,
  MenuItem,
  TextField,
  InputAdornment,
  FormControl,
  InputLabel,
  Select,
  SelectChangeEvent,
} from '@mui/material';
import {
  DragIndicator,
  Edit,
  Delete,
  Visibility,
  MoreVert,
  Search,
  FilterList,
} from '@mui/icons-material';

// Design System Colors
const colors = {
  primary: '#4A90D9',
  success: '#43A047',
  error: '#E53935',
  warning: '#FB8C00',
  info: '#2196F3',
  background: '#F5F5F5',
  card: '#FFFFFF',
  textPrimary: '#212121',
  textSecondary: '#757575',
  sidebar: '#1a237e',
};

// Types
export type TestStatus = 'draft' | 'published' | 'archived';

export interface Test {
  id: string;
  title: string;
  description?: string;
  status: TestStatus;
  categoryId: string;
  categoryName?: string;
  questionCount: number;
  totalPoints: number;
  timeLimit?: number; // in minutes
  passingScore?: number;
  attemptsCount?: number;
  completionRate?: number; // percentage
  createdAt: string;
  updatedAt: string;
  order: number;
}

interface TestListProps {
  tests: Test[];
  categories: { id: string; name: string }[];
  onReorder: (tests: Test[]) => void;
  onEdit: (testId: string) => void;
  onDelete: (testId: string) => void;
  onPreview: (testId: string) => void;
  onDuplicate?: (testId: string) => void;
  onStatusChange?: (testId: string, status: TestStatus) => void;
}

interface SortableTestItemProps {
  test: Test;
  onEdit: (testId: string) => void;
  onDelete: (testId: string) => void;
  onPreview: (testId: string) => void;
  onDuplicate?: (testId: string) => void;
  onStatusChange?: (testId: string, status: TestStatus) => void;
}

// Status configuration
const statusConfig: Record<TestStatus, { label: string; color: string; bgColor: string }> = {
  draft: {
    label: 'Draft',
    color: colors.textSecondary,
    bgColor: '#E0E0E0',
  },
  published: {
    label: 'Published',
    color: colors.success,
    bgColor: '#E8F5E9',
  },
  archived: {
    label: 'Archived',
    color: colors.error,
    bgColor: '#FFEBEE',
  },
};

// Sortable Test Item Component
const SortableTestItem: React.FC<SortableTestItemProps> = ({
  test,
  onEdit,
  onDelete,
  onPreview,
  onDuplicate,
  onStatusChange,
}) => {
  const [menuAnchorEl, setMenuAnchorEl] = useState<null | HTMLElement>(null);

  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
    isDragging,
  } = useSortable({ id: test.id });

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
    opacity: isDragging ? 0.5 : 1,
  };

  const status = statusConfig[test.status];
  const menuOpen = Boolean(menuAnchorEl);

  const handleMenuOpen = (event: React.MouseEvent<HTMLElement>) => {
    setMenuAnchorEl(event.currentTarget);
  };

  const handleMenuClose = () => {
    setMenuAnchorEl(null);
  };

  const handleStatusChange = (newStatus: TestStatus) => {
    onStatusChange?.(test.id, newStatus);
    handleMenuClose();
  };

  const formatDuration = (minutes?: number) => {
    if (!minutes) return 'No limit';
    if (minutes < 60) return `${minutes} min`;
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    return mins > 0 ? `${hours}h ${mins}m` : `${hours}h`;
  };

  return (
    <Paper
      ref={setNodeRef}
      style={style}
      sx={{
        display: 'flex',
        alignItems: 'center',
        padding: '12px 16px',
        marginBottom: '8px',
        backgroundColor: colors.card,
        border: `1px solid ${isDragging ? colors.primary : '#e0e0e0'}`,
        borderRadius: '12px',
        '&:hover': {
          boxShadow: '0 2px 12px rgba(0,0,0,0.1)',
        },
      }}
    >
      {/* Drag Handle */}
      <Box
        {...attributes}
        {...listeners}
        sx={{
          cursor: 'grab',
          display: 'flex',
          alignItems: 'center',
          color: colors.textSecondary,
          mr: 1,
          '&:active': { cursor: 'grabbing' },
        }}
      >
        <DragIndicator />
      </Box>

      {/* Test Info */}
      <Box sx={{ flex: 1, minWidth: 0, mr: 2 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 0.5 }}>
          <Typography
            variant="subtitle1"
            sx={{
              color: colors.textPrimary,
              fontWeight: 600,
              overflow: 'hidden',
              textOverflow: 'ellipsis',
              whiteSpace: 'nowrap',
            }}
          >
            {test.title}
          </Typography>
          <Chip
            size="small"
            label={status.label}
            sx={{
              backgroundColor: status.bgColor,
              color: status.color,
              fontWeight: 500,
              fontSize: '0.75rem',
            }}
          />
        </Box>

        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, flexWrap: 'wrap' }}>
          {test.categoryName && (
            <Typography variant="caption" sx={{ color: colors.textSecondary }}>
              {test.categoryName}
            </Typography>
          )}
          <Typography variant="caption" sx={{ color: colors.textSecondary }}>
            {test.questionCount} questions
          </Typography>
          <Typography variant="caption" sx={{ color: colors.textSecondary }}>
            {test.totalPoints} points
          </Typography>
          <Typography variant="caption" sx={{ color: colors.textSecondary }}>
            {formatDuration(test.timeLimit)}
          </Typography>
          {test.passingScore !== undefined && (
            <Typography variant="caption" sx={{ color: colors.textSecondary }}>
              Pass: {test.passingScore}%
            </Typography>
          )}
        </Box>
      </Box>

      {/* Progress Indicator */}
      {test.completionRate !== undefined && (
        <Box sx={{ width: 120, mr: 2 }}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 0.5 }}>
            <Typography variant="caption" sx={{ color: colors.textSecondary }}>
              Completion
            </Typography>
            <Typography variant="caption" sx={{ color: colors.primary, fontWeight: 500 }}>
              {test.completionRate}%
            </Typography>
          </Box>
          <LinearProgress
            variant="determinate"
            value={test.completionRate}
            sx={{
              height: 6,
              borderRadius: 3,
              backgroundColor: colors.background,
              '& .MuiLinearProgress-bar': {
                backgroundColor: colors.primary,
                borderRadius: 3,
              },
            }}
          />
        </Box>
      )}

      {/* Attempts Count */}
      {test.attemptsCount !== undefined && (
        <Box sx={{ mr: 2, textAlign: 'center' }}>
          <Typography variant="h6" sx={{ color: colors.primary, fontWeight: 600, lineHeight: 1 }}>
            {test.attemptsCount}
          </Typography>
          <Typography variant="caption" sx={{ color: colors.textSecondary }}>
            attempts
          </Typography>
        </Box>
      )}

      {/* Quick Actions */}
      <Box sx={{ display: 'flex', gap: 0.5 }}>
        <Tooltip title="Preview">
          <IconButton size="small" onClick={() => onPreview(test.id)} sx={{ color: colors.info }}>
            <Visibility fontSize="small" />
          </IconButton>
        </Tooltip>
        <Tooltip title="Edit">
          <IconButton size="small" onClick={() => onEdit(test.id)} sx={{ color: colors.primary }}>
            <Edit fontSize="small" />
          </IconButton>
        </Tooltip>
        <Tooltip title="More actions">
          <IconButton size="small" onClick={handleMenuOpen}>
            <MoreVert fontSize="small" />
          </IconButton>
        </Tooltip>
      </Box>

      {/* Actions Menu */}
      <Menu
        anchorEl={menuAnchorEl}
        open={menuOpen}
        onClose={handleMenuClose}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
        transformOrigin={{ vertical: 'top', horizontal: 'right' }}
      >
        {test.status !== 'published' && (
          <MenuItem onClick={() => handleStatusChange('published')}>
            Publish
          </MenuItem>
        )}
        {test.status !== 'draft' && (
          <MenuItem onClick={() => handleStatusChange('draft')}>
            Move to Draft
          </MenuItem>
        )}
        {test.status !== 'archived' && (
          <MenuItem onClick={() => handleStatusChange('archived')}>
            Archive
          </MenuItem>
        )}
        {onDuplicate && (
          <MenuItem onClick={() => { onDuplicate(test.id); handleMenuClose(); }}>
            Duplicate
          </MenuItem>
        )}
        <MenuItem onClick={() => { onDelete(test.id); handleMenuClose(); }} sx={{ color: colors.error }}>
          Delete
        </MenuItem>
      </Menu>
    </Paper>
  );
};

// Main Test List Component
const TestList: React.FC<TestListProps> = ({
  tests,
  categories,
  onReorder,
  onEdit,
  onDelete,
  onPreview,
  onDuplicate,
  onStatusChange,
}) => {
  const [activeId, setActiveId] = useState<string | null>(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState<TestStatus | 'all'>('all');
  const [categoryFilter, setCategoryFilter] = useState<string>('all');

  const sensors = useSensors(
    useSensor(PointerSensor, {
      activationConstraint: {
        distance: 8,
      },
    }),
    useSensor(KeyboardSensor, {
      coordinateGetter: sortableKeyboardCoordinates,
    })
  );

  // Filter tests
  const filteredTests = useMemo(() => {
    return tests.filter((test) => {
      const matchesSearch = test.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
        (test.description?.toLowerCase().includes(searchQuery.toLowerCase()) ?? false);
      const matchesStatus = statusFilter === 'all' || test.status === statusFilter;
      const matchesCategory = categoryFilter === 'all' || test.categoryId === categoryFilter;
      return matchesSearch && matchesStatus && matchesCategory;
    });
  }, [tests, searchQuery, statusFilter, categoryFilter]);

  const handleDragStart = (event: DragStartEvent) => {
    setActiveId(event.active.id as string);
  };

  const handleDragEnd = (event: DragEndEvent) => {
    const { active, over } = event;
    setActiveId(null);

    if (over && active.id !== over.id) {
      const oldIndex = filteredTests.findIndex((t) => t.id === active.id);
      const newIndex = filteredTests.findIndex((t) => t.id === over.id);

      if (oldIndex !== -1 && newIndex !== -1) {
        const newTests = [...filteredTests];
        const [movedItem] = newTests.splice(oldIndex, 1);
        newTests.splice(newIndex, 0, movedItem);

        // Update order values
        const updatedTests = newTests.map((test, index) => ({
          ...test,
          order: index,
        }));

        onReorder(updatedTests);
      }
    }
  };

  const dropAnimation: DropAnimation = {
    sideEffects: defaultDropAnimationSideEffects({
      styles: {
        active: {
          opacity: '0.5',
        },
      },
    }),
  };

  const activeTest = activeId ? tests.find((t) => t.id === activeId) : null;

  return (
    <Box sx={{ width: '100%' }}>
      {/* Header with Filters */}
      <Box sx={{ mb: 3 }}>
        <Typography variant="h6" sx={{ color: colors.textPrimary, fontWeight: 600, mb: 2 }}>
          Tests
        </Typography>

        <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
          {/* Search */}
          <TextField
            size="small"
            placeholder="Search tests..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <Search fontSize="small" sx={{ color: colors.textSecondary }} />
                </InputAdornment>
              ),
            }}
            sx={{ flex: 1, minWidth: 200 }}
          />

          {/* Status Filter */}
          <FormControl size="small" sx={{ minWidth: 120 }}>
            <InputLabel>Status</InputLabel>
            <Select
              value={statusFilter}
              label="Status"
              onChange={(e: SelectChangeEvent) => setStatusFilter(e.target.value as TestStatus | 'all')}
            >
              <MenuItem value="all">All</MenuItem>
              <MenuItem value="draft">Draft</MenuItem>
              <MenuItem value="published">Published</MenuItem>
              <MenuItem value="archived">Archived</MenuItem>
            </Select>
          </FormControl>

          {/* Category Filter */}
          <FormControl size="small" sx={{ minWidth: 150 }}>
            <InputLabel>Category</InputLabel>
            <Select
              value={categoryFilter}
              label="Category"
              onChange={(e: SelectChangeEvent) => setCategoryFilter(e.target.value)}
            >
              <MenuItem value="all">All Categories</MenuItem>
              {categories.map((cat) => (
                <MenuItem key={cat.id} value={cat.id}>
                  {cat.name}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        </Box>
      </Box>

      {/* Results Count */}
      <Typography variant="body2" sx={{ color: colors.textSecondary, mb: 2 }}>
        Showing {filteredTests.length} of {tests.length} tests
      </Typography>

      {/* Drag and Drop Context */}
      <DndContext
        sensors={sensors}
        collisionDetection={closestCenter}
        onDragStart={handleDragStart}
        onDragEnd={handleDragEnd}
      >
        <SortableContext
          items={filteredTests.map((t) => t.id)}
          strategy={verticalListSortingStrategy}
        >
          <Box sx={{ display: 'flex', flexDirection: 'column' }}>
            {filteredTests.map((test) => (
              <SortableTestItem
                key={test.id}
                test={test}
                onEdit={onEdit}
                onDelete={onDelete}
                onPreview={onPreview}
                onDuplicate={onDuplicate}
                onStatusChange={onStatusChange}
              />
            ))}
          </Box>
        </SortableContext>

        {/* Drag Overlay */}
        <DragOverlay dropAnimation={dropAnimation}>
          {activeTest ? (
            <Paper
              sx={{
                display: 'flex',
                alignItems: 'center',
                padding: '12px 16px',
                backgroundColor: colors.card,
                border: `2px solid ${colors.primary}`,
                borderRadius: '12px',
                boxShadow: '0 4px 12px rgba(0,0,0,0.15)',
              }}
            >
              <DragIndicator sx={{ mr: 1 }} />
              <Typography sx={{ fontWeight: 600 }}>{activeTest.title}</Typography>
            </Paper>
          ) : null}
        </DragOverlay>
      </DndContext>

      {/* Empty State */}
      {filteredTests.length === 0 && (
        <Paper
          sx={{
            padding: 4,
            textAlign: 'center',
            backgroundColor: colors.background,
            border: '2px dashed #ccc',
          }}
        >
          <FilterList sx={{ fontSize: 48, color: colors.textSecondary, mb: 2 }} />
          <Typography variant="h6" sx={{ color: colors.textSecondary, mb: 1 }}>
            No tests found
          </Typography>
          <Typography variant="body2" sx={{ color: colors.textSecondary }}>
            Try adjusting your filters or create a new test
          </Typography>
        </Paper>
      )}
    </Box>
  );
};

export default TestList;
