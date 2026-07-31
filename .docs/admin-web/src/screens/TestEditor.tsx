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
  Skeleton,
  Divider,
  Tooltip,
  InputAdornment,
  Autocomplete,
} from '@mui/material';
import {
  Save as SaveIcon,
  Preview as PreviewIcon,
  ArrowBack as ArrowBackIcon,
  Add as AddIcon,
  Delete as DeleteIcon,
  DragIndicator as DragIndicatorIcon,
  Image as ImageIcon,
  Timer as TimerIcon,
  CheckCircle as CheckCircleIcon,
  TrendingUp as TrendingUpIcon,
  Settings as SettingsIcon,
  Assessment as AssessmentIcon,
  Info as InfoIcon,
} from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useParams, useNavigate } from 'react-router-dom';

// Design System Colors
const COLORS = {
  primary: '#4A90D9',
  success: '#43A047',
  error: '#E53935',
  warning: '#FB8C00',
  info: '#2196F3',
  background: '#F5F5F5',
  card: '#FFFFFF',
  textPrimary: '#212121',
  textSecondary: '#757575',
};

// Types
interface Test {
  id: string;
  title: string;
  description: string;
  categoryId: string;
  difficulty: 'easy' | 'medium' | 'hard';
  status: 'draft' | 'published' | 'archived';
  imageUrl?: string;
  timeLimit: number;
  passingScore: number;
  maxAttempts: number;
  shuffleQuestions: boolean;
  showResults: boolean;
  allowRetake: boolean;
}

interface Question {
  id: string;
  type: 'multiple_choice' | 'true_false' | 'fill_blank' | 'matching';
  text: string;
  options: string[];
  correctAnswer: string | string[];
  points: number;
  explanation?: string;
}

interface Category {
  id: string;
  name: string;
  path: string;
}

// Mock API
const fetchTest = async (id: string): Promise<Test> => {
  await new Promise((resolve) => setTimeout(resolve, 500));
  
  if (id === 'new') {
    return {
      id: 'new',
      title: '',
      description: '',
      categoryId: '',
      difficulty: 'medium',
      status: 'draft',
      timeLimit: 30,
      passingScore: 70,
      maxAttempts: 0,
      shuffleQuestions: false,
      showResults: true,
      allowRetake: true,
    };
  }
  
  return {
    id,
    title: 'Sample Test',
    description: 'This is a sample test description.',
    categoryId: '1',
    difficulty: 'medium',
    status: 'draft',
    timeLimit: 30,
    passingScore: 70,
    maxAttempts: 3,
    shuffleQuestions: true,
    showResults: true,
    allowRetake: true,
  };
};

const fetchCategories = async (): Promise<Category[]> => {
  await new Promise((resolve) => setTimeout(resolve, 300));
  return [
    { id: '1', name: 'Grammar', path: 'Grammar' },
    { id: '1-1', name: 'Tenses', path: 'Grammar > Tenses' },
    { id: '1-1-1', name: 'Present Simple', path: 'Grammar > Tenses > Present Simple' },
    { id: '1-1-2', name: 'Past Simple', path: 'Grammar > Tenses > Past Simple' },
    { id: '2', name: 'Vocabulary', path: 'Vocabulary' },
    { id: '2-1', name: 'Business', path: 'Vocabulary > Business' },
    { id: '3', name: 'Listening', path: 'Listening' },
    { id: '4', name: 'Reading', path: 'Reading' },
    { id: '5', name: 'Writing', path: 'Writing' },
  ];
};

const saveTest = async (test: Test): Promise<Test> => {
  await new Promise((resolve) => setTimeout(resolve, 800));
  return { ...test, id: test.id === 'new' ? Math.random().toString(36).substr(2, 9) : test.id };
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
}

const QuestionBuilder: React.FC<QuestionBuilderProps> = ({ question, onChange, onDelete }) => {
  return (
    <Paper sx={{ p: 3, mb: 2, border: '1px solid #E0E0E0' }}>
      <Box display="flex" justifyContent="space-between" alignItems="flex-start" mb={2}>
        <Box display="flex" alignItems="center" gap={1}>
          <DragIndicatorIcon sx={{ color: COLORS.textSecondary, cursor: 'grab' }} />
          <Chip
            label={question.type.replace('_', ' ').toUpperCase()}
            size="small"
            sx={{ backgroundColor: `${COLORS.primary}20`, color: COLORS.primary }}
          />
        </Box>
        <IconButton onClick={onDelete} sx={{ color: COLORS.error }}>
          <DeleteIcon />
        </IconButton>
      </Box>

      <TextField
        label="Question Text"
        fullWidth
        multiline
        rows={2}
        value={question.text}
        onChange={(e) => onChange({ ...question, text: e.target.value })}
        sx={{ mb: 2 }}
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
                  onChange({ ...question, options: newOptions });
                }}
              />
              <FormControlLabel
                control={
                  <Switch
                    checked={question.correctAnswer === option}
                    onChange={() => onChange({ ...question, correctAnswer: option })}
                  />
                }
                label="Correct"
              />
            </Box>
          ))}
        </Box>
      )}

      <TextField
        label="Explanation (optional)"
        fullWidth
        multiline
        rows={2}
        value={question.explanation || ''}
        onChange={(e) => onChange({ ...question, explanation: e.target.value })}
        sx={{ mt: 2 }}
      />
    </Paper>
  );
};

// Question List Component
interface QuestionListProps {
  questions: Question[];
  onChange: (questions: Question[]) => void;
}

const QuestionList: React.FC<QuestionListProps> = ({ questions, onChange }) => {
  const handleAddQuestion = (type: Question['type']) => {
    const newQuestion: Question = {
      id: Math.random().toString(36).substr(2, 9),
      type,
      text: '',
      options: type === 'multiple_choice' ? ['', '', '', ''] : [],
      correctAnswer: '',
      points: 1,
    };
    onChange([...questions, newQuestion]);
  };

  const handleUpdateQuestion = (index: number, question: Question) => {
    const newQuestions = [...questions];
    newQuestions[index] = question;
    onChange(newQuestions);
  };

  const handleDeleteQuestion = (index: number) => {
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
        >
          Multiple Choice
        </Button>
        <Button
          variant="outlined"
          startIcon={<AddIcon />}
          onClick={() => handleAddQuestion('true_false')}
          size="small"
        >
          True/False
        </Button>
        <Button
          variant="outlined"
          startIcon={<AddIcon />}
          onClick={() => handleAddQuestion('fill_blank')}
          size="small"
        >
          Fill in Blank
        </Button>
        <Button
          variant="outlined"
          startIcon={<AddIcon />}
          onClick={() => handleAddQuestion('matching')}
          size="small"
        >
          Matching
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
  
  const [activeTab, setActiveTab] = useState(0);
  const [test, setTest] = useState<Test | null>(null);
  const [questions, setQuestions] = useState<Question[]>([]);
  const [hasChanges, setHasChanges] = useState(false);
  const [saveDialogOpen, setSaveDialogOpen] = useState(false);

  const { data: testData, isLoading: testLoading } = useQuery({
    queryKey: ['test', id],
    queryFn: () => fetchTest(id || 'new'),
    enabled: !!id,
  });

  const { data: categories = [] } = useQuery({
    queryKey: ['categories'],
    queryFn: fetchCategories,
  });

  const saveMutation = useMutation({
    mutationFn: saveTest,
    onSuccess: (savedTest) => {
      queryClient.invalidateQueries({ queryKey: ['tests'] });
      queryClient.invalidateQueries({ queryKey: ['test', savedTest.id] });
      setHasChanges(false);
      if (id === 'new') {
        navigate(`/tests/${savedTest.id}/edit`);
      }
    },
  });

  useEffect(() => {
    if (testData) {
      setTest(testData);
    }
  }, [testData]);

  const handleChange = (field: keyof Test, value: any) => {
    if (test) {
      setTest({ ...test, [field]: value });
      setHasChanges(true);
    }
  };

  const handleSave = () => {
    if (test) {
      saveMutation.mutate(test);
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
      navigate('/tests');
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
            <IconButton onClick={handleBack}>
              <ArrowBackIcon />
            </IconButton>
            <Box>
              <Typography variant="h5" fontWeight="bold">
                {id === 'new' ? 'Create Test' : test.title || 'Untitled Test'}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                {hasChanges ? 'Unsaved changes' : 'All changes saved'}
              </Typography>
            </Box>
          </Box>
          <Box display="flex" gap={1}>
            <Button
              variant="outlined"
              startIcon={<PreviewIcon />}
              onClick={handlePreview}
              disabled={id === 'new'}
            >
              Preview
            </Button>
            <Button
              variant="contained"
              startIcon={<SaveIcon />}
              onClick={handleSave}
              disabled={!hasChanges || saveMutation.isPending}
              sx={{ backgroundColor: COLORS.primary }}
            >
              {saveMutation.isPending ? 'Saving...' : 'Save'}
            </Button>
          </Box>
        </Box>
      </Paper>

      {/* Tabs */}
      <Box sx={{ borderBottom: 1, borderColor: 'divider', px: 3 }}>
        <Tabs value={activeTab} onChange={(_, v) => setActiveTab(v)}>
          <Tab icon={<InfoIcon fontSize="small" />} iconPosition="start" label="General" />
          <Tab icon={<CheckCircleIcon fontSize="small" />} iconPosition="start" label={`Questions (${questions.length})`} />
          <Tab icon={<SettingsIcon fontSize="small" />} iconPosition="start" label="Settings" />
          <Tab icon={<AssessmentIcon fontSize="small" />} iconPosition="start" label="Analytics" />
        </Tabs>
      </Box>

      {/* Content */}
      <Box px={3} py={2}>
        {/* General Tab */}
        <TabPanel value={activeTab} index={0}>
          <Grid container spacing={3}>
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
                  error={!test.title.trim()}
                  helperText={!test.title.trim() ? 'Title is required' : ''}
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
                />

                <FormControl fullWidth sx={{ mb: 3 }}>
                  <InputLabel>Category</InputLabel>
                  <Select
                    value={test.categoryId}
                    label="Category"
                    onChange={(e) => handleChange('categoryId', e.target.value)}
                  >
                    {categories.map((cat) => (
                      <MenuItem key={cat.id} value={cat.id}>
                        {cat.path}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>

                <FormControl fullWidth sx={{ mb: 3 }}>
                  <InputLabel>Difficulty</InputLabel>
                  <Select
                    value={test.difficulty}
                    label="Difficulty"
                    onChange={(e) => handleChange('difficulty', e.target.value)}
                  >
                    <MenuItem value="easy">Easy</MenuItem>
                    <MenuItem value="medium">Medium</MenuItem>
                    <MenuItem value="hard">Hard</MenuItem>
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
                    backgroundColor: '#F5F5F5',
                    borderRadius: 1,
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: 'center',
                    justifyContent: 'center',
                    border: '2px dashed #E0E0E0',
                    cursor: 'pointer',
                    '&:hover': {
                      borderColor: COLORS.primary,
                      backgroundColor: 'rgba(74, 144, 217, 0.05)',
                    },
                  }}
                >
                  <ImageIcon sx={{ fontSize: 48, color: COLORS.textSecondary, mb: 1 }} />
                  <Typography variant="body2" color="text.secondary">
                    Click to upload image
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    Recommended: 800x400px
                  </Typography>
                </Box>
              </Paper>
            </Grid>
          </Grid>
        </TabPanel>

        {/* Questions Tab */}
        <TabPanel value={activeTab} index={1}>
          <QuestionList questions={questions} onChange={setQuestions} />
        </TabPanel>

        {/* Settings Tab */}
        <TabPanel value={activeTab} index={2}>
          <Grid container spacing={3}>
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
            <AssessmentIcon sx={{ fontSize: 64, color: COLORS.textSecondary, mb: 2 }} />
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
          <Button onClick={() => navigate('/tests')}>Discard</Button>
          <Button onClick={() => setSaveDialogOpen(false)}>Cancel</Button>
          <Button onClick={handleSave} variant="contained" sx={{ backgroundColor: COLORS.primary }}>
            Save & Leave
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default TestEditor;
