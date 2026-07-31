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
} from '@mui/material';
import { Delete, Add } from '@mui/icons-material';
import { TextSelectContent, AnswerOption } from '../../types/questions';

interface TextSelectEditorProps {
  content: TextSelectContent;
  onChange: (content: TextSelectContent) => void;
}

export const TextSelectEditor: React.FC<TextSelectEditorProps> = ({
  content,
  onChange,
}) => {
  const handleTextChange = (text: string) => {
    onChange({ ...content, text });
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

  return (
    <Box>
      <TextField
        fullWidth
        multiline
        rows={2}
        label="Текст вопроса"
        value={content.text}
        onChange={(e) => handleTextChange(e.target.value)}
        sx={{ mb: 3 }}
      />

      <Typography variant="subtitle1" gutterBottom>
        Варианты ответов
      </Typography>

      {content.answers.map((answer, index) => (
        <Paper key={answer.id} sx={{ p: 2, mb: 2 }}>
          <Box display="flex" alignItems="center" gap={2}>
            <TextField
              fullWidth
              label={`Вариант ${index + 1}`}
              value={answer.text}
              onChange={(e) => handleAnswerChange(index, 'text', e.target.value)}
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
        disabled={content.answers.length >= 6}
        variant="outlined"
      >
        Добавить вариант
      </Button>

      {content.answers.filter((a) => a.isCorrect).length === 0 && (
        <Typography color="error" variant="body2" sx={{ mt: 2 }}>
          Выберите хотя бы один правильный ответ
        </Typography>
      )}
    </Box>
  );
};
