import { useMemo, useState } from 'react';
import {
  Box,
  Button,
  Paper,
  Slider,
  TextField,
  Typography,
} from '@mui/material';
import type { Grade, GradeRequest } from '../../api/speakingApi';

const CRITERIA = [
  { key: 'grammar', label: 'Grammar' },
  { key: 'vocabulary', label: 'Vocabulary' },
  { key: 'pronunciation', label: 'Pronunciation' },
  { key: 'fluency', label: 'Fluency' },
] as const;

type CriterionKey = (typeof CRITERIA)[number]['key'];

const MAX_COMMENT_LENGTH = 2000;

interface RubricFormProps {
  /** Заполнено при status = REVIEWED — режим просмотра с кнопкой «Edit grade» */
  grade?: Grade;
  isSaving: boolean;
  onSave: (data: GradeRequest) => void;
  /** «Пропустить» (мокап frame-grading): переход к следующей NEW-записи без оценки */
  onSkip?: () => void;
}

const clamp = (v: number) => Math.min(10, Math.max(1, Math.round(v)));

/**
 * Рубрика оценки (мокап frame-grading, .rubric): 4 критерия (1–10) — слайдер
 * с крупным текущим значением справа от подписи (.rubric-head), панель
 * «Общий балл (среднее)» (.avg-box), комментарий ≤2000. Save disabled, пока
 * все 4 критерия не выставлены осознанно (защита от отправки дефолтов).
 * totalScore в GradeRequest НЕ отправляем — backend пересчитывает сам.
 */
export default function RubricForm({ grade, isSaving, onSave, onSkip }: RubricFormProps) {
  const isReviewed = !!grade;
  const [editMode, setEditMode] = useState(!isReviewed);

  const [values, setValues] = useState<Record<CriterionKey, number>>({
    grammar: grade?.grammar ?? 5,
    vocabulary: grade?.vocabulary ?? 5,
    pronunciation: grade?.pronunciation ?? 5,
    fluency: grade?.fluency ?? 5,
  });
  // NEW: критерий считается выставленным только после явного изменения
  const [touched, setTouched] = useState<Record<CriterionKey, boolean>>({
    grammar: isReviewed,
    vocabulary: isReviewed,
    pronunciation: isReviewed,
    fluency: isReviewed,
  });
  const [comment, setComment] = useState(grade?.comment ?? '');

  const allTouched = CRITERIA.every((c) => touched[c.key]);

  const total = useMemo(() => {
    const sum = CRITERIA.reduce((acc, c) => acc + values[c.key], 0);
    return Math.round((sum / CRITERIA.length) * 10) / 10;
  }, [values]);

  const setCriterion = (key: CriterionKey, raw: number) => {
    const value = clamp(Number.isNaN(raw) ? 1 : raw);
    setValues((prev) => ({ ...prev, [key]: value }));
    setTouched((prev) => ({ ...prev, [key]: true }));
  };

  const handleSave = () => {
    onSave({
      grammar: values.grammar,
      vocabulary: values.vocabulary,
      pronunciation: values.pronunciation,
      fluency: values.fluency,
      comment: comment.trim() || undefined,
    });
    if (isReviewed) setEditMode(false);
  };

  const disabled = !editMode;

  return (
    <Paper sx={{ p: 3 }} data-testid="rubric-form">
      <Typography variant="h6" sx={{ mb: 2 }}>
        Оценка по рубрике
      </Typography>

      {CRITERIA.map(({ key, label }) => (
        <Box key={key} sx={{ mb: 2 }} data-testid={`rubric-row-${key}`}>
          {/* .rubric-head: подпись слева, крупное значение справа (.val) */}
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline' }}>
            <Typography variant="body2" fontWeight={700}>
              {label}
            </Typography>
            <Typography
              variant="h6"
              fontWeight={700}
              color="primary"
              data-testid={`rubric-value-${key}`}
            >
              {values[key]}
            </Typography>
          </Box>
          <Slider
            value={values[key]}
            min={1}
            max={10}
            step={1}
            disabled={disabled}
            onChange={(_, v) => setCriterion(key, Array.isArray(v) ? v[0] : v)}
            data-testid={`rubric-slider-${key}`}
            aria-label={`${label}, от 1 до 10`}
          />
          {/* .rubric-scale */}
          <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
            {[1, 5, 10].map((mark) => (
              <Typography key={mark} variant="caption" color="text.secondary">
                {mark}
              </Typography>
            ))}
          </Box>
        </Box>
      ))}

      {/* .avg-box: фиолетовая панель «Общий балл (среднее)» */}
      <Box
        data-testid="rubric-avg-panel"
        sx={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          // tokens.css --color-surface-warm (light) / dark-вариант
          bgcolor: (theme) => (theme.palette.mode === 'dark' ? '#252B4A' : '#E5DCFF'),
          borderRadius: 2,
          px: 2,
          py: 1.5,
          my: 2,
        }}
      >
        <Typography variant="body2" fontWeight={700}>
          Общий балл (среднее)
        </Typography>
        <Typography
          variant="h5"
          fontWeight={700}
          sx={{ color: 'secondary.main' }}
          data-testid="rubric-total"
        >
          {total.toFixed(1)}
        </Typography>
      </Box>

      <TextField
        label="Comment"
        multiline
        rows={4}
        fullWidth
        value={comment}
        disabled={disabled}
        onChange={(e) => setComment(e.target.value.slice(0, MAX_COMMENT_LENGTH))}
        helperText={`${comment.length}/${MAX_COMMENT_LENGTH}`}
        data-testid="rubric-comment"
      />

      {isReviewed && grade && (
        <Typography variant="caption" color="text.secondary" display="block" sx={{ mt: 1 }}>
          Graded at {grade.gradedAt ? new Date(grade.gradedAt).toLocaleString() : '—'}
          {grade.updatedAt && `, updated at ${new Date(grade.updatedAt).toLocaleString()}`}
          {grade.reviewerName && ` by ${grade.reviewerName}`}
        </Typography>
      )}

      {/* .admin-actions: «Пропустить» (ghost) + «Сохранить оценку» (primary) */}
      <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 2, mt: 2 }}>
        {onSkip && (
          <Button variant="text" onClick={onSkip} data-testid="skip-submission-button">
            Пропустить
          </Button>
        )}
        {isReviewed && !editMode ? (
          <Button
            variant="contained"
            onClick={() => setEditMode(true)}
            data-testid="edit-grade-button"
          >
            Edit grade
          </Button>
        ) : (
          <Button
            variant="contained"
            onClick={handleSave}
            disabled={!allTouched || isSaving}
            data-testid="save-grade-button"
          >
            {isReviewed ? 'Update grade' : 'Save grade'}
          </Button>
        )}
      </Box>
    </Paper>
  );
}
