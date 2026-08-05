import { useEffect } from 'react';
import {
  Box,
  Button,
  CircularProgress,
  FormControlLabel,
  Paper,
  Switch,
  TextField,
  Typography,
} from '@mui/material';
import { useNavigate, useParams } from 'react-router-dom';
import { useForm, Controller } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { AxiosError } from 'axios';
import MediaUploader from '../components/MediaUploader';
import { PageLoader } from '../components/feedback/PageLoader';
import { useConfirm, useToast } from '../hooks';
import { ConfirmDialog } from '../components/feedback';
import { useSaveLibrary, useSpeakingLibrary } from '../hooks/useSpeaking';

const librarySchema = z.object({
  name: z.string().min(1, 'Название обязательно').max(120),
  description: z.string().max(1000).optional(),
  coverUrl: z.string().url().optional().or(z.literal('')).optional(),
  displayOrder: z.number().int().min(0),
  isPublished: z.boolean(),
});

type LibraryFormValues = z.infer<typeof librarySchema>;

export default function SpeakingLibraryEditor() {
  const { id } = useParams<{ id: string }>();
  const isEdit = !!id;
  const navigate = useNavigate();
  const toast = useToast();
  const { confirm, confirmState, handleConfirm, handleCancel: handleConfirmCancel } = useConfirm();

  const { data: library, isLoading } = useSpeakingLibrary(id);
  const saveMutation = useSaveLibrary();

  const {
    register,
    control,
    handleSubmit,
    reset,
    setValue,
    watch,
    formState: { errors, isSubmitting, isDirty },
  } = useForm<LibraryFormValues>({
    resolver: zodResolver(librarySchema),
    defaultValues: {
      name: '',
      description: '',
      coverUrl: undefined,
      displayOrder: 0,
      isPublished: false,
    },
  });

  const coverUrl = watch('coverUrl');

  useEffect(() => {
    if (isEdit && library) {
      reset({
        name: library.name,
        description: library.description ?? '',
        coverUrl: library.coverUrl,
        displayOrder: library.displayOrder,
        isPublished: library.isPublished,
      });
    }
  }, [isEdit, library, reset]);

  // MVP: предупреждение о несохранённых изменениях только на beforeunload
  // (legacy <Routes> — useBlocker недоступен, зафиксировано в спеке §4.3)
  useEffect(() => {
    const handler = (e: BeforeUnloadEvent) => {
      if (isDirty) e.preventDefault();
    };
    window.addEventListener('beforeunload', handler);
    return () => window.removeEventListener('beforeunload', handler);
  }, [isDirty]);

  const onSubmit = async (values: LibraryFormValues) => {
    const payload = {
      name: values.name,
      description: values.description || undefined,
      coverUrl: values.coverUrl || undefined,
      displayOrder: values.displayOrder,
      isPublished: values.isPublished,
    };
    try {
      await saveMutation.mutateAsync({ id, data: payload });
      toast.success(isEdit ? 'Сохранено' : 'Тема создана');
      if (!isEdit) navigate('/speaking/libraries');
    } catch (err) {
      toast.error(
        (err as AxiosError<{ message?: string }>).response?.data?.message ??
          (err as Error).message ??
          'Не удалось сохранить тему'
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

  return (
    <Box sx={{ maxWidth: 720 }}>
      <Typography variant="h4" data-testid="page-title" sx={{ mb: 3 }}>
        {isEdit ? 'Edit Library' : 'New Library'}
      </Typography>

      <Paper sx={{ p: 3 }}>
        <Box component="form" onSubmit={handleSubmit(onSubmit)} noValidate>
          <TextField
            label="Name"
            fullWidth
            required
            margin="normal"
            data-testid="library-name-input"
            error={!!errors.name}
            helperText={errors.name?.message}
            {...register('name')}
          />

          <TextField
            label="Description"
            fullWidth
            multiline
            rows={3}
            margin="normal"
            data-testid="library-description-input"
            error={!!errors.description}
            helperText={errors.description?.message}
            {...register('description')}
          />

          <Box sx={{ mt: 2 }} data-testid="library-cover-uploader">
            <MediaUploader
              value={coverUrl || undefined}
              onChange={(url) => setValue('coverUrl', url, { shouldDirty: true })}
              folder="speaking/covers"
              accept="image/*"
              mediaKind="image"
              label="Обложка темы"
            />
          </Box>

          <TextField
            label="Order"
            type="number"
            fullWidth
            margin="normal"
            data-testid="library-order-input"
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
                    data-testid="library-published-switch"
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
              data-testid="save-library-button"
              disabled={isSubmitting || saveMutation.isPending}
              startIcon={
                isSubmitting || saveMutation.isPending ? (
                  <CircularProgress size={18} color="inherit" />
                ) : undefined
              }
            >
              {isEdit ? 'Save' : 'Create'}
            </Button>
          </Box>
        </Box>
      </Paper>

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
