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
  Card,
  CardContent,
} from '@mui/material';
import { Delete, Add, VolumeUp } from '@mui/icons-material';
import { AudioSelectContent, AnswerOption } from '../../types/questions';
import MediaUploader from '../MediaUploader';

interface AudioSelectEditorProps {
  content: AudioSelectContent;
  onChange: (content: AudioSelectContent) => void;
}

export const AudioSelectEditor: React.FC<AudioSelectEditorProps> = ({
  content,
  onChange,
}) => {
  const handleAudioChange = (url: string | undefined) => {
    onChange({ ...content, audioUrl: url || '' });
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
      <Card sx={{ mb: 3, bgcolor: 'primary.light' }}>
        <CardContent>
          <Typography variant="h6" gutterBottom display="flex" alignItems="center" gap={1}>
            <VolumeUp />
            Аудио вопрос
          </Typography>

          <MediaUploader
            value={content.audioUrl}
            onChange={handleAudioChange}
            accept="audio/*"
            folder="audio"
            label="Аудио файл"
          />
        </CardContent>
      </Card>

      <TextField
        fullWidth
        label="Транскрипция (для админа)"
        value={content.transcript || ''}
        onChange={(e) => onChange({ ...content, transcript: e.target.value })}
        sx={{ mb: 2 }}
        multiline
        rows={2}
      />

      <TextField
        fullWidth
        label="Вопрос к аудио"
        value={content.text || ''}
        onChange={(e) => onChange({ ...content, text: e.target.value })}
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
    </Box>
  );
};
