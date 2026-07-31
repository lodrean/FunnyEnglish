import React from 'react';
import {
  Box,
  TextField,
  FormControlLabel,
  Checkbox,
  IconButton,
  Button,
  Typography,
  Paper,
  Chip,
} from '@mui/material';
import { Delete, Add } from '@mui/icons-material';
import { FillBlankContent, AnswerOption } from '../../types/questions';

interface FillBlankEditorProps {
  content: FillBlankContent;
  onChange: (content: FillBlankContent) => void;
}

export const FillBlankEditor: React.FC<FillBlankEditorProps> = ({
  content,
  onChange,
}) => {
  const handleTextBeforeChange = (text: string) => {
    onChange({ ...content, textBefore: text });
  };

  const handleTextAfterChange = (text: string) => {
    onChange({ ...content, textAfter: text });
  };

  const handleAnswerChange = (index: number, field: keyof AnswerOption, value: any) => {
    const newAnswers = [...content.answers];
    newAnswers[index] = { ...newAnswers[index], [field]: value };
    onChange({ ...content, answers: newAnswers });
  };

  const handleAddAnswer = () => {
    const newAnswer: AnswerOption = {
      id: `answer_${Date.now()}`,
      text: '',
      isCorrect: false,
    };
    onChange({ ...content, answers: [...content.answers, newAnswer] });
  };

  const handleDeleteAnswer = (index: number) => {
    const newAnswers = content.answers.filter((_, i) => i !== index);
    onChange({ ...content, answers: newAnswers });
  };

  const previewText = `${content.textBefore} [_____] ${content.textAfter}`;

  return (
    <Box>
      <Paper sx={{ p: 2, mb: 3, bgcolor: 'grey.50' }}>
        <Typography variant="subtitle2" color="text.secondary" gutterBottom>
          Предпросмотр:
        </Typography>
        <Typography variant="h6">
          {previewText}
        </Typography>
      </Paper>

      <Box display="flex" gap={2} mb={3}>
        <TextField
          fullWidth
          label="Текст до пропуска"
          value={content.textBefore}
          onChange={(e) => handleTextBeforeChange(e.target.value)}
          placeholder="I"
        />
        <Chip label="ПРОПУСК" color="primary" sx={{ alignSelf: 'center' }} />
        <TextField
          fullWidth
          label="Текст после пропуска"
          value={content.textAfter}
          onChange={(e) => handleTextAfterChange(e.target.value)}
          placeholder="an apple every day."
        />
      </Box>

      <Typography variant="subtitle1" gutterBottom>
        Варианты ответов
      </Typography>

      <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
        Правильный ответ должен точно соответствовать слову, которое подходит в пропуск
      </Typography>

      {content.answers.map((answer, index) => (
        <Paper key={answer.id} sx={{ p: 2, mb: 2 }}>
          <Box display="flex" alignItems="center" gap={2}>
            <TextField
              fullWidth
              label={`Вариант ${index + 1}`}
              value={answer.text}
              onChange={(e) => handleAnswerChange(index, 'text', e.target.value)}
              placeholder="eat"
            />
            <FormControlLabel
              control={
                <Checkbox
                  checked={answer.isCorrect}
                  onChange={(e) => handleAnswerChange(index, 'isCorrect', e.target.checked)}
                />
              }
              label="Правильный"
            />
            <IconButton
              onClick={() => handleDeleteAnswer(index)}
              disabled={content.answers.length <= 2}
              color="error"
            >
              <Delete />
            </IconButton>
          </Box>
        </Paper>
      ))}

      <Button
        startIcon={<Add />}
        onClick={handleAddAnswer}
        disabled={content.answers.length >= 4}
        variant="outlined"
      >
        Добавить вариант
      </Button>

      {content.answers.filter((a) => a.isCorrect).length === 0 && (
        <Typography color="error" variant="body2" sx={{ mt: 2 }}>
          Выберите один правильный ответ
        </Typography>
      )}
    </Box>
  );
};
