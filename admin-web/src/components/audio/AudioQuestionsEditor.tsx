import { useState, useRef, useEffect } from 'react';
import {
  Box,
  Button,
  Card,
  CardContent,
  Typography,
  IconButton,
  Chip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Alert,
} from '@mui/material';
import {
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  PlayArrow as PlayIcon,
  Pause as PauseIcon,
} from '@mui/icons-material';
import { AudioTestQuestion, CreateAudioQuestionRequest } from '../../api/audioTestApi';
import QuestionForm from './QuestionForm';

interface Props {
  audioTestId: string;
  audioFileUrl: string;
  durationSeconds: number;
  questions: AudioTestQuestion[];
  onQuestionsChange: (questions: AudioTestQuestion[]) => void;
}

export default function AudioQuestionsEditor({
  audioFileUrl,
  durationSeconds,
  questions,
  onQuestionsChange,
}: Props) {
  const [editingQuestion, setEditingQuestion] = useState<AudioTestQuestion | null>(null);
  const [isAddDialogOpen, setIsAddDialogOpen] = useState(false);
  const [deleteConfirmId, setDeleteConfirmId] = useState<string | null>(null);
  const [currentTime, setCurrentTime] = useState(0);
  const [isPlaying, setIsPlaying] = useState(false);
  const audioRef = useRef<HTMLAudioElement>(null);

  useEffect(() => {
    const audio = audioRef.current;
    if (!audio) return;

    const handleTimeUpdate = () => setCurrentTime(audio.currentTime);
    const handlePlay = () => setIsPlaying(true);
    const handlePause = () => setIsPlaying(false);

    audio.addEventListener('timeupdate', handleTimeUpdate);
    audio.addEventListener('play', handlePlay);
    audio.addEventListener('pause', handlePause);

    return () => {
      audio.removeEventListener('timeupdate', handleTimeUpdate);
      audio.removeEventListener('play', handlePlay);
      audio.removeEventListener('pause', handlePause);
    };
  }, []);

  const handleSaveQuestion = (questionData: CreateAudioQuestionRequest) => {
    if (editingQuestion) {
      // Update existing
      const updated = questions.map((q) =>
        q.id === editingQuestion.id ? { ...q, ...questionData } as AudioTestQuestion : q
      );
      onQuestionsChange(updated);
      setEditingQuestion(null);
    } else {
      // Add new
      const newQuestion: AudioTestQuestion = {
        id: `temp-${Date.now()}`,
        ...questionData,
        answers: questionData.answers.map((a, i) => ({
          id: `temp-answer-${Date.now()}-${i}`,
          ...a,
        })),
      } as AudioTestQuestion;
      onQuestionsChange([...questions, newQuestion]);
      setIsAddDialogOpen(false);
    }
  };

  const handleDeleteQuestion = (id: string) => {
    onQuestionsChange(questions.filter((q) => q.id !== id));
    setDeleteConfirmId(null);
  };

  const handleSeekToQuestion = (startTime: number) => {
    if (audioRef.current) {
      audioRef.current.currentTime = startTime;
      audioRef.current.play();
    }
  };

  const formatTime = (seconds: number) => {
    const mins = Math.floor(seconds / 60);
    const secs = Math.floor(seconds % 60);
    return `${mins}:${secs.toString().padStart(2, '0')}`;
  };

  // Sort questions by start time
  const sortedQuestions = [...questions].sort(
    (a, b) => a.startTimeSeconds - b.startTimeSeconds
  );

  return (
    <Box>
      {/* Audio Player */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
            <IconButton
              onClick={() =>
                isPlaying ? audioRef.current?.pause() : audioRef.current?.play()
              }
            >
              {isPlaying ? <PauseIcon /> : <PlayIcon />}
            </IconButton>
            <audio ref={audioRef} src={audioFileUrl} style={{ flex: 1 }} controls />
          </Box>
          <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
            Current: {formatTime(currentTime)} / {formatTime(durationSeconds)}
          </Typography>
        </CardContent>
      </Card>

      {/* Questions List */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
        <Typography variant="h6">
          Questions ({questions.length})
        </Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => setIsAddDialogOpen(true)}
        >
          Add Question
        </Button>
      </Box>

      {questions.length === 0 ? (
        <Alert severity="info">
          No questions yet. Add questions that will appear at specific times in the audio.
        </Alert>
      ) : (
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
          {sortedQuestions.map((question, index) => (
            <Card key={question.id} variant="outlined">
              <CardContent>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                  <Box>
                    <Typography variant="subtitle1" fontWeight={500}>
                      {index + 1}. {question.title || 'Untitled Question'}
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      {question.text}
                    </Typography>
                    <Box sx={{ mt: 1, display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                      <Chip
                        size="small"
                        label={formatTime(question.startTimeSeconds)}
                        onClick={() => handleSeekToQuestion(question.startTimeSeconds)}
                        sx={{ cursor: 'pointer' }}
                      />
                      <span>-</span>
                      <Chip
                        size="small"
                        label={formatTime(question.endTimeSeconds)}
                      />
                      <Chip
                        size="small"
                        label={question.questionType.replace(/_/g, ' ')}
                        color="primary"
                        variant="outlined"
                      />
                      <Chip
                        size="small"
                        label={`${question.points} pt${question.points > 1 ? 's' : ''}`}
                      />
                      <Chip
                        size="small"
                        label={`${question.answers.length} answers`}
                      />
                    </Box>
                  </Box>
                  <Box>
                    <IconButton
                      size="small"
                      onClick={() => setEditingQuestion(question)}
                    >
                      <EditIcon />
                    </IconButton>
                    <IconButton
                      size="small"
                      color="error"
                      onClick={() => setDeleteConfirmId(question.id)}
                    >
                      <DeleteIcon />
                    </IconButton>
                  </Box>
                </Box>
              </CardContent>
            </Card>
          ))}
        </Box>
      )}

      {/* Add/Edit Dialog */}
      <Dialog
        open={isAddDialogOpen || !!editingQuestion}
        onClose={() => {
          setIsAddDialogOpen(false);
          setEditingQuestion(null);
        }}
        maxWidth="md"
        fullWidth
      >
        <DialogTitle>
          {editingQuestion ? 'Edit Question' : 'Add Question'}
        </DialogTitle>
        <DialogContent>
          <QuestionForm
            initialData={editingQuestion || undefined}
            audioDuration={durationSeconds}
            currentTime={currentTime}
            onSeek={handleSeekToQuestion}
            onSave={handleSaveQuestion}
            onCancel={() => {
              setIsAddDialogOpen(false);
              setEditingQuestion(null);
            }}
          />
        </DialogContent>
      </Dialog>

      {/* Delete Confirmation */}
      <Dialog open={!!deleteConfirmId} onClose={() => setDeleteConfirmId(null)}>
        <DialogTitle>Delete Question?</DialogTitle>
        <DialogContent>
          <Typography>
            Are you sure you want to delete this question?
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteConfirmId(null)}>Cancel</Button>
          <Button
            color="error"
            variant="contained"
            onClick={() => deleteConfirmId && handleDeleteQuestion(deleteConfirmId)}
          >
            Delete
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
