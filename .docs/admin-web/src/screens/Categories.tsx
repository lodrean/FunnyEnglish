import React, { useState, useCallback } from 'react';
import {
  Box,
  Typography,
  Button,
  TextField,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  IconButton,
  Menu,
  MenuItem,
  Paper,
  Alert,
  Skeleton,
  Chip,
  Tooltip,
  Breadcrumbs,
  Link,
  InputAdornment,
  FormControlLabel,
  Switch,
  Divider,
} from '@mui/material';
import {
  Add as AddIcon,
  MoreVert as MoreVertIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  Folder as FolderIcon,
  FolderOpen as FolderOpenIcon,
  ExpandMore as ExpandMoreIcon,
  ChevronRight as ChevronRightIcon,
  Search as SearchIcon,
  DragIndicator as DragIndicatorIcon,
  SubdirectoryArrowRight as SubdirectoryArrowRightIcon,
} from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';

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
interface Category {
  id: string;
  name: string;
  description?: string;
  parentId: string | null;
  order: number;
  isActive: boolean;
  testCount: number;
  children?: Category[];
}

interface CategoryFormData {
  name: string;
  description: string;
  parentId: string | null;
  isActive: boolean;
}

// Mock API
const fetchCategories = async (): Promise<Category[]> => {
  await new Promise((resolve) => setTimeout(resolve, 600));
  
  return [
    {
      id: '1',
      name: 'Grammar',
      description: 'English grammar tests and exercises',
      parentId: null,
      order: 1,
      isActive: true,
      testCount: 45,
      children: [
        {
          id: '1-1',
          name: 'Tenses',
          description: 'Past, present, and future tenses',
          parentId: '1',
          order: 1,
          isActive: true,
          testCount: 15,
          children: [
            { id: '1-1-1', name: 'Present Simple', description: '', parentId: '1-1', order: 1, isActive: true, testCount: 5 },
            { id: '1-1-2', name: 'Past Simple', description: '', parentId: '1-1', order: 2, isActive: true, testCount: 5 },
            { id: '1-1-3', name: 'Future Tenses', description: '', parentId: '1-1', order: 3, isActive: true, testCount: 5 },
          ],
        },
        {
          id: '1-2',
          name: 'Conditionals',
          description: 'If clauses and conditional sentences',
          parentId: '1',
          order: 2,
          isActive: true,
          testCount: 12,
        },
        {
          id: '1-3',
          name: 'Modal Verbs',
          description: 'Can, could, may, might, must, should',
          parentId: '1',
          order: 3,
          isActive: false,
          testCount: 8,
        },
      ],
    },
    {
      id: '2',
      name: 'Vocabulary',
      description: 'Vocabulary building exercises',
      parentId: null,
      order: 2,
      isActive: true,
      testCount: 62,
      children: [
        {
          id: '2-1',
          name: 'Business English',
          description: 'Professional vocabulary',
          parentId: '2',
          order: 1,
          isActive: true,
          testCount: 20,
        },
        {
          id: '2-2',
          name: 'Academic Words',
          description: 'University-level vocabulary',
          parentId: '2',
          order: 2,
          isActive: true,
          testCount: 18,
        },
        {
          id: '2-3',
          name: 'Idioms',
          description: 'Common English idioms',
          parentId: '2',
          order: 3,
          isActive: true,
          testCount: 15,
        },
      ],
    },
    {
      id: '3',
      name: 'Listening',
      description: 'Listening comprehension tests',
      parentId: null,
      order: 3,
      isActive: true,
      testCount: 38,
      children: [
        {
          id: '3-1',
          name: 'Conversations',
          description: 'Daily dialogues',
          parentId: '3',
          order: 1,
          isActive: true,
          testCount: 15,
        },
        {
          id: '3-2',
          name: 'Lectures',
          description: 'Academic lectures',
          parentId: '3',
          order: 2,
          isActive: true,
          testCount: 12,
        },
      ],
    },
    {
      id: '4',
      name: 'Reading',
      description: 'Reading comprehension exercises',
      parentId: null,
      order: 4,
      isActive: true,
      testCount: 52,
    },
    {
      id: '5',
      name: 'Writing',
      description: 'Writing skills and essays',
      parentId: null,
      order: 5,
      isActive: true,
      testCount: 28,
    },
    {
      id: '6',
      name: 'Speaking',
      description: 'Speaking and pronunciation',
      parentId: null,
      order: 6,
      isActive: false,
      testCount: 15,
    },
  ];
};

const createCategory = async (data: CategoryFormData): Promise<Category> => {
  await new Promise((resolve) => setTimeout(resolve, 500));
  return {
    id: Math.random().toString(36).substr(2, 9),
    ...data,
    order: 0,
    testCount: 0,
  };
};

const updateCategory = async (id: string, data: CategoryFormData): Promise<Category> => {
  await new Promise((resolve) => setTimeout(resolve, 500));
  return {
    id,
    ...data,
    order: 0,
    testCount: 0,
  };
};

const deleteCategory = async (id: string): Promise<void> => {
  await new Promise((resolve) => setTimeout(resolve, 500));
};

// Flatten categories for search
const flattenCategories = (categories: Category[], parentPath: string = ''): Array<Category & { path: string }> => {
  const result: Array<Category & { path: string }> = [];
  
  categories.forEach((cat) => {
    const currentPath = parentPath ? `${parentPath} > ${cat.name}` : cat.name;
    result.push({ ...cat, path: currentPath });
    
    if (cat.children && cat.children.length > 0) {
      result.push(...flattenCategories(cat.children, currentPath));
    }
  });
  
  return result;
};

// Category Tree Item Component
interface CategoryTreeItemProps {
  category: Category;
  level: number;
  expandedIds: Set<string>;
  onToggleExpand: (id: string) => void;
  onEdit: (category: Category) => void;
  onDelete: (category: Category) => void;
  onAddChild: (parentId: string) => void;
}

const CategoryTreeItem: React.FC<CategoryTreeItemProps> = ({
  category,
  level,
  expandedIds,
  onToggleExpand,
  onEdit,
  onDelete,
  onAddChild,
}) => {
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const hasChildren = category.children && category.children.length > 0;
  const isExpanded = expandedIds.has(category.id);

  const handleMenuOpen = (event: React.MouseEvent<HTMLElement>) => {
    event.stopPropagation();
    setAnchorEl(event.currentTarget);
  };

  const handleMenuClose = (event: React.MouseEvent) => {
    event.stopPropagation();
    setAnchorEl(null);
  };

  return (
    <Box>
      <Paper
        elevation={0}
        sx={{
          display: 'flex',
          alignItems: 'center',
          p: 1.5,
          pl: 2 + level * 2,
          mb: 0.5,
          borderRadius: 1,
          border: '1px solid #E0E0E0',
          backgroundColor: COLORS.card,
          '&:hover': {
            backgroundColor: 'rgba(74, 144, 217, 0.05)',
            borderColor: COLORS.primary,
          },
          transition: 'all 0.2s',
        }}
      >
        <IconButton
          size="small"
          onClick={(e) => {
            e.stopPropagation();
            onToggleExpand(category.id);
          }}
          sx={{
            visibility: hasChildren ? 'visible' : 'hidden',
            transform: isExpanded ? 'rotate(90deg)' : 'rotate(0deg)',
            transition: 'transform 0.2s',
          }}
        >
          <ChevronRightIcon fontSize="small" />
        </IconButton>

        <Box sx={{ color: category.isActive ? COLORS.primary : COLORS.textSecondary, mr: 1.5 }}>
          {isExpanded ? <FolderOpenIcon /> : <FolderIcon />}
        </Box>

        <Box flex={1}>
          <Typography variant="body1" fontWeight={500}>
            {category.name}
            {!category.isActive && (
              <Chip
                label="Inactive"
                size="small"
                sx={{
                  ml: 1,
                  backgroundColor: `${COLORS.textSecondary}20`,
                  color: COLORS.textSecondary,
                  height: 20,
                  fontSize: '0.7rem',
                }}
              />
            )}
          </Typography>
          {category.description && (
            <Typography variant="body2" color="text.secondary">
              {category.description}
            </Typography>
          )}
        </Box>

        <Chip
          label={`${category.testCount} tests`}
          size="small"
          sx={{
            backgroundColor: `${COLORS.success}15`,
            color: COLORS.success,
            mr: 2,
          }}
        />

        <Tooltip title="More actions">
          <IconButton size="small" onClick={handleMenuOpen}>
            <MoreVertIcon fontSize="small" />
          </IconButton>
        </Tooltip>

        <Menu
          anchorEl={anchorEl}
          open={Boolean(anchorEl)}
          onClose={handleMenuClose}
          anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
          transformOrigin={{ vertical: 'top', horizontal: 'right' }}
        >
          <MenuItem
            onClick={(e) => {
              handleMenuClose(e);
              onEdit(category);
            }}
          >
            <EditIcon fontSize="small" sx={{ mr: 1 }} />
            Edit
          </MenuItem>
          <MenuItem
            onClick={(e) => {
              handleMenuClose(e);
              onAddChild(category.id);
            }}
          >
            <SubdirectoryArrowRightIcon fontSize="small" sx={{ mr: 1 }} />
            Add Subcategory
          </MenuItem>
          <Divider />
          <MenuItem
            onClick={(e) => {
              handleMenuClose(e);
              onDelete(category);
            }}
            sx={{ color: COLORS.error }}
          >
            <DeleteIcon fontSize="small" sx={{ mr: 1 }} />
            Delete
          </MenuItem>
        </Menu>
      </Paper>

      {hasChildren && isExpanded && (
        <Box>
          {category.children!.map((child) => (
            <CategoryTreeItem
              key={child.id}
              category={child}
              level={level + 1}
              expandedIds={expandedIds}
              onToggleExpand={onToggleExpand}
              onEdit={onEdit}
              onDelete={onDelete}
              onAddChild={onAddChild}
            />
          ))}
        </Box>
      )}
    </Box>
  );
};

// Main Component
const Categories: React.FC = () => {
  const queryClient = useQueryClient();
  const [searchQuery, setSearchQuery] = useState('');
  const [expandedIds, setExpandedIds] = useState<Set<string>>(new Set(['1', '2']));
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editingCategory, setEditingCategory] = useState<Category | null>(null);
  const [parentIdForNew, setParentIdForNew] = useState<string | null>(null);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [categoryToDelete, setCategoryToDelete] = useState<Category | null>(null);
  
  const [formData, setFormData] = useState<CategoryFormData>({
    name: '',
    description: '',
    parentId: null,
    isActive: true,
  });

  const { data: categories = [], isLoading, error } = useQuery({
    queryKey: ['categories'],
    queryFn: fetchCategories,
  });

  const createMutation = useMutation({
    mutationFn: createCategory,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['categories'] });
      handleCloseDialog();
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: string; data: CategoryFormData }) => updateCategory(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['categories'] });
      handleCloseDialog();
    },
  });

  const deleteMutation = useMutation({
    mutationFn: deleteCategory,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['categories'] });
      setDeleteDialogOpen(false);
      setCategoryToDelete(null);
    },
  });

  const handleToggleExpand = useCallback((id: string) => {
    setExpandedIds((prev) => {
      const newSet = new Set(prev);
      if (newSet.has(id)) {
        newSet.delete(id);
      } else {
        newSet.add(id);
      }
      return newSet;
    });
  }, []);

  const handleOpenDialog = (category?: Category, parentId?: string | null) => {
    if (category) {
      setEditingCategory(category);
      setFormData({
        name: category.name,
        description: category.description || '',
        parentId: category.parentId,
        isActive: category.isActive,
      });
    } else {
      setEditingCategory(null);
      setParentIdForNew(parentId || null);
      setFormData({
        name: '',
        description: '',
        parentId: parentId || null,
        isActive: true,
      });
    }
    setDialogOpen(true);
  };

  const handleCloseDialog = () => {
    setDialogOpen(false);
    setEditingCategory(null);
    setParentIdForNew(null);
    setFormData({ name: '', description: '', parentId: null, isActive: true });
  };

  const handleSubmit = () => {
    if (!formData.name.trim()) return;

    if (editingCategory) {
      updateMutation.mutate({ id: editingCategory.id, data: formData });
    } else {
      createMutation.mutate(formData);
    }
  };

  const handleDelete = (category: Category) => {
    setCategoryToDelete(category);
    setDeleteDialogOpen(true);
  };

  const confirmDelete = () => {
    if (categoryToDelete) {
      deleteMutation.mutate(categoryToDelete.id);
    }
  };

  const handleExpandAll = () => {
    const allIds = new Set<string>();
    const collectIds = (cats: Category[]) => {
      cats.forEach((cat) => {
        if (cat.children && cat.children.length > 0) {
          allIds.add(cat.id);
          collectIds(cat.children);
        }
      });
    };
    collectIds(categories);
    setExpandedIds(allIds);
  };

  const handleCollapseAll = () => {
    setExpandedIds(new Set());
  };

  // Filter categories based on search
  const filteredCategories = searchQuery
    ? flattenCategories(categories).filter(
        (cat) =>
          cat.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
          cat.description?.toLowerCase().includes(searchQuery.toLowerCase())
      )
    : categories;

  if (error) {
    return (
      <Box p={3}>
        <Alert severity="error">Failed to load categories. Please try again.</Alert>
      </Box>
    );
  }

  return (
    <Box p={{ xs: 2, md: 3 }}>
      {/* Header */}
      <Box display="flex" flexDirection={{ xs: 'column', sm: 'row' }} justifyContent="space-between" alignItems={{ xs: 'flex-start', sm: 'center' }} mb={3} gap={2}>
        <Typography variant="h4" fontWeight="bold" color={COLORS.textPrimary}>
          Categories
        </Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => handleOpenDialog()}
          sx={{ backgroundColor: COLORS.primary }}
        >
          Add Root Category
        </Button>
      </Box>

      {/* Toolbar */}
      <Paper sx={{ p: 2, mb: 3 }}>
        <Box display="flex" flexDirection={{ xs: 'column', sm: 'row' }} gap={2} alignItems={{ xs: 'stretch', sm: 'center' }}>
          <TextField
            placeholder="Search categories..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            size="small"
            sx={{ flex: 1, maxWidth: { sm: 400 } }}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <SearchIcon fontSize="small" />
                </InputAdornment>
              ),
            }}
          />
          <Box display="flex" gap={1}>
            <Button variant="outlined" size="small" onClick={handleExpandAll}>
              Expand All
            </Button>
            <Button variant="outlined" size="small" onClick={handleCollapseAll}>
              Collapse All
            </Button>
          </Box>
        </Box>
      </Paper>

      {/* Category Tree */}
      <Box>
        {isLoading ? (
          <>
            {[1, 2, 3, 4, 5].map((i) => (
              <Skeleton key={i} variant="rectangular" height={60} sx={{ mb: 1, borderRadius: 1 }} />
            ))}
          </>
        ) : searchQuery ? (
          // Search results
          filteredCategories.map((category) => (
            <Paper
              key={category.id}
              elevation={0}
              sx={{
                display: 'flex',
                alignItems: 'center',
                p: 1.5,
                mb: 0.5,
                borderRadius: 1,
                border: '1px solid #E0E0E0',
                '&:hover': {
                  backgroundColor: 'rgba(74, 144, 217, 0.05)',
                },
              }}
            >
              <FolderIcon sx={{ color: COLORS.primary, mr: 1.5 }} />
              <Box flex={1}>
                <Typography variant="body1" fontWeight={500}>
                  {category.name}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  {category.path}
                </Typography>
              </Box>
              <Chip label={`${category.testCount} tests`} size="small" sx={{ mr: 2 }} />
            </Paper>
          ))
        ) : (
          // Tree view
          categories.map((category) => (
            <CategoryTreeItem
              key={category.id}
              category={category}
              level={0}
              expandedIds={expandedIds}
              onToggleExpand={handleToggleExpand}
              onEdit={(cat) => handleOpenDialog(cat)}
              onDelete={handleDelete}
              onAddChild={(parentId) => handleOpenDialog(undefined, parentId)}
            />
          ))
        )}
      </Box>

      {/* Add/Edit Dialog */}
      <Dialog open={dialogOpen} onClose={handleCloseDialog} maxWidth="sm" fullWidth>
        <DialogTitle>{editingCategory ? 'Edit Category' : 'Add Category'}</DialogTitle>
        <DialogContent>
          <TextField
            autoFocus
            label="Category Name"
            fullWidth
            margin="normal"
            value={formData.name}
            onChange={(e) => setFormData({ ...formData, name: e.target.value })}
            error={!formData.name.trim()}
            helperText={!formData.name.trim() ? 'Name is required' : ''}
          />
          <TextField
            label="Description"
            fullWidth
            margin="normal"
            multiline
            rows={3}
            value={formData.description}
            onChange={(e) => setFormData({ ...formData, description: e.target.value })}
          />
          <FormControlLabel
            control={
              <Switch
                checked={formData.isActive}
                onChange={(e) => setFormData({ ...formData, isActive: e.target.checked })}
              />
            }
            label="Active"
            sx={{ mt: 2 }}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDialog}>Cancel</Button>
          <Button
            onClick={handleSubmit}
            variant="contained"
            disabled={!formData.name.trim() || createMutation.isPending || updateMutation.isPending}
            sx={{ backgroundColor: COLORS.primary }}
          >
            {createMutation.isPending || updateMutation.isPending
              ? 'Saving...'
              : editingCategory
              ? 'Update'
              : 'Create'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <Dialog open={deleteDialogOpen} onClose={() => setDeleteDialogOpen(false)}>
        <DialogTitle>Delete Category</DialogTitle>
        <DialogContent>
          <Typography>
            Are you sure you want to delete &quot;{categoryToDelete?.name}&quot;?
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
            This action cannot be undone. All tests in this category will need to be reassigned.
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteDialogOpen(false)}>Cancel</Button>
          <Button
            onClick={confirmDelete}
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

export default Categories;
