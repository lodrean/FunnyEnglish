import React, { useState, useCallback, useMemo } from 'react';
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
  TextField,
  Button,
  Tooltip,
  Chip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  useTheme,
} from '@mui/material';
import {
  ExpandMore,
  ExpandLess,
  DragIndicator,
  Add,
  Delete,
  Edit,
  Folder,
  FolderOpen,
  Save,
  Cancel,
} from '@mui/icons-material';


// Types
export interface Category {
  id: string;
  name: string;
  parentId: string | null;
  order: number;
  children?: Category[];
  testCount?: number;
}

interface CategoryTreeProps {
  categories: Category[];
  onReorder: (categories: Category[]) => void;
  onUpdate: (category: Category) => void;
  onDelete: (categoryId: string) => void;
  onAddChild: (parentId: string, name: string) => void;
  onCreateRoot: (name: string) => void;
}

// CategoryNodeProps is used by the internal CategoryNode component
// eslint-disable-next-line @typescript-eslint/no-unused-vars
interface CategoryNodeProps {
  category: Category;
  depth: number;
  onToggle: (id: string) => void;
  expanded: Set<string>;
  onUpdate: (category: Category) => void;
  onDelete: (categoryId: string) => void;
  onAddChild: (parentId: string, name: string) => void;
  allCategories: Category[];
}

interface FlatCategory extends Category {
  depth: number;
  parentId: string | null;
}

// Flatten tree to array for sortable context
const flattenTree = (
  categories: Category[],
  depth = 0,
  parentId: string | null = null
): FlatCategory[] => {
  const result: FlatCategory[] = [];
  
  categories.forEach((cat) => {
    result.push({ ...cat, depth, parentId });
    if (cat.children && cat.children.length > 0) {
      result.push(...flattenTree(cat.children, depth + 1, cat.id));
    }
  });
  
  return result;
};

// Sortable Category Item Component
const SortableCategoryItem: React.FC<{
  category: FlatCategory;
  onToggle: (id: string) => void;
  expanded: Set<string>;
  onUpdate: (category: Category) => void;
  onDelete: (categoryId: string) => void;
  onAddChild: (parentId: string, name: string) => void;
  allCategories: Category[];
}> = ({ category, onToggle, expanded, onUpdate, onDelete, onAddChild, allCategories }) => {
  const theme = useTheme();
  const [isEditing, setIsEditing] = useState(false);
  const [editName, setEditName] = useState(category.name);
  const [isAddingChild, setIsAddingChild] = useState(false);
  const [childName, setChildName] = useState('');
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);

  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
    isDragging,
  } = useSortable({ id: category.id });

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
    opacity: isDragging ? 0.5 : 1,
  };

  const hasChildren = useMemo(() => {
    return allCategories.some((c) => c.parentId === category.id);
  }, [allCategories, category.id]);

  const isExpanded = expanded.has(category.id);

  const handleSave = () => {
    if (editName.trim()) {
      onUpdate({ ...category, name: editName.trim() });
      setIsEditing(false);
    }
  };

  const handleCancel = () => {
    setEditName(category.name);
    setIsEditing(false);
  };

  const handleAddChild = () => {
    if (childName.trim()) {
      onAddChild(category.id, childName.trim());
      setChildName('');
      setIsAddingChild(false);
    }
  };

  const handleDelete = () => {
    onDelete(category.id);
    setDeleteDialogOpen(false);
  };

  const indentWidth = 20;

  return (
    <>
      <Paper
        ref={setNodeRef}
        style={style}
        sx={{
          display: 'flex',
          alignItems: 'center',
          padding: '8px 12px',
          marginBottom: '4px',
          marginLeft: `${category.depth * indentWidth}px`,
          backgroundColor: theme.palette.background.paper,
          border: `1px solid ${isDragging ? theme.palette.primary.main : '#e0e0e0'}`,
          borderRadius: '8px',
          '&:hover': {
            boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
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
            color: theme.palette.text.secondary,
            '&:active': { cursor: 'grabbing' },
          }}
        >
          <DragIndicator fontSize="small" />
        </Box>

        {/* Expand/Collapse */}
        <IconButton
          size="small"
          onClick={() => onToggle(category.id)}
          sx={{ visibility: hasChildren ? 'visible' : 'hidden' }}
        >
          {isExpanded ? <ExpandLess fontSize="small" /> : <ExpandMore fontSize="small" />}
        </IconButton>

        {/* Folder Icon */}
        <Box sx={{ mx: 1, color: theme.palette.primary.main }}>
          {isExpanded ? <FolderOpen fontSize="small" /> : <Folder fontSize="small" />}
        </Box>

        {/* Category Name (Edit or Display) */}
        <Box sx={{ flex: 1, minWidth: 0 }}>
          {isEditing ? (
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <TextField
                size="small"
                value={editName}
                onChange={(e) => setEditName(e.target.value)}
                autoFocus
                onKeyDown={(e) => {
                  if (e.key === 'Enter') handleSave();
                  if (e.key === 'Escape') handleCancel();
                }}
                sx={{ flex: 1 }}
              />
              <IconButton size="small" onClick={handleSave} color="success">
                <Save fontSize="small" />
              </IconButton>
              <IconButton size="small" onClick={handleCancel} color="error">
                <Cancel fontSize="small" />
              </IconButton>
            </Box>
          ) : (
            <Typography
              variant="body1"
              sx={{
                cursor: 'pointer',
                color: theme.palette.text.primary,
                fontWeight: 500,
                '&:hover': { color: theme.palette.primary.main },
              }}
              onClick={() => setIsEditing(true)}
            >
              {category.name}
            </Typography>
          )}
        </Box>

        {/* Test Count */}
        {category.testCount !== undefined && category.testCount > 0 && (
          <Chip
            size="small"
            label={`${category.testCount} tests`}
            sx={{
              mr: 1,
              backgroundColor: theme.palette.background.default,
              color: theme.palette.text.secondary,
            }}
          />
        )}

        {/* Actions */}
        <Box sx={{ display: 'flex', gap: 0.5 }}>
          <Tooltip title="Edit name">
            <IconButton size="small" onClick={() => setIsEditing(true)}>
              <Edit fontSize="small" />
            </IconButton>
          </Tooltip>
          <Tooltip title="Add subcategory">
            <IconButton size="small" onClick={() => setIsAddingChild(true)}>
              <Add fontSize="small" />
            </IconButton>
          </Tooltip>
          <Tooltip title="Delete">
            <IconButton size="small" onClick={() => setDeleteDialogOpen(true)} color="error">
              <Delete fontSize="small" />
            </IconButton>
          </Tooltip>
        </Box>
      </Paper>

      {/* Add Child Dialog */}
      <Dialog open={isAddingChild} onClose={() => setIsAddingChild(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Add Subcategory to &quot;{category.name}&quot;</DialogTitle>
        <DialogContent>
          <TextField
            autoFocus
            fullWidth
            label="Subcategory Name"
            value={childName}
            onChange={(e) => setChildName(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && handleAddChild()}
            sx={{ mt: 1 }}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setIsAddingChild(false)}>Cancel</Button>
          <Button
            onClick={handleAddChild}
            variant="contained"
            disabled={!childName.trim()}
            sx={{ backgroundColor: theme.palette.primary.main }}
          >
            Add
          </Button>
        </DialogActions>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <Dialog open={deleteDialogOpen} onClose={() => setDeleteDialogOpen(false)}>
        <DialogTitle>Delete Category</DialogTitle>
        <DialogContent>
          <Typography>
            Are you sure you want to delete &quot;{category.name}&quot;?
            {hasChildren && (
              <Typography color="error" sx={{ mt: 1 }}>
                Warning: This category has subcategories that will also be deleted.
              </Typography>
            )}
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteDialogOpen(false)}>Cancel</Button>
          <Button onClick={handleDelete} variant="contained" color="error">
            Delete
          </Button>
        </DialogActions>
      </Dialog>
    </>
  );
};

// Main Category Tree Component
const CategoryTree: React.FC<CategoryTreeProps> = ({
  categories,
  onReorder,
  onUpdate,
  onDelete,
  onAddChild,
  onCreateRoot,
}) => {
  const theme = useTheme();
  const [expanded, setExpanded] = useState<Set<string>>(new Set());
  const [activeId, setActiveId] = useState<string | null>(null);
  const [isCreatingRoot, setIsCreatingRoot] = useState(false);
  const [rootName, setRootName] = useState('');

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

  // Flatten categories for drag-drop
  const flatCategories = useMemo(() => flattenTree(categories), [categories]);

  const toggleExpand = useCallback((id: string) => {
    setExpanded((prev) => {
      const next = new Set(prev);
      if (next.has(id)) {
        next.delete(id);
      } else {
        next.add(id);
      }
      return next;
    });
  }, []);

  const handleDragStart = useCallback((event: DragStartEvent) => {
    setActiveId(event.active.id as string);
  }, []);

  const handleDragEnd = useCallback((event: DragEndEvent) => {
    const { active, over } = event;
    setActiveId(null);

    if (over && active.id !== over.id) {
      const oldIndex = flatCategories.findIndex((c) => c.id === active.id);
      const newIndex = flatCategories.findIndex((c) => c.id === over.id);

      if (oldIndex !== -1 && newIndex !== -1) {
        const newFlatCategories = [...flatCategories];
        const [movedItem] = newFlatCategories.splice(oldIndex, 1);
        newFlatCategories.splice(newIndex, 0, movedItem);

        // Update order values
        const updatedCategories = newFlatCategories.map((cat, index) => ({
          ...cat,
          order: index,
        }));

        // Convert back to tree structure
        const treeCategories = buildTreeFromFlat(updatedCategories);
        onReorder(treeCategories);
      }
    }
  }, [flatCategories, onReorder]);

  const handleCreateRoot = () => {
    if (rootName.trim()) {
      onCreateRoot(rootName.trim());
      setRootName('');
      setIsCreatingRoot(false);
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

  const activeCategory = activeId
    ? flatCategories.find((c) => c.id === activeId)
    : null;

  return (
    <Box sx={{ width: '100%' }}>
      {/* Header */}
      <Box
        sx={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          mb: 2,
        }}
      >
        <Typography variant="h6" sx={{ color: theme.palette.text.primary, fontWeight: 600 }}>
          Categories
        </Typography>
        <Button
          variant="contained"
          startIcon={<Add />}
          onClick={() => setIsCreatingRoot(true)}
          sx={{ backgroundColor: theme.palette.primary.main }}
        >
          New Category
        </Button>
      </Box>

      {/* Category Count */}
      <Typography variant="body2" sx={{ color: theme.palette.text.secondary, mb: 2 }}>
        {flatCategories.length} categories total
      </Typography>

      {/* Drag and Drop Context */}
      <DndContext
        sensors={sensors}
        collisionDetection={closestCenter}
        onDragStart={handleDragStart}
        onDragEnd={handleDragEnd}
      >
        <SortableContext
          items={flatCategories.map((c) => c.id)}
          strategy={verticalListSortingStrategy}
        >
          <Box sx={{ display: 'flex', flexDirection: 'column' }}>
            {flatCategories.map((category) => (
              <SortableCategoryItem
                key={category.id}
                category={category}
                onToggle={toggleExpand}
                expanded={expanded}
                onUpdate={onUpdate}
                onDelete={onDelete}
                onAddChild={onAddChild}
                allCategories={flatCategories}
              />
            ))}
          </Box>
        </SortableContext>

        {/* Drag Overlay */}
        <DragOverlay dropAnimation={dropAnimation}>
          {activeCategory ? (
            <Paper
              sx={{
                display: 'flex',
                alignItems: 'center',
                padding: '8px 12px',
                marginLeft: `${activeCategory.depth * 20}px`,
                backgroundColor: theme.palette.background.paper,
                border: `2px solid ${theme.palette.primary.main}`,
                borderRadius: '8px',
                boxShadow: '0 4px 12px rgba(0,0,0,0.15)',
              }}
            >
              <DragIndicator fontSize="small" sx={{ mr: 1 }} />
              <Folder fontSize="small" sx={{ mr: 1, color: theme.palette.primary.main }} />
              <Typography>{activeCategory.name}</Typography>
            </Paper>
          ) : null}
        </DragOverlay>
      </DndContext>

      {/* Empty State */}
      {flatCategories.length === 0 && (
        <Paper
          sx={{
            padding: 4,
            textAlign: 'center',
            backgroundColor: theme.palette.background.default,
            border: '2px dashed #ccc',
          }}
        >
          <Typography variant="body1" color={theme.palette.text.secondary}>
            No categories yet. Create your first category to get started.
          </Typography>
        </Paper>
      )}

      {/* Create Root Dialog */}
      <Dialog open={isCreatingRoot} onClose={() => setIsCreatingRoot(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Create New Category</DialogTitle>
        <DialogContent>
          <TextField
            autoFocus
            fullWidth
            label="Category Name"
            value={rootName}
            onChange={(e) => setRootName(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && handleCreateRoot()}
            sx={{ mt: 1 }}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setIsCreatingRoot(false)}>Cancel</Button>
          <Button
            onClick={handleCreateRoot}
            variant="contained"
            disabled={!rootName.trim()}
            sx={{ backgroundColor: theme.palette.primary.main }}
          >
            Create
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

// Helper function to build tree from flat array
const buildTreeFromFlat = (flatCategories: FlatCategory[]): Category[] => {
  const categoryMap = new Map<string, Category>();
  const roots: Category[] = [];

  // First pass: create all category objects
  flatCategories.forEach((cat) => {
    categoryMap.set(cat.id, { ...cat, children: [] });
  });

  // Second pass: build parent-child relationships
  flatCategories.forEach((cat) => {
    const category = categoryMap.get(cat.id)!;
    if (cat.parentId && categoryMap.has(cat.parentId)) {
      const parent = categoryMap.get(cat.parentId)!;
      if (!parent.children) parent.children = [];
      parent.children.push(category);
    } else {
      roots.push(category);
    }
  });

  return roots;
};

export default CategoryTree;
