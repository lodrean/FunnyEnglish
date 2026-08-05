import { useMemo, useState } from 'react';
import {
  Alert,
  Avatar,
  Box,
  Button,
  IconButton,
  InputAdornment,
  Paper,
  Switch,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Tooltip,
  Typography,
} from '@mui/material';
import {
  Add as AddIcon,
  Delete as DeleteIcon,
  Edit as EditIcon,
  Search as SearchIcon,
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { AxiosError } from 'axios';
import { ConfirmDialog, EmptyState } from '../components/feedback';
import { SkeletonCard } from '../components/feedback/SkeletonCard';
import { useConfirm, useToast } from '../hooks';
import {
  useDeleteLibrary,
  usePublishLibrary,
  useSpeakingLibraries,
} from '../hooks/useSpeaking';
import type { SpeakingLibrary } from '../api/speakingApi';

export default function SpeakingLibraries() {
  const navigate = useNavigate();
  const toast = useToast();
  const { confirm, confirmState, handleConfirm, handleCancel } = useConfirm();

  const [search, setSearch] = useState('');
  const { data: libraries, isLoading, isError, refetch } = useSpeakingLibraries();
  const publishMutation = usePublishLibrary();
  const deleteMutation = useDeleteLibrary();

  const filtered = useMemo(() => {
    const query = search.trim().toLowerCase();
    const list = libraries ?? [];
    if (!query) return list;
    return list.filter((l) => l.name.toLowerCase().includes(query));
  }, [libraries, search]);

  const handleTogglePublish = (library: SpeakingLibrary) => {
    publishMutation.mutate(
      { id: library.id, isPublished: !library.isPublished },
      {
        onError: (err) =>
          toast.error(
            (err as AxiosError<{ message?: string }>).response?.data?.message ??
              'Не удалось изменить публикацию'
          ),
      }
    );
  };

  const handleDelete = async (library: SpeakingLibrary) => {
    const topicsWarning =
      library.topicsCount > 0
        ? ` В теме ${library.topicsCount} топик(ов) — они будут удалены вместе с темой. Удаление невозможно, пока у топиков есть записи учеников.`
        : '';
    const ok = await confirm({
      title: 'Удалить тему?',
      message: `Тема «${library.name}» будет удалена безвозвратно.${topicsWarning}`,
      confirmText: 'Удалить',
      danger: true,
    });
    if (!ok) return;
    deleteMutation.mutate(library.id, {
      onSuccess: () => toast.success('Тема удалена'),
      onError: (err) =>
        toast.error(
          (err as AxiosError<{ message?: string }>).response?.data?.message ??
            'Не удалось удалить тему. Возможно, у её топиков есть записи учеников.'
        ),
    });
  };

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" data-testid="page-title">
          Speaking Libraries
        </Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          data-testid="add-library-button"
          onClick={() => navigate('/speaking/libraries/new')}
        >
          Add Library
        </Button>
      </Box>

      <TextField
        placeholder="Search libraries…"
        value={search}
        onChange={(e) => setSearch(e.target.value)}
        size="small"
        data-testid="search-libraries"
        sx={{ mb: 2, minWidth: 320 }}
        InputProps={{
          startAdornment: (
            <InputAdornment position="start">
              <SearchIcon />
            </InputAdornment>
          ),
        }}
      />

      {isLoading && <SkeletonCard count={3} />}

      {isError && (
        <Alert
          severity="error"
          action={
            <Button color="inherit" size="small" onClick={() => refetch()}>
              Retry
            </Button>
          }
        >
          Не удалось загрузить список тем. Проверьте соединение и попробуйте снова.
        </Alert>
      )}

      {!isLoading && !isError && filtered.length === 0 && (
        <Box data-testid="libraries-empty">
          <EmptyState
            title={search ? 'Ничего не найдено' : 'Нет ни одной темы'}
            message={
              search
                ? `По запросу «${search}» темы не найдены.`
                : 'Нет ни одной темы. Создайте первую.'
            }
            action={
              search
                ? undefined
                : { label: 'Add Library', onClick: () => navigate('/speaking/libraries/new') }
            }
          />
        </Box>
      )}

      {!isLoading && !isError && filtered.length > 0 && (
        <TableContainer component={Paper}>
          <Table data-testid="libraries-table">
            <TableHead>
              <TableRow>
                <TableCell>Cover</TableCell>
                <TableCell>Name</TableCell>
                <TableCell align="right">Topics</TableCell>
                <TableCell align="right">Order</TableCell>
                <TableCell>Published</TableCell>
                <TableCell align="right">Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {filtered.map((library) => (
                <TableRow key={library.id} hover>
                  <TableCell>
                    <Avatar
                      src={library.coverUrl}
                      variant="rounded"
                      sx={{ width: 48, height: 48 }}
                    >
                      {library.name.charAt(0).toUpperCase()}
                    </Avatar>
                  </TableCell>
                  <TableCell>
                    <Typography variant="body1">{library.name}</Typography>
                    {library.description && (
                      <Typography variant="caption" color="text.secondary" noWrap>
                        {library.description}
                      </Typography>
                    )}
                  </TableCell>
                  <TableCell align="right">{library.topicsCount}</TableCell>
                  <TableCell align="right">{library.displayOrder}</TableCell>
                  <TableCell>
                    <Switch
                      checked={library.isPublished}
                      onChange={() => handleTogglePublish(library)}
                      disabled={publishMutation.isPending}
                      data-testid={`publish-switch-${library.id}`}
                      inputProps={{ 'aria-label': `publish ${library.name}` }}
                    />
                  </TableCell>
                  <TableCell align="right">
                    <Tooltip title="Edit">
                      <IconButton
                        onClick={() => navigate(`/speaking/libraries/${library.id}/edit`)}
                        data-testid={`edit-library-${library.id}`}
                      >
                        <EditIcon />
                      </IconButton>
                    </Tooltip>
                    <Tooltip title="Delete">
                      <IconButton
                        color="error"
                        onClick={() => handleDelete(library)}
                        data-testid={`delete-library-${library.id}`}
                      >
                        <DeleteIcon />
                      </IconButton>
                    </Tooltip>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      <ConfirmDialog
        open={confirmState.isOpen}
        title={confirmState.title}
        message={confirmState.message}
        confirmText={confirmState.confirmText}
        cancelText={confirmState.cancelText}
        variant={confirmState.danger ? 'danger' : 'warning'}
        loading={deleteMutation.isPending}
        onConfirm={handleConfirm}
        onCancel={handleCancel}
      />
    </Box>
  );
}
