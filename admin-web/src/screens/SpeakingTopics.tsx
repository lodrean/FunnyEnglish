import { useMemo, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  Chip,
  FormControl,
  IconButton,
  InputAdornment,
  InputLabel,
  Link,
  MenuItem,
  Paper,
  Select,
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
  Check as CheckIcon,
  Close as CloseIcon,
  Delete as DeleteIcon,
  Edit as EditIcon,
  OndemandVideo as VideoIcon,
  Search as SearchIcon,
  WarningAmber as WarningIcon,
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { AxiosError } from 'axios';
import { ConfirmDialog, EmptyState } from '../components/feedback';
import { SkeletonCard } from '../components/feedback/SkeletonCard';
import { useConfirm, useToast } from '../hooks';
import {
  useDeleteTopic,
  usePublishTopic,
  useSpeakingLibraries,
  useSpeakingTopics,
} from '../hooks/useSpeaking';
import type { SpeakingTopic } from '../api/speakingApi';
import { formatMmSs } from '../utils/format';

/** Опубликованный топик без видео или без вопросов — ученик не сможет пройти */
const isNotPlayable = (topic: SpeakingTopic) =>
  topic.isPublished && !topic.isArchived && (!topic.videoUrl || topic.questionsCount === 0);

export default function SpeakingTopics() {
  const navigate = useNavigate();
  const toast = useToast();
  const { confirm, confirmState, handleConfirm, handleCancel } = useConfirm();

  const [search, setSearch] = useState('');
  const [libraryFilter, setLibraryFilter] = useState<string>('');

  const { data: libraries } = useSpeakingLibraries();
  const {
    data: topics,
    isLoading,
    isError,
    refetch,
  } = useSpeakingTopics(libraryFilter || undefined);
  const publishMutation = usePublishTopic();
  const deleteMutation = useDeleteTopic();

  const filtered = useMemo(() => {
    const query = search.trim().toLowerCase();
    const list = topics ?? [];
    if (!query) return list;
    return list.filter((t) => t.name.toLowerCase().includes(query));
  }, [topics, search]);

  const handleTogglePublish = (topic: SpeakingTopic) => {
    publishMutation.mutate(
      { id: topic.id, isPublished: !topic.isPublished },
      {
        onError: (err) =>
          toast.error(
            (err as AxiosError<{ message?: string }>).response?.data?.message ??
              'Не удалось изменить публикацию'
          ),
      }
    );
  };

  const handleDelete = async (topic: SpeakingTopic) => {
    const ok = await confirm({
      title: 'Архивировать топик?',
      message: `Топик «${topic.name}» будет архивирован. Записи учеников сохранятся.`,
      confirmText: 'Архивировать',
      danger: true,
    });
    if (!ok) return;
    deleteMutation.mutate(topic.id, {
      onSuccess: () => toast.success('Топик архивирован'),
      onError: (err) =>
        toast.error(
          (err as AxiosError<{ message?: string }>).response?.data?.message ??
            'Не удалось архивировать топик'
        ),
    });
  };

  const noLibraries = !isLoading && (libraries ?? []).length === 0;

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" data-testid="page-title">
          Speaking Topics
        </Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          data-testid="add-topic-button"
          onClick={() =>
            navigate(
              libraryFilter ? `/speaking/topics/new?libraryId=${libraryFilter}` : '/speaking/topics/new'
            )
          }
        >
          Add Topic
        </Button>
      </Box>

      <Box sx={{ display: 'flex', gap: 2, mb: 2, flexWrap: 'wrap' }}>
        <TextField
          placeholder="Search topics…"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          size="small"
          data-testid="search-topics"
          sx={{ minWidth: 280 }}
          InputProps={{
            startAdornment: (
              <InputAdornment position="start">
                <SearchIcon />
              </InputAdornment>
            ),
          }}
        />
        <FormControl size="small" sx={{ minWidth: 220 }}>
          <InputLabel id="library-filter-label">Library</InputLabel>
          <Select
            labelId="library-filter-label"
            label="Library"
            value={libraryFilter}
            onChange={(e) => setLibraryFilter(e.target.value)}
            data-testid="library-filter-select"
          >
            <MenuItem value="">All libraries</MenuItem>
            {(libraries ?? []).map((lib) => (
              <MenuItem key={lib.id} value={lib.id}>
                {lib.name}
              </MenuItem>
            ))}
          </Select>
        </FormControl>
      </Box>

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
          Не удалось загрузить список топиков. Проверьте соединение и попробуйте снова.
        </Alert>
      )}

      {!isLoading && !isError && noLibraries && (
        <Box data-testid="topics-empty">
          <EmptyState
            title="Сначала создайте тему"
            message="Топики привязаны к темам (Libraries). Создайте первую тему, затем добавьте топики."
            action={{ label: 'Create Library', onClick: () => navigate('/speaking/libraries/new') }}
          />
        </Box>
      )}

      {!isLoading && !isError && !noLibraries && filtered.length === 0 && (
        <Box data-testid="topics-empty">
          <EmptyState
            title="Топиков пока нет"
            message="Создайте первый топик — ученики смотрят видео и отвечают на вопросы голосом."
            action={{ label: 'Add Topic', onClick: () => navigate('/speaking/topics/new') }}
          />
        </Box>
      )}

      {!isLoading && !isError && filtered.length > 0 && (
        <TableContainer component={Paper}>
          <Table data-testid="topics-table">
            <TableHead>
              <TableRow>
                <TableCell>Name</TableCell>
                <TableCell>Library</TableCell>
                <TableCell>Duration</TableCell>
                <TableCell>Video</TableCell>
                <TableCell>Subtitles</TableCell>
                <TableCell align="right">Questions</TableCell>
                <TableCell>Published</TableCell>
                <TableCell>Status</TableCell>
                <TableCell align="right">Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {filtered.map((topic) => (
                <TableRow
                  key={topic.id}
                  hover
                  sx={topic.isArchived ? { bgcolor: 'action.hover' } : undefined}
                >
                  <TableCell>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                      <Typography variant="body1">{topic.name}</Typography>
                      {isNotPlayable(topic) && (
                        <Tooltip title="Опубликованный топик неиграбелен: нет видео или нет вопросов">
                          <WarningIcon color="warning" fontSize="small" />
                        </Tooltip>
                      )}
                    </Box>
                  </TableCell>
                  <TableCell>{topic.libraryName ?? '—'}</TableCell>
                  <TableCell>
                    {topic.durationSeconds != null ? formatMmSs(topic.durationSeconds) : '—'}
                  </TableCell>
                  <TableCell>
                    {topic.videoUrl ? (
                      <Link href={topic.videoUrl} target="_blank" rel="noopener">
                        <VideoIcon fontSize="small" />
                      </Link>
                    ) : (
                      '—'
                    )}
                  </TableCell>
                  <TableCell>
                    {topic.subtitlesUrl ? (
                      <CheckIcon color="success" fontSize="small" />
                    ) : (
                      <CloseIcon color="disabled" fontSize="small" />
                    )}
                  </TableCell>
                  <TableCell align="right">{topic.questionsCount}</TableCell>
                  <TableCell>
                    <Switch
                      checked={topic.isPublished}
                      onChange={() => handleTogglePublish(topic)}
                      disabled={publishMutation.isPending || topic.isArchived}
                      data-testid={`publish-switch-${topic.id}`}
                      inputProps={{ 'aria-label': `publish ${topic.name}` }}
                    />
                  </TableCell>
                  <TableCell>
                    {topic.isArchived && <Chip label="Archived" size="small" />}
                  </TableCell>
                  <TableCell align="right">
                    <Tooltip title="Edit">
                      <IconButton
                        onClick={() => navigate(`/speaking/topics/${topic.id}/edit`)}
                        data-testid={`edit-topic-${topic.id}`}
                      >
                        <EditIcon />
                      </IconButton>
                    </Tooltip>
                    {!topic.isArchived && (
                      <Tooltip title="Archive">
                        <IconButton
                          color="error"
                          onClick={() => handleDelete(topic)}
                          data-testid={`delete-topic-${topic.id}`}
                        >
                          <DeleteIcon />
                        </IconButton>
                      </Tooltip>
                    )}
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
