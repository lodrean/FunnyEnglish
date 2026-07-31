import { useState, useRef, useEffect } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Button,
  LinearProgress,
  Chip,
  Radio,
  FormControlLabel,
  Paper,
} from '@mui/material';
// TODO: implement play/pause functionality using PlayIcon and PauseIcon
import { AudioTestDetail } from '../../api/audioTestApi';

interface Props {
  audioTest: AudioTestDetail;
}

export default function AudioTestPreview({ audioTest }: Props) {
  const [currentTime, setCurrentTime] = useState(0);
  const [, setIsPlaying] = useState(false);
  const [selectedAnswers, setSelectedAnswers] = useState<Record<string, string>>({});
  const [showResults, setShowResults] = useState(false);
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

  const formatTime = (seconds: number) => {
    const mins = Math.floor(seconds / 60);
    const secs = Math.floor(seconds % 60);
    return `${mins}:${secs.toString().padStart(2, '0')}`;
  };

  const activeQuestions = audioTest.questions.filter(
    (q) => currentTime >= q.startTimeSeconds && currentTime <= q.endTimeSeconds
  );

  const handleAnswerSelect = (questionId: string, answerId: string) => {
    setSelectedAnswers((prev) => ({ ...prev, [questionId]: answerId }));
  };

  const calculateScore = () => {
    let correct = 0;
    let total = 0;
    audioTest.questions.forEach((q) => {
      total += q.points;
      const selectedId = selectedAnswers[q.id];
      const correctAnswer = q.answers.find((a) => a.isCorrect);
      if (selectedId && correctAnswer && selectedId === correctAnswer.id) {
        correct += q.points;
      }
    });
    return { correct, total, percentage: total > 0 ? Math.round((correct / total) * 100) : 0 };
  };

  const { correct, total, percentage } = calculateScore();

  return (
    <Box>
      <Typography variant="h6" gutterBottom>
        Preview Mode
      </Typography>

      {/* Audio Player */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <audio ref={audioRef} src={audioTest.audioFileUrl} style={{ width: '100%' }} controls />
          <Box sx={{ mt: 2 }}>
            <LinearProgress
              variant="determinate"
              value={(currentTime / audioTest.durationSeconds) * 100}
            />
            <Typography variant="body2" sx={{ mt: 1 }}>
              {formatTime(currentTime)} / {formatTime(audioTest.durationSeconds)}
            </Typography>
          </Box>
        </CardContent>
      </Card>

      {/* Active Questions */}
      {activeQuestions.length > 0 && (
        <Paper sx={{ p: 3, mb: 3, bgcolor: 'primary.light' }}>
          <Typography variant="h6" gutterBottom>
            🎯 Active Question{activeQuestions.length > 1 ? 's' : ''}
          </Typography>
          {activeQuestions.map((question) => (
            <Card key={question.id} sx={{ mb: 2 }}>
              <CardContent>
                <Typography variant="subtitle1" fontWeight={500}>
                  {question.title || 'Question'}
                </Typography>
                {question.text && (
                  <Typography variant="body2" color="text.secondary">
                    {question.text}
                  </Typography>
                )}
                <Box sx={{ mt: 2 }}>
                  {question.answers.map((answer) => (
                    <FormControlLabel
                      key={answer.id}
                      control={
                        <Radio
                          checked={selectedAnswers[question.id] === answer.id}
                          onChange={() => handleAnswerSelect(question.id, answer.id)}
                        />
                      }
                      label={answer.text}
                    />
                  ))}
                </Box>
              </CardContent>
            </Card>
          ))}
        </Paper>
      )}

      {/* All Questions Overview */}
      <Typography variant="h6" gutterBottom>
        Questions Overview
      </Typography>
      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
        {audioTest.questions
          .sort((a, b) => a.startTimeSeconds - b.startTimeSeconds)
          .map((question, index) => {
            const isAnswered = !!selectedAnswers[question.id];
            const selectedAnswer = question.answers.find(
              (a) => a.id === selectedAnswers[question.id]
            );
            const isCorrect = selectedAnswer?.isCorrect;

            return (
              <Card
                key={question.id}
                variant="outlined"
                sx={{
                  borderColor: isAnswered ? (isCorrect ? 'success.main' : 'error.main') : 'grey.300',
                }}
              >
                <CardContent>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <Typography>
                      {index + 1}. {question.title || 'Untitled'}
                    </Typography>
                    <Box sx={{ display: 'flex', gap: 1 }}>
                      <Chip size="small" label={formatTime(question.startTimeSeconds)} />
                      <Chip
                        size="small"
                        label={isAnswered ? (isCorrect ? '✓ Correct' : '✗ Wrong') : 'Not answered'}
                        color={isAnswered ? (isCorrect ? 'success' : 'error') : 'default'}
                      />
                    </Box>
                  </Box>
                </CardContent>
              </Card>
            );
          })}
      </Box>

      {/* Results */}
      {showResults && (
        <Paper sx={{ p: 3, mt: 3, textAlign: 'center' }}>
          <Typography variant="h5" gutterBottom>
            Results
          </Typography>
          <Typography variant="h3" color={percentage >= 60 ? 'success.main' : 'error.main'}>
            {percentage}%
          </Typography>
          <Typography>
            {correct} / {total} points
          </Typography>
        </Paper>
      )}

      <Box sx={{ display: 'flex', gap: 2, justifyContent: 'center', mt: 3 }}>
        <Button variant="outlined" onClick={() => setShowResults(!showResults)}>
          {showResults ? 'Hide Results' : 'Show Results'}
        </Button>
        <Button
          variant="outlined"
          onClick={() => {
            setSelectedAnswers({});
            setShowResults(false);
          }}
        >
          Reset
        </Button>
      </Box>
    </Box>
  );
}
