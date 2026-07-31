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
  Grid,
} from '@mui/material';
import { Delete, Add } from '@mui/icons-material';
import { ImageSelectContent, ImageAnswerOption } from '../../types/questions';
import MediaUploader from '../MediaUploader';

interface ImageSelectEditorProps {
  content: ImageSelectContent;
  onChange: (content: ImageSelectContent) => void;
}

export const ImageSelectEditor: React.FC<ImageSelectEditorProps> = ({
  content,
  onChange,
}) => {
  const handleTextChange = (text: string) => {
    onChange({ ...content, text });
  };

  const handleAnswerChange = (index: number, field: keyof ImageAnswerOption, value: any) => {
    const newAnswers = [...content.answers];
    newAnswers[index] = { ...newAnswers[index], [field]: value };
    onChange({ ...content, answers: newAnswers });
  };

  const handleImageChange = (index: number, url: string | undefined) => {
    handleAnswerChange(index, 'imageUrl', url || '');
  };

  const handleAddAnswer = () => {
    const newAnswer: ImageAnswerOption = {
      id: `answer_${Date.now()}`,
      text: '',
      imageUrl: '',
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
        label="Текст вопроса (опционально)"
        value={content.text || ''}
        onChange={(e) => handleTextChange(e.target.value)}
        sx={{ mb: 3 }}
      />

      <Typography variant="subtitle1" gutterBottom>
        Варианты ответов
      </Typography>

      <Grid container spacing={2}>
        {content.answers.map((answer, index) => (
          <Grid item xs={12} sm={6} key={answer.id}>
            <Paper sx={{ p: 2 }}>
              <Box display="flex" flexDirection="column" gap={2}>
                <MediaUploader
                  value={answer.imageUrl}
                  onChange={(url) => handleImageChange(index, url)}
                  accept="image/*"
                  folder="answers"
                  label={`Вариант ${index + 1}`}
                />

                <TextField
                  label="Эмодзи (опционально)"
                  value={answer.emoji || ''}
                  onChange={(e) => handleAnswerChange(index, 'emoji', e.target.value)}
                  size="small"
                  placeholder="🍎"
                />

                <TextField
                  label="Текст подсказки"
                  value={answer.text || ''}
                  onChange={(e) => handleAnswerChange(index, 'text', e.target.value)}
                  size="small"
                />

                <Box display="flex" alignItems="center" justifyContent="space-between">
                  <FormControlLabel
                    control={
                      <Checkbox
                        checked={answer.isCorrect}
                        onChange={(e) =>
                          handleAnswerChange(index, 'isCorrect', e.target.checked)
                        }
                      />
                    }
                    label="Правильный"
                  />
                  <IconButton
                    onClick={() => handleDeleteAnswer(index)}
                    disabled={content.answers.length <= 2}
                    color="error"
                    size="small"
                  >
                    <Delete />
                  </IconButton>
                </Box>
              </Box>
            </Paper>
          </Grid>
        ))}
      </Grid>

      <Button
        startIcon={<Add />}
        onClick={handleAddAnswer}
        disabled={content.answers.length >= 6}
        variant="outlined"
        sx={{ mt: 2 }}
      >
        Добавить вариант
      </Button>
    </Box>
  );
};
