import { useEffect, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  CircularProgress,
  FormControl,
  FormControlLabel,
  FormHelperText,
  InputLabel,
  MenuItem,
  Paper,
  Select,
  Switch,
  Tab,
  Tabs,
  TextField,
  Tooltip,
} from '@mui/material';
import { useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { useForm, Controller } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { AxiosError } from 'axios';
import MediaUploader from '../components/MediaUploader';
import TopicQuestionsEditor from '../components/speaking/TopicQuestionsEditor';
import { ConfirmDialog } from '../components/feedback';
import { PageLoader } from '../components/feedback/PageLoader';
import { useConfirm, useToast } from '../hooks';
import {
  useSaveTopic,
  useSpeakingLibraries,
  useSpeakingTopic,
  useUpsertTopicVideo,
} from '../hooks/useSpeaking';

const topicSchema = z.object({
  libraryId: z.string().min(1, 'Выберите тему'),
  name: z.string().min(1, 'Название обязательно').max(160),
  description: z.string().max(1000).optional(),
  videoUrl: z.string().min(1, 'Загрузите видео'),
  subtitlesUrl: z.string().optional(),
  durationSeconds: z
    .number({ invalid_type_error: 'Укажите длительность' })
    .int()
    .positive('Длительность должна быть больше 0'),
  displayOrder: z.number().int().min(0),
  isPublished: z.boolean(),
});

type TopicFormValues = z.infer<typeof topicSchema>;

export default function SpeakingTopicEditor() {
  const { id } = useParams<{ id: string }>();
  const isEdit = !!id;
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const toast = useToast();
  const { confirm, confirmState, handleConfirm, handleCancel: handleConfirmCancel } = useConfirm();

  const [tab, setTab] = useState(0);

  const { data: libraries } = useSpeakingLibraries();
  const { data: topic, isLoading, isError } = useSpeakingTopic(id);
  const saveTopic = useSaveTopic();
  const upsertVideo = useUpsertTopicVideo();

  const {
    register,
    control,
    handleSubmit,
    reset,
    setValue,
    watch,
    formState: { errors, isSubmitting, isDirty },
  } = useForm<TopicFormValues>({
    resolver: zodResolver(topicSchema),
    defaultValues: {
      libraryId: searchParams.get('libraryId') ?? '',
      name: '',
      description: '',
      videoUrl: '',
      subtitlesUrl: undefined,
      durationSeconds: undefined,
      displayOrder: 0,
      isPublished: false,
    },
  });

  const videoUrl = watch('videoUrl');
  const subtitlesUrl = watch('subtitlesUrl');

  useEffect(() => {
    if (isEdit && topic) {
      reset({
        libraryId: topic.libraryId,
        name: topic.name,
        description: topic.description ?? '',
        videoUrl: topic.videoUrl ?? '',
        subtitlesUrl: topic.subtitlesUrl,
        durationSeconds: topic.durationSeconds,
        displayOrder: topic.displayOrder,
        isPublished: topic.isPublished,
      });
    }
  }, [isEdit, topic, reset]);

  // Автодлительность: читаем метаданные загруженного видео (поле остаётся редактируемым)
  useEffect(() => {
    if (!videoUrl) return;
    const el = document.createElement('video');
    el.preload = 'metadata';
    el.src = videoUrl;
    const onLoaded = () => {
      if (Number.isFinite(el.duration) && el.duration > 0) {
        setValue('durationSeconds', Math.round(el.duration), { shouldValidate: true });
      }
    };
    el.addEventListener('loadedmetadata', onLoaded);
    return () => {
      el.removeEventListener('loadedmetadata', onLoaded);
      el.src = '';
    };
  }, [videoUrl, setValue]);

  // MVP: предупреждение о несохранённых изменениях только на beforeunload (см. §4.3 спеки)
  useEffect(() => {
    const handler = (e: BeforeUnloadEvent) => {
      if (isDirty) e.preventDefault();
    };
    window.addEventListener('beforeunload', handler);
    return () => window.removeEventListener('beforeunload', handler);
  }, [isDirty]);

  const isSaving = isSubmitting || saveTopic.isPending || upsertVideo.isPending;

  const onSubmit = async (values: TopicFormValues) => {
    try {
      // 1) Поля топика (backend: create/update БЕЗ видео)
      const saved = await saveTopic.mutateAsync({
        id,
        data: {
          libraryId: values.libraryId,
          name: values.name,
          description: values.description || undefined,
          displayOrder: values.displayOrder,
          isPublished: values.isPublished,
        },
      });
      // 2) Видео/субтитры — отдельный upsert
      await upsertVideo.mutateAsync({
        id: saved.id,
        data: {
          videoUrl: values.videoUrl,
          subtitlesUrl: values.subtitlesUrl || undefined,
          durationSeconds: values.durationSeconds,
        },
      });
      toast.success(isEdit ? 'Топик сохранён' : 'Топик создан');
      if (!isEdit) {
        // Замена URL — включается вкладка Questions
        navigate(`/speaking/topics/${saved.id}/edit`, { replace: true });
      }
    } catch (err) {
      toast.error(
        (err as AxiosError<{ message?: string }>).response?.data?.message ??
          (err as Error).message ??
          'Не удалось сохранить топик'
      );
    }
  };

  const handleCancel = async () => {
    if (isDirty) {
      const ok = await confirm({
        title: 'Отменить изменения?',
        message: 'Несохранённые изменения будут потеряны.',
        confirmText: 'Да, отменить',
      });
      if (!ok) return;
    }
    navigate(-1);
  };

  if (isEdit && isLoading) return <PageLoader loading />;
  if (isEdit && isError) {
    return <Alert severity="error">Не удалось загрузить топик. Откройте его из списка топиков.</Alert>;
  }

  return (
    <Box sx={{ maxWidth: 860 }}>
      <Tabs
        value={tab}
        onChange={(_, v) => setTab(v)}
        sx={{ mb: 3 }}
        data-testid="topic-editor-tabs"
      >
        <Tab label="Details" data-testid="tab-details" />
        {/* Tab должен быть ПРЯМЫМ ребёнком Tabs (MUI инжектит onClick только в них) —
            поэтому Tooltip внутри label, а не снаружи */}
        <Tab
          label={
            <Tooltip title={isEdit ? '' : 'Сначала сохраните топик'}>
              <span>Questions</span>
            </Tooltip>
          }
          disabled={!isEdit}
          data-testid="tab-questions"
        />
      </Tabs>

      {tab === 0 && (
        <Paper sx={{ p: 3 }}>
          <Box component="form" onSubmit={handleSubmit(onSubmit)} noValidate>
            <Controller
              name="libraryId"
              control={control}
              render={({ field }) => (
                <FormControl fullWidth margin="normal" error={!!errors.libraryId} disabled={isEdit}>
                  <InputLabel id="topic-library-label">Library</InputLabel>
                  <Select
                    labelId="topic-library-label"
                    label="Library"
                    data-testid="topic-library-select"
                    {...field}
                  >
                    {(libraries ?? []).map((lib) => (
                      <MenuItem key={lib.id} value={lib.id}>
                        {lib.name}
                      </MenuItem>
                    ))}
                  </Select>
                  {errors.libraryId && (
                    <FormHelperText>{errors.libraryId.message}</FormHelperText>
                  )}
                </FormControl>
              )}
            />

            <TextField
              label="Name"
              fullWidth
              required
              margin="normal"
              data-testid="topic-name-input"
              error={!!errors.name}
              helperText={errors.name?.message}
              {...register('name')}
            />

            <TextField
              label="Description"
              fullWidth
              multiline
              rows={2}
              margin="normal"
              data-testid="topic-description-input"
              error={!!errors.description}
              helperText={errors.description?.message}
              {...register('description')}
            />

            <Box sx={{ mt: 2 }} data-testid="topic-video-uploader">
              <MediaUploader
                value={videoUrl || undefined}
                onChange={(url) => setValue('videoUrl', url ?? '', { shouldDirty: true })}
                folder="speaking/videos"
                accept="video/*"
                mediaKind="video"
                label="Видео топика"
                hint="MP4, WebM до 50 МБ"
              />
              {errors.videoUrl && (
                <FormHelperText error>{errors.videoUrl.message}</FormHelperText>
              )}
            </Box>

            <Box sx={{ mt: 2 }} data-testid="topic-subtitles-uploader">
              <MediaUploader
                value={subtitlesUrl || undefined}
                onChange={(url) => setValue('subtitlesUrl', url, { shouldDirty: true })}
                folder="speaking/subtitles"
                accept=".vtt"
                mediaKind="file"
                label="Субтитры (WebVTT)"
                hint="WebVTT (.vtt) — из субтитров автоматически формируется полный текст видео с пословной подсветкой, отдельный транскрипт не нужен"
              />
              {subtitlesUrl && (
                <Button
                  size="small"
                  sx={{ mt: 1 }}
                  onClick={() => setValue('subtitlesUrl', undefined, { shouldDirty: true })}
                  data-testid="remove-subtitles-button"
                >
                  Убрать субтитры
                </Button>
              )}
            </Box>

            <TextField
              label="Duration (sec)"
              type="number"
              fullWidth
              margin="normal"
              data-testid="topic-duration-input"
              error={!!errors.durationSeconds}
              helperText={
                errors.durationSeconds?.message ?? 'Заполняется автоматически после загрузки видео'
              }
              {...register('durationSeconds', { valueAsNumber: true })}
            />

            <TextField
              label="Order"
              type="number"
              fullWidth
              margin="normal"
              data-testid="topic-order-input"
              error={!!errors.displayOrder}
              helperText={errors.displayOrder?.message}
              {...register('displayOrder', { valueAsNumber: true })}
            />

            <Controller
              name="isPublished"
              control={control}
              render={({ field }) => (
                <FormControlLabel
                  control={
                    <Switch
                      checked={field.value}
                      onChange={field.onChange}
                      data-testid="topic-published-switch"
                    />
                  }
                  label="Published"
                />
              )}
            />

            <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 2, mt: 3 }}>
              <Button variant="outlined" color="inherit" onClick={handleCancel}>
                Cancel
              </Button>
              <Button
                type="submit"
                variant="contained"
                data-testid="save-topic-button"
                disabled={isSaving}
                startIcon={isSaving ? <CircularProgress size={18} color="inherit" /> : undefined}
              >
                {isEdit ? 'Save' : 'Create'}
              </Button>
            </Box>
          </Box>
        </Paper>
      )}

      {tab === 1 && isEdit && id && <TopicQuestionsEditor topicId={id} />}

      <ConfirmDialog
        open={confirmState.isOpen}
        title={confirmState.title}
        message={confirmState.message}
        confirmText={confirmState.confirmText}
        cancelText={confirmState.cancelText}
        variant={confirmState.danger ? 'danger' : 'warning'}
        onConfirm={handleConfirm}
        onCancel={handleConfirmCancel}
      />
    </Box>
  );
}
