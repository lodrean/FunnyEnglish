import React, { useState, useCallback } from 'react';
import {
  Box,
  Typography,
  Button,
  TextField,
  Paper,
  MenuItem,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  FormControl,
  InputLabel,
  Select,
  Alert,
  InputAdornment,
  Avatar,
  useTheme,
} from '@mui/material';
import {
  Add as AddIcon,
  Search as SearchIcon,

} from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { DataTable, ColumnDef, StatusBadge } from '@components/data';
import { getAdminTests, deleteTest, getCategories } from '../api/client';
import type { Test as ApiTest, Category } from '../types';


// Types for Tests List (mapped from API)
interface TestListItem {
  id: string;
  title: string;
  category: string;
  categoryId: string;
  difficulty: 'EASY' | 'MEDIUM' | 'HARD';
  status: 'draft' | 'published' | 'archived';
  questionCount: number;
  timeLimit: number;
  passingScore: number;
  attempts: number;
  completions: number;
  avgScore: number;
  createdAt: string;
  updatedAt: string;
  createdBy: string;
  imageUrl?: string;
  isPublished: boolean;
}

interface TestFilters {
  search: string;
  category: string;
  difficulty: string;
  status: string;
}

// Fetch tests with category mapping
const fetchTests = async (): Promise<TestListItem[]> => {
  const [tests, categories] = await Promise.all([
    getAdminTests(),
    getCategories(),
  ]);

  // Create category map for lookup
  const categoryMap = new Map(categories.map((c: Category) => [c.id, c.name]));

  return tests.map((test: ApiTest) => ({
    id: test.id,
    title: test.title,
    category: categoryMap.get(test.categoryId) || 'Uncategorized',
    categoryId: test.categoryId,
    difficulty: test.difficulty,
    status: test.isPublished ? 'published' : 'draft',
    questionCount: test.questions?.length || 0,
    timeLimit: (test.timeLimitSeconds || 1800) / 60, // Convert to minutes
    passingScore: 70, // Default
    attempts: 0, // Not available in API yet
    completions: 0, // Not available in API yet
    avgScore: 0, // Not available in API yet
    createdAt: new Date().toISOString(), // Not available in API yet
    updatedAt: new Date().toISOString(), // Not available in API yet
    createdBy: 'Admin',
    imageUrl: test.thumbnailUrl,
    isPublished: test.isPublished,
  }));
};



// Difficulty colors
const getDifficultyColor = (difficulty: TestListItem['difficulty']) => {
  switch (difficulty) {
    case 'EASY':
      return 'success';
    case 'MEDIUM':
      return 'warning';
    case 'HARD':
      return 'error';
    default:
      return 'default';
  }
};

// Status mapping
const getStatusVariant = (status: TestListItem['status']) => {
  switch (status) {
    case 'published':
      return 'success';
    case 'draft':
      return 'warning';
    case 'archived':
      return 'inactive';
    default:
      return 'default';
  }
};

// Score badge color
const getScoreVariant = (score: number) => {
  if (score >= 80) return 'success';
  if (score >= 60) return 'warning';
  return 'error';
};

const Tests: React.FC = () => {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const theme = useTheme();
  
  const [filters, setFilters] = useState<TestFilters>({
    search: '',
    category: '',
    difficulty: '',
    status: '',
  });
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [selectedTest, setSelectedTest] = useState<TestListItem | null>(null);

  const { data: tests = [], isLoading, error } = useQuery({
    queryKey: ['tests'],
    queryFn: fetchTests,
  });

  const deleteMutation = useMutation({
    mutationFn: async (id: string) => { await deleteTest(id); },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tests'] });
      setDeleteDialogOpen(false);
      setSelectedTest(null);
    },
  });

  // Filter tests
  const filteredTests = tests.filter((test) => {
    const matchesSearch =
      !filters.search ||
      test.title.toLowerCase().includes(filters.search.toLowerCase()) ||
      test.category.toLowerCase().includes(filters.search.toLowerCase());
    const matchesCategory = !filters.category || test.categoryId === filters.category;
    const matchesDifficulty = !filters.difficulty || test.difficulty === filters.difficulty.toUpperCase();
    const matchesStatus = !filters.status || test.status === filters.status;
    return matchesSearch && matchesCategory && matchesDifficulty && matchesStatus;
  });

  const handleDelete = useCallback(() => {
    if (selectedTest) {
      deleteMutation.mutate(selectedTest.id);
    }
  }, [selectedTest, deleteMutation]);

  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const _handlePreview = useCallback((testId: string) => {
    window.open(`/test/${testId}/preview`, '_blank');
  }, []);

  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const _handleDuplicate = useCallback((test: TestListItem) => {
    console.log('Duplicate test:', test.id);
  }, []);

  // Table columns definition
  const columns: ColumnDef<TestListItem>[] = [
    {
      key: 'title',
      header: 'Test',
      accessor: (test: TestListItem) => (
        <Box display="flex" alignItems="center" gap={1.5}>
          <Avatar
            sx={{
              width: 32,
              height: 32,
              backgroundColor: `${theme.palette.primary.main}20`,
              color: theme.palette.primary.main,
              fontSize: '0.875rem',
            }}
          >
            {test.title.charAt(0)}
          </Avatar>
          <Box>
            <Typography variant="body2" fontWeight={500}>
              {test.title}
            </Typography>
            <Typography variant="caption" color="text.secondary">
              Updated {new Date(test.updatedAt).toLocaleDateString()}
            </Typography>
          </Box>
        </Box>
      ),
      sortable: true,
      width: '25%',
    },
    {
      key: 'category',
      header: 'Category',
      accessor: (test: TestListItem) => test.category,
      sortable: true,
      width: '15%',
    },
    {
      key: 'difficulty',
      header: 'Difficulty',
      accessor: (test: TestListItem) => (
        <StatusBadge
          status={getDifficultyColor(test.difficulty) as any}
          label={test.difficulty.charAt(0) + test.difficulty.slice(1).toLowerCase()}
          size="small"
        />
      ),
      sortable: true,
      width: '10%',
    },
    {
      key: 'status',
      header: 'Status',
      accessor: (test: TestListItem) => (
        <StatusBadge
          status={getStatusVariant(test.status) as any}
          label={test.status.charAt(0).toUpperCase() + test.status.slice(1)}
          size="small"
        />
      ),
      sortable: true,
      width: '10%',
    },
    {
      key: 'questions',
      header: 'Questions',
      accessor: (test: TestListItem) => test.questionCount,
      align: 'center',
      width: '8%',
    },
    {
      key: 'timeLimit',
      header: 'Time',
      accessor: (test: TestListItem) => `${test.timeLimit}m`,
      align: 'center',
      width: '8%',
    },
    {
      key: 'attempts',
      header: 'Attempts',
      accessor: (test: TestListItem) => test.attempts.toLocaleString(),
      align: 'center',
      width: '10%',
    },
    {
      key: 'avgScore',
      header: 'Avg Score',
      accessor: (test: TestListItem) =>
        test.avgScore > 0 ? (
          <StatusBadge
            status={getScoreVariant(test.avgScore) as any}
            label={`${test.avgScore}%`}
            size="small"
          />
        ) : (
          '-'
        ),
      align: 'center',
      width: '10%',
    },
  ];

  if (error) {
    return (
      <Box p={3}>
        <Alert severity="error">Failed to load tests. Please try again.</Alert>
      </Box>
    );
  }

  return (
    <Box p={{ xs: 3, md: 4 }}>
      {/* Header */}
      <Box display="flex" flexDirection={{ xs: 'column', sm: 'row' }} justifyContent="space-between" alignItems={{ xs: 'flex-start', sm: 'center' }} mb={3} gap={2}>
        <Typography variant="h4" fontWeight="bold" color={theme.palette.text.primary} data-testid="page-title">
          Tests
        </Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => navigate('/content/tests/new')}
          data-testid="add-test-button"
          sx={{ backgroundColor: theme.palette.primary.main }}
        >
          Add Test
        </Button>
      </Box>

      {/* Filters */}
      <Paper sx={{ p: 2, mb: 3 }}>
        <Box display="flex" flexDirection={{ xs: 'column', md: 'row' }} gap={2} alignItems={{ xs: 'stretch', md: 'center' }}>
          <TextField
            placeholder="Search tests..."
            value={filters.search}
            onChange={(e) => setFilters({ ...filters, search: e.target.value })}
            size="small"
            data-testid="search-tests"
            sx={{ flex: 1 }}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <SearchIcon fontSize="small" />
                </InputAdornment>
              ),
            }}
          />
          <FormControl size="small" sx={{ minWidth: 150 }}>
            <InputLabel>Category</InputLabel>
            <Select
              value={filters.category}
              label="Category"
              onChange={(e) => setFilters({ ...filters, category: e.target.value })}
            >
              <MenuItem value="">All Categories</MenuItem>
              <MenuItem value="1">Grammar</MenuItem>
              <MenuItem value="2">Vocabulary</MenuItem>
              <MenuItem value="3">Listening</MenuItem>
              <MenuItem value="4">Reading</MenuItem>
              <MenuItem value="5">Writing</MenuItem>
            </Select>
          </FormControl>
          <FormControl size="small" sx={{ minWidth: 120 }}>
            <InputLabel>Difficulty</InputLabel>
            <Select
              value={filters.difficulty}
              label="Difficulty"
              onChange={(e) => setFilters({ ...filters, difficulty: e.target.value })}
            >
              <MenuItem value="">All</MenuItem>
              <MenuItem value="EASY">Easy</MenuItem>
              <MenuItem value="MEDIUM">Medium</MenuItem>
              <MenuItem value="HARD">Hard</MenuItem>
            </Select>
          </FormControl>
          <FormControl size="small" sx={{ minWidth: 120 }}>
            <InputLabel>Status</InputLabel>
            <Select
              value={filters.status}
              label="Status"
              onChange={(e) => setFilters({ ...filters, status: e.target.value })}
            >
              <MenuItem value="">All</MenuItem>
              <MenuItem value="published">Published</MenuItem>
              <MenuItem value="draft">Draft</MenuItem>
              <MenuItem value="archived">Archived</MenuItem>
            </Select>
          </FormControl>
        </Box>
      </Paper>

      {/* Data Table with Design System */}
      <DataTable
        columns={columns}
        data={filteredTests}
        keyExtractor={(test) => test.id}
        loading={isLoading}
        onRowClick={(test) => navigate(`/tests/${test.id}/edit`)}
        onEdit={(test) => navigate(`/tests/${test.id}/edit`)}
        onDelete={(test) => {
          setSelectedTest(test);
          setDeleteDialogOpen(true);
        }}
        emptyMessage="No tests found"
        defaultRowsPerPage={10}
        rowsPerPageOptions={[5, 10, 25, 50]}
        stickyHeader
      />

      {/* Delete Confirmation Dialog */}
      <Dialog open={deleteDialogOpen} onClose={() => setDeleteDialogOpen(false)}>
        <DialogTitle>Delete Test</DialogTitle>
        <DialogContent>
          <Typography>
            Are you sure you want to delete &quot;{selectedTest?.title}&quot;?
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
            This action cannot be undone. All test data will be permanently removed.
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteDialogOpen(false)}>Cancel</Button>
          <Button
            onClick={handleDelete}
            variant="contained"
            color="error"
            disabled={deleteMutation.isPending}
          >
            {deleteMutation.isPending ? 'Deleting...' : 'Delete'}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default Tests;
