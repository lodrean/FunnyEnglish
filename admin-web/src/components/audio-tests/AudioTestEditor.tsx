import React, { useState } from 'react';
import {
  Box,
  TextField,
  Button,
  Typography,
  Stepper,
  Step,
  StepLabel,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Switch,
  FormControlLabel,
  Slider,
  Paper,
  IconButton,
  Divider,
  Alert,
} from '@mui/material';
import { Add, Delete, Check } from '@mui/icons-material';
import MediaUploader from '../MediaUploader';
import type {
  CreateAudioTestRequest,
  UpdateAudioTestRequest,
  CreateAudioTestQuestionRequest,
} from '../../types/questions';

const steps = ['Аудио файл', 'Настройки', 'Вопросы'];

interface AudioTestEditorProps {
  initialData?: {
    id: string;
    title: string;
    description?: string;
    thumbnailUrl?: string;
    difficulty: 'EASY' | 'MEDIUM' | 'HARD';
    pointsReward: number;
    maxPlays: number;
    allowPause: boolean;
    timeLimitSeconds?: number;
    audioFile?: {
      id: string;
      url: string;
      durationSeconds: number;
      transcript?: string;
    };
    questions: CreateAudioTestQuestionRequest[];
  } | null;
  categoryId: string;
  onSave: (data: CreateAudioTestRequest | UpdateAudioTestRequest) => void;
  onCancel: () => void;
}

export const AudioTestEditor: React.FC<AudioTestEditorProps> = ({
  initialData,
  categoryId,
  onSave,
  onCancel,
}) => {
  const isEditing = !!initialData;
  const [activeStep, setActiveStep] = useState(0);
  const [error, setError] = useState<string | null>(null);

  // Audio file state
  const [audioUrl, setAudioUrl] = useState<string>(initialData?.audioFile?.url || '');
  const [audioTranscript, setAudioTranscript] = useState<string>(initialData?.audioFile?.transcript || '');

  // Settings state
  const [title, setTitle] = useState(initialData?.title || '');
  const [description, setDescription] = useState(initialData?.description || '');
  const [difficulty, setDifficulty] = useState<'EASY' | 'MEDIUM' | 'HARD'>(
    initialData?.difficulty || 'MEDIUM'
  );
  const [pointsReward, setPointsReward] = useState(initialData?.pointsReward || 50);
  const [maxPlays, setMaxPlays] = useState(initialData?.maxPlays || 2);
  const [allowPause, setAllowPause] = useState(initialData?.allowPause ?? true);
  const [timeLimitSeconds, setTimeLimitSeconds] = useState<number | undefined>(
    initialData?.timeLimitSeconds
  );
  const [enableTimeLimit, setEnableTimeLimit] = useState(!!initialData?.timeLimitSeconds);

  // Questions state
  const [questions, setQuestions] = useState<CreateAudioTestQuestionRequest[]>(
    initialData?.questions || []
  );

  const handleAudioChange = (url: string | undefined) => {
    setAudioUrl(url || '');
  };

  const handleAddQuestion = () => {
    const newQuestion: CreateAudioTestQuestionRequest = {
      text: '',
      points: 1,
      options: [
        { text: '', isCorrect: false },
        { text: '', isCorrect: false },
        { text: '', isCorrect: false },
      ],
    };
    setQuestions([...questions, newQuestion]);
  };

  const handleRemoveQuestion = (index: number) => {
    setQuestions(questions.filter((_, i) => i !== index));
  };

  const handleQuestionChange = (
    index: number,
    field: keyof CreateAudioTestQuestionRequest,
    value: any
  ) => {
    const updated = [...questions];
    updated[index] = { ...updated[index], [field]: value };
    setQuestions(updated);
  };

  const handleOptionChange = (
    questionIndex: number,
    optionIndex: number,
    field: 'text' | 'isCorrect',
    value: string | boolean
  ) => {
    const updated = [...questions];
    const question = updated[questionIndex];

    if (field === 'isCorrect' && value === true) {
      // Uncheck all other options (single choice)
      question.options = question.options.map((opt, i) => ({
        ...opt,
        isCorrect: i === optionIndex,
      }));
    } else {
      question.options[optionIndex] = {
        ...question.options[optionIndex],
        [field]: value,
      };
    }

    setQuestions(updated);
  };

  const handleAddOption = (questionIndex: number) => {
    const updated = [...questions];
    updated[questionIndex].options.push({
      text: '',
      isCorrect: false,
    });
    setQuestions(updated);
  };

  const handleRemoveOption = (questionIndex: number, optionIndex: number) => {
    const updated = [...questions];
    updated[questionIndex].options = updated[questionIndex].options.filter(
      (_, i) => i !== optionIndex
    );
    setQuestions(updated);
  };

  const validateStep = (): boolean => {
    setError(null);

    switch (activeStep) {
      case 0:
        if (!audioUrl) {
          setError('Загрузите аудио файл');
          return false;
        }
        return true;
      case 1:
        if (!title.trim()) {
          setError('Введите название теста');
          return false;
        }
        return true;
      case 2:
        if (questions.length === 0) {
          setError('Добавьте хотя бы один вопрос');
          return false;
        }
        for (const q of questions) {
          if (!q.text.trim()) {
            setError('Заполните текст всех вопросов');
            return false;
          }
          if (!q.options.some((o) => o.isCorrect)) {
            setError('Выберите правильный ответ для каждого вопроса');
            return false;
          }
          if (q.options.some((o) => !o.text.trim())) {
            setError('Заполните все варианты ответов');
            return false;
          }
        }
        return true;
      default:
        return true;
    }
  };

  const handleNext = () => {
    if (validateStep()) {
      setActiveStep((prev) => prev + 1);
    }
  };

  const handleBack = () => {
    setActiveStep((prev) => prev - 1);
  };

  const handleSave = () => {
    if (!validateStep()) return;

    // Extract audio file ID from URL
    const audioFileId = audioUrl.split('/').pop()?.split('.')[0] || `audio_${Date.now()}`;

    if (isEditing) {
      const updateData: UpdateAudioTestRequest = {
        title,
        description: description || undefined,
        difficulty,
        pointsReward,
        maxPlays,
        allowPause,
        timeLimitSeconds: enableTimeLimit ? timeLimitSeconds : undefined,
        questions,
      };
      onSave(updateData);
    } else {
      const createData: CreateAudioTestRequest = {
        categoryId,
        title,
        description: description || undefined,
        difficulty,
        pointsReward,
        maxPlays,
        allowPause,
        timeLimitSeconds: enableTimeLimit ? timeLimitSeconds : undefined,
        audioFileId,
        questions,
      };
      onSave(createData);
    }
  };

  const renderAudioStep = () => (
    <Box>
      <Typography variant="h6" gutterBottom>
        Загрузка аудио файла
      </Typography>

      <MediaUploader
        value={audioUrl}
        onChange={handleAudioChange}
        accept="audio/*"
        folder="audio-tests"
        label="Аудио файл"
      />

      {audioUrl && (
        <TextField
          fullWidth
          label="Транскрипция (для админа)"
          multiline
          rows={3}
          value={audioTranscript}
          onChange={(e) => setAudioTranscript(e.target.value)}
          sx={{ mt: 3 }}
        />
      )}
    </Box>
  );

  const renderSettingsStep = () => (
    <Box>
      <Typography variant="h6" gutterBottom>
        Настройки теста
      </Typography>

      <TextField
        fullWidth
        label="Название теста"
        value={title}
        onChange={(e) => setTitle(e.target.value)}
        sx={{ mb: 2 }}
        required
      />

      <TextField
        fullWidth
        label="Описание"
        multiline
        rows={2}
        value={description}
        onChange={(e) => setDescription(e.target.value)}
        sx={{ mb: 2 }}
      />

      <FormControl fullWidth sx={{ mb: 2 }}>
        <InputLabel>Сложность</InputLabel>
        <Select
          value={difficulty}
          onChange={(e) =>
            setDifficulty(e.target.value as 'EASY' | 'MEDIUM' | 'HARD')
          }
          label="Сложность"
        >
          <MenuItem value="EASY">Легкая</MenuItem>
          <MenuItem value="MEDIUM">Средняя</MenuItem>
          <MenuItem value="HARD">Сложная</MenuItem>
        </Select>
      </FormControl>

      <Box sx={{ mb: 2 }}>
        <Typography gutterBottom>
          Награда баллов: {pointsReward}
        </Typography>
        <Slider
          value={pointsReward}
          onChange={(_, value) => setPointsReward(value as number)}
          min={10}
          max={200}
          step={10}
          marks
          valueLabelDisplay="auto"
        />
      </Box>

      <Box sx={{ mb: 2 }}>
        <Typography gutterBottom>
          Максимальное количество прослушиваний: {maxPlays}
        </Typography>
        <Slider
          value={maxPlays}
          onChange={(_, value) => setMaxPlays(value as number)}
          min={1}
          max={5}
          step={1}
          marks
          valueLabelDisplay="auto"
        />
      </Box>

      <FormControlLabel
        control={
          <Switch
            checked={allowPause}
            onChange={(e) => setAllowPause(e.target.checked)}
          />
        }
        label="Разрешить паузу"
        sx={{ mb: 2 }}
      />

      <FormControlLabel
        control={
          <Switch
            checked={enableTimeLimit}
            onChange={(e) => setEnableTimeLimit(e.target.checked)}
          />
        }
        label="Ограничение по времени"
        sx={{ mb: enableTimeLimit ? 0 : 2 }}
      />

      {enableTimeLimit && (
        <Box sx={{ mb: 2, ml: 4 }}>
          <Typography gutterBottom>
            Лимит времени: {timeLimitSeconds || 0} секунд
          </Typography>
          <Slider
            value={timeLimitSeconds || 60}
            onChange={(_, value) => setTimeLimitSeconds(value as number)}
            min={30}
            max={600}
            step={30}
            marks={[
              { value: 60, label: '1 мин' },
              { value: 180, label: '3 мин' },
              { value: 300, label: '5 мин' },
              { value: 600, label: '10 мин' },
            ]}
            valueLabelDisplay="auto"
          />
        </Box>
      )}
    </Box>
  );

  const renderQuestionsStep = () => (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
        <Typography variant="h6">
          Вопросы ({questions.length})
        </Typography>
        <Button
          variant="contained"
          startIcon={<Add />}
          onClick={handleAddQuestion}
        >
          Добавить вопрос
        </Button>
      </Box>

      {questions.map((question, qIndex) => (
        <Paper key={qIndex} sx={{ p: 3, mb: 2 }}>
          <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
            <Typography variant="subtitle1" fontWeight="bold">
              Вопрос {qIndex + 1}
            </Typography>
            <IconButton
              onClick={() => handleRemoveQuestion(qIndex)}
              color="error"
              disabled={questions.length <= 1}
            >
              <Delete />
            </IconButton>
          </Box>

          <TextField
            fullWidth
            label="Текст вопроса"
            multiline
            rows={2}
            value={question.text}
            onChange={(e) =>
              handleQuestionChange(qIndex, 'text', e.target.value)
            }
            sx={{ mb: 2 }}
          />

          <TextField
            fullWidth
            label="Пояснение к ответу"
            multiline
            rows={2}
            value={question.explanation || ''}
            onChange={(e) =>
              handleQuestionChange(qIndex, 'explanation', e.target.value)
            }
            sx={{ mb: 2 }}
          />

          <Box sx={{ mb: 2 }}>
            <Typography gutterBottom>
              Баллы: {question.points}
            </Typography>
            <Slider
              value={question.points}
              onChange={(_, value) =>
                handleQuestionChange(qIndex, 'points', value as number)
              }
              min={1}
              max={10}
              step={1}
              marks
              valueLabelDisplay="auto"
            />
          </Box>

          <Divider sx={{ my: 2 }} />

          <Typography variant="subtitle2" gutterBottom>
            Варианты ответов (выберите правильный)
          </Typography>

          {question.options.map((option, oIndex) => (
            <Box key={oIndex} display="flex" alignItems="center" gap={1} mb={1}>
              <IconButton
                onClick={() =>
                  handleOptionChange(qIndex, oIndex, 'isCorrect', !option.isCorrect)
                }
                color={option.isCorrect ? 'success' : 'default'}
              >
                {option.isCorrect ? <Check /> : <Box width={24} height={24} />}
              </IconButton>
              <TextField
                fullWidth
                size="small"
                label={`Вариант ${oIndex + 1}`}
                value={option.text}
                onChange={(e) =>
                  handleOptionChange(qIndex, oIndex, 'text', e.target.value)
                }
              />
              <IconButton
                onClick={() => handleRemoveOption(qIndex, oIndex)}
                color="error"
                disabled={question.options.length <= 2}
              >
                <Delete />
              </IconButton>
            </Box>
          ))}

          <Button
            startIcon={<Add />}
            onClick={() => handleAddOption(qIndex)}
            disabled={question.options.length >= 6}
            size="small"
          >
            Добавить вариант
          </Button>
        </Paper>
      ))}

      {questions.length === 0 && (
        <Alert severity="info" sx={{ mb: 2 }}>
          Добавьте хотя бы один вопрос к аудио
        </Alert>
      )}
    </Box>
  );

  return (
    <Box sx={{ maxWidth: 800, mx: 'auto', p: 2 }}>
      <Typography variant="h4" gutterBottom>
        {isEditing ? 'Редактирование аудио теста' : 'Создание аудио теста'}
      </Typography>

      <Stepper activeStep={activeStep} sx={{ mb: 4 }}>
        {steps.map((label) => (
          <Step key={label}>
            <StepLabel>{label}</StepLabel>
          </Step>
        ))}
      </Stepper>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      <Paper sx={{ p: 3, mb: 3 }}>
        {activeStep === 0 && renderAudioStep()}
        {activeStep === 1 && renderSettingsStep()}
        {activeStep === 2 && renderQuestionsStep()}
      </Paper>

      <Box display="flex" justifyContent="space-between">
        <Button onClick={onCancel}>
          Отмена
        </Button>
        <Box>
          {activeStep > 0 && (
            <Button onClick={handleBack} sx={{ mr: 1 }}>
              Назад
            </Button>
          )}
          {activeStep < steps.length - 1 ? (
            <Button variant="contained" onClick={handleNext}>
              Далее
            </Button>
          ) : (
            <Button variant="contained" onClick={handleSave}>
              {isEditing ? 'Сохранить' : 'Создать'}
            </Button>
          )}
        </Box>
      </Box>
    </Box>
  );
};
