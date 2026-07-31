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
  Tooltip,
  Collapse,
  Button,
} from '@mui/material';
import {
  DragIndicator,
  Edit,
  Delete,
  ExpandMore,
  ExpandLess,
  TextFields,
  Image,
  Audiotrack,
  DragHandle,
  Input,
  CheckCircle,
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

// Question Types
export type QuestionType =
  | 'TEXT_SELECT'
  | 'IMAGE_SELECT'
  | 'AUDIO_SELECT'
  | 'DRAG_DROP'
  | 'TEXT_INPUT';

export interface Question {
  id: string;
  type: QuestionType;
  question: string;
  options?: string[];
  correctAnswers?: string[] | number[];
  images?: string[];
  audioUrl?: string;
  points: number;
  order: number;
  explanation?: string;
  hint?: string;
  timeLimit?: number; // in seconds
}

interface QuestionListProps {
  questions: Question[];
  onReorder: (questions: Question[]) => void;
  onEdit: (questionId: string) => void;
  onDelete: (questionId: string) => void;
  onPreview?: (questionId: string) => void;
}

interface SortableQuestionItemProps {
  question: Question;
  index: number;
  onEdit: (questionId: string) => void;
  onDelete: (questionId: string) => void;
  onPreview?: (questionId: string) => void;
}

// Question Type Configuration
const questionTypeConfig: Record<QuestionType, { label: string; icon: React.ElementType; color: string }> = {
  TEXT_SELECT: {
    label: 'Text Choice',
    icon: TextFields,
    color: colors.primary,
  },
  IMAGE_SELECT: {
    label: 'Image Choice',
    icon: Image,
    color: colors.success,
  },
  AUDIO_SELECT: {
    label: 'Audio Choice',
    icon: Audiotrack,
    color: colors.warning,
  },
  DRAG_DROP: {
    label: 'Drag & Drop',
    icon: DragHandle,
    color: colors.info,
  },
  TEXT_INPUT: {
    label: 'Text Input',
    icon: Input,
    color: colors.textSecondary,
  },
};

// Get correct answer count for display
const getCorrectAnswerCount = (question: Question): number => {
  if (!question.correctAnswers) return 0;
  return question.correctAnswers.length;
};

// Get option count for display
const getOptionCount = (question: Question): number => {
  if (question.options) return question.options.length;
  if (question.images) return question.images.length;
  return 0;
};

// Sortable Question Item Component
const SortableQuestionItem: React.FC<SortableQuestionItemProps> = ({
  question,
  index,
  onEdit,
  onDelete,
  onPreview,
}) => {
  const [expanded, setExpanded] = useState(false);

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

  const typeConfig = questionTypeConfig[question.type];
  const TypeIcon = typeConfig.icon;
  const correctCount = getCorrectAnswerCount(question);
  const optionCount = getOptionCount(question);

  // Truncate question text for preview
  const previewText = question.question.length > 100
    ? question.question.substring(0, 100) + '...'
    : question.question;

  return (
    <Paper
      ref={setNodeRef}
      style={style}
      sx={{
        marginBottom: '8px',
        backgroundColor: colors.card,
        border: `1px solid ${isDragging ? colors.primary : '#e0e0e0'}`,
        borderRadius: '12px',
        overflow: 'hidden',
        '&:hover': {
          boxShadow: '0 2px 12px rgba(0,0,0,0.1)',
        },
      }}
    >
      {/* Main Row */}
      <Box
        sx={{
          display: 'flex',
          alignItems: 'center',
          padding: '12px 16px',
        }}
      >
        {/* Question Number */}
        <Box
          sx={{
            width: 32,
            height: 32,
            borderRadius: '50%',
            backgroundColor: colors.primary,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            mr: 2,
          }}
        >
          <Typography variant="body2" sx={{ color: '#fff', fontWeight: 600 }}>
            {index + 1}
          </Typography>
        </Box>

        {/* Drag Handle */}
        <Box
          {...attributes}
          {...listeners}
          sx={{
            cursor: 'grab',
            display: 'flex',
            alignItems: 'center',
            color: colors.textSecondary,
            mr: 2,
            '&:active': { cursor: 'grabbing' },
          }}
        >
          <DragIndicator />
        </Box>

        {/* Type Icon */}
        <Box
          sx={{
            width: 40,
            height: 40,
            borderRadius: '8px',
            backgroundColor: `${typeConfig.color}15`,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            mr: 2,
          }}
        >
          <TypeIcon sx={{ color: typeConfig.color }} />
        </Box>

        {/* Question Preview */}
        <Box sx={{ flex: 1, minWidth: 0, mr: 2 }}>
          <Typography
            variant="body1"
            sx={{
              color: colors.textPrimary,
              fontWeight: 500,
              overflow: 'hidden',
              textOverflow: 'ellipsis',
              whiteSpace: expanded ? 'normal' : 'nowrap',
            }}
          >
            {previewText}
          </Typography>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mt: 0.5 }}>
            <Chip
              size="small"
              label={typeConfig.label}
              sx={{
                backgroundColor: `${typeConfig.color}15`,
                color: typeConfig.color,
                fontSize: '0.7rem',
                height: 20,
              }}
            />
            {optionCount > 0 && (
              <Typography variant="caption" sx={{ color: colors.textSecondary }}>
                {optionCount} options
              </Typography>
            )}
            {correctCount > 0 && (
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                <CheckCircle fontSize="small" sx={{ color: colors.success, fontSize: 14 }} />
                <Typography variant="caption" sx={{ color: colors.success }}>
                  {correctCount} correct
                </Typography>
              </Box>
            )}
          </Box>
        </Box>

        {/* Points */}
        <Box
          sx={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            minWidth: 60,
            height: 36,
            borderRadius: '18px',
            backgroundColor: colors.background,
            mr: 2,
          }}
        >
          <Typography variant="body2" sx={{ color: colors.primary, fontWeight: 600 }}>
            {question.points} pts
          </Typography>
        </Box>

        {/* Expand Button */}
        <IconButton
          size="small"
          onClick={() => setExpanded(!expanded)}
          sx={{ mr: 1 }}
        >
          {expanded ? <ExpandLess /> : <ExpandMore />}
        </IconButton>

        {/* Actions */}
        <Box sx={{ display: 'flex', gap: 0.5 }}>
          <Tooltip title="Edit">
            <IconButton size="small" onClick={() => onEdit(question.id)} sx={{ color: colors.primary }}>
              <Edit fontSize="small" />
            </IconButton>
          </Tooltip>
          <Tooltip title="Delete">
            <IconButton size="small" onClick={() => onDelete(question.id)} sx={{ color: colors.error }}>
              <Delete fontSize="small" />
            </IconButton>
          </Tooltip>
        </Box>
      </Box>

      {/* Expanded Content - Question Preview */}
      <Collapse in={expanded}>
        <Box
          sx={{
            padding: '16px',
            backgroundColor: colors.background,
            borderTop: '1px solid #e0e0e0',
          }}
        >
          <Typography variant="subtitle2" sx={{ color: colors.textPrimary, mb: 1 }}>
            Full Question:
          </Typography>
          <Typography variant="body2" sx={{ color: colors.textSecondary, mb: 2 }}>
            {question.question}
          </Typography>

          {/* Options Preview */}
          {question.options && question.options.length > 0 && (
            <Box sx={{ mb: 2 }}>
              <Typography variant="subtitle2" sx={{ color: colors.textPrimary, mb: 1 }}>
                Options:
              </Typography>
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0.5 }}>
                {question.options.map((option, idx) => (
                  <Box
                    key={idx}
                    sx={{
                      display: 'flex',
                      alignItems: 'center',
                      gap: 1,
                      padding: '8px 12px',
                      backgroundColor: colors.card,
                      borderRadius: '8px',
                      border: question.correctAnswers?.includes(idx) ||
                        question.correctAnswers?.includes(option)
                        ? `1px solid ${colors.success}`
                        : '1px solid transparent',
                    }}
                  >
                    {question.correctAnswers?.includes(idx) ||
                      question.correctAnswers?.includes(option) ? (
                      <CheckCircle fontSize="small" sx={{ color: colors.success }} />
                    ) : (
                      <Box sx={{ width: 20 }} />
                    )}
                    <Typography variant="body2">{option}</Typography>
                  </Box>
                ))}
              </Box>
            </Box>
          )}

          {/* Explanation */}
          {question.explanation && (
            <Box sx={{ mt: 2 }}>
              <Typography variant="subtitle2" sx={{ color: colors.textPrimary, mb: 0.5 }}>
                Explanation:
              </Typography>
              <Typography variant="body2" sx={{ color: colors.textSecondary }}>
                {question.explanation}
              </Typography>
            </Box>
          )}

          {/* Hint */}
          {question.hint && (
            <Box sx={{ mt: 2 }}>
              <Typography variant="subtitle2" sx={{ color: colors.textPrimary, mb: 0.5 }}>
                Hint:
              </Typography>
              <Typography variant="body2" sx={{ color: colors.warning }}>
                {question.hint}
              </Typography>
            </Box>
          )}

          {/* Time Limit */}
          {question.timeLimit && (
            <Box sx={{ mt: 2 }}>
              <Typography variant="caption" sx={{ color: colors.textSecondary }}>
                Time limit: {question.timeLimit} seconds
              </Typography>
            </Box>
          )}
        </Box>
      </Collapse>
    </Paper>
  );
};

// Main Question List Component
const QuestionList: React.FC<QuestionListProps> = ({
  questions,
  onReorder,
  onEdit,
  onDelete,
  onPreview,
}) => {
  const [activeId, setActiveId] = useState<string | null>(null);

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

  const handleDragStart = (event: DragStartEvent) => {
    setActiveId(event.active.id as string);
  };

  const handleDragEnd = (event: DragEndEvent) => {
    const { active, over } = event;
    setActiveId(null);

    if (over && active.id !== over.id) {
      const oldIndex = questions.findIndex((q) => q.id === active.id);
      const newIndex = questions.findIndex((q) => q.id === over.id);

      if (oldIndex !== -1 && newIndex !== -1) {
        const newQuestions = [...questions];
        const [movedItem] = newQuestions.splice(oldIndex, 1);
        newQuestions.splice(newIndex, 0, movedItem);

        // Update order values
        const updatedQuestions = newQuestions.map((q, index) => ({
          ...q,
          order: index,
        }));

        onReorder(updatedQuestions);
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

  const activeQuestion = activeId ? questions.find((q) => q.id === activeId) : null;

  // Calculate total points
  const totalPoints = questions.reduce((sum, q) => sum + q.points, 0);

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
        <Box>
          <Typography variant="h6" sx={{ color: colors.textPrimary, fontWeight: 600 }}>
            Questions
          </Typography>
          <Typography variant="body2" sx={{ color: colors.textSecondary }}>
            {questions.length} questions · {totalPoints} total points
          </Typography>
        </Box>
      </Box>

      {/* Drag and Drop Context */}
      <DndContext
        sensors={sensors}
        collisionDetection={closestCenter}
        onDragStart={handleDragStart}
        onDragEnd={handleDragEnd}
      >
        <SortableContext
          items={questions.map((q) => q.id)}
          strategy={verticalListSortingStrategy}
        >
          <Box sx={{ display: 'flex', flexDirection: 'column' }}>
            {questions.map((question, index) => (
              <SortableQuestionItem
                key={question.id}
                question={question}
                index={index}
                onEdit={onEdit}
                onDelete={onDelete}
                onPreview={onPreview}
              />
            ))}
          </Box>
        </SortableContext>

        {/* Drag Overlay */}
        <DragOverlay dropAnimation={dropAnimation}>
          {activeQuestion ? (
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
              <Box
                sx={{
                  width: 32,
                  height: 32,
                  borderRadius: '50%',
                  backgroundColor: colors.primary,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  mr: 2,
                }}
              >
                <Typography variant="body2" sx={{ color: '#fff', fontWeight: 600 }}>
                  {questions.findIndex((q) => q.id === activeQuestion.id) + 1}
                </Typography>
              </Box>
              <DragIndicator sx={{ mr: 1 }} />
              <Typography sx={{ fontWeight: 500 }}>
                {activeQuestion.question.substring(0, 50)}...
              </Typography>
            </Paper>
          ) : null}
        </DragOverlay>
      </DndContext>

      {/* Empty State */}
      {questions.length === 0 && (
        <Paper
          sx={{
            padding: 4,
            textAlign: 'center',
            backgroundColor: colors.background,
            border: '2px dashed #ccc',
          }}
        >
          <Typography variant="h6" sx={{ color: colors.textSecondary, mb: 1 }}>
            No questions yet
          </Typography>
          <Typography variant="body2" sx={{ color: colors.textSecondary, mb: 2 }}>
            Add your first question to get started
          </Typography>
        </Paper>
      )}
    </Box>
  );
};

export default QuestionList;
