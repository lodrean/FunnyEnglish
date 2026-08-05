import { useEffect, useMemo, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  IconButton,
  List,
  ListItem,
  Paper,
  TextField,
  Tooltip,
  Typography,
} from '@mui/material';
import {
  ArrowDownward as DownIcon,
  ArrowUpward as UpIcon,
  Delete as DeleteIcon,
  Edit as EditIcon,
} from '@mui/icons-material';
import { AxiosError } from 'axios';
import { ConfirmDialog, EmptyState } from '../feedback';
import { useConfirm, useToast } from '../../hooks';
import {
  useDeleteQuestion,
  useReorderQuestions,
  useSaveQuestion,
  useTopicQuestions,
} from '../../hooks/useSpeaking';
import type { SpeakingQuestion } from '../../api/speakingApi';

const MAX_QUESTION_LENGTH = 500;

interface TopicQuestionsEditorProps {
  topicId: string;
}

/**
 * Вкладка Questions редактора топика: CRUD + reorder ↑/↓ (без DnD — кнопки стабильнее в E2E).
 * Reorder-endpoint'а на backend нет — «Save order» делает цепочку PUT displayOrder (см. speakingApi).
 */
export default function TopicQuestionsEditor({ topicId }: TopicQuestionsEditorProps) {
  const toast = useToast();
  const { confirm, confirmState, handleConfirm, handleCancel } = useConfirm();

  const { data: questions, isLoading, isError, refetch } = useTopicQuestions(topicId);
  const saveMutation = useSaveQuestion(topicId);
  const deleteMutation = useDeleteQuestion(topicId);
  const reorderMutation = useReorderQuestions(topicId);

  const [newText, setNewText] = useState('');
  const [editingId, setEditingId] = useState<string | null>(null);
  const [editText, setEditText] = useState('');
  // Локальный порядок после перестановок ↑/↓ до нажатия «Save order»
  const [localOrder, setLocalOrder] = useState<SpeakingQuestion[] | null>(null);

  const sorted = useMemo(
    () => [...(questions ?? [])].sort((a, b) => a.displayOrder - b.displayOrder),
    [questions]
  );
  const displayed = localOrder ?? sorted;
  const orderChanged = localOrder !== null;

  // Сброс локального порядка при обновлении данных с сервера
  useEffect(() => {
    setLocalOrder(null);
  }, [questions]);

  const mutationError = (err: unknown, fallback: string) =>
    (err as AxiosError<{ message?: string }>).response?.data?.message ?? fallback;

  const handleAdd = () => {
    const text = newText.trim();
    if (!text) {
      toast.warning('Текст вопроса не может быть пустым');
      return;
    }
    if (text.length > MAX_QUESTION_LENGTH) {
      toast.warning(`Максимум ${MAX_QUESTION_LENGTH} символов`);
      return;
    }
    if (sorted.some((q) => q.text.trim().toLowerCase() === text.toLowerCase())) {
      toast.warning('Такой вопрос уже есть в топике');
    }
    const maxOrder = sorted.reduce((max, q) => Math.max(max, q.displayOrder), -1);
    saveMutation.mutate(
      { data: { text, displayOrder: maxOrder + 1 } },
      {
        onSuccess: () => {
          setNewText('');
          toast.success('Вопрос добавлен');
        },
        onError: (err) => toast.error(mutationError(err, 'Не удалось добавить вопрос')),
      }
    );
  };

  const handleStartEdit = (q: SpeakingQuestion) => {
    setEditingId(q.id);
    setEditText(q.text);
  };

  const handleSaveEdit = (q: SpeakingQuestion) => {
    const text = editText.trim();
    if (!text) {
      toast.warning('Текст вопроса не может быть пустым');
      return;
    }
    saveMutation.mutate(
      { id: q.id, data: { text, displayOrder: q.displayOrder } },
      {
        onSuccess: () => {
          setEditingId(null);
          toast.success('Вопрос сохранён');
        },
        onError: (err) => toast.error(mutationError(err, 'Не удалось сохранить вопрос')),
      }
    );
  };

  const handleDelete = async (q: SpeakingQuestion) => {
    const ok = await confirm({
      title: 'Удалить вопрос?',
      message: `Вопрос «${q.text.slice(0, 80)}${q.text.length > 80 ? '…' : ''}» будет удалён.`,
      confirmText: 'Удалить',
      danger: true,
    });
    if (!ok) return;
    deleteMutation.mutate(q.id, {
      onSuccess: () => toast.success('Вопрос удалён'),
      onError: (err) => toast.error(mutationError(err, 'Не удалось удалить вопрос')),
    });
  };

  const move = (index: number, direction: -1 | 1) => {
    const next = [...displayed];
    const target = index + direction;
    if (target < 0 || target >= next.length) return;
    [next[index], next[target]] = [next[target], next[index]];
    setLocalOrder(next.map((q, i) => ({ ...q, displayOrder: i })));
  };

  const handleSaveOrder = () => {
    if (!localOrder) return;
    reorderMutation.mutate(localOrder, {
      onSuccess: () => toast.success('Порядок сохранён'),
      onError: (err) => toast.error(mutationError(err, 'Не удалось сохранить порядок')),
    });
  };

  if (isLoading) return <Typography color="text.secondary">Загрузка вопросов…</Typography>;

  if (isError) {
    return (
      <Alert
        severity="error"
        action={
          <Button color="inherit" size="small" onClick={() => refetch()}>
            Retry
          </Button>
        }
      >
        Не удалось загрузить вопросы топика.
      </Alert>
    );
  }

  return (
    <Box>
      {displayed.length === 0 ? (
        <Box data-testid="questions-empty">
          <EmptyState
            title="У топика пока нет вопросов"
            message="Добавьте первый — ученики отвечают на вопросы голосом."
          />
        </Box>
      ) : (
        <Paper variant="outlined">
          <List disablePadding>
            {displayed.map((q, index) => (
              <ListItem
                key={q.id}
                data-testid={`question-item-${q.id}`}
                sx={{
                  borderBottom: index < displayed.length - 1 ? '1px solid' : 'none',
                  borderColor: 'divider',
                  alignItems: 'flex-start',
                  gap: 1,
                }}
                secondaryAction={
                  <Box sx={{ display: 'flex', alignItems: 'center' }}>
                    <Tooltip title="Выше">
                      <span>
                        <IconButton
                          size="small"
                          disabled={index === 0}
                          onClick={() => move(index, -1)}
                          data-testid={`question-up-${q.id}`}
                        >
                          <UpIcon fontSize="small" />
                        </IconButton>
                      </span>
                    </Tooltip>
                    <Tooltip title="Ниже">
                      <span>
                        <IconButton
                          size="small"
                          disabled={index === displayed.length - 1}
                          onClick={() => move(index, 1)}
                          data-testid={`question-down-${q.id}`}
                        >
                          <DownIcon fontSize="small" />
                        </IconButton>
                      </span>
                    </Tooltip>
                    <Tooltip title="Edit">
                      <IconButton size="small" onClick={() => handleStartEdit(q)}>
                        <EditIcon fontSize="small" />
                      </IconButton>
                    </Tooltip>
                    <Tooltip title="Delete">
                      <IconButton size="small" color="error" onClick={() => handleDelete(q)}>
                        <DeleteIcon fontSize="small" />
                      </IconButton>
                    </Tooltip>
                  </Box>
                }
              >
                <Box sx={{ flex: 1, pr: 2 }}>
                  <Typography variant="caption" color="text.secondary">
                    #{index + 1}
                  </Typography>
                  {editingId === q.id ? (
                    <Box sx={{ display: 'flex', gap: 1, alignItems: 'flex-start', mt: 0.5 }}>
                      <TextField
                        value={editText}
                        onChange={(e) => setEditText(e.target.value)}
                        size="small"
                        fullWidth
                        multiline
                        inputProps={{ maxLength: MAX_QUESTION_LENGTH }}
                        autoFocus
                      />
                      <Button
                        size="small"
                        variant="contained"
                        onClick={() => handleSaveEdit(q)}
                        disabled={saveMutation.isPending}
                      >
                        Save
                      </Button>
                      <Button size="small" onClick={() => setEditingId(null)}>
                        Cancel
                      </Button>
                    </Box>
                  ) : (
                    <Typography variant="body2" sx={{ whiteSpace: 'pre-wrap' }}>
                      {q.text}
                    </Typography>
                  )}
                </Box>
              </ListItem>
            ))}
          </List>
        </Paper>
      )}

      {orderChanged && (
        <Box sx={{ mt: 2 }}>
          <Button
            variant="contained"
            onClick={handleSaveOrder}
            disabled={reorderMutation.isPending}
            data-testid="save-order-button"
          >
            Save order
          </Button>
        </Box>
      )}

      <Box sx={{ display: 'flex', gap: 1, mt: 3, alignItems: 'flex-start' }}>
        <TextField
          placeholder="New question text…"
          value={newText}
          onChange={(e) => setNewText(e.target.value)}
          size="small"
          fullWidth
          multiline
          inputProps={{ maxLength: MAX_QUESTION_LENGTH }}
          helperText={`${newText.length}/${MAX_QUESTION_LENGTH}`}
        />
        <Button
          variant="contained"
          onClick={handleAdd}
          disabled={saveMutation.isPending}
          data-testid="add-question-button"
        >
          Add question
        </Button>
      </Box>

      <ConfirmDialog
        open={confirmState.isOpen}
        title={confirmState.title}
        message={confirmState.message}
        confirmText={confirmState.confirmText}
        cancelText={confirmState.cancelText}
        variant={confirmState.danger ? 'danger' : 'warning'}
        onConfirm={handleConfirm}
        onCancel={handleCancel}
      />
    </Box>
  );
}
