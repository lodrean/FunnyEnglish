import React, { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  Button,
  Tabs,
  Tab,
  TextField,
  Paper,
  Grid,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Slider,
  Switch,
  FormControlLabel,
  Chip,
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Alert,
  InputAdornment,
  Skeleton,
  useTheme,
  alpha,
  Snackbar,
  CircularProgress,
} from '@mui/material';
import {
  Save as SaveIcon,
  Preview as PreviewIcon,
  ArrowBack as ArrowBackIcon,
  Add as AddIcon,
  Delete as DeleteIcon,
  DragIndicator as DragIndicatorIcon,
  Image as ImageIcon,
  CheckCircle as CheckCircleIcon,
  Settings as SettingsIcon,
  Assessment as AssessmentIcon,
  Info as InfoIcon,
  CloudUpload as CloudUploadIcon,
} from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useParams, useNavigate } from 'react-router-dom';
import { ImageWordMatchEditor } from '../components/questions/ImageWordMatchEditor';
import { HotspotShape } from '../types/questions';
import { ErrorDisplay, useApiError, type ErrorDetails } from '../components/ErrorDisplay';
import { 
  getAdminTest, createTest, updateTest, getCategories,
  createQuestion, updateQuestion, deleteQuestion, getQuestionsByTest,
  createImageWordMatchQuestion, updateImageWordMatchQuestion,
  uploadMedia
} from '../api/client';
import type { Test, Category, CreateTestRequest } from '../types';
import type { 
  QuestionV2, 
  CreateQuestionRequest, 
  ImageWordMatchContent,
  QuestionTypeV2 
} from '../types/questions';



// Types for Test Editor (extended from API types)
interface TestFormData {
  id: string;
  title: string;
  description: string;
  categoryId: string;
  difficulty: 'EASY' | 'MEDIUM' | 'HARD';
  status: 'draft' | 'published' | 'archived';
  imageUrl?: string;
  timeLimit: number; // in minutes for UI
  passingScore: number;
  maxAttempts: number;
  shuffleQuestions: boolean;
  showResults: boolean;
  allowRetake: boolean;
  isPublished: boolean;
  pointsReward: number;
  displayOrder: number;
}

interface Question {
  id: string;
  type: 'multiple_choice' | 'true_false' | 'fill_blank' | 'matching' | 'image_word_match';
  text: string;
  options: string[];
  correctAnswer: string | string[];
  points: number;
  explanation?: string;
  imageUrl?: string;
  words?: { id: string; text: string; translation?: string }[];
  hotspots?: { id: string; x: number; y: number; width: number; height: number; shape: HotspotShape; wordId: string }[];
  isSaving?: boolean;
  isNew?: boolean;
}

// API Functions
const fetchTest = async (id: string): Promise<TestFormData> => {
  if (id === 'new') {
    return {
      id: 'new',
      title: '',
      description: '',
      categoryId: '',
      difficulty: 'MEDIUM',
      status: 'draft',
      timeLimit: 30,
      passingScore: 70,
      maxAttempts: 0,
      shuffleQuestions: false,
      showResults: true,
      allowRetake: true,
      isPublished: false,
      pointsReward: 10,
      displayOrder: 0,
    };
  }
  
  const test = await getAdminTest(id);
  return {
    ...test,
    id: test.id,
    title: test.title,
    description: test.description || '',
    categoryId: test.categoryId,
    difficulty: test.difficulty,
    status: test.isPublished ? 'published' : 'draft',
    timeLimit: (test.timeLimitSeconds || 1800) / 60, // Convert seconds to minutes
    passingScore: 70, // Default, backend doesn't have this field yet
    maxAttempts: 0, // Default, backend doesn't have this field yet
    shuffleQuestions: false, // Default
    showResults: true, // Default
    allowRetake: true, // Default
    isPublished: test.isPublished,
    pointsReward: test.pointsReward,
    displayOrder: test.displayOrder,
  };
};

// UUID validation regex
const UUID_REGEX = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;

const isValidUUID = (str: string): boolean => {
  return UUID_REGEX.test(str);
};

const saveTest = async (test: TestFormData): Promise<Test> => {
  // Validate categoryId is a valid UUID
  if (!test.categoryId || test.categoryId.trim() === '') {
    throw new Error('Please select a category from the dropdown.');
  }
  if (!isValidUUID(test.categoryId)) {
    throw new Error(`Invalid category ID format: "${test.categoryId}". Please select a valid category from the dropdown.`);
  }

  const testData: CreateTestRequest = {
    categoryId: test.categoryId,
    title: test.title,
    description: test.description,
    thumbnailUrl: test.imageUrl,
    difficulty: test.difficulty,
    pointsReward: test.pointsReward,
    timeLimitSeconds: test.timeLimit * 60, // Convert minutes to seconds
    isPublished: test.status === 'published' || test.isPublished,
    displayOrder: test.displayOrder,
    questions: [], // Questions are managed separately
  };

  if (test.id === 'new') {
    return await createTest(testData);
  } else {
    return await updateTest(test.id, testData);
  }
};

// Convert local question format to API format
const convertToApiQuestion = (question: Question, testId: string, displayOrder: number): CreateQuestionRequest => {
  // For IMAGE_WORD_MATCH type
  if (question.type === 'image_word_match') {
    const content: ImageWordMatchContent = {
      imageUrl: question.imageUrl || '',
      instruction: question.text || 'Match the words to the objects',
      words: question.words || [],
      hotspots: question.hotspots || [],
    };

    return {
      testId,
      type: 'IMAGE_WORD_MATCH' as QuestionTypeV2,
      title: question.text || 'Image Word Match Question',
      content,
      mediaUrl: question.imageUrl,
      displayOrder,
      points: question.points || 1,
      explanation: question.explanation,
    };
  }

  // For other types (default to TEXT_SELECT)
  const content = {
    text: question.text || '',
    answers: question.options?.map((opt, idx) => ({
      id: `ans_${idx}`,
      text: opt,
      isCorrect: question.correctAnswer === opt,
    })) || [],
  };

  return {
    testId,
    type: 'TEXT_SELECT' as QuestionTypeV2,
    title: question.text || 'Question',
    content,
    mediaUrl: question.imageUrl,
    displayOrder,
    points: question.points || 1,
    explanation: question.explanation,
  };
};

// Save single question to API
const saveSingleQuestion = async (
  testId: string, 
  question: Question,
  displayOrder: number
): Promise<QuestionV2> => {
  console.log('Saving single question:', question.id, 'Type:', question.type);

  if (question.type === 'image_word_match') {
    const iwData = {
      testId,
      instruction: question.text || 'Match the words to the objects',
      imageUrl: question.imageUrl || '',
      words: question.words || [],
      hotspots: (question.hotspots || []).map(h => ({
        ...h,
        shape: h.shape || 'RECTANGLE'
      })),
      points: question.points || 10
    };
    
    console.log('IMAGE_WORD_MATCH data:', iwData);

    if (question.isNew || question.id.startsWith('temp-')) {
      console.log('Creating new IMAGE_WORD_MATCH question');
      return await createImageWordMatchQuestion(iwData);
    } else {
      console.log('Updating existing IMAGE_WORD_MATCH question:', question.id);
      return await updateImageWordMatchQuestion(question.id, iwData);
    }
  } else {
    const apiQuestion = convertToApiQuestion(question, testId, displayOrder);
    
    if (question.isNew || question.id.startsWith('temp-')) {
      return await createQuestion(apiQuestion);
    } else {
      return await updateQuestion(question.id, apiQuestion);
    }
  }
};

// Tab Panel Component
interface TabPanelProps {
  children: React.ReactNode;
  value: number;
  index: number;
}

const TabPanel: React.FC<TabPanelProps> = ({ children, value, index }) => (
  <Box role="tabpanel" hidden={value !== index} sx={{ py: 3 }}>
    {value === index && children}
  </Box>
);

// Question Builder Component
interface QuestionBuilderProps {
  question: Question;
  onChange: (question: Question) => void;
  onDelete: () => void;
  onSave: (question: Question) => Promise<void>;
  testId: string;
}

const QuestionBuilder: React.FC<QuestionBuilderProps> = ({ question, onChange, onDelete, onSave, testId }) => {
  const theme = useTheme();
  const [isSaving, setIsSaving] = useState(false);
  const [saveError, setSaveError] = useState<string | null>(null);

  const handleSave = async (data: any) => {
    setIsSaving(true);
    setSaveError(null);
    
    try {
      const updatedQuestion = {
        ...question,
        text: data.instruction,
        imageUrl: data.imageUrl,
        words: data.words,
        hotspots: data.hotspots,
        points: data.points
      };
      
      // Update local state first
      onChange(updatedQuestion);
      
      // Save to server
      await onSave(updatedQuestion);
    } catch (error: any) {
      console.error('Failed to save question:', error);
      setSaveError(error.response?.data?.message || 'Failed to save question');
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <Paper sx={{ p: 3, mb: 2, border: '1px solid #E0E0E0' }} data-testid={`question-card-${question.id}`}>
      <Box display="flex" justifyContent="space-between" alignItems="flex-start" mb={2}>
        <Box display="flex" alignItems="center" gap={1}>
          <DragIndicatorIcon sx={{ color: theme.palette.text.secondary, cursor: 'grab' }} />
          <Chip
            label={question.type.replace('_', ' ').toUpperCase()}
            size="small"
            sx={{ backgroundColor: alpha(theme.palette.primary.main, 0.2), color: theme.palette.primary.main }}
          />
          {question.isNew && (
            <Chip label="UNSAVED" size="small" color="warning" variant="outlined" />
          )}
          {isSaving && (
            <Chip 
              label="SAVING..." 
              size="small" 
              color="primary" 
              variant="outlined"
              icon={<CircularProgress size={12} />}
            />
          )}
        </Box>
        <IconButton onClick={onDelete} sx={{ color: theme.palette.error.main }} data-testid="delete-question-button">
          <DeleteIcon />
        </IconButton>
      </Box>

      {saveError && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {saveError}
        </Alert>
      )}

      <TextField
        label="Question Text"
        fullWidth
        multiline
        rows={2}
        value={question.text}
        onChange={(e) => onChange({ ...question, text: e.target.value, isNew: true })}
        sx={{ mb: 2 }}
        disabled={question.type === 'image_word_match'}
        helperText={question.type === 'image_word_match' ? 'Edit text in the editor below' : ''}
      />

      {question.type === 'multiple_choice' && (
        <Box>
          <Typography variant="subtitle2" gutterBottom>
            Options
          </Typography>
          {question.options.map((option, index) => (
            <Box key={index} display="flex" gap={1} mb={1}>
              <TextField
                fullWidth
                size="small"
                placeholder={`Option ${index + 1}`}
                value={option}
                onChange={(e) => {
                  const newOptions = [...question.options];
                  newOptions[index] = e.target.value;
                  onChange({ ...question, options: newOptions, isNew: true });
                }}
              />
              <FormControlLabel
                control={
                  <Switch
                    checked={question.correctAnswer === option}
                    onChange={() => onChange({ ...question, correctAnswer: option, isNew: true })}
                  />
                }
                label="Correct"
              />
            </Box>
          ))}
        </Box>
      )}

      {question.type === 'image_word_match' && (
        <Box sx={{ mt: 2 }}>
          <ImageWordMatchEditor
            testId={testId}
            initialContent={{
              imageUrl: question.imageUrl || '',
              instruction: question.text || 'Match the words to the objects',
              words: question.words || [],
              hotspots: question.hotspots || []
            }}
            onSave={handleSave}
            onCancel={() => {}}
          />
        </Box>
      )}

      <TextField
        label="Explanation (optional)"
        fullWidth
        multiline
        rows={2}
        value={question.explanation || ''}
        onChange={(e) => onChange({ ...question, explanation: e.target.value, isNew: true })}
        sx={{ mt: 2 }}
      />
    </Paper>
  );
};

// Question List Component
interface QuestionListProps {
  questions: Question[];
  onChange: (questions: Question[]) => void;
  onSaveQuestion: (question: Question, index: number) => Promise<void>;
  testId: string;
}

const QuestionList: React.FC<QuestionListProps> = ({ questions, onChange, onSaveQuestion, testId }) => {
  const handleAddQuestion = (type: Question['type']) => {
    const newQuestion: Question = {
      id: `temp-${Date.now()}`,
      type,
      text: type === 'image_word_match' ? 'Match the words to the objects' : '',
      options: type === 'multiple_choice' ? ['', '', '', ''] : [],
      correctAnswer: '',
      points: type === 'image_word_match' ? 10 : 1,
      imageUrl: type === 'image_word_match' ? '' : undefined,
      words: type === 'image_word_match' ? [] : undefined,
      hotspots: type === 'image_word_match' ? [] : undefined,
      isNew: true,
    };
    onChange([...questions, newQuestion]);
  };

  const handleUpdateQuestion = (index: number, question: Question) => {
    const newQuestions = [...questions];
    newQuestions[index] = question;
    onChange(newQuestions);
  };

  const handleDeleteQuestion = async (index: number) => {
    const question = questions[index];
    
    // If question is saved on server, delete it
    if (!question.id.startsWith('temp-')) {
      try {
        await deleteQuestion(question.id);
      } catch (error) {
        console.error('Failed to delete question:', error);
      }
    }
    
    const newQuestions = [...questions];
    newQuestions.splice(index, 1);
    onChange(newQuestions);
  };

  return (
    <Box>
      <Box display="flex" gap={1} mb={3} flexWrap="wrap">
        <Button
          variant="outlined"
          startIcon={<AddIcon />}
          onClick={() => handleAddQuestion('multiple_choice')}
          size="small"
          data-testid="add-multiple-choice-button"
        >
          Multiple Choice
        </Button>
        <Button
          variant="outlined"
          startIcon={<AddIcon />}
          onClick={() => handleAddQuestion('true_false')}
          size="small"
          data-testid="add-true-false-button"
        >
          True/False
        </Button>
        <Button
          variant="outlined"
          startIcon={<AddIcon />}
          onClick={() => handleAddQuestion('fill_blank')}
          size="small"
          data-testid="add-fill-blank-button"
        >
          Fill in Blank
        </Button>
        <Button
          variant="outlined"
          startIcon={<AddIcon />}
          onClick={() => handleAddQuestion('matching')}
          size="small"
          data-testid="add-matching-button"
        >
          Matching
        </Button>
        <Button
          variant="outlined"
          startIcon={<AddIcon />}
          onClick={() => handleAddQuestion('image_word_match')}
          size="small"
          sx={{ borderColor: 'primary.main', color: 'primary.main', '&:hover': { borderColor: 'primary.dark', backgroundColor: (theme) => alpha(theme.palette.primary.main, 0.05) } }}
          data-testid="add-image-word-match-button"
        >
          🖼️ Image Word Match
        </Button>
      </Box>

      {questions.length === 0 ? (
        <Paper sx={{ p: 4, textAlign: 'center', border: '2px dashed #E0E0E0' }}>
          <Typography color="text.secondary" gutterBottom>
            No questions yet
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Add your first question using the buttons above
          </Typography>
        </Paper>
      ) : (
        questions.map((question, index) => (
          <QuestionBuilder
            key={question.id}
            question={question}
            onChange={(q) => handleUpdateQuestion(index, q)}
            onDelete={() => handleDeleteQuestion(index)}
            onSave={(q) => onSaveQuestion(q, index)}
            testId={testId}
          />
        ))
      )}
    </Box>
  );
};

// Main Component
const TestEditor: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const theme = useTheme();
  
  const [activeTab, setActiveTab] = useState(0);
  const [test, setTest] = useState<TestFormData | null>(() => 
    id === 'new' || !id ? {
      id: 'new',
      title: '',
      description: '',
      categoryId: '',
      difficulty: 'MEDIUM',
      status: 'draft',
      timeLimit: 30,
      passingScore: 70,
      maxAttempts: 0,
      shuffleQuestions: false,
      showResults: true,
      allowRetake: true,
      isPublished: false,
      pointsReward: 10,
      displayOrder: 0,
    } : null
  );
  const [questions, setQuestions] = useState<Question[]>([]);
  const [hasChanges, setHasChanges] = useState(false);
  const [saveDialogOpen, setSaveDialogOpen] = useState(false);
  const [snackbar, setSnackbar] = useState<{ open: boolean; message: string; severity: 'success' | 'error' }>({
    open: false,
    message: '',
    severity: 'success',
  });
  const [pageError, setPageError] = useState<ErrorDetails | null>(null);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const { parseError } = useApiError();

  const { data: testData, isLoading: testLoading } = useQuery({
    queryKey: ['test', id],
    queryFn: () => fetchTest(id || 'new'),
    enabled: !!id,
  });

  // Load existing questions when editing
  const { data: existingQuestions } = useQuery({
    queryKey: ['questions', id],
    queryFn: () => getQuestionsByTest(id!),
    enabled: !!id && id !== 'new',
  });

  const { data: apiCategories = [] } = useQuery({
    queryKey: ['categories'],
    queryFn: getCategories,
    staleTime: 0, // Always fetch fresh data
    refetchOnMount: true,
  });

  // Transform categories for display
  const categories = apiCategories.map((cat: Category, idx: number) => {
    console.log(`Mapping category ${idx}:`, cat);
    return {
      id: cat.id,
      name: cat.name,
      path: cat.name,
    };
  });
  console.log('Transformed categories:', categories);

  // Mutation for saving individual question
  const saveQuestionMutation = useMutation({
    mutationFn: async ({ question, index }: { question: Question; index: number }) => {
      if (!test || test.id === 'new') {
        throw new Error('Please save the test first before adding questions');
      }
      return saveSingleQuestion(test.id, question, index);
    },
    onSuccess: (savedQuestion, variables) => {
      // Update question ID if it was new
      const newQuestions = [...questions];
      newQuestions[variables.index] = {
        ...variables.question,
        id: savedQuestion.id,
        isNew: false,
      };
      setQuestions(newQuestions);
      
      // Invalidate queries to refresh data
      queryClient.invalidateQueries({ queryKey: ['questions', id] });
      
      setSnackbar({ 
        open: true, 
        message: 'Question saved successfully!', 
        severity: 'success' 
      });
    },
    onError: (error: any) => {
      console.error('Save question error:', error);
      setSnackbar({ 
        open: true, 
        message: error.message || 'Failed to save question', 
        severity: 'error' 
      });
    },
  });

  const saveMutation = useMutation({
    mutationFn: async (data: { test: TestFormData; questions: Question[] }) => {
      // First save the test
      const savedTest = await saveTest(data.test);
      
      // Then save all unsaved questions
      const unsavedQuestions = data.questions.filter(q => q.isNew || q.id.startsWith('temp-'));
      for (let i = 0; i < unsavedQuestions.length; i++) {
        const question = unsavedQuestions[i];
        const originalIndex = data.questions.indexOf(question);
        await saveSingleQuestion(savedTest.id, question, originalIndex);
      }
      
      return savedTest;
    },
    onSuccess: (savedTest) => {
      queryClient.invalidateQueries({ queryKey: ['tests'] });
      queryClient.invalidateQueries({ queryKey: ['test', savedTest.id] });
      queryClient.invalidateQueries({ queryKey: ['questions', savedTest.id] });
      setHasChanges(false);
      setSnackbar({ open: true, message: 'Test saved successfully!', severity: 'success' });
      if (id === 'new') {
        navigate(`/content/tests/${savedTest.id}`);
      }
    },
    onError: (error: any) => {
      console.error('Save error:', error);
      const parsedError = parseError(error);
      setPageError(parsedError);
      
      // Set field-level errors for form highlighting
      if (parsedError?.fieldErrors) {
        setFieldErrors(parsedError.fieldErrors);
      }
      
      // Also show snackbar for quick feedback
      setSnackbar({ 
        open: true, 
        message: parsedError?.message || 'Failed to save test. Please try again.', 
        severity: 'error' 
      });
    },
  });

  useEffect(() => {
    if (testData) {
      setTest(testData);
    }
  }, [testData]);

  // Convert and load existing questions
  useEffect(() => {
    if (existingQuestions && existingQuestions.length > 0) {
      const convertedQuestions: Question[] = existingQuestions.map((q, index) => {
        if (q.type === 'IMAGE_WORD_MATCH') {
          // Используем imageWordMatchData из /details endpoint
          const iwData = q.imageWordMatchData;
          return {
            id: q.id,
            type: 'image_word_match',
            text: iwData?.instruction || q.title,
            options: [],
            correctAnswer: '',
            points: q.points,
            explanation: q.explanation,
            imageUrl: iwData?.imageUrl || q.mediaUrl,
            words: iwData?.words || [],
            hotspots: iwData?.hotspots || [],
            isNew: false,
          };
        }
        // Handle other question types
        return {
          id: q.id,
          type: 'multiple_choice',
          text: q.title,
          options: (q.content as { options?: string[] })?.options || [],
          correctAnswer: '',
          points: q.points,
          explanation: q.explanation,
          isNew: false,
        };
      });
      setQuestions(convertedQuestions);
    }
  }, [existingQuestions]);

  const handleChange = (field: keyof TestFormData, value: any) => {
    if (test) {
      setTest({ ...test, [field]: value });
      setHasChanges(true);
      
      // Clear error for this field when user changes it
      if (fieldErrors[field as string]) {
        setFieldErrors(prev => {
          const newErrors = { ...prev };
          delete newErrors[field as string];
          return newErrors;
        });
      }
      
      // Clear page error when user makes changes
      if (pageError) {
        setPageError(null);
      }
    }
  };

  const handleSave = () => {
    if (test) {
      saveMutation.mutate({ test, questions });
    }
  };

  const handlePreview = () => {
    if (test?.id && test.id !== 'new') {
      window.open(`/test/${test.id}/preview`, '_blank');
    }
  };

  const handleBack = () => {
    if (hasChanges) {
      setSaveDialogOpen(true);
    } else {
      navigate('/content/tests');
    }
  };

  const handleSaveQuestion = async (question: Question, index: number) => {
    await saveQuestionMutation.mutateAsync({ question, index });
  };

  const handleImageUpload = async (file: File) => {
    try {
      const url = await uploadMedia(file, 'test-thumbnails');
      handleChange('imageUrl', url);
    } catch (error) {
      console.error('Failed to upload image:', error);
      setSnackbar({
        open: true,
        message: 'Failed to upload image',
        severity: 'error',
      });
    }
  };

  if (testLoading) {
    return (
      <Box p={3}>
        <Skeleton variant="text" width="40%" height={40} />
        <Skeleton variant="rectangular" height={400} sx={{ mt: 2 }} />
      </Box>
    );
  }

  if (!test) {
    return (
      <Box p={3}>
        <Alert severity="error">Failed to load test data.</Alert>
      </Box>
    );
  }

  return (
    <Box>
      {/* Header */}
      <Paper sx={{ px: 3, py: 2, borderRadius: 0, borderBottom: '1px solid #E0E0E0' }}>
        <Box display="flex" flexDirection={{ xs: 'column', sm: 'row' }} justifyContent="space-between" alignItems={{ xs: 'flex-start', sm: 'center' }} gap={2}>
          <Box display="flex" alignItems="center" gap={2}>
            <IconButton onClick={handleBack} data-testid="back-button">
              <ArrowBackIcon />
            </IconButton>
            <Box>
              <Typography variant="h5" fontWeight="bold">
                {id === 'new' ? 'Create Test' : test.title || 'Untitled Test'}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                {hasChanges || questions.some(q => q.isNew) ? 'Unsaved changes' : 'All changes saved'}
              </Typography>
            </Box>
          </Box>
          <Box display="flex" gap={1}>
            <Button
              variant="outlined"
              startIcon={<PreviewIcon />}
              onClick={handlePreview}
              disabled={id === 'new'}
              data-testid="preview-button"
            >
              Preview
            </Button>
            <Button
              variant="contained"
              startIcon={<SaveIcon />}
              onClick={handleSave}
              disabled={(!hasChanges && !questions.some(q => q.isNew)) || saveMutation.isPending}
              data-testid="save-test-button"
            >
              {saveMutation.isPending ? 'Saving...' : 'Save'}
            </Button>
          </Box>
        </Box>
      </Paper>

      {/* Tabs */}
      <Box sx={{ borderBottom: 1, borderColor: 'divider', px: 3 }}>
        <Tabs value={activeTab} onChange={(_, v) => setActiveTab(v)}>
          <Tab icon={<InfoIcon fontSize="small" />} iconPosition="start" label="General" data-testid="general-tab" />
          <Tab icon={<CheckCircleIcon fontSize="small" />} iconPosition="start" label={`Questions (${questions.length})`} data-testid="questions-tab" />
          <Tab icon={<SettingsIcon fontSize="small" />} iconPosition="start" label="Settings" data-testid="settings-tab" />
          <Tab icon={<AssessmentIcon fontSize="small" />} iconPosition="start" label="Analytics" data-testid="analytics-tab" />
        </Tabs>
      </Box>

      {/* Content */}
      <Box px={3} py={2}>
        {/* General Tab */}
        <TabPanel value={activeTab} index={0}>
          {/* Error Display */}
          {pageError && (
            <ErrorDisplay 
              error={pageError} 
              onClose={() => setPageError(null)}
              maxWidth="100%"
            />
          )}
          
          <Grid container spacing={4}>
            <Grid size={{ xs: 12, md: 8 }}>
              <Paper sx={{ p: 3 }}>
                <Typography variant="h6" gutterBottom>
                  Basic Information
                </Typography>
                
                <TextField
                  label="Test Title"
                  fullWidth
                  required
                  value={test.title}
                  onChange={(e) => handleChange('title', e.target.value)}
                  sx={{ mb: 3 }}
                  error={!!fieldErrors.title || !test.title.trim()}
                  helperText={fieldErrors.title || (!test.title.trim() ? 'Title is required' : '')}
                  data-testid="test-title-input"
                />

                <TextField
                  label="Description"
                  fullWidth
                  multiline
                  rows={4}
                  value={test.description}
                  onChange={(e) => handleChange('description', e.target.value)}
                  sx={{ mb: 3 }}
                  placeholder="Describe what this test covers..."
                  error={!!fieldErrors.description}
                  helperText={fieldErrors.description || ''}
                />

                <FormControl fullWidth sx={{ mb: 3 }} error={!!fieldErrors.categoryId || !test.categoryId}>
                  <InputLabel>Category *</InputLabel>
                  <Select
                    value={test.categoryId || ''}
                    label="Category *"
                    disabled={categories.length === 0}
                    onChange={(e) => {
                      const selectedValue = String(e.target.value);
                      console.log('Category Select - raw value:', selectedValue, 'categories count:', categories.length);
                      
                      // Check if it's a valid UUID already
                      if (UUID_REGEX.test(selectedValue)) {
                        console.log('Category Select - using as UUID');
                        handleChange('categoryId', selectedValue);
                        return;
                      }
                      
                      // Try to parse as index (MUI sometimes returns index as string)
                      const index = parseInt(selectedValue, 10);
                      console.log('Category Select - parsed index:', index, 'valid:', !isNaN(index));
                      
                      if (!isNaN(index) && index >= 0 && index < categories.length && categories[index]) {
                        const categoryId = categories[index].id;
                        console.log('Category Select - resolved index', index, 'to category ID:', categoryId);
                        handleChange('categoryId', categoryId);
                      } else if (selectedValue !== '') {
                        console.warn('Category Select - could not resolve index', index, 'using raw value:', selectedValue);
                        handleChange('categoryId', selectedValue);
                      }
                    }}
                  >
                    <MenuItem value="" disabled>
                      <em>{categories.length === 0 ? 'Loading categories...' : 'Select a category...'}</em>
                    </MenuItem>
                    {categories.map((cat, idx) => (
                      <MenuItem key={cat.id} value={cat.id} data-index={idx}>
                        {cat.path}
                      </MenuItem>
                    ))}
                  </Select>
                  {(fieldErrors.categoryId || !test.categoryId) && (
                    <Typography variant="caption" color="error" sx={{ mt: 0.5, ml: 1.5 }}>
                      {fieldErrors.categoryId || 'Category is required'}
                    </Typography>
                  )}
                </FormControl>

                <FormControl fullWidth sx={{ mb: 3 }}>
                  <InputLabel>Difficulty</InputLabel>
                  <Select
                    value={test.difficulty}
                    label="Difficulty"
                    onChange={(e) => handleChange('difficulty', e.target.value)}
                  >
                    <MenuItem value="EASY">Easy</MenuItem>
                    <MenuItem value="MEDIUM">Medium</MenuItem>
                    <MenuItem value="HARD">Hard</MenuItem>
                  </Select>
                </FormControl>

                <FormControl fullWidth>
                  <InputLabel>Status</InputLabel>
                  <Select
                    value={test.status}
                    label="Status"
                    onChange={(e) => handleChange('status', e.target.value)}
                  >
                    <MenuItem value="draft">Draft</MenuItem>
                    <MenuItem value="published">Published</MenuItem>
                    <MenuItem value="archived">Archived</MenuItem>
                  </Select>
                </FormControl>
              </Paper>
            </Grid>

            <Grid size={{ xs: 12, md: 4 }}>
              <Paper sx={{ p: 3 }}>
                <Typography variant="h6" gutterBottom>
                  Test Image
                </Typography>
                <Box
                  sx={{
                    width: '100%',
                    height: 200,
                    backgroundColor: test.imageUrl ? 'transparent' : theme.palette.background.default,
                    borderRadius: 1,
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: 'center',
                    justifyContent: 'center',
                    border: '2px dashed #E0E0E0',
                    cursor: 'pointer',
                    '&:hover': {
                      borderColor: theme.palette.primary.main,
                      backgroundColor: alpha(theme.palette.primary.main, 0.05),
                    },
                    overflow: 'hidden',
                  }}
                  component="label"
                >
                  {test.imageUrl ? (
                    <img 
                      src={test.imageUrl} 
                      alt="Test thumbnail" 
                      style={{ width: '100%', height: '100%', objectFit: 'cover' }} 
                    />
                  ) : (
                    <>
                      <ImageIcon sx={{ fontSize: 48, color: theme.palette.text.secondary, mb: 1 }} />
                      <Typography variant="body2" color="text.secondary">
                        Click to upload image
                      </Typography>
                      <Typography variant="caption" color="text.secondary">
                        Recommended: 800x400px
                      </Typography>
                    </>
                  )}
                  <input
                    type="file"
                    accept="image/*"
                    hidden
                    onChange={(e) => {
                      const file = e.target.files?.[0];
                      if (file) handleImageUpload(file);
                    }}
                  />
                </Box>
                {test.imageUrl && (
                  <Button
                    variant="outlined"
                    size="small"
                    fullWidth
                    sx={{ mt: 2 }}
                    onClick={() => handleChange('imageUrl', undefined)}
                  >
                    Remove Image
                  </Button>
                )}
              </Paper>
            </Grid>
          </Grid>
        </TabPanel>

        {/* Questions Tab */}
        <TabPanel value={activeTab} index={1}>
          <QuestionList 
            questions={questions} 
            onChange={setQuestions}
            onSaveQuestion={handleSaveQuestion}
            testId={test.id}
          />
        </TabPanel>

        {/* Settings Tab */}
        <TabPanel value={activeTab} index={2}>
          <Grid container spacing={4}>
            <Grid size={{ xs: 12, md: 6 }}>
              <Paper sx={{ p: 3 }}>
                <Typography variant="h6" gutterBottom>
                  Test Settings
                </Typography>

                <Box sx={{ mb: 3 }}>
                  <Typography variant="subtitle2" gutterBottom>
                    Time Limit (minutes)
                  </Typography>
                  <Box display="flex" alignItems="center" gap={2}>
                    <Slider
                      value={test.timeLimit}
                      onChange={(_, v) => handleChange('timeLimit', v)}
                      min={5}
                      max={120}
                      step={5}
                      sx={{ flex: 1 }}
                    />
                    <TextField
                      value={test.timeLimit}
                      onChange={(e) => handleChange('timeLimit', parseInt(e.target.value) || 0)}
                      size="small"
                      sx={{ width: 80 }}
                      InputProps={{ endAdornment: <InputAdornment position="end">m</InputAdornment> }}
                    />
                  </Box>
                </Box>

                <Box sx={{ mb: 3 }}>
                  <Typography variant="subtitle2" gutterBottom>
                    Passing Score (%)
                  </Typography>
                  <Box display="flex" alignItems="center" gap={2}>
                    <Slider
                      value={test.passingScore}
                      onChange={(_, v) => handleChange('passingScore', v)}
                      min={0}
                      max={100}
                      step={5}
                      sx={{ flex: 1 }}
                    />
                    <TextField
                      value={test.passingScore}
                      onChange={(e) => handleChange('passingScore', parseInt(e.target.value) || 0)}
                      size="small"
                      sx={{ width: 80 }}
                      InputProps={{ endAdornment: <InputAdornment position="end">%</InputAdornment> }}
                    />
                  </Box>
                </Box>

                <Box sx={{ mb: 3 }}>
                  <Typography variant="subtitle2" gutterBottom>
                    Maximum Attempts (0 = unlimited)
                  </Typography>
                  <TextField
                    type="number"
                    value={test.maxAttempts}
                    onChange={(e) => handleChange('maxAttempts', parseInt(e.target.value) || 0)}
                    size="small"
                    fullWidth
                  />
                </Box>
              </Paper>
            </Grid>

            <Grid size={{ xs: 12, md: 6 }}>
              <Paper sx={{ p: 3 }}>
                <Typography variant="h6" gutterBottom>
                  Display Options
                </Typography>

                <FormControlLabel
                  control={
                    <Switch
                      checked={test.shuffleQuestions}
                      onChange={(e) => handleChange('shuffleQuestions', e.target.checked)}
                    />
                  }
                  label="Shuffle questions order"
                  sx={{ display: 'block', mb: 2 }}
                />

                <FormControlLabel
                  control={
                    <Switch
                      checked={test.showResults}
                      onChange={(e) => handleChange('showResults', e.target.checked)}
                    />
                  }
                  label="Show results after completion"
                  sx={{ display: 'block', mb: 2 }}
                />

                <FormControlLabel
                  control={
                    <Switch
                      checked={test.allowRetake}
                      onChange={(e) => handleChange('allowRetake', e.target.checked)}
                    />
                  }
                  label="Allow retaking test"
                  sx={{ display: 'block' }}
                />
              </Paper>
            </Grid>
          </Grid>
        </TabPanel>

        {/* Analytics Tab */}
        <TabPanel value={activeTab} index={3}>
          <Paper sx={{ p: 4, textAlign: 'center' }}>
            <AssessmentIcon sx={{ fontSize: 64, color: theme.palette.text.secondary, mb: 2 }} />
            <Typography variant="h6" gutterBottom>
              Analytics Coming Soon
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Test performance analytics will be available once the test has been published and attempted.
            </Typography>
          </Paper>
        </TabPanel>
      </Box>

      {/* Unsaved Changes Dialog */}
      <Dialog open={saveDialogOpen} onClose={() => setSaveDialogOpen(false)}>
        <DialogTitle>Unsaved Changes</DialogTitle>
        <DialogContent>
          <Typography>
            You have unsaved changes. Do you want to save before leaving?
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => navigate('/content/tests')}>Discard</Button>
          <Button onClick={() => setSaveDialogOpen(false)}>Cancel</Button>
          <Button onClick={handleSave} variant="contained">
            Save & Leave
          </Button>
        </DialogActions>
      </Dialog>

      {/* Snackbar for feedback */}
      <Snackbar
        open={snackbar.open}
        autoHideDuration={6000}
        onClose={() => setSnackbar({ ...snackbar, open: false })}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      >
        <Alert 
          severity={snackbar.severity} 
          onClose={() => setSnackbar({ ...snackbar, open: false })}
        >
          {snackbar.message}
        </Alert>
      </Snackbar>
    </Box>
  );
};

export default TestEditor;
