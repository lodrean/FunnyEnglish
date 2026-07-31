import React, { useState, useEffect } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  TextField,
  Box,
  Typography,
  Divider,
  Alert,
  Chip,
} from '@mui/material';
import { QuestionTypeSelector } from './QuestionTypeSelector';
import { TextSelectEditor } from './TextSelectEditor';
import { ImageSelectEditor } from './ImageSelectEditor';
import { AudioSelectEditor } from './AudioSelectEditor';
import { DragDropMatchEditor } from './DragDropMatchEditor';
import { FillBlankEditor } from './FillBlankEditor';
import { ImageWordMatchEditor } from './ImageWordMatchEditor';
import {
  QuestionTypeV2,
  QuestionContent,
  CreateQuestionRequest,
  UpdateQuestionRequest,
  TextSelectContent,
  ImageSelectContent,
  AudioSelectContent,
  DragDropMatchContent,
  DragDropSortContent,
  FillBlankContent,
  ImageWordMatchContent,
  CreateImageWordMatchRequest,
} from '../../types/questions';

interface QuestionEditorProps {
  open: boolean;
  onClose: () => void;
  onSave: (data: CreateQuestionRequest | UpdateQuestionRequest) => void;
  initialData?: {
    id: string;
    type: QuestionTypeV2;
    title: string;
    content: QuestionContent;
    mediaUrl?: string;
    points: number;
    timeLimitSeconds?: number;
    explanation?: string;
    hint?: string;
    isPublished: boolean;
  } | null;
  testId?: string;
}

const createDefaultContent = (type: QuestionTypeV2): QuestionContent => {
  switch (type) {
    case 'TEXT_SELECT':
      return {
        text: '',
        answers: [
          { id: 'a1', text: '', isCorrect: false },
          { id: 'a2', text: '', isCorrect: false },
        ],
      } as TextSelectContent;
    case 'IMAGE_SELECT':
      return {
        text: '',
        answers: [
          { id: 'a1', imageUrl: '', isCorrect: false },
          { id: 'a2', imageUrl: '', isCorrect: false },
        ],
      } as ImageSelectContent;
    case 'AUDIO_SELECT':
      return {
        audioUrl: '',
        text: '',
        answers: [
          { id: 'a1', text: '', isCorrect: false },
          { id: 'a2', text: '', isCorrect: false },
        ],
      } as AudioSelectContent;
    case 'DRAG_DROP_MATCH':
      return {
        text: 'Соедините элементы:',
        items: [
          { id: 'i1', text: '', targetId: 't1' },
          { id: 'i2', text: '', targetId: 't2' },
        ],
        targets: [
          { id: 't1', text: '' },
          { id: 't2', text: '' },
        ],
      } as DragDropMatchContent;
    case 'DRAG_DROP_SORT':
      return {
        text: 'Упорядочите элементы:',
        items: [
          { id: 'i1', text: '', correctOrder: 1 },
          { id: 'i2', text: '', correctOrder: 2 },
          { id: 'i3', text: '', correctOrder: 3 },
        ],
      } as DragDropSortContent;
    case 'FILL_BLANK':
      return {
        textBefore: '',
        textAfter: '',
        answers: [
          { id: 'a1', text: '', isCorrect: false },
          { id: 'a2', text: '', isCorrect: false },
        ],
      } as FillBlankContent;
    case 'IMAGE_WORD_MATCH':
      return {
        imageUrl: '',
        instruction: 'Match the words to the objects',
        words: [],
        hotspots: [],
      } as ImageWordMatchContent;
    default:
      throw new Error(`Unknown question type: ${type}`);
  }
};

export const QuestionEditor: React.FC<QuestionEditorProps> = ({
  open,
  onClose,
  onSave,
  initialData,
  testId,
}) => {
  const isEditing = !!initialData;
  const [step, setStep] = useState<'type' | 'content'>(isEditing ? 'content' : 'type');
  const [type, setType] = useState<QuestionTypeV2 | undefined>(initialData?.type);
  const [title, setTitle] = useState(initialData?.title || '');
  const [content, setContent] = useState<QuestionContent | undefined>(initialData?.content);
  const [points, setPoints] = useState(initialData?.points || 1);
  const [explanation, setExplanation] = useState(initialData?.explanation || '');
  const [hint, setHint] = useState(initialData?.hint || '');
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (open) {
      setStep(isEditing ? 'content' : 'type');
      setType(initialData?.type);
      setTitle(initialData?.title || '');
      setContent(initialData?.content);
      setPoints(initialData?.points || 1);
      setExplanation(initialData?.explanation || '');
      setHint(initialData?.hint || '');
      setError(null);
    }
  }, [open, initialData, isEditing]);

  const handleTypeSelect = (selectedType: QuestionTypeV2) => {
    setType(selectedType);
    setContent(createDefaultContent(selectedType));
    setStep('content');
  };

  const validateContent = (): boolean => {
    if (!type || !content) return false;

    switch (type) {
      case 'TEXT_SELECT':
      case 'FILL_BLANK': {
        const textContent = content as TextSelectContent | FillBlankContent;
        if (!textContent.answers.some((a) => a.isCorrect)) {
          setError('Выберите хотя бы один правильный ответ');
          return false;
        }
        break;
      }
      case 'IMAGE_SELECT': {
        const imageContent = content as ImageSelectContent;
        if (!imageContent.answers.some((a) => a.isCorrect)) {
          setError('Выберите хотя бы один правильный ответ');
          return false;
        }
        break;
      }
      case 'AUDIO_SELECT': {
        const audioContent = content as AudioSelectContent;
        if (!audioContent.audioUrl) {
          setError('Загрузите аудио файл');
          return false;
        }
        if (!audioContent.answers.some((a) => a.isCorrect)) {
          setError('Выберите хотя бы один правильный ответ');
          return false;
        }
        break;
      }
      case 'DRAG_DROP_MATCH': {
        const matchContent = content as DragDropMatchContent;
        if (matchContent.items.length < 2) {
          setError('Добавьте хотя бы 2 пары');
          return false;
        }
        break;
      }
      case 'IMAGE_WORD_MATCH': {
        const imageWordContent = content as ImageWordMatchContent;
        if (imageWordContent.words.length < 2) {
          setError('Добавьте хотя бы 2 слова');
          return false;
        }
        if (imageWordContent.hotspots.length !== imageWordContent.words.length) {
          setError('Каждое слово должно быть связано с областью на изображении');
          return false;
        }
        break;
      }
    }

    return true;
  };

  const handleSave = () => {
    if (!type || !content) return;

    if (!title.trim()) {
      setError('Введите название вопроса');
      return;
    }

    if (!validateContent()) return;

    const data: CreateQuestionRequest | UpdateQuestionRequest = isEditing
      ? {
          title,
          content,
          points,
          explanation: explanation || undefined,
          hint: hint || undefined,
        }
      : {
          testId,
          type,
          title,
          content,
          points,
          explanation: explanation || undefined,
          hint: hint || undefined,
        };

    onSave(data);
    onClose();
  };

  const renderContentEditor = () => {
    if (!type || !content) return null;

    switch (type) {
      case 'TEXT_SELECT':
        return (
          <TextSelectEditor
            content={content as TextSelectContent}
            onChange={setContent}
          />
        );
      case 'IMAGE_SELECT':
        return (
          <ImageSelectEditor
            content={content as ImageSelectContent}
            onChange={setContent}
          />
        );
      case 'AUDIO_SELECT':
        return (
          <AudioSelectEditor
            content={content as AudioSelectContent}
            onChange={setContent}
          />
        );
      case 'DRAG_DROP_MATCH':
        return (
          <DragDropMatchEditor
            content={content as DragDropMatchContent}
            onChange={setContent}
          />
        );
      case 'FILL_BLANK':
        return (
          <FillBlankEditor
            content={content as FillBlankContent}
            onChange={setContent}
          />
        );
      case 'IMAGE_WORD_MATCH':
        return (
          <ImageWordMatchEditorWrapper
            testId={testId || ''}
            content={content as ImageWordMatchContent}
            onChange={setContent}
          />
        );
      default:
        return <Typography>Редактор для этого типа в разработке</Typography>;
    }
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>
        {isEditing ? 'Редактировать вопрос' : 'Новый вопрос'}
        {type && (
          <Chip
            label={type}
            size="small"
            color="primary"
            sx={{ ml: 2 }}
          />
        )}
      </DialogTitle>
      <DialogContent>
        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}

        {step === 'type' && !isEditing ? (
          <QuestionTypeSelector selectedType={type} onSelect={handleTypeSelect} />
        ) : (
          <Box>
            <TextField
              fullWidth
              label="Название вопроса"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              sx={{ mb: 2 }}
            />

            <Box display="flex" gap={2} mb={2}>
              <TextField
                type="number"
                label="Баллы"
                value={points}
                onChange={(e) => setPoints(Number(e.target.value))}
                inputProps={{ min: 1, max: 100 }}
                sx={{ width: 100 }}
              />
              <TextField
                fullWidth
                label="Подсказка (опционально)"
                value={hint}
                onChange={(e) => setHint(e.target.value)}
              />
            </Box>

            <Divider sx={{ my: 2 }} />

            {renderContentEditor()}

            <Divider sx={{ my: 2 }} />

            <TextField
              fullWidth
              multiline
              rows={2}
              label="Пояснение к ответу (показывается после)"
              value={explanation}
              onChange={(e) => setExplanation(e.target.value)}
            />
          </Box>
        )}
      </DialogContent>
      <DialogActions>
        {step === 'content' && !isEditing && (
          <Button onClick={() => setStep('type')}>Назад</Button>
        )}
        <Button onClick={onClose}>Отмена</Button>
        {step === 'content' && (
          <Button onClick={handleSave} variant="contained">
            {isEditing ? 'Сохранить' : 'Создать'}
          </Button>
        )}
      </DialogActions>
    </Dialog>
  );
};

// Wrapper component to adapt ImageWordMatchEditor to QuestionEditor interface
interface ImageWordMatchEditorWrapperProps {
  testId: string;
  content: ImageWordMatchContent;
  onChange: (content: QuestionContent) => void;
}

const ImageWordMatchEditorWrapper: React.FC<ImageWordMatchEditorWrapperProps> = ({
  testId,
  content,
  onChange,
}) => {
  const handleSave = (data: CreateImageWordMatchRequest) => {
    // Convert CreateImageWordMatchRequest back to ImageWordMatchContent
    const newContent: ImageWordMatchContent = {
      imageUrl: data.imageUrl,
      instruction: data.instruction,
      words: data.words,
      hotspots: data.hotspots,
    };
    onChange(newContent);
  };

  return (
    <ImageWordMatchEditor
      testId={testId}
      initialContent={content}
      onSave={handleSave}
      onCancel={() => {}}
    />
  );
};
