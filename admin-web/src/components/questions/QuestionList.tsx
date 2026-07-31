import React, { useState } from 'react';
import {
  Box,
  List,
  ListItem,
  ListItemText,
  IconButton,
  Typography,
  Chip,
  Paper,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Tooltip,
} from '@mui/material';
import {
  Edit,
  Delete,
  Add,
  DragHandle,
  CheckCircle,
  Image,
  Audiotrack,
  DragIndicator,
  ShortText,
  TextFields,
  ContentCopy,
  ImageSearch,
} from '@mui/icons-material';
import {
  DndContext,
  closestCenter,
  KeyboardSensor,
  PointerSensor,
  useSensor,
  useSensors,
} from '@dnd-kit/core';
import {
  arrayMove,
  SortableContext,
  sortableKeyboardCoordinates,
  verticalListSortingStrategy,
  useSortable,
} from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import { QuestionV2, QuestionTypeV2, QuestionContent } from '../../types/questions';

interface SortableQuestionItemProps {
  question: QuestionV2;
  index: number;
  onEdit: (question: QuestionV2) => void;
  onDelete: (id: string) => void;
  onDuplicate: (id: string) => void;
}

const typeIcons: Record<QuestionTypeV2, React.ReactNode> = {
  TEXT_SELECT: <TextFields />,
  IMAGE_SELECT: <Image />,
  AUDIO_SELECT: <Audiotrack />,
  DRAG_DROP_MATCH: <DragIndicator />,
  DRAG_DROP_SORT: <DragIndicator />,
  FILL_BLANK: <ShortText />,
  IMAGE_WORD_MATCH: <ImageSearch />,
};

const typeLabels: Record<QuestionTypeV2, string> = {
  TEXT_SELECT: 'Текст',
  IMAGE_SELECT: 'Изображение',
  AUDIO_SELECT: 'Аудио',
  DRAG_DROP_MATCH: 'Сопоставление',
  DRAG_DROP_SORT: 'Сортировка',
  FILL_BLANK: 'Пропуск',
  IMAGE_WORD_MATCH: 'Изображение и слово',
};

const SortableQuestionItem: React.FC<SortableQuestionItemProps> = ({
  question,
  index,
  onEdit,
  onDelete,
  onDuplicate,
}) => {
  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
    isDragging,
  } = useSortable({ id: question.id });

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
    opacity: isDragging ? 0.5 : 1,
  };

  // Extract preview text from content
  const getPreviewText = (content: QuestionContent): string => {
    if ('text' in content && content.text) {
      return content.text;
    }
    if ('textBefore' in content && content.textBefore) {
      return `${content.textBefore} [...] ${content.textAfter || ''}`;
    }
    return 'Без текста';
  };

  return (
    <ListItem
      ref={setNodeRef}
      style={style}
      component={Paper}
      sx={{ mb: 1, display: 'flex', alignItems: 'center' }}
    >
      <Box {...attributes} {...listeners} sx={{ cursor: 'grab', mr: 2 }}>
        <DragHandle color="action" />
      </Box>
      <Box sx={{ mr: 2, minWidth: 40 }}>
        <Typography variant="body2" color="text.secondary">
          #{index + 1}
        </Typography>
      </Box>
      <Box sx={{ mr: 2, color: 'primary.main' }}>{typeIcons[question.type]}</Box>
      <ListItemText
        primary={
          <Box display="flex" alignItems="center" gap={1}>
            <Typography variant="body1" noWrap sx={{ maxWidth: 300 }}>
              {question.title}
            </Typography>
            <Chip
              label={typeLabels[question.type]}
              size="small"
              variant="outlined"
            />
            <Chip
              label={`${question.points} балл${question.points !== 1 ? 'ов' : ''}`}
              size="small"
              color="primary"
              variant="outlined"
            />
            {question.isPublished && (
              <Chip
                icon={<CheckCircle />}
                label="Опубликован"
                size="small"
                color="success"
                variant="outlined"
              />
            )}
          </Box>
        }
        secondary={getPreviewText(question.content)}
      />
      <Box>
        <Tooltip title="Дублировать">
          <IconButton onClick={() => onDuplicate(question.id)} color="primary">
            <ContentCopy />
          </IconButton>
        </Tooltip>
        <Tooltip title="Редактировать">
          <IconButton onClick={() => onEdit(question)}>
            <Edit />
          </IconButton>
        </Tooltip>
        <Tooltip title="Удалить">
          <IconButton onClick={() => onDelete(question.id)} color="error">
            <Delete />
          </IconButton>
        </Tooltip>
      </Box>
    </ListItem>
  );
};

interface QuestionListProps {
  questions: QuestionV2[];
  onReorder: (questionIds: string[]) => void;
  onEdit: (question: QuestionV2) => void;
  onDelete: (id: string) => void;
  onDuplicate: (id: string) => void;
  onAdd: () => void;
}

export const QuestionList: React.FC<QuestionListProps> = ({
  questions,
  onReorder,
  onEdit,
  onDelete,
  onDuplicate,
  onAdd,
}) => {
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [questionToDelete, setQuestionToDelete] = useState<string | null>(null);

  const sensors = useSensors(
    useSensor(PointerSensor),
    useSensor(KeyboardSensor, {
      coordinateGetter: sortableKeyboardCoordinates,
    })
  );

  const handleDragEnd = (event: any) => {
    const { active, over } = event;

    if (active.id !== over.id) {
      const oldIndex = questions.findIndex((q) => q.id === active.id);
      const newIndex = questions.findIndex((q) => q.id === over.id);
      const reordered = arrayMove(questions, oldIndex, newIndex);
      onReorder(reordered.map((q) => q.id));
    }
  };

  const handleDeleteClick = (id: string) => {
    setQuestionToDelete(id);
    setDeleteDialogOpen(true);
  };

  const handleConfirmDelete = () => {
    if (questionToDelete) {
      onDelete(questionToDelete);
    }
    setDeleteDialogOpen(false);
    setQuestionToDelete(null);
  };

  const totalPoints = questions.reduce((sum, q) => sum + q.points, 0);

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
        <Box>
          <Typography variant="h6">
            Вопросы ({questions.length})
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Всего баллов: {totalPoints}
          </Typography>
        </Box>
        <Button
          variant="contained"
          startIcon={<Add />}
          onClick={onAdd}
        >
          Добавить вопрос
        </Button>
      </Box>

      {questions.length === 0 ? (
        <Paper sx={{ p: 4, textAlign: 'center' }}>
          <Typography color="text.secondary" gutterBottom>
            В этом тесте пока нет вопросов
          </Typography>
          <Button variant="outlined" onClick={onAdd} startIcon={<Add />}>
            Создать первый вопрос
          </Button>
        </Paper>
      ) : (
        <DndContext
          sensors={sensors}
          collisionDetection={closestCenter}
          onDragEnd={handleDragEnd}
        >
          <SortableContext
            items={questions.map((q) => q.id)}
            strategy={verticalListSortingStrategy}
          >
            <List>
              {questions.map((question, index) => (
                <SortableQuestionItem
                  key={question.id}
                  question={question}
                  index={index}
                  onEdit={onEdit}
                  onDelete={handleDeleteClick}
                  onDuplicate={onDuplicate}
                />
              ))}
            </List>
          </SortableContext>
        </DndContext>
      )}

      <Dialog open={deleteDialogOpen} onClose={() => setDeleteDialogOpen(false)}>
        <DialogTitle>Удалить вопрос?</DialogTitle>
        <DialogContent>
          <Typography>
            Это действие нельзя отменить. Вопрос будет удалён из теста.
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteDialogOpen(false)}>Отмена</Button>
          <Button onClick={handleConfirmDelete} color="error" variant="contained">
            Удалить
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};
