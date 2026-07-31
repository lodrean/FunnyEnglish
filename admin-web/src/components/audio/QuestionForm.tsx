import { useState } from 'react';
import {
  Box,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Button,
  Typography,
  IconButton,
  Chip,
  Slider,
  Paper,
} from '@mui/material';
import { Add as AddIcon, Delete as DeleteIcon } from '@mui/icons-material';
import { AudioTestQuestion, CreateAudioQuestionRequest } from '../../api/audioTestApi';

interface Props {
  initialData?: AudioTestQuestion;
  audioDuration: number;
  currentTime: number;
  onSeek: (time: number) => void;
  onSave: (data: CreateAudioQuestionRequest) => void;
  onCancel: () => void;
}

const questionTypes = [
  { value: 'LISTENING_COMPREHENSION', label: 'Listening Comprehension' },
  { value: 'FILL_BLANK', label: 'Fill in the Blank' },
  { value: 'TRUE_FALSE', label: 'True / False' },
  { value: 'DICTATION', label: 'Dictation' },
];

export default function QuestionForm({
  initialData,
  audioDuration,
  currentTime,
  onSeek: _onSeek,
  onSave,
  onCancel,
}: Props) {
  const [type, setType] = useState(initialData?.questionType || 'LISTENING_COMPREHENSION');
  const [title, setTitle] = useState(initialData?.title || '');
  const [text, setText] = useState(initialData?.text || '');
  const [startTime, setStartTime] = useState(initialData?.startTimeSeconds || 0);
  const [endTime, setEndTime] = useState(initialData?.endTimeSeconds || 10);
  const [points, setPoints] = useState(initialData?.points || 1);
  const [answers, setAnswers] = useState(
    initialData?.answers.map((a) => ({ text: a.text, isCorrect: a.isCorrect })) || [
      { text: '', isCorrect: true },
      { text: '', isCorrect: false },
    ]
  );

  const formatTime = (seconds: number) => {
    const mins = Math.floor(seconds / 60);
    const secs = Math.floor(seconds % 60);
    return `${mins}:${secs.toString().padStart(2, '0')}`;
  };

  const handleAddAnswer = () => {
    setAnswers([...answers, { text: '', isCorrect: false }]);
  };

  const handleRemoveAnswer = (index: number) => {
    setAnswers(answers.filter((_, i) => i !== index));
  };

  const handleAnswerChange = (index: number, field: string, value: any) => {
    const updated = [...answers];
    updated[index] = { ...updated[index], [field]: value };
    setAnswers(updated);
  };

  const handleSetCurrentTimeAsStart = () => {
    setStartTime(Math.floor(currentTime));
  };

  const handleSetCurrentTimeAsEnd = () => {
    setEndTime(Math.floor(currentTime));
  };

  const handleSubmit = () => {
    onSave({
      questionType: type as any,
      title: title || undefined,
      text: text || undefined,
      startTimeSeconds: startTime,
      endTimeSeconds: endTime,
      points,
      displayOrder: 0,
      answers: answers.map((a, i) => ({ ...a, displayOrder: i })),
    });
  };

  const isValid =
    title.trim() &&
    answers.length >= 2 &&
    answers.every((a) => a.text.trim()) &&
    answers.some((a) => a.isCorrect) &&
    startTime < endTime &&
    endTime <= audioDuration;

  return (
    <Box sx={{ pt: 2 }}>
      {/* Time Range Selection */}
      <Paper sx={{ p: 2, mb: 3 }}>
        <Typography variant="subtitle2" gutterBottom>
          Time Range in Audio
        </Typography>
        <Box sx={{ display: 'flex', gap: 2, alignItems: 'center', mb: 2 }}>
          <Box sx={{ flex: 1 }}>
            <TextField
              fullWidth
              label="Start Time"
              type="number"
              value={startTime}
              onChange={(e) => setStartTime(Number(e.target.value))}
              InputProps={{
                endAdornment: <Button size="small" onClick={handleSetCurrentTimeAsStart}>Set Current</Button>,
              }}
            />
            <Typography variant="caption" color="text.secondary">
              {formatTime(startTime)}
            </Typography>
          </Box>
          <Typography>to</Typography>
          <Box sx={{ flex: 1 }}>
            <TextField
              fullWidth
              label="End Time"
              type="number"
              value={endTime}
              onChange={(e) => setEndTime(Number(e.target.value))}
              InputProps={{
                endAdornment: <Button size="small" onClick={handleSetCurrentTimeAsEnd}>Set Current</Button>,
              }}
            />
            <Typography variant="caption" color="text.secondary">
              {formatTime(endTime)}
            </Typography>
          </Box>
        </Box>
        <Slider
          value={[startTime, endTime]}
          onChange={(_, value) => {
            setStartTime((value as number[])[0]);
            setEndTime((value as number[])[1]);
          }}
          min={0}
          max={audioDuration}
          valueLabelDisplay="auto"
          valueLabelFormat={(v) => formatTime(v)}
        />
      </Paper>

      <FormControl fullWidth margin="normal">
        <InputLabel>Question Type</InputLabel>
        <Select value={type} onChange={(e) => setType(e.target.value as typeof type)} label="Question Type">
          {questionTypes.map((t) => (
            <MenuItem key={t.value} value={t.value}>
              {t.label}
            </MenuItem>
          ))}
        </Select>
      </FormControl>

      <TextField
        fullWidth
        label="Title"
        value={title}
        onChange={(e) => setTitle(e.target.value)}
        margin="normal"
        placeholder="e.g., What is the main topic?"
      />

      <TextField
        fullWidth
        label="Question Text"
        value={text}
        onChange={(e) => setText(e.target.value)}
        margin="normal"
        multiline
        rows={2}
        placeholder="Additional context or question details"
      />

      <TextField
        fullWidth
        label="Points"
        type="number"
        value={points}
        onChange={(e) => setPoints(Number(e.target.value))}
        margin="normal"
        inputProps={{ min: 1, max: 10 }}
      />

      {/* Answers */}
      <Typography variant="h6" sx={{ mt: 3, mb: 2 }}>
        Answers
      </Typography>

      {answers.map((answer, index) => (
        <Box key={index} sx={{ display: 'flex', gap: 1, alignItems: 'center', mb: 2 }}>
          <Chip
            label={answer.isCorrect ? 'Correct' : 'Wrong'}
            color={answer.isCorrect ? 'success' : 'default'}
            onClick={() => handleAnswerChange(index, 'isCorrect', !answer.isCorrect)}
            sx={{ minWidth: 80 }}
          />
          <TextField
            fullWidth
            label={`Answer ${index + 1}`}
            value={answer.text}
            onChange={(e) => handleAnswerChange(index, 'text', e.target.value)}
            placeholder="Answer text"
          />
          <IconButton onClick={() => handleRemoveAnswer(index)} disabled={answers.length <= 2}>
            <DeleteIcon />
          </IconButton>
        </Box>
      ))}

      <Button startIcon={<AddIcon />} onClick={handleAddAnswer} variant="outlined" sx={{ mb: 3 }}>
        Add Answer
      </Button>

      <Box sx={{ display: 'flex', gap: 2, justifyContent: 'flex-end' }}>
        <Button onClick={onCancel}>Cancel</Button>
        <Button variant="contained" onClick={handleSubmit} disabled={!isValid}>
          Save Question
        </Button>
      </Box>
    </Box>
  );
}
