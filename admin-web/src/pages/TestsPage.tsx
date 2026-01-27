import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Box,
  Button,
  Card,
  Chip,
  IconButton,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
  CircularProgress,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
} from '@mui/material';
import {
  Add,
  Edit,
  Delete,
  Visibility,
  VisibilityOff,
} from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getAdminTests, deleteTest, updateTest } from '../api/client';
import type { Test } from '../types';

const difficultyColors: Record<string, 'success' | 'warning' | 'error'> = {
  EASY: 'success',
  MEDIUM: 'warning',
  HARD: 'error',
};

const difficultyLabels: Record<string, string> = {
  EASY: 'Легко',
  MEDIUM: 'Средне',
  HARD: 'Сложно',
};

export default function TestsPage() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [testToDelete, setTestToDelete] = useState<Test | null>(null);

  const { data: tests, isLoading } = useQuery({
    queryKey: ['admin-tests'],
    queryFn: getAdminTests,
  });

  const deleteMutation = useMutation({
    mutationFn: deleteTest,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-tests'] });
      setDeleteDialogOpen(false);
      setTestToDelete(null);
    },
  });

  const togglePublishMutation = useMutation({
    mutationFn: ({ id, isPublished }: { id: string; isPublished: boolean }) =>
      updateTest(id, { isPublished }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-tests'] });
    },
  });

  const handleDelete = (test: Test) => {
    setTestToDelete(test);
    setDeleteDialogOpen(true);
  };

  const confirmDelete = () => {
    if (testToDelete) {
      deleteMutation.mutate(testToDelete.id);
    }
  };

  const handleTogglePublish = (test: Test) => {
    togglePublishMutation.mutate({
      id: test.id,
      isPublished: !test.isPublished,
    });
  };

  if (isLoading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 3 }}>
        <Typography variant="h5" fontWeight="bold">
          Тесты
        </Typography>
        <Button
          variant="contained"
          startIcon={<Add />}
          onClick={() => navigate('/tests/new')}
        >
          Создать тест
        </Button>
      </Box>

      <Card>
        <TableContainer>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Название</TableCell>
                <TableCell>Сложность</TableCell>
                <TableCell>Вопросов</TableCell>
                <TableCell>Очки</TableCell>
                <TableCell>Статус</TableCell>
                <TableCell align="right">Действия</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {tests?.map((test) => (
                <TableRow key={test.id} hover>
                  <TableCell>
                    <Typography fontWeight="medium">{test.title}</Typography>
                    {test.description && (
                      <Typography variant="body2" color="text.secondary">
                        {test.description.substring(0, 50)}
                        {test.description.length > 50 ? '...' : ''}
                      </Typography>
                    )}
                  </TableCell>
                  <TableCell>
                    <Chip
                      label={difficultyLabels[test.difficulty]}
                      color={difficultyColors[test.difficulty]}
                      size="small"
                    />
                  </TableCell>
                  <TableCell>{test.questions.length}</TableCell>
                  <TableCell>{test.pointsReward}</TableCell>
                  <TableCell>
                    <Chip
                      label={test.isPublished ? 'Опубликован' : 'Черновик'}
                      color={test.isPublished ? 'success' : 'default'}
                      size="small"
                      variant={test.isPublished ? 'filled' : 'outlined'}
                    />
                  </TableCell>
                  <TableCell align="right">
                    <IconButton
                      onClick={() => handleTogglePublish(test)}
                      title={test.isPublished ? 'Снять с публикации' : 'Опубликовать'}
                    >
                      {test.isPublished ? <VisibilityOff /> : <Visibility />}
                    </IconButton>
                    <IconButton
                      onClick={() => navigate(`/tests/${test.id}`)}
                      title="Редактировать"
                    >
                      <Edit />
                    </IconButton>
                    <IconButton
                      onClick={() => handleDelete(test)}
                      color="error"
                      title="Удалить"
                    >
                      <Delete />
                    </IconButton>
                  </TableCell>
                </TableRow>
              ))}
              {tests?.length === 0 && (
                <TableRow>
                  <TableCell colSpan={6} align="center">
                    <Typography color="text.secondary" py={4}>
                      Тесты не найдены. Создайте первый тест!
                    </Typography>
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </TableContainer>
      </Card>

      <Dialog open={deleteDialogOpen} onClose={() => setDeleteDialogOpen(false)}>
        <DialogTitle>Удалить тест?</DialogTitle>
        <DialogContent>
          Вы уверены, что хотите удалить тест "{testToDelete?.title}"? Это
          действие нельзя отменить.
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteDialogOpen(false)}>Отмена</Button>
          <Button
            onClick={confirmDelete}
            color="error"
            variant="contained"
            disabled={deleteMutation.isPending}
          >
            {deleteMutation.isPending ? 'Удаление...' : 'Удалить'}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
