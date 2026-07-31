import React, { useState, useEffect } from 'react';
import {
  Box,
  Paper,
  Typography,
  TextField,
  Slider,
  FormControl,
  FormControlLabel,
  InputLabel,
  Select,
  MenuItem,
  Switch,
  Chip,
  Tooltip,
  IconButton,
  Alert,
  Grid,
} from '@mui/material';
import {
  Timer,
  TrendingUp,
  Replay,
  Shuffle,
  Visibility,
  Lock,
  Info,
  School,
} from '@mui/icons-material';

// Design System Colors
const colors = {
  primary: '#4A90D9',
  success: '#43A047',
  error: '#E53935',
  warning: '#FB8C00',
  info: '#2196F3',
  background: '#F5F5F5',
  card: '#FFFFFF',
  textPrimary: '#212121',
  textSecondary: '#757575',
  sidebar: '#1a237e',
};

// Types
export type TestStatus = 'draft' | 'published' | 'archived';
export type DifficultyLevel = 'beginner' | 'intermediate' | 'advanced';

export interface TestSettings {
  title: string;
  description: string;
  categoryId: string;
  status: TestStatus;
  difficulty: DifficultyLevel;
  timeLimit: number; // in minutes, 0 = no limit
  passingScore: number; // percentage
  attemptsLimit: number; // 0 = unlimited
  shuffleQuestions: boolean;
  shuffleOptions: boolean;
  showCorrectAnswers: boolean;
  showExplanation: boolean;
  showScore: boolean;
  allowRetake: boolean;
  isPublic: boolean;
  requireLogin: boolean;
  certificateEnabled: boolean;
  certificateThreshold: number;
}

interface TestSettingsProps {
  settings: Partial<TestSettings>;
  categories: { id: string; name: string }[];
  totalPoints: number;
  questionCount: number;
  onChange: (settings: TestSettings) => void;
}

const defaultSettings: TestSettings = {
  title: '',
  description: '',
  categoryId: '',
  status: 'draft',
  difficulty: 'intermediate',
  timeLimit: 0,
  passingScore: 70,
  attemptsLimit: 0,
  shuffleQuestions: false,
  shuffleOptions: false,
  showCorrectAnswers: true,
  showExplanation: true,
  showScore: true,
  allowRetake: true,
  isPublic: true,
  requireLogin: false,
  certificateEnabled: false,
  certificateThreshold: 80,
};

const difficultyConfig: Record<DifficultyLevel, { label: string; color: string }> = {
  beginner: { label: 'Beginner', color: colors.success },
  intermediate: { label: 'Intermediate', color: colors.warning },
  advanced: { label: 'Advanced', color: colors.error },
};

const TestSettings: React.FC<TestSettingsProps> = ({
  settings: initialSettings,
  categories,
  totalPoints,
  questionCount,
  onChange,
}) => {
  const [settings, setSettings] = useState<TestSettings>({
    ...defaultSettings,
    ...initialSettings,
  });

  // Notify parent of changes
  useEffect(() => {
    onChange(settings);
  }, [settings, onChange]);

  const handleChange = <K extends keyof TestSettings>(
    key: K,
    value: TestSettings[K]
  ) => {
    setSettings((prev) => ({ ...prev, [key]: value }));
  };

  const formatTime = (minutes: number): string => {
    if (minutes === 0) return 'No limit';
    if (minutes < 60) return `${minutes} minutes`;
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    return mins > 0 ? `${hours}h ${mins}m` : `${hours} hours`;
  };

  return (
    <Box sx={{ width: '100%' }}>
      {/* Header */}
      <Typography variant="h6" sx={{ color: colors.textPrimary, fontWeight: 600, mb: 3 }}>
        Test Settings
      </Typography>

      {/* Basic Information */}
      <Paper sx={{ p: 3, mb: 3, borderRadius: '12px' }}>
        <Typography variant="subtitle1" sx={{ color: colors.textPrimary, fontWeight: 600, mb: 2 }}>
          Basic Information
        </Typography>

        <Grid container spacing={3}>
          {/* Title */}
          <Grid item xs={12}>
            <TextField
              fullWidth
              label="Test Title"
              value={settings.title}
              onChange={(e) => handleChange('title', e.target.value)}
              placeholder="Enter test title..."
              required
            />
          </Grid>

          {/* Description */}
          <Grid item xs={12}>
            <TextField
              fullWidth
              multiline
              rows={3}
              label="Description"
              value={settings.description}
              onChange={(e) => handleChange('description', e.target.value)}
              placeholder="Describe what this test covers..."
            />
          </Grid>

          {/* Category */}
          <Grid item xs={12} sm={6}>
            <FormControl fullWidth>
              <InputLabel>Category</InputLabel>
              <Select
                value={settings.categoryId || ''}
                label="Category"
                onChange={(e) => {
                  const selectedValue = e.target.value;
                  // MUI Select sometimes returns index instead of value (as string like "3")
                  const index = parseInt(selectedValue as string, 10);
                  const selectedId = !isNaN(index) && index >= 0 && index < categories.length
                    ? categories[index]?.id 
                    : selectedValue;
                  handleChange('categoryId', String(selectedId || ''));
                }}
              >
                <MenuItem value="" disabled>
                  <em>Select a category...</em>
                </MenuItem>
                {categories.map((cat) => (
                  <MenuItem key={cat.id} value={cat.id}>
                    {cat.name}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          </Grid>

          {/* Difficulty */}
          <Grid item xs={12} sm={6}>
            <FormControl fullWidth>
              <InputLabel>Difficulty Level</InputLabel>
              <Select
                value={settings.difficulty}
                label="Difficulty Level"
                onChange={(e) => handleChange('difficulty', e.target.value as DifficultyLevel)}
              >
                {Object.entries(difficultyConfig).map(([key, config]) => (
                  <MenuItem key={key} value={key}>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      <Box
                        sx={{
                          width: 12,
                          height: 12,
                          borderRadius: '50%',
                          backgroundColor: config.color,
                        }}
                      />
                      {config.label}
                    </Box>
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          </Grid>

          {/* Status */}
          <Grid item xs={12} sm={6}>
            <FormControl fullWidth>
              <InputLabel>Status</InputLabel>
              <Select
                value={settings.status}
                label="Status"
                onChange={(e) => handleChange('status', e.target.value as TestStatus)}
              >
                <MenuItem value="draft">
                  <Chip
                    size="small"
                    label="Draft"
                    sx={{ backgroundColor: '#E0E0E0', color: colors.textSecondary }}
                  />
                </MenuItem>
                <MenuItem value="published">
                  <Chip
                    size="small"
                    label="Published"
                    sx={{ backgroundColor: '#E8F5E9', color: colors.success }}
                  />
                </MenuItem>
                <MenuItem value="archived">
                  <Chip
                    size="small"
                    label="Archived"
                    sx={{ backgroundColor: '#FFEBEE', color: colors.error }}
                  />
                </MenuItem>
              </Select>
            </FormControl>
          </Grid>
        </Grid>
      </Paper>

      {/* Test Configuration */}
      <Paper sx={{ p: 3, mb: 3, borderRadius: '12px' }}>
        <Typography variant="subtitle1" sx={{ color: colors.textPrimary, fontWeight: 600, mb: 2 }}>
          Test Configuration
        </Typography>

        <Grid container spacing={4}>
          {/* Time Limit */}
          <Grid item xs={12} md={6}>
            <Box sx={{ mb: 2 }}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                <Timer sx={{ color: colors.primary }} />
                <Typography variant="body1" sx={{ color: colors.textPrimary, fontWeight: 500 }}>
                  Time Limit
                </Typography>
                <Tooltip title="Set to 0 for no time limit">
                  <IconButton size="small">
                    <Info fontSize="small" sx={{ color: colors.textSecondary }} />
                  </IconButton>
                </Tooltip>
              </Box>
              <Typography variant="body2" sx={{ color: colors.textSecondary, mb: 2 }}>
                {formatTime(settings.timeLimit)}
              </Typography>
              <Slider
                value={settings.timeLimit}
                onChange={(_, value) => handleChange('timeLimit', value as number)}
                min={0}
                max={180}
                step={5}
                marks={[
                  { value: 0, label: 'None' },
                  { value: 30, label: '30m' },
                  { value: 60, label: '1h' },
                  { value: 120, label: '2h' },
                  { value: 180, label: '3h' },
                ]}
                valueLabelDisplay="auto"
                valueLabelFormat={(value) => formatTime(value)}
              />
            </Box>
          </Grid>

          {/* Passing Score */}
          <Grid item xs={12} md={6}>
            <Box sx={{ mb: 2 }}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                <TrendingUp sx={{ color: colors.success }} />
                <Typography variant="body1" sx={{ color: colors.textPrimary, fontWeight: 500 }}>
                  Passing Score
                </Typography>
                <Tooltip title="Percentage required to pass the test">
                  <IconButton size="small">
                    <Info fontSize="small" sx={{ color: colors.textSecondary }} />
                  </IconButton>
                </Tooltip>
              </Box>
              <Typography variant="body2" sx={{ color: colors.textSecondary, mb: 2 }}>
                {settings.passingScore}% required to pass
              </Typography>
              <Slider
                value={settings.passingScore}
                onChange={(_, value) => handleChange('passingScore', value as number)}
                min={0}
                max={100}
                step={5}
                marks={[
                  { value: 0, label: '0%' },
                  { value: 50, label: '50%' },
                  { value: 70, label: '70%' },
                  { value: 100, label: '100%' },
                ]}
                valueLabelDisplay="auto"
                valueLabelFormat={(value) => `${value}%`}
              />
            </Box>
          </Grid>

          {/* Attempts Limit */}
          <Grid item xs={12} md={6}>
            <Box>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                <Replay sx={{ color: colors.info }} />
                <Typography variant="body1" sx={{ color: colors.textPrimary, fontWeight: 500 }}>
                  Attempts Limit
                </Typography>
                <Tooltip title="Set to 0 for unlimited attempts">
                  <IconButton size="small">
                    <Info fontSize="small" sx={{ color: colors.textSecondary }} />
                  </IconButton>
                </Tooltip>
              </Box>
              <Typography variant="body2" sx={{ color: colors.textSecondary, mb: 2 }}>
                {settings.attemptsLimit === 0
                  ? 'Unlimited attempts'
                  : `${settings.attemptsLimit} attempt${settings.attemptsLimit > 1 ? 's' : ''}`}
              </Typography>
              <Slider
                value={settings.attemptsLimit}
                onChange={(_, value) => handleChange('attemptsLimit', value as number)}
                min={0}
                max={10}
                step={1}
                marks={[
                  { value: 0, label: '∞' },
                  { value: 1, label: '1' },
                  { value: 3, label: '3' },
                  { value: 5, label: '5' },
                  { value: 10, label: '10' },
                ]}
                valueLabelDisplay="auto"
                valueLabelFormat={(value) => (value === 0 ? '∞' : value.toString())}
              />
            </Box>
          </Grid>
        </Grid>
      </Paper>

      {/* Question Options */}
      <Paper sx={{ p: 3, mb: 3, borderRadius: '12px' }}>
        <Typography variant="subtitle1" sx={{ color: colors.textPrimary, fontWeight: 600, mb: 2 }}>
          Question Options
        </Typography>

        <Grid container spacing={2}>
          <Grid item xs={12} sm={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={settings.shuffleQuestions}
                  onChange={(e) => handleChange('shuffleQuestions', e.target.checked)}
                />
              }
              label={
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  <Shuffle fontSize="small" sx={{ color: colors.primary }} />
                  <Typography variant="body2">Shuffle Questions</Typography>
                </Box>
              }
            />
          </Grid>

          <Grid item xs={12} sm={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={settings.shuffleOptions}
                  onChange={(e) => handleChange('shuffleOptions', e.target.checked)}
                />
              }
              label={
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  <Shuffle fontSize="small" sx={{ color: colors.info }} />
                  <Typography variant="body2">Shuffle Answer Options</Typography>
                </Box>
              }
            />
          </Grid>

          <Grid item xs={12} sm={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={settings.showCorrectAnswers}
                  onChange={(e) => handleChange('showCorrectAnswers', e.target.checked)}
                />
              }
              label={
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  <Visibility fontSize="small" sx={{ color: colors.success }} />
                  <Typography variant="body2">Show Correct Answers</Typography>
                </Box>
              }
            />
          </Grid>

          <Grid item xs={12} sm={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={settings.showExplanation}
                  onChange={(e) => handleChange('showExplanation', e.target.checked)}
                />
              }
              label={
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  <School fontSize="small" sx={{ color: colors.warning }} />
                  <Typography variant="body2">Show Explanations</Typography>
                </Box>
              }
            />
          </Grid>

          <Grid item xs={12} sm={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={settings.showScore}
                  onChange={(e) => handleChange('showScore', e.target.checked)}
                />
              }
              label={
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  <TrendingUp fontSize="small" sx={{ color: colors.primary }} />
                  <Typography variant="body2">Show Score</Typography>
                </Box>
              }
            />
          </Grid>

          <Grid item xs={12} sm={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={settings.allowRetake}
                  onChange={(e) => handleChange('allowRetake', e.target.checked)}
                />
              }
              label={
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  <Replay fontSize="small" sx={{ color: colors.info }} />
                  <Typography variant="body2">Allow Retake</Typography>
                </Box>
              }
            />
          </Grid>
        </Grid>
      </Paper>

      {/* Access Control */}
      <Paper sx={{ p: 3, mb: 3, borderRadius: '12px' }}>
        <Typography variant="subtitle1" sx={{ color: colors.textPrimary, fontWeight: 600, mb: 2 }}>
          Access Control
        </Typography>

        <Grid container spacing={2}>
          <Grid item xs={12} sm={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={settings.isPublic}
                  onChange={(e) => handleChange('isPublic', e.target.checked)}
                />
              }
              label={
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  <Visibility fontSize="small" sx={{ color: colors.success }} />
                  <Typography variant="body2">Public Test</Typography>
                </Box>
              }
            />
          </Grid>

          <Grid item xs={12} sm={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={settings.requireLogin}
                  onChange={(e) => handleChange('requireLogin', e.target.checked)}
                />
              }
              label={
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  <Lock fontSize="small" sx={{ color: colors.error }} />
                  <Typography variant="body2">Require Login</Typography>
                </Box>
              }
            />
          </Grid>
        </Grid>

        {!settings.isPublic && (
          <Alert severity="info" sx={{ mt: 2 }}>
            This test will be private and only accessible via direct link.
          </Alert>
        )}
      </Paper>

      {/* Certificate Settings */}
      <Paper sx={{ p: 3, mb: 3, borderRadius: '12px' }}>
        <Typography variant="subtitle1" sx={{ color: colors.textPrimary, fontWeight: 600, mb: 2 }}>
          Certificate Settings
        </Typography>

        <FormControlLabel
          control={
            <Switch
              checked={settings.certificateEnabled}
              onChange={(e) => handleChange('certificateEnabled', e.target.checked)}
            />
          }
          label={
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <School sx={{ color: colors.primary }} />
              <Typography variant="body2">Enable Certificate</Typography>
            </Box>
          }
        />

        {settings.certificateEnabled && (
          <Box sx={{ mt: 3 }}>
            <Typography variant="body2" sx={{ color: colors.textSecondary, mb: 2 }}>
              Certificate Threshold: {settings.certificateThreshold}%
            </Typography>
            <Slider
              value={settings.certificateThreshold}
              onChange={(_, value) => handleChange('certificateThreshold', value as number)}
              min={50}
              max={100}
              step={5}
              marks={[
                { value: 50, label: '50%' },
                { value: 70, label: '70%' },
                { value: 80, label: '80%' },
                { value: 90, label: '90%' },
                { value: 100, label: '100%' },
              ]}
              valueLabelDisplay="auto"
              valueLabelFormat={(value) => `${value}%`}
            />
            <Alert severity="success" sx={{ mt: 2 }}>
              Users who score {settings.certificateThreshold}% or higher will receive a certificate.
            </Alert>
          </Box>
        )}
      </Paper>

      {/* Test Summary */}
      <Paper
        sx={{
          p: 3,
          borderRadius: '12px',
          backgroundColor: colors.background,
        }}
      >
        <Typography variant="subtitle1" sx={{ color: colors.textPrimary, fontWeight: 600, mb: 2 }}>
          Test Summary
        </Typography>

        <Grid container spacing={2}>
          <Grid item xs={6} sm={3}>
            <Box sx={{ textAlign: 'center' }}>
              <Typography variant="h4" sx={{ color: colors.primary, fontWeight: 600 }}>
                {questionCount}
              </Typography>
              <Typography variant="body2" sx={{ color: colors.textSecondary }}>
                Questions
              </Typography>
            </Box>
          </Grid>

          <Grid item xs={6} sm={3}>
            <Box sx={{ textAlign: 'center' }}>
              <Typography variant="h4" sx={{ color: colors.success, fontWeight: 600 }}>
                {totalPoints}
              </Typography>
              <Typography variant="body2" sx={{ color: colors.textSecondary }}>
                Total Points
              </Typography>
            </Box>
          </Grid>

          <Grid item xs={6} sm={3}>
            <Box sx={{ textAlign: 'center' }}>
              <Typography variant="h4" sx={{ color: colors.info, fontWeight: 600 }}>
                {settings.passingScore}%
              </Typography>
              <Typography variant="body2" sx={{ color: colors.textSecondary }}>
                Pass Score
              </Typography>
            </Box>
          </Grid>

          <Grid item xs={6} sm={3}>
            <Box sx={{ textAlign: 'center' }}>
              <Typography variant="h4" sx={{ color: colors.warning, fontWeight: 600 }}>
                {settings.timeLimit === 0 ? '∞' : formatTime(settings.timeLimit)}
              </Typography>
              <Typography variant="body2" sx={{ color: colors.textSecondary }}>
                Time Limit
              </Typography>
            </Box>
          </Grid>
        </Grid>
      </Paper>
    </Box>
  );
};

export default TestSettings;
