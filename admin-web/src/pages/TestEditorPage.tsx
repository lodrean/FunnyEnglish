import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  Box,
  Button,
  Card,
  CardContent,
  FormControl,
  FormControlLabel,
  Grid,
  IconButton,
  InputLabel,
  MenuItem,
  Select,
  Switch,
  TextField,
  Typography,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  CircularProgress,
} from '@mui/material';
import {
  Save,
  ArrowBack,
  Add,
  Delete,
  ExpandMore,
  DragIndicator,
} from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useForm, useFieldArray, Controller } from 'react-hook-form';
import {
  getAdminTest,
  getCategories,
  createTest,
  updateTest,
} from '../api/client';
import type { CreateTestRequest, CreateQuestionRequest, CreateAnswerRequest } from '../types';
import MediaUploader from '../components/MediaUploader';

const questionTypes = [
  { value: 'TEXT_SELECT', label: 'Выбор текста' },
  { value: 'IMAGE_SELECT', label: 'Выбор картинки' },
  { value: 'AUDIO_SELECT', label: 'Выбор по аудио' },
  { value: 'DRAG_DROP_IMAGE', label: 'Перетаскивание' },
  { value: 'FILL_BLANK', label: 'Заполнить пропуск' },
];

const difficulties = [
  { value: 'EASY', label: 'Легко' },
  { value: 'MEDIUM', label: 'Средне' },
  { value: 'HARD', label: 'Сложно' },
];

export default function TestEditorPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const isEditing = !!id;
  const [expandedQuestion, setExpandedQuestion] = useState<number | false>(0);

  const { data: categories } = useQuery({
    queryKey: ['categories'],
    queryFn: getCategories,
  });

  const { data: existingTest, isLoading } = useQuery({
    queryKey: ['admin-test', id],
    queryFn: () => getAdminTest(id!),
    enabled: isEditing,
  });

  const {
    control,
    register,
    handleSubmit,
    reset,
    watch,
    formState: { errors },
  } = useForm<CreateTestRequest>({
    defaultValues: {
      categoryId: '',
      title: '',
      description: '',
      difficulty: 'EASY',
      pointsReward: 10,
      isPublished: false,
      displayOrder: 0,
      questions: [createEmptyQuestion()],
    },
  });

  const {
    fields: questionFields,
    append: appendQuestion,
    remove: removeQuestion,
  } = useFieldArray({
    control,
    name: 'questions',
  });

  useEffect(() => {
    if (existingTest) {
      reset({
        categoryId: existingTest.categoryId,
        title: existingTest.title,
        description: existingTest.description || '',
        thumbnailUrl: existingTest.thumbnailUrl || '',
        difficulty: existingTest.difficulty,
        pointsReward: existingTest.pointsReward,
        timeLimitSeconds: existingTest.timeLimitSeconds || undefined,
        isPublished: existingTest.isPublished,
        displayOrder: existingTest.displayOrder,
        questions: existingTest.questions.map((q) => ({
          type: q.type,
          text: q.text || '',
          audioUrl: q.audioUrl || '',
          imageUrl: q.imageUrl || '',
          displayOrder: q.displayOrder,
          points: q.points,
          answers: q.answers.map((a) => ({
            text: a.text || '',
            imageUrl: a.imageUrl || '',
            audioUrl: a.audioUrl || '',
            isCorrect: a.isCorrect,
            displayOrder: a.displayOrder,
            matchTarget: a.matchTarget || '',
          })),
        })),
      });
    }
  }, [existingTest, reset]);

  const createMutation = useMutation({
    mutationFn: createTest,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-tests'] });
      navigate('/tests');
    },
  });

  const updateMutation = useMutation({
    mutationFn: (data: CreateTestRequest) => updateTest(id!, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-tests'] });
      queryClient.invalidateQueries({ queryKey: ['admin-test', id] });
      navigate('/tests');
    },
  });

  const onSubmit = (data: CreateTestRequest) => {
    if (isEditing) {
      updateMutation.mutate(data);
    } else {
      createMutation.mutate(data);
    }
  };

  const isPending = createMutation.isPending || updateMutation.isPending;

  if (isEditing && isLoading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box>
      <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
        <IconButton onClick={() => navigate('/tests')} sx={{ mr: 2 }}>
          <ArrowBack />
        </IconButton>
        <Typography variant="h5" fontWeight="bold">
          {isEditing ? 'Редактирование теста' : 'Новый тест'}
        </Typography>
      </Box>

      <form onSubmit={handleSubmit(onSubmit)}>
        <Grid container spacing={3}>
          {/* Basic Info */}
          <Grid item xs={12} md={8}>
            <Card>
              <CardContent>
                <Typography variant="h6" mb={2}>
                  Основная информация
                </Typography>
                <Grid container spacing={2}>
                  <Grid item xs={12}>
                    <TextField
                      fullWidth
                      label="Название"
                      {...register('title', { required: 'Обязательное поле' })}
                      error={!!errors.title}
                      helperText={errors.title?.message}
                    />
                  </Grid>
                  <Grid item xs={12}>
                    <TextField
                      fullWidth
                      multiline
                      rows={3}
                      label="Описание"
                      {...register('description')}
                    />
                  </Grid>
                  <Grid item xs={12} sm={6}>
                    <Controller
                      name="categoryId"
                      control={control}
                      rules={{ required: 'Выберите категорию' }}
                      render={({ field }) => (
                        <FormControl fullWidth error={!!errors.categoryId}>
                          <InputLabel>Категория</InputLabel>
                          <Select {...field} label="Категория">
                            {categories?.map((cat) => (
                              <MenuItem key={cat.id} value={cat.id}>
                                {cat.name}
                              </MenuItem>
                            ))}
                          </Select>
                        </FormControl>
                      )}
                    />
                  </Grid>
                  <Grid item xs={12} sm={6}>
                    <Controller
                      name="difficulty"
                      control={control}
                      render={({ field }) => (
                        <FormControl fullWidth>
                          <InputLabel>Сложность</InputLabel>
                          <Select {...field} label="Сложность">
                            {difficulties.map((d) => (
                              <MenuItem key={d.value} value={d.value}>
                                {d.label}
                              </MenuItem>
                            ))}
                          </Select>
                        </FormControl>
                      )}
                    />
                  </Grid>
                  <Grid item xs={12} sm={6}>
                    <TextField
                      fullWidth
                      type="number"
                      label="Очки за прохождение"
                      {...register('pointsReward', { valueAsNumber: true })}
                    />
                  </Grid>
                  <Grid item xs={12} sm={6}>
                    <TextField
                      fullWidth
                      type="number"
                      label="Лимит времени (сек)"
                      {...register('timeLimitSeconds', { valueAsNumber: true })}
                    />
                  </Grid>
                </Grid>
              </CardContent>
            </Card>
          </Grid>

          {/* Sidebar */}
          <Grid item xs={12} md={4}>
            <Card>
              <CardContent>
                <Typography variant="h6" mb={2}>
                  Публикация
                </Typography>
                <Controller
                  name="isPublished"
                  control={control}
                  render={({ field }) => (
                    <FormControlLabel
                      control={<Switch {...field} checked={field.value} />}
                      label={field.value ? 'Опубликован' : 'Черновик'}
                    />
                  )}
                />
                <Box mt={3}>
                  <Button
                    type="submit"
                    variant="contained"
                    fullWidth
                    startIcon={<Save />}
                    disabled={isPending}
                  >
                    {isPending ? 'Сохранение...' : 'Сохранить'}
                  </Button>
                </Box>
              </CardContent>
            </Card>

            <Card sx={{ mt: 2 }}>
              <CardContent>
                <Typography variant="h6" mb={2}>
                  Обложка
                </Typography>
                <Controller
                  name="thumbnailUrl"
                  control={control}
                  render={({ field }) => (
                    <MediaUploader
                      value={field.value}
                      onChange={field.onChange}
                      folder="thumbnails"
                    />
                  )}
                />
              </CardContent>
            </Card>
          </Grid>

          {/* Questions */}
          <Grid item xs={12}>
            <Card>
              <CardContent>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
                  <Typography variant="h6">
                    Вопросы ({questionFields.length})
                  </Typography>
                  <Button
                    startIcon={<Add />}
                    onClick={() => {
                      appendQuestion(createEmptyQuestion());
                      setExpandedQuestion(questionFields.length);
                    }}
                  >
                    Добавить вопрос
                  </Button>
                </Box>

                {questionFields.map((field, qIndex) => (
                  <QuestionEditor
                    key={field.id}
                    index={qIndex}
                    control={control}
                    register={register}
                    watch={watch}
                    expanded={expandedQuestion === qIndex}
                    onExpand={() =>
                      setExpandedQuestion(expandedQuestion === qIndex ? false : qIndex)
                    }
                    onRemove={() => removeQuestion(qIndex)}
                    canRemove={questionFields.length > 1}
                  />
                ))}
              </CardContent>
            </Card>
          </Grid>
        </Grid>
      </form>
    </Box>
  );
}

function QuestionEditor({
  index,
  control,
  register,
  watch,
  expanded,
  onExpand,
  onRemove,
  canRemove,
}: {
  index: number;
  control: any;
  register: any;
  watch: any;
  expanded: boolean;
  onExpand: () => void;
  onRemove: () => void;
  canRemove: boolean;
}) {
  const questionType = watch(`questions.${index}.type`);

  const {
    fields: answerFields,
    append: appendAnswer,
    remove: removeAnswer,
  } = useFieldArray({
    control,
    name: `questions.${index}.answers`,
  });

  return (
    <Accordion expanded={expanded} onChange={onExpand}>
      <AccordionSummary expandIcon={<ExpandMore />}>
        <Box sx={{ display: 'flex', alignItems: 'center', width: '100%' }}>
          <DragIndicator sx={{ mr: 1, color: 'text.secondary' }} />
          <Typography>Вопрос {index + 1}</Typography>
          {canRemove && (
            <IconButton
              size="small"
              onClick={(e) => {
                e.stopPropagation();
                onRemove();
              }}
              sx={{ ml: 'auto', mr: 1 }}
            >
              <Delete fontSize="small" />
            </IconButton>
          )}
        </Box>
      </AccordionSummary>
      <AccordionDetails>
        <Grid container spacing={2}>
          <Grid item xs={12} sm={6}>
            <Controller
              name={`questions.${index}.type`}
              control={control}
              render={({ field }) => (
                <FormControl fullWidth size="small">
                  <InputLabel>Тип вопроса</InputLabel>
                  <Select {...field} label="Тип вопроса">
                    {questionTypes.map((t) => (
                      <MenuItem key={t.value} value={t.value}>
                        {t.label}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
              )}
            />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              size="small"
              type="number"
              label="Очки"
              {...register(`questions.${index}.points`, { valueAsNumber: true })}
            />
          </Grid>
          <Grid item xs={12}>
            <TextField
              fullWidth
              size="small"
              label="Текст вопроса"
              {...register(`questions.${index}.text`)}
            />
          </Grid>

          {/* Answers */}
          <Grid item xs={12}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
              <Typography variant="subtitle2">Ответы</Typography>
              <Button
                size="small"
                startIcon={<Add />}
                onClick={() => appendAnswer(createEmptyAnswer())}
              >
                Добавить
              </Button>
            </Box>

            {answerFields.map((answerField, aIndex) => (
              <Box
                key={answerField.id}
                sx={{
                  display: 'flex',
                  gap: 1,
                  mb: 1,
                  alignItems: 'center',
                }}
              >
                <TextField
                  size="small"
                  label="Текст ответа"
                  {...register(`questions.${index}.answers.${aIndex}.text`)}
                  sx={{ flex: 1 }}
                />
                {questionType === 'DRAG_DROP_IMAGE' && (
                  <TextField
                    size="small"
                    label="Цель"
                    {...register(`questions.${index}.answers.${aIndex}.matchTarget`)}
                    sx={{ width: 120 }}
                  />
                )}
                <Controller
                  name={`questions.${index}.answers.${aIndex}.isCorrect`}
                  control={control}
                  render={({ field }) => (
                    <FormControlLabel
                      control={<Switch {...field} checked={field.value} size="small" />}
                      label="Верный"
                    />
                  )}
                />
                <IconButton
                  size="small"
                  onClick={() => removeAnswer(aIndex)}
                  disabled={answerFields.length <= 1}
                >
                  <Delete fontSize="small" />
                </IconButton>
              </Box>
            ))}
          </Grid>
        </Grid>
      </AccordionDetails>
    </Accordion>
  );
}

function createEmptyQuestion(): CreateQuestionRequest {
  return {
    type: 'TEXT_SELECT',
    text: '',
    audioUrl: '',
    imageUrl: '',
    displayOrder: 0,
    points: 1,
    answers: [createEmptyAnswer(), createEmptyAnswer()],
  };
}

function createEmptyAnswer(): CreateAnswerRequest {
  return {
    text: '',
    imageUrl: '',
    audioUrl: '',
    isCorrect: false,
    displayOrder: 0,
    matchTarget: '',
  };
}
