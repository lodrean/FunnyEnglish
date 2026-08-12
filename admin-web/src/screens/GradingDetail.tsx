import { useState } from 'react';
import {
  Accordion,
  AccordionDetails,
  AccordionSummary,
  Alert,
  Avatar,
  Box,
  Chip,
  Grid,
  List,
  ListItem,
  ListItemText,
  Paper,
  Typography,
  useTheme,
} from '@mui/material';
import {
  ExpandMore as ExpandMoreIcon,
} from '@mui/icons-material';
import { useNavigate, useParams } from 'react-router-dom';
import { useQueryClient } from '@tanstack/react-query';
import { format } from 'date-fns';
import { AxiosError } from 'axios';
import SubmissionAudioPlayer from '../components/speaking/SubmissionAudioPlayer';
import RubricForm from '../components/speaking/RubricForm';
import { PageLoader } from '../components/feedback/PageLoader';
import { useToast } from '../hooks';
import {
  speakingKeys,
  useSaveGrade,
  useSubmission,
  useSubmissions,
  useTopicQuestions,
} from '../hooks/useSpeaking';
import type { Grade, GradeRequest } from '../api/speakingApi';
import { formatMmSs } from '../utils/format';

/** Инициалы для .avatar (мокап frame-grading): первые буквы 1–2 слов имени */
const initialsOf = (name: string) =>
  name
    .split(/\s+/)
    .map((w) => w[0])
    .filter(Boolean)
    .slice(0, 2)
    .join('')
    .toUpperCase();

export default function GradingDetail() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const theme = useTheme();
  const toast = useToast();
  const queryClient = useQueryClient();

  const { data: submission, isLoading, isError, error } = useSubmission(id);
  const [grade, setGrade] = useState<Grade | undefined>(undefined);

  const effectiveGrade = grade ?? submission?.grade;
  const status = effectiveGrade ? 'REVIEWED' : (submission?.status ?? 'NEW');

  const { data: questions } = useTopicQuestions(submission?.topic.id);
  const saveGrade = useSaveGrade(id ?? '', status === 'REVIEWED' ? 'edit' : 'create');

  // «Пропустить» (G4, client-side): лента NEW для перехода к следующей записи без оценки
  const { data: newSubmissions } = useSubmissions({ status: 'NEW', page: 0, size: 100 });

  const handleSkip = () => {
    const list = newSubmissions?.content ?? [];
    const idx = list.findIndex((s) => s.id === id);
    const next = idx >= 0 ? list[idx + 1] : list.find((s) => s.id !== id);
    if (next) navigate(`/grading/submissions/${next.id}`);
    else navigate('/grading');
  };

  const handleSaveGrade = async (data: GradeRequest) => {
    if (!id || !submission) return;
    try {
      const saved = await saveGrade.mutateAsync(data);
      setGrade(saved);
      // Детали submission живут только в кэше (GET /submissions/{id} нет) — обновляем вручную
      queryClient.setQueryData(speakingKeys.submission(id), {
        ...submission,
        status: 'REVIEWED',
        grade: saved,
      });
      toast.success(status === 'REVIEWED' ? 'Оценка обновлена' : 'Оценка сохранена');
    } catch (err) {
      toast.error(
        (err as AxiosError<{ message?: string }>).response?.data?.message ??
          'Не удалось сохранить оценку'
      );
    }
  };

  if (isLoading) return <PageLoader loading />;
  if (isError || !submission) {
    return (
      <Alert severity="error">
        {(error as Error)?.message || 'Запись не найдена. Откройте её из Grading Inbox.'}
      </Alert>
    );
  }

  const questionsBlock = (
    <>
      <Typography variant="subtitle2" color="text.secondary" sx={{ mb: 1 }}>
        Ученик отвечал на эти вопросы за 30 секунд
      </Typography>
      <List dense data-testid="submission-questions">
        {(questions ?? []).map((q, i) => (
          <ListItem key={q.id} disableGutters>
            <ListItemText primary={`${i + 1}. ${q.text}`} />
          </ListItem>
        ))}
      </List>
    </>
  );

  return (
    <Box>
      {/* .student-row (мокап frame-grading): аватар + имя + мета + чип статуса */}
      <Paper sx={{ p: 2, mb: 3 }} data-testid="student-card">
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, flexWrap: 'wrap' }}>
          <Avatar
            data-testid="student-avatar"
            sx={{
              width: 48,
              height: 48,
              // tokens.css --color-tertiary (student-row .avatar)
              bgcolor: '#006C4C',
              color: '#fff',
              fontWeight: 800,
            }}
          >
            {initialsOf(submission.student.name)}
          </Avatar>
          <Box sx={{ flex: 1, minWidth: 200 }}>
            <Typography variant="subtitle1" fontWeight={700} data-testid="student-name">
              {submission.student.name}
            </Typography>
            <Typography variant="caption" color="text.secondary" data-testid="student-meta">
              {submission.topic.libraryName ? `${submission.topic.libraryName} → ` : ''}
              {submission.topic.name} · отправлено{' '}
              {submission.submittedAt
                ? format(new Date(submission.submittedAt), 'dd.MM.yyyy HH:mm')
                : '—'}
            </Typography>
          </Box>
          <Chip
            label={status}
            data-testid="submission-status-chip"
            sx={{
              bgcolor:
                status === 'NEW'
                  ? theme.palette.speaking.status.newContainer
                  : theme.palette.speaking.status.reviewedContainer,
              // chip-new: --color-status-new-container + текст #8a5200
              color: status === 'NEW' ? '#8a5200' : 'text.primary',
              fontWeight: 700,
            }}
          />
        </Box>
      </Paper>

      <Grid container spacing={3}>
        {/* Левая колонка: плеер + вопросы */}
        <Grid item xs={12} md={7}>
          <Typography variant="h6" sx={{ mb: 1 }} data-testid="recording-title">
            Запись · {formatMmSs(submission.durationSeconds)}
          </Typography>
          <SubmissionAudioPlayer
            audioUrl={submission.audioUrl}
            durationSeconds={submission.durationSeconds}
          />

          <Box sx={{ mt: 3 }}>
            {(questions?.length ?? 0) > 5 ? (
              <Accordion defaultExpanded={false}>
                <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                  <Typography>Questions ({questions?.length})</Typography>
                </AccordionSummary>
                <AccordionDetails>{questionsBlock}</AccordionDetails>
              </Accordion>
            ) : (
              questionsBlock
            )}
          </Box>
        </Grid>

        {/* Правая колонка: рубрика */}
        <Grid item xs={12} md={5}>
          {/* key — ремонт формы при смене режима NEW → REVIEWED / обновлении оценки */}
          <RubricForm
            key={effectiveGrade ? `${effectiveGrade.gradedAt}-${effectiveGrade.updatedAt}` : 'new'}
            grade={effectiveGrade}
            isSaving={saveGrade.isPending}
            onSave={handleSaveGrade}
            onSkip={handleSkip}
          />
        </Grid>
      </Grid>
    </Box>
  );
}
