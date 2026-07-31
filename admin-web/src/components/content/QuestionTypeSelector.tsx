import React from 'react';
import { Box, Paper, Typography, Grid } from '@mui/material';
import {
  TextFields,
  Image,
  Audiotrack,
  DragHandle,
  Input,
  TouchApp,
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

// Question Types
export type QuestionType =
  | 'TEXT_SELECT'
  | 'IMAGE_SELECT'
  | 'AUDIO_SELECT'
  | 'DRAG_DROP'
  | 'TEXT_INPUT'
  | 'IMAGE_WORD_MATCH';

interface QuestionTypeConfig {
  type: QuestionType;
  label: string;
  description: string;
  icon: React.ElementType;
  color: string;
  bgColor: string;
  features: string[];
}

interface QuestionTypeSelectorProps {
  selectedType: QuestionType | null;
  onSelect: (type: QuestionType) => void;
  disabledTypes?: QuestionType[];
}

// Question type configurations
const questionTypes: QuestionTypeConfig[] = [
  {
    type: 'TEXT_SELECT',
    label: 'Text Choice',
    description: 'Multiple choice question with text options',
    icon: TextFields,
    color: colors.primary,
    bgColor: '#E3F2FD',
    features: ['Single or multiple correct answers', 'Up to 10 options', 'Randomize options'],
  },
  {
    type: 'IMAGE_SELECT',
    label: 'Image Choice',
    description: 'Multiple choice question with image options',
    icon: Image,
    color: colors.success,
    bgColor: '#E8F5E9',
    features: ['Upload images', 'Image + text labels', 'Visual learning'],
  },
  {
    type: 'AUDIO_SELECT',
    label: 'Audio Choice',
    description: 'Listen to audio and select the correct answer',
    icon: Audiotrack,
    color: colors.warning,
    bgColor: '#FFF3E0',
    features: ['Upload audio files', 'Listening comprehension', 'Replay option'],
  },
  {
    type: 'DRAG_DROP',
    label: 'Drag & Drop',
    description: 'Drag items to their correct positions',
    icon: DragHandle,
    color: colors.info,
    bgColor: '#E1F5FE',
    features: ['Interactive matching', 'Visual feedback', 'Multiple drop zones'],
  },
  {
    type: 'TEXT_INPUT',
    label: 'Text Input',
    description: 'Free text answer question',
    icon: Input,
    color: colors.textSecondary,
    bgColor: '#F5F5F5',
    features: ['Case sensitive option', 'Multiple correct answers', 'Auto-grading'],
  },
  {
    type: 'IMAGE_WORD_MATCH',
    label: 'Image Word Match',
    description: 'Drag words to hotspots on an image',
    icon: TouchApp,
    color: '#9C27B0',
    bgColor: '#F3E5F5',
    features: ['Interactive image', 'Hotspot drawing', 'Visual learning'],
  },
];

const QuestionTypeSelector: React.FC<QuestionTypeSelectorProps> = ({
  selectedType,
  onSelect,
  disabledTypes = [],
}) => {
  return (
    <Box sx={{ width: '100%' }}>
      <Typography
        variant="body1"
        sx={{ color: colors.textSecondary, mb: 3 }}
      >
        Select a question type to get started. Each type offers different ways to engage learners.
      </Typography>

      <Grid container spacing={3}>
        {questionTypes.map((questionType) => {
          const Icon = questionType.icon;
          const isSelected = selectedType === questionType.type;
          const isDisabled = disabledTypes.includes(questionType.type);

          return (
            <Grid item xs={12} sm={6} md={4} key={questionType.type}>
              <Paper
                onClick={() => !isDisabled && onSelect(questionType.type)}
                sx={{
                  p: 3,
                  height: '100%',
                  cursor: isDisabled ? 'not-allowed' : 'pointer',
                  backgroundColor: isSelected ? questionType.bgColor : colors.card,
                  border: `2px solid ${isSelected ? questionType.color : 'transparent'}`,
                  borderRadius: '16px',
                  opacity: isDisabled ? 0.5 : 1,
                  transition: 'all 0.2s ease',
                  '&:hover': {
                    transform: isDisabled ? 'none' : 'translateY(-4px)',
                    boxShadow: isDisabled ? 'none' : '0 8px 24px rgba(0,0,0,0.12)',
                    borderColor: isDisabled ? 'transparent' : questionType.color,
                  },
                }}
              >
                {/* Icon */}
                <Box
                  sx={{
                    width: 64,
                    height: 64,
                    borderRadius: '16px',
                    backgroundColor: questionType.bgColor,
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    mb: 2,
                  }}
                >
                  <Icon
                    sx={{
                      fontSize: 32,
                      color: questionType.color,
                    }}
                  />
                </Box>

                {/* Label */}
                <Typography
                  variant="h6"
                  sx={{
                    color: colors.textPrimary,
                    fontWeight: 600,
                    mb: 1,
                  }}
                >
                  {questionType.label}
                </Typography>

                {/* Description */}
                <Typography
                  variant="body2"
                  sx={{
                    color: colors.textSecondary,
                    mb: 2,
                    minHeight: 40,
                  }}
                >
                  {questionType.description}
                </Typography>

                {/* Features */}
                <Box
                  sx={{
                    display: 'flex',
                    flexWrap: 'wrap',
                    gap: 0.5,
                  }}
                >
                  {questionType.features.map((feature, index) => (
                    <Typography
                      key={index}
                      variant="caption"
                      sx={{
                        px: 1,
                        py: 0.5,
                        backgroundColor: questionType.bgColor,
                        color: questionType.color,
                        borderRadius: '4px',
                        fontWeight: 500,
                      }}
                    >
                      {feature}
                    </Typography>
                  ))}
                </Box>

                {/* Selected Indicator */}
                {isSelected && (
                  <Box
                    sx={{
                      position: 'absolute',
                      top: 16,
                      right: 16,
                      width: 24,
                      height: 24,
                      borderRadius: '50%',
                      backgroundColor: questionType.color,
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                    }}
                  >
                    <Box
                      component="span"
                      sx={{
                        width: 10,
                        height: 10,
                        borderRadius: '50%',
                        backgroundColor: '#fff',
                      }}
                    />
                  </Box>
                )}
              </Paper>
            </Grid>
          );
        })}
      </Grid>

      {/* Help Text */}
      <Box
        sx={{
          mt: 4,
          p: 3,
          backgroundColor: colors.background,
          borderRadius: '12px',
        }}
      >
        <Typography variant="subtitle2" sx={{ color: colors.textPrimary, mb: 1 }}>
          Need help choosing?
        </Typography>
        <Typography variant="body2" sx={{ color: colors.textSecondary }}>
          <strong>Text Choice</strong> is great for vocabulary and grammar questions.{' '}
          <strong>Image Choice</strong> works well for visual recognition.{' '}
          <strong>Audio Choice</strong> is perfect for listening comprehension.{' '}
          <strong>Drag & Drop</strong> adds interactivity for matching exercises.{' '}
          <strong>Text Input</strong> tests spelling and free-form answers.
        </Typography>
      </Box>
    </Box>
  );
};

export default QuestionTypeSelector;
