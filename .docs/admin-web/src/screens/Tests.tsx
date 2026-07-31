import React, { useState } from 'react';
import {
  Box,
  Typography,
  Button,
  TextField,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TablePagination,
  Chip,
  IconButton,
  Menu,
  MenuItem,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  FormControl,
  InputLabel,
  Select,
  Skeleton,
  Alert,
  Tooltip,
  InputAdornment,
  Avatar,
} from '@mui/material';
import {
  Add as AddIcon,
  Search as SearchIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  Visibility as VisibilityIcon,
  MoreVert as MoreVertIcon,
  FilterList as FilterListIcon,
  Sort as SortIcon,
  PlayArrow as PlayArrowIcon,
  ContentCopy as ContentCopyIcon,
} from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';

// Design System Colors
const COLORS = {
  primary: '#4A90D9',
  success: '#43A047',
  error: '#E53935',
  warning: '#FB8C00',
  info: '#2196F3',
  background: '#F5F5F5',
  card: '#FFFFFF',
  textPrimary: '#212121',
  textSecondary: '#757575',
};

// Types
interface Test {
  id: string;
  title: string;
  category: string;
  categoryId: string;
  difficulty: 'easy' | 'medium' | 'hard';
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
}

interface TestFilters {
  search: string;
  category: string;
  difficulty: string;
  status: string;
}

// Mock API
const fetchTests = async (): Promise<Test[]> => {
  await new Promise((resolve) => setTimeout(resolve, 700));
  
  return [
    {
      id: '1',
      title: 'Basic Grammar - Present Simple',
      category: 'Grammar > Tenses',
      categoryId: '1-1-1',
      difficulty: 'easy',
      status: 'published',
      questionCount: 20,
      timeLimit: 15,
      passingScore: 70,
      attempts: 1250,
      completions: 980,
      avgScore: 78.5,
      createdAt: '2024-01-01',
      updatedAt: '2024-01-10',
      createdBy: 'Admin',
    },
    {
      id: '2',
      title: 'Business English Vocabulary',
      category: 'Vocabulary > Business',
      categoryId: '2-1',
      difficulty: 'medium',
      status: 'published',
      questionCount: 30,
      timeLimit: 25,
      passingScore: 75,
      attempts: 850,
      completions: 680,
      avgScore: 72.3,
      createdAt: '2024-01-05',
      updatedAt: '2024-01-12',
      createdBy: 'Admin',
    },
    {
      id: '3',
      title: 'Advanced Listening Comprehension',
      category: 'Listening > Lectures',
      categoryId: '3-2',
      difficulty: 'hard',
      status: 'published',
      questionCount: 25,
      timeLimit: 30,
      passingScore: 65,
      attempts: 420,
      completions: 310,
      avgScore: 68.9,
      createdAt: '2024-01-08',
      updatedAt: '2024-01-14',
      createdBy: 'Admin',
    },
    {
      id: '4',
      title: 'Idioms and Expressions',
      category: 'Vocabulary > Idioms',
      categoryId: '2-3',
      difficulty: 'medium',
      status: 'draft',
      questionCount: 15,
      timeLimit: 20,
      passingScore: 70,
      attempts: 0,
      completions: 0,
      avgScore: 0,
      createdAt: '2024-01-15',
      updatedAt: '2024-01-15',
      createdBy: 'Admin',
    },
    {
      id: '5',
      title: 'Reading Comprehension B2',
      category: 'Reading',
      categoryId: '4',
      difficulty: 'medium',
      status: 'published',
      questionCount: 20,
      timeLimit: 25,
      passingScore: 70,
      attempts: 670,
      completions: 540,
      avgScore: 74.2,
      createdAt: '2023-12-20',
      updatedAt: '2024-01-08',
      createdBy: 'Admin',
    },
    {
      id: '6',
      title: 'Past Simple vs Present Perfect',
      category: 'Grammar > Tenses',
      categoryId: '1-1-2',
      difficulty: 'medium',
      status: 'published',
      questionCount: 25,
      timeLimit: 20,
      passingScore: 72,
      attempts: 920,
      completions: 750,
      avgScore: 76.8,
      createdAt: '2023-12-15',
      updatedAt: '2024-01-05',
      createdBy: 'Admin',
    },
    {
      id: '7',
      title: 'Academic Writing Skills',
      category: 'Writing',
      categoryId: '5',
      difficulty: 'hard',
      status: 'archived',
      questionCount: 10,
      timeLimit: 45,
      passingScore: 60,
      attempts: 180,
      completions: 120,
      avgScore: 62.5,
      createdAt: '2023-11-01',
      updatedAt: '2023-12-01',
      createdBy: 'Admin',
    },
    {
      id: '8',
      title: 'Conditional Sentences',
      category: 'Grammar > Conditionals',
      categoryId: '1-2',
      difficulty: 'hard',
      status: 'published',
      questionCount: 20,
      timeLimit: 20,
      passingScore: 68,
      attempts: 580,
      completions: 420,
      avgScore: 70.1,
      createdAt: '2024-01-03',
      updatedAt: '2024-01-11',
      createdBy: 'Admin',
    },
    {
      id: '9',
      title: 'Everyday Conversations',
      category: 'Listening > Conversations',
      categoryId: '3-1',
      difficulty: 'easy',
      status: 'published',
      questionCount: 15,
      timeLimit: 15,
      passingScore: 75,
      attempts: 1450,
      completions: 1280,
      avgScore: 82.3,
      createdAt: '2023-12-10',
      updatedAt: '2024-01-09',
      createdBy: 'Admin',
    },
    {
      id: '10',
      title: 'Modal Verbs Practice',
      category: 'Grammar > Modal Verbs',
      categoryId: '1-3',
      difficulty: 'medium',
      status: 'draft',
      questionCount: 18,
      timeLimit: 18,
      passingScore: 70,
      attempts: 0,
      completions: 0,
      avgScore: 0,
      createdAt: '2024-01-14',
      updatedAt: '2024-01-14',
      createdBy: 'Admin',
    },
  ];
};

const deleteTest = async (id: string): Promise<void> => {
  await new Promise((resolve) => setTimeout(resolve, 500));
};

const duplicateTest = async (id: string): Promise<Test> => {
  await new Promise((resolve) => setTimeout(resolve, 500));
  throw new Error('Not implemented');
};

// Difficulty colors
const getDifficultyColor = (difficulty: Test['difficulty']) => {
  switch (difficulty) {
    case 'easy':
      return COLORS.success;
    case 'medium':
      return COLORS.warning;
    case 'hard':
      return COLORS.error;
    default:
      return COLORS.textSecondary;
  }
};

// Status colors
const getStatusColor = (status: Test['status']) => {
  switch (status) {
    case 'published':
      return COLORS.success;
    case 'draft':
      return COLORS.warning;
    case 'archived':
      return COLORS.textSecondary;
    default:
      return COLORS.textSecondary;
  }
};

// Status chip
const StatusChip: React.FC<{ status: Test['status'] }> = ({ status }) => (
  <Chip
    label={status.charAt(0).toUpperCase() + status.slice(1)}
    size="small"
    sx={{
      backgroundColor: `${getStatusColor(status)}20`,
      color: getStatusColor(status),
      fontWeight: 500,
      textTransform: 'capitalize',
    }}
  />
);

// Difficulty chip
const DifficultyChip: React.FC<{ difficulty: Test['difficulty'] }> = ({ difficulty }) => (
  <Chip
    label={difficulty.charAt(0).toUpperCase() + difficulty.slice(1)}
    size="small"
    sx={{
      backgroundColor: `${getDifficultyColor(difficulty)}20`,
      color: getDifficultyColor(difficulty),
      fontWeight: 500,
      textTransform: 'capitalize',
    }}
  />
);

const Tests: React.FC = () => {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  
  const [filters, setFilters] = useState<TestFilters>({
    search: '',
    category: '',
    difficulty: '',
    status: '',
  });
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [sortField, setSortField] = useState<keyof Test>('updatedAt');
  const [sortDirection, setSortDirection] = useState<'asc' | 'desc'>('desc');
  const [menuAnchorEl, setMenuAnchorEl] = useState<null | HTMLElement>(null);
  const [selectedTest, setSelectedTest] = useState<Test | null>(null);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);

  const { data: tests = [], isLoading, error } = useQuery({
    queryKey: ['tests'],
    queryFn: fetchTests,
  });

  const deleteMutation = useMutation({
    mutationFn: deleteTest,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tests'] });
      setDeleteDialogOpen(false);
      setSelectedTest(null);
    },
  });

  // Filter and sort tests
  const filteredTests = tests
    .filter((test) => {
      const matchesSearch =
        !filters.search ||
        test.title.toLowerCase().includes(filters.search.toLowerCase()) ||
        test.category.toLowerCase().includes(filters.search.toLowerCase());
      const matchesCategory = !filters.category || test.categoryId === filters.category;
      const matchesDifficulty = !filters.difficulty || test.difficulty === filters.difficulty;
      const matchesStatus = !filters.status || test.status === filters.status;
      return matchesSearch && matchesCategory && matchesDifficulty && matchesStatus;
    })
    .sort((a, b) => {
      const aValue = a[sortField];
      const bValue = b[sortField];
      if (sortDirection === 'asc') {
        return aValue > bValue ? 1 : -1;
      }
      return aValue < bValue ? 1 : -1;
    });

  const paginatedTests = filteredTests.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage);

  const handleSort = (field: keyof Test) => {
    if (sortField === field) {
      setSortDirection(sortDirection === 'asc' ? 'desc' : 'asc');
    } else {
      setSortField(field);
      setSortDirection('asc');
    }
  };

  const handleMenuOpen = (event: React.MouseEvent<HTMLElement>, test: Test) => {
    event.stopPropagation();
    setMenuAnchorEl(event.currentTarget);
    setSelectedTest(test);
  };

  const handleMenuClose = () => {
    setMenuAnchorEl(null);
    setSelectedTest(null);
  };

  const handleDelete = () => {
    if (selectedTest) {
      deleteMutation.mutate(selectedTest.id);
    }
  };

  const handlePreview = (testId: string) => {
    window.open(`/test/${testId}/preview`, '_blank');
  };

  const handleDuplicate = (testId: string) => {
    // TODO: Implement duplicate
    console.log('Duplicate test:', testId);
    handleMenuClose();
  };

  if (error) {
    return (
      <Box p={3}>
        <Alert severity="error">Failed to load tests. Please try again.</Alert>
      </Box>
    );
  }

  return (
    <Box p={{ xs: 2, md: 3 }}>
      {/* Header */}
      <Box display="flex" flexDirection={{ xs: 'column', sm: 'row' }} justifyContent="space-between" alignItems={{ xs: 'flex-start', sm: 'center' }} mb={3} gap={2}>
        <Typography variant="h4" fontWeight="bold" color={COLORS.textPrimary}>
          Tests
        </Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => navigate('/tests/new')}
          sx={{ backgroundColor: COLORS.primary }}
        >
          Create Test
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
              <MenuItem value="easy">Easy</MenuItem>
              <MenuItem value="medium">Medium</MenuItem>
              <MenuItem value="hard">Hard</MenuItem>
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

      {/* Table */}
      <Paper>
        <TableContainer>
          <Table size="small">
            <TableHead>
              <TableRow sx={{ backgroundColor: '#FAFAFA' }}>
                <TableCell>
                  <Box
                    display="flex"
                    alignItems="center"
                    sx={{ cursor: 'pointer' }}
                    onClick={() => handleSort('title')}
                  >
                    Test
                    {sortField === 'title' && <SortIcon fontSize="small" sx={{ ml: 0.5 }} />}
                  </Box>
                </TableCell>
                <TableCell>Category</TableCell>
                <TableCell>
                  <Box
                    display="flex"
                    alignItems="center"
                    sx={{ cursor: 'pointer' }}
                    onClick={() => handleSort('difficulty')}
                  >
                    Difficulty
                    {sortField === 'difficulty' && <SortIcon fontSize="small" sx={{ ml: 0.5 }} />}
                  </Box>
                </TableCell>
                <TableCell>
                  <Box
                    display="flex"
                    alignItems="center"
                    sx={{ cursor: 'pointer' }}
                    onClick={() => handleSort('status')}
                  >
                    Status
                    {sortField === 'status' && <SortIcon fontSize="small" sx={{ ml: 0.5 }} />}
                  </Box>
                </TableCell>
                <TableCell align="center">Questions</TableCell>
                <TableCell align="center">Time</TableCell>
                <TableCell align="center">Attempts</TableCell>
                <TableCell align="center">Avg Score</TableCell>
                <TableCell align="right">Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {isLoading ? (
                [...Array(5)].map((_, i) => (
                  <TableRow key={i}>
                    {[...Array(9)].map((_, j) => (
                      <TableCell key={j}>
                        <Skeleton variant="text" />
                      </TableCell>
                    ))}
                  </TableRow>
                ))
              ) : paginatedTests.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={9} align="center" sx={{ py: 4 }}>
                    <Typography color="text.secondary">No tests found</Typography>
                  </TableCell>
                </TableRow>
              ) : (
                paginatedTests.map((test) => (
                  <TableRow
                    key={test.id}
                    hover
                    sx={{ cursor: 'pointer' }}
                    onClick={() => navigate(`/tests/${test.id}/edit`)}
                  >
                    <TableCell>
                      <Box display="flex" alignItems="center" gap={1.5}>
                        <Avatar
                          sx={{
                            width: 32,
                            height: 32,
                            backgroundColor: `${COLORS.primary}20`,
                            color: COLORS.primary,
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
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2">{test.category}</Typography>
                    </TableCell>
                    <TableCell>
                      <DifficultyChip difficulty={test.difficulty} />
                    </TableCell>
                    <TableCell>
                      <StatusChip status={test.status} />
                    </TableCell>
                    <TableCell align="center">{test.questionCount}</TableCell>
                    <TableCell align="center">{test.timeLimit}m</TableCell>
                    <TableCell align="center">{test.attempts.toLocaleString()}</TableCell>
                    <TableCell align="center">
                      {test.avgScore > 0 ? (
                        <Chip
                          label={`${test.avgScore}%`}
                          size="small"
                          sx={{
                            backgroundColor:
                              test.avgScore >= 80
                                ? `${COLORS.success}20`
                                : test.avgScore >= 60
                                ? `${COLORS.warning}20`
                                : `${COLORS.error}20`,
                            color:
                              test.avgScore >= 80
                                ? COLORS.success
                                : test.avgScore >= 60
                                ? COLORS.warning
                                : COLORS.error,
                          }}
                        />
                      ) : (
                        '-'
                      )}
                    </TableCell>
                    <TableCell align="right">
                      <Box display="flex" justifyContent="flex-end" gap={0.5}>
                        <Tooltip title="Preview">
                          <IconButton
                            size="small"
                            onClick={(e) => {
                              e.stopPropagation();
                              handlePreview(test.id);
                            }}
                          >
                            <VisibilityIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                        <Tooltip title="Edit">
                          <IconButton
                            size="small"
                            onClick={(e) => {
                              e.stopPropagation();
                              navigate(`/tests/${test.id}/edit`);
                            }}
                          >
                            <EditIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                        <IconButton
                          size="small"
                          onClick={(e) => handleMenuOpen(e, test)}
                        >
                          <MoreVertIcon fontSize="small" />
                        </IconButton>
                      </Box>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </TableContainer>

        {/* Pagination */}
        <TablePagination
          component="div"
          count={filteredTests.length}
          page={page}
          onPageChange={(_, newPage) => setPage(newPage)}
          rowsPerPage={rowsPerPage}
          onRowsPerPageChange={(e) => {
            setRowsPerPage(parseInt(e.target.value, 10));
            setPage(0);
          }}
          rowsPerPageOptions={[5, 10, 25, 50]}
        />
      </Paper>

      {/* Actions Menu */}
      <Menu
        anchorEl={menuAnchorEl}
        open={Boolean(menuAnchorEl)}
        onClose={handleMenuClose}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
        transformOrigin={{ vertical: 'top', horizontal: 'right' }}
      >
        <MenuItem
          onClick={() => {
            if (selectedTest) handlePreview(selectedTest.id);
            handleMenuClose();
          }}
        >
          <VisibilityIcon fontSize="small" sx={{ mr: 1 }} />
          Preview
        </MenuItem>
        <MenuItem
          onClick={() => {
            if (selectedTest) navigate(`/tests/${selectedTest.id}/edit`);
            handleMenuClose();
          }}
        >
          <EditIcon fontSize="small" sx={{ mr: 1 }} />
          Edit
        </MenuItem>
        <MenuItem
          onClick={() => {
            if (selectedTest) handleDuplicate(selectedTest.id);
          }}
        >
          <ContentCopyIcon fontSize="small" sx={{ mr: 1 }} />
          Duplicate
        </MenuItem>
        <MenuItem
          onClick={() => {
            setDeleteDialogOpen(true);
            handleMenuClose();
          }}
          sx={{ color: COLORS.error }}
        >
          <DeleteIcon fontSize="small" sx={{ mr: 1 }} />
          Delete
        </MenuItem>
      </Menu>

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
