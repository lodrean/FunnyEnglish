import React, { useState, useEffect, useRef } from 'react';
import {
  Box,
  Paper,
  Typography,
  TextField,
  Button,
  IconButton,
  Chip,
  Slider,
  FormControlLabel,
  Checkbox,
  Tooltip,
  Divider,
  Alert,
  Snackbar,
  CircularProgress,
  useTheme,
} from '@mui/material';
import {
  Add,
  Delete,
  Image as ImageIcon,
  Audiotrack,
  CheckCircle,
  CloudUpload,
  Save,
  Close,
} from '@mui/icons-material';
import QuestionTypeSelector from './QuestionTypeSelector';



// Question Types
export type QuestionType =
  | 'TEXT_SELECT'
  | 'IMAGE_SELECT'
  | 'AUDIO_SELECT'
  | 'DRAG_DROP'
  | 'TEXT_INPUT'
  | 'IMAGE_WORD_MATCH';

export interface Question {
  id: string;
  type: QuestionType;
  question: string;
  options?: string[];
  correctAnswers?: (string | number)[];
  images?: string[];
  audioUrl?: string;
  points: number;
  order: number;
  explanation?: string;
  hint?: string;
  timeLimit?: number;
  allowMultipleAnswers?: boolean;
  caseSensitive?: boolean;
  // Image Word Match fields
  imageUrl?: string;
  words?: { id: string; text: string; translation?: string }[];
  hotspots?: { id: string; x: number; y: number; width: number; height: number; shape: 'RECTANGLE' | 'CIRCLE'; wordId: string }[];
}

interface QuestionBuilderProps {
  question?: Question;
  onSave: (question: Question) => void;
  onCancel: () => void;
  onAutoSave?: (question: Question) => void;
  autoSaveInterval?: number; // in milliseconds, default 30000 (30s)
}

// Default question template
const createDefaultQuestion = (type: QuestionType): Question => ({
  id: `temp-${Date.now()}`,
  type,
  question: type === 'IMAGE_WORD_MATCH' ? 'Match the words to the objects' : '',
  options: type === 'TEXT_INPUT' ? [] : ['', ''],
  correctAnswers: [],
  points: type === 'IMAGE_WORD_MATCH' ? 15 : 10,
  order: 0,
  allowMultipleAnswers: false,
  caseSensitive: false,
  imageUrl: type === 'IMAGE_WORD_MATCH' ? '' : undefined,
  words: type === 'IMAGE_WORD_MATCH' ? [] : undefined,
  hotspots: type === 'IMAGE_WORD_MATCH' ? [] : undefined,
});

// Validate question
const validateQuestion = (question: Question): string[] => {
  const errors: string[] = [];

  if (!question.question.trim()) {
    errors.push('Question text is required');
  }

  if (question.type !== 'TEXT_INPUT') {
    const optionCount = question.options?.filter((o) => o.trim()).length ?? 0;
    if (optionCount < 2) {
      errors.push('At least 2 options are required');
    }

    if (!question.correctAnswers || question.correctAnswers.length === 0) {
      errors.push('At least one correct answer must be selected');
    }
  }

  if (question.type === 'IMAGE_SELECT' && (!question.images || question.images.length < 2)) {
    errors.push('At least 2 images are required for image selection');
  }

  if (question.type === 'AUDIO_SELECT' && !question.audioUrl) {
    errors.push('Audio file is required for audio selection');
  }

  if (question.points <= 0) {
    errors.push('Points must be greater than 0');
  }

  return errors;
};

const QuestionBuilder: React.FC<QuestionBuilderProps> = ({
  question: initialQuestion,
  onSave,
  onCancel,
  onAutoSave,
  autoSaveInterval = 30000,
}) => {
  const [question, setQuestion] = useState<Question>(
    initialQuestion || createDefaultQuestion('TEXT_SELECT')
  );
  const [selectedType, setSelectedType] = useState<QuestionType | null>(
    initialQuestion ? initialQuestion.type : null
  );
  const [errors, setErrors] = useState<string[]>([]);
  const [autoSaveStatus, setAutoSaveStatus] = useState<'idle' | 'saving' | 'saved'>('idle');
  const [showSnackbar, setShowSnackbar] = useState(false);
  const autoSaveTimerRef = useRef<NodeJS.Timeout | null>(null);
  const lastSavedRef = useRef<Question | null>(null);
  const theme = useTheme();

  // Auto-save functionality
  useEffect(() => {
    if (!onAutoSave || !selectedType) return;

    // Clear existing timer
    if (autoSaveTimerRef.current) {
      clearTimeout(autoSaveTimerRef.current);
    }

    // Check if question has changed
    const questionChanged = JSON.stringify(question) !== JSON.stringify(lastSavedRef.current);

    if (questionChanged && question.question.trim()) {
      setAutoSaveStatus('idle');
      autoSaveTimerRef.current = setTimeout(() => {
        const validationErrors = validateQuestion(question);
        if (validationErrors.length === 0) {
          setAutoSaveStatus('saving');
          onAutoSave(question);
          lastSavedRef.current = { ...question };
          setAutoSaveStatus('saved');
          setShowSnackbar(true);
        }
      }, autoSaveInterval);
    }

    return () => {
      if (autoSaveTimerRef.current) {
        clearTimeout(autoSaveTimerRef.current);
      }
    };
  }, [question, onAutoSave, autoSaveInterval, selectedType]);

  const handleTypeSelect = (type: QuestionType) => {
    setSelectedType(type);
    setQuestion(createDefaultQuestion(type));
    setErrors([]);
  };

  const handleQuestionChange = (value: string) => {
    setQuestion((prev) => ({ ...prev, question: value }));
  };

  const handleAddOption = () => {
    setQuestion((prev) => ({
      ...prev,
      options: [...(prev.options || []), ''],
    }));
  };

  const handleRemoveOption = (index: number) => {
    setQuestion((prev) => {
      const newOptions = [...(prev.options || [])];
      newOptions.splice(index, 1);

      // Remove from correct answers if this option was selected
      const newCorrectAnswers = (prev.correctAnswers || []).filter(
        (ca) => ca !== index && ca !== prev.options?.[index]
      );

      return {
        ...prev,
        options: newOptions,
        correctAnswers: newCorrectAnswers,
      };
    });
  };

  const handleOptionChange = (index: number, value: string) => {
    setQuestion((prev) => {
      const newOptions = [...(prev.options || [])];
      newOptions[index] = value;
      return { ...prev, options: newOptions };
    });
  };

  const handleCorrectAnswerToggle = (index: number) => {
    setQuestion((prev) => {
      const currentCorrect = prev.correctAnswers || [];
      const isSelected = currentCorrect.includes(index);

      let newCorrectAnswers: (string | number)[];

      if (prev.allowMultipleAnswers) {
        // Multiple selection allowed
        newCorrectAnswers = isSelected
          ? currentCorrect.filter((ca) => ca !== index)
          : [...currentCorrect, index];
      } else {
        // Single selection only
        newCorrectAnswers = isSelected ? [] : [index];
      }

      return { ...prev, correctAnswers: newCorrectAnswers };
    });
  };

  const handleImageUpload = async (index: number, file: File) => {
    // In a real app, this would upload to a server
    // For now, we'll create a local URL
    const imageUrl = URL.createObjectURL(file);

    setQuestion((prev) => {
      const newImages = [...(prev.images || [])];
      newImages[index] = imageUrl;
      return { ...prev, images: newImages };
    });
  };

  const handleAudioUpload = async (file: File) => {
    // In a real app, this would upload to a server
    const audioUrl = URL.createObjectURL(file);
    setQuestion((prev) => ({ ...prev, audioUrl }));
  };

  const handleSave = () => {
    const validationErrors = validateQuestion(question);
    if (validationErrors.length > 0) {
      setErrors(validationErrors);
      return;
    }

    onSave(question);
  };

  const handleAddImageOption = () => {
    setQuestion((prev) => ({
      ...prev,
      images: [...(prev.images || []), ''],
      options: [...(prev.options || []), ''],
    }));
  };

  const handleRemoveImageOption = (index: number) => {
    setQuestion((prev) => {
      const newImages = [...(prev.images || [])];
      newImages.splice(index, 1);
      const newOptions = [...(prev.options || [])];
      newOptions.splice(index, 1);
      return { ...prev, images: newImages, options: newOptions };
    });
  };

  // If no type selected, show type selector
  if (!selectedType) {
    return (
      <Box sx={{ width: '100%' }}>
        <Typography variant="h6" sx={{ color: theme.palette.text.primary, fontWeight: 600, mb: 2 }}>
          Select Question Type
        </Typography>
        <QuestionTypeSelector selectedType={null} onSelect={handleTypeSelect} />
      </Box>
    );
  }

  return (
    <Box sx={{ width: '100%' }}>
      {/* Header */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h6" sx={{ color: theme.palette.text.primary, fontWeight: 600 }}>
          {initialQuestion ? 'Edit Question' : 'Create Question'}
        </Typography>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          {autoSaveStatus === 'saving' && (
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <CircularProgress size={16} />
              <Typography variant="caption" sx={{ color: theme.palette.text.secondary }}>
                Saving...
              </Typography>
            </Box>
          )}
          {autoSaveStatus === 'saved' && (
            <Typography variant="caption" sx={{ color: theme.palette.success.main }}>
              Auto-saved
            </Typography>
          )}
          <Button variant="outlined" onClick={onCancel} startIcon={<Close />}>
            Cancel
          </Button>
          <Button
            variant="contained"
            onClick={handleSave}
            startIcon={<Save />}
            sx={{ backgroundColor: theme.palette.primary.main }}
          >
            Save Question
          </Button>
        </Box>
      </Box>

      {/* Error Messages */}
      {errors.length > 0 && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {errors.map((error, index) => (
            <div key={index}>{error}</div>
          ))}
        </Alert>
      )}

      {/* Question Type Display */}
      <Paper sx={{ p: 2, mb: 3, backgroundColor: theme.palette.background.default }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Typography variant="subtitle2" sx={{ color: theme.palette.text.secondary }}>
            Question Type
          </Typography>
          <Chip
            label={selectedType.replace('_', ' ')}
            sx={{
              backgroundColor: theme.palette.primary.main,
              color: '#fff',
              textTransform: 'capitalize',
            }}
          />
        </Box>
      </Paper>

      {/* Question Text */}
      <Box sx={{ mb: 3 }}>
        <Typography variant="subtitle1" sx={{ color: theme.palette.text.primary, mb: 1, fontWeight: 500 }}>
          Question Text *
        </Typography>
        <TextField
          fullWidth
          multiline
          rows={3}
          placeholder="Enter your question here..."
          value={question.question}
          onChange={(e) => handleQuestionChange(e.target.value)}
          error={errors.some((e) => e.includes('Question text'))}
        />
      </Box>

      {/* Audio Upload for AUDIO_SELECT */}
      {selectedType === 'AUDIO_SELECT' && (
        <Box sx={{ mb: 3 }}>
          <Typography variant="subtitle1" sx={{ color: theme.palette.text.primary, mb: 1, fontWeight: 500 }}>
            Audio File *
          </Typography>
          <Paper
            sx={{
              p: 3,
              border: `2px dashed ${question.audioUrl ? theme.palette.success.main : theme.palette.primary.main}`,
              borderRadius: '12px',
              textAlign: 'center',
            }}
          >
            {question.audioUrl ? (
              <Box>
                <audio controls src={question.audioUrl} style={{ width: '100%' }} />
                <Button
                  variant="outlined"
                  size="small"
                  onClick={() => setQuestion((prev) => ({ ...prev, audioUrl: undefined }))}
                  sx={{ mt: 2 }}
                >
                  Remove Audio
                </Button>
              </Box>
            ) : (
              <Box>
                <Audiotrack sx={{ fontSize: 48, color: theme.palette.primary.main, mb: 1 }} />
                <Typography variant="body2" sx={{ color: theme.palette.text.secondary, mb: 2 }}>
                  Upload an audio file for this question
                </Typography>
                <Button
                  variant="contained"
                  component="label"
                  startIcon={<CloudUpload />}
                  sx={{ backgroundColor: theme.palette.primary.main }}
                >
                  Upload Audio
                  <input
                    type="file"
                    accept="audio/*"
                    hidden
                    onChange={(e) => {
                      const file = e.target.files?.[0];
                      if (file) handleAudioUpload(file);
                    }}
                  />
                </Button>
              </Box>
            )}
          </Paper>
        </Box>
      )}

      {/* Options Section */}
      {selectedType !== 'TEXT_INPUT' && (
        <Box sx={{ mb: 3 }}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
            <Typography variant="subtitle1" sx={{ color: theme.palette.text.primary, fontWeight: 500 }}>
              Answer Options *
            </Typography>
            <FormControlLabel
              control={
                <Checkbox
                  checked={question.allowMultipleAnswers}
                  onChange={(e) =>
                    setQuestion((prev) => ({
                      ...prev,
                      allowMultipleAnswers: e.target.checked,
                      correctAnswers: [],
                    }))
                  }
                />
              }
              label="Allow multiple correct answers"
            />
          </Box>

          {/* TEXT_SELECT Options */}
          {selectedType === 'TEXT_SELECT' && (
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
              {question.options?.map((option, index) => (
                <Paper
                  key={index}
                  sx={{
                    p: 2,
                    display: 'flex',
                    alignItems: 'center',
                    gap: 2,
                    border: question.correctAnswers?.includes(index)
                      ? `2px solid ${theme.palette.success.main}`
                      : '1px solid #e0e0e0',
                  }}
                >
                  <Tooltip title={question.correctAnswers?.includes(index) ? 'Correct answer' : 'Mark as correct'}>
                    <IconButton
                      onClick={() => handleCorrectAnswerToggle(index)}
                      sx={{
                        color: question.correctAnswers?.includes(index)
                          ? theme.palette.success.main
                          : theme.palette.text.secondary,
                      }}
                    >
                      <CheckCircle />
                    </IconButton>
                  </Tooltip>
                  <TextField
                    fullWidth
                    size="small"
                    placeholder={`Option ${index + 1}`}
                    value={option}
                    onChange={(e) => handleOptionChange(index, e.target.value)}
                  />
                  <IconButton
                    onClick={() => handleRemoveOption(index)}
                    disabled={question.options?.length === 1}
                    color="error"
                  >
                    <Delete />
                  </IconButton>
                </Paper>
              ))}
              <Button
                variant="outlined"
                startIcon={<Add />}
                onClick={handleAddOption}
                sx={{ mt: 1 }}
              >
                Add Option
              </Button>
            </Box>
          )}

          {/* IMAGE_SELECT Options */}
          {selectedType === 'IMAGE_SELECT' && (
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
              {question.images?.map((image, index) => (
                <Paper
                  key={index}
                  sx={{
                    p: 2,
                    display: 'flex',
                    alignItems: 'center',
                    gap: 2,
                    border: question.correctAnswers?.includes(index)
                      ? `2px solid ${theme.palette.success.main}`
                      : '1px solid #e0e0e0',
                  }}
                >
                  <Tooltip title={question.correctAnswers?.includes(index) ? 'Correct answer' : 'Mark as correct'}>
                    <IconButton
                      onClick={() => handleCorrectAnswerToggle(index)}
                      sx={{
                        color: question.correctAnswers?.includes(index)
                          ? theme.palette.success.main
                          : theme.palette.text.secondary,
                      }}
                    >
                      <CheckCircle />
                    </IconButton>
                  </Tooltip>
                  {image ? (
                    <Box
                      component="img"
                      src={image}
                      alt={`Option ${index + 1}`}
                      sx={{ width: 80, height: 80, objectFit: 'cover', borderRadius: 1 }}
                    />
                  ) : (
                    <Box
                      sx={{
                        width: 80,
                        height: 80,
                        backgroundColor: theme.palette.background.default,
                        borderRadius: 1,
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                      }}
                    >
                      <ImageIcon sx={{ color: theme.palette.text.secondary }} />
                    </Box>
                  )}
                  <Button
                    variant="outlined"
                    component="label"
                    size="small"
                    startIcon={<CloudUpload />}
                  >
                    {image ? 'Change' : 'Upload'}
                    <input
                      type="file"
                      accept="image/*"
                      hidden
                      onChange={(e) => {
                        const file = e.target.files?.[0];
                        if (file) handleImageUpload(index, file);
                      }}
                    />
                  </Button>
                  <TextField
                    size="small"
                    placeholder="Label (optional)"
                    value={question.options?.[index] || ''}
                    onChange={(e) => handleOptionChange(index, e.target.value)}
                    sx={{ flex: 1 }}
                  />
                  <IconButton onClick={() => handleRemoveImageOption(index)} color="error">
                    <Delete />
                  </IconButton>
                </Paper>
              ))}
              <Button
                variant="outlined"
                startIcon={<Add />}
                onClick={handleAddImageOption}
                sx={{ mt: 1 }}
              >
                Add Image Option
              </Button>
            </Box>
          )}

          {/* AUDIO_SELECT Options */}
          {selectedType === 'AUDIO_SELECT' && (
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
              {question.options?.map((option, index) => (
                <Paper
                  key={index}
                  sx={{
                    p: 2,
                    display: 'flex',
                    alignItems: 'center',
                    gap: 2,
                    border: question.correctAnswers?.includes(index)
                      ? `2px solid ${theme.palette.success.main}`
                      : '1px solid #e0e0e0',
                  }}
                >
                  <Tooltip title={question.correctAnswers?.includes(index) ? 'Correct answer' : 'Mark as correct'}>
                    <IconButton
                      onClick={() => handleCorrectAnswerToggle(index)}
                      sx={{
                        color: question.correctAnswers?.includes(index)
                          ? theme.palette.success.main
                          : theme.palette.text.secondary,
                      }}
                    >
                      <CheckCircle />
                    </IconButton>
                  </Tooltip>
                  <TextField
                    fullWidth
                    size="small"
                    placeholder={`Option ${index + 1}`}
                    value={option}
                    onChange={(e) => handleOptionChange(index, e.target.value)}
                  />
                  <IconButton onClick={() => handleRemoveOption(index)} color="error">
                    <Delete />
                  </IconButton>
                </Paper>
              ))}
              <Button
                variant="outlined"
                startIcon={<Add />}
                onClick={handleAddOption}
                sx={{ mt: 1 }}
              >
                Add Option
              </Button>
            </Box>
          )}

          {/* DRAG_DROP Options */}
          {selectedType === 'DRAG_DROP' && (
            <Box>
              <Alert severity="info" sx={{ mb: 2 }}>
                For drag & drop questions, enter the items that can be dragged. The correct answer
                indicates which item(s) should be placed in the drop zone.
              </Alert>
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
                {question.options?.map((option, index) => (
                  <Paper
                    key={index}
                    sx={{
                      p: 2,
                      display: 'flex',
                      alignItems: 'center',
                      gap: 2,
                      border: question.correctAnswers?.includes(index)
                        ? `2px solid ${theme.palette.success.main}`
                        : '1px solid #e0e0e0',
                    }}
                  >
                    <Tooltip title={question.correctAnswers?.includes(index) ? 'Correct answer' : 'Mark as correct'}>
                      <IconButton
                        onClick={() => handleCorrectAnswerToggle(index)}
                        sx={{
                          color: question.correctAnswers?.includes(index)
                            ? theme.palette.success.main
                            : theme.palette.text.secondary,
                        }}
                      >
                        <CheckCircle />
                      </IconButton>
                    </Tooltip>
                    <TextField
                      fullWidth
                      size="small"
                      placeholder={`Draggable item ${index + 1}`}
                      value={option}
                      onChange={(e) => handleOptionChange(index, e.target.value)}
                    />
                    <IconButton onClick={() => handleRemoveOption(index)} color="error">
                      <Delete />
                    </IconButton>
                  </Paper>
                ))}
                <Button
                  variant="outlined"
                  startIcon={<Add />}
                  onClick={handleAddOption}
                  sx={{ mt: 1 }}
                >
                  Add Draggable Item
                </Button>
              </Box>
            </Box>
          )}

          {/* IMAGE_WORD_MATCH Options */}
          {selectedType === 'IMAGE_WORD_MATCH' && (
            <Box>
              <Alert severity="info" sx={{ mb: 2 }}>
                For image word match questions, upload an image and draw hotspots where students 
                should drag the words. Each hotspot must be linked to a word.
              </Alert>
              
              {/* Image Upload */}
              <Box sx={{ mb: 3 }}>
                <Typography variant="subtitle1" sx={{ color: theme.palette.text.primary, mb: 1, fontWeight: 500 }}>
                  Background Image *
                </Typography>
                <Paper
                  sx={{
                    p: 3,
                    border: '2px dashed #E0E0E0',
                    backgroundColor: theme.palette.background.default,
                    textAlign: 'center',
                  }}
                >
                  {question.imageUrl ? (
                    <Box>
                      <img 
                        src={question.imageUrl} 
                        alt="Question" 
                        style={{ maxWidth: '100%', maxHeight: 300, borderRadius: 8 }} 
                      />
                      <Button
                        variant="outlined"
                        size="small"
                        onClick={() => setQuestion((prev) => ({ ...prev, imageUrl: undefined }))}
                        sx={{ mt: 2 }}
                      >
                        Remove Image
                      </Button>
                    </Box>
                  ) : (
                    <Box>
                      <ImageIcon sx={{ fontSize: 48, color: theme.palette.primary.main, mb: 1 }} />
                      <Typography variant="body2" sx={{ color: theme.palette.text.secondary, mb: 2 }}>
                        Upload an image with objects to label
                      </Typography>
                      <Button
                        variant="contained"
                        component="label"
                        startIcon={<CloudUpload />}
                        sx={{ backgroundColor: theme.palette.primary.main }}
                      >
                        Upload Image
                        <input
                          type="file"
                          accept="image/*"
                          hidden
                          onChange={(e) => {
                            const file = e.target.files?.[0];
                            if (file) {
                              const imageUrl = URL.createObjectURL(file);
                              setQuestion((prev) => ({ ...prev, imageUrl }));
                            }
                          }}
                        />
                      </Button>
                    </Box>
                  )}
                </Paper>
              </Box>

              {/* Words */}
              <Box sx={{ mb: 3 }}>
                <Typography variant="subtitle1" sx={{ color: theme.palette.text.primary, mb: 1, fontWeight: 500 }}>
                  Words ({question.words?.length || 0}/8)
                </Typography>
                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
                  {question.words?.map((word, index) => (
                    <Paper
                      key={word.id}
                      sx={{
                        p: 2,
                        display: 'flex',
                        alignItems: 'center',
                        gap: 2,
                        border: '1px solid #e0e0e0',
                      }}
                    >
                      <Typography sx={{ fontWeight: 500, minWidth: 24 }}>{index + 1}</Typography>
                      <TextField
                        fullWidth
                        size="small"
                        placeholder="Word (e.g., 'refrigerator')"
                        value={word.text}
                        onChange={(e) => {
                          const newWords = [...(question.words || [])];
                          newWords[index] = { ...word, text: e.target.value };
                          setQuestion((prev) => ({ ...prev, words: newWords }));
                        }}
                      />
                      <TextField
                        fullWidth
                        size="small"
                        placeholder="Translation (optional)"
                        value={word.translation || ''}
                        onChange={(e) => {
                          const newWords = [...(question.words || [])];
                          newWords[index] = { ...word, translation: e.target.value };
                          setQuestion((prev) => ({ ...prev, words: newWords }));
                        }}
                      />
                      <IconButton 
                        onClick={() => {
                          const newWords = [...(question.words || [])];
                          newWords.splice(index, 1);
                          setQuestion((prev) => ({ ...prev, words: newWords }));
                        }} 
                        color="error"
                      >
                        <Delete />
                      </IconButton>
                    </Paper>
                  ))}
                  {(question.words?.length || 0) < 8 && (
                    <Button
                      variant="outlined"
                      startIcon={<Add />}
                      onClick={() =>
                        setQuestion((prev) => ({
                          ...prev,
                          words: [...(prev.words || []), { id: `word-${Date.now()}`, text: '' }],
                        }))
                      }
                      sx={{ mt: 1 }}
                    >
                      Add Word
                    </Button>
                  )}
                </Box>
              </Box>

              {/* Hotspots */}
              {question.imageUrl && question.words && question.words.length >= 2 && (
                <Box sx={{ mb: 3 }}>
                  <Typography variant="subtitle1" sx={{ color: theme.palette.text.primary, mb: 1, fontWeight: 500 }}>
                    Hotspots ({question.hotspots?.length || 0}/{question.words?.length || 0})
                  </Typography>
                  <Alert severity="warning" sx={{ mb: 2 }}>
                    Hotspot drawing requires the advanced editor. For now, hotspots will be created 
                    automatically when you save.
                  </Alert>
                </Box>
              )}
            </Box>
          )}
        </Box>
      )}

      {/* TEXT_INPUT Settings */}
      {selectedType === 'TEXT_INPUT' && (
        <Box sx={{ mb: 3 }}>
          <Alert severity="info" sx={{ mb: 2 }}>
            For text input questions, enter the acceptable correct answers. Multiple answers can be
            provided for variations.
          </Alert>
          <Typography variant="subtitle1" sx={{ color: theme.palette.text.primary, mb: 1, fontWeight: 500 }}>
            Correct Answers *
          </Typography>
          <FormControlLabel
            control={
              <Checkbox
                checked={question.caseSensitive}
                onChange={(e) =>
                  setQuestion((prev) => ({ ...prev, caseSensitive: e.target.checked }))
                }
              />
            }
            label="Case sensitive"
            sx={{ mb: 2 }}
          />
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
            {question.correctAnswers?.map((answer, index) => (
              <Paper
                key={index}
                sx={{
                  p: 2,
                  display: 'flex',
                  alignItems: 'center',
                  gap: 2,
                  border: `2px solid ${theme.palette.success.main}`,
                }}
              >
                <CheckCircle sx={{ color: theme.palette.success.main }} />
                <TextField
                  fullWidth
                  size="small"
                  placeholder={`Correct answer ${index + 1}`}
                  value={answer}
                  onChange={(e) => {
                    const newAnswers = [...(question.correctAnswers || [])];
                    newAnswers[index] = e.target.value;
                    setQuestion((prev) => ({ ...prev, correctAnswers: newAnswers }));
                  }}
                />
                <IconButton
                  onClick={() => {
                    const newAnswers = [...(question.correctAnswers || [])];
                    newAnswers.splice(index, 1);
                    setQuestion((prev) => ({ ...prev, correctAnswers: newAnswers }));
                  }}
                  color="error"
                >
                  <Delete />
                </IconButton>
              </Paper>
            ))}
            <Button
              variant="outlined"
              startIcon={<Add />}
              onClick={() =>
                setQuestion((prev) => ({
                  ...prev,
                  correctAnswers: [...(prev.correctAnswers || []), ''],
                }))
              }
              sx={{ mt: 1 }}
            >
              Add Correct Answer
            </Button>
          </Box>
        </Box>
      )}

      <Divider sx={{ my: 3 }} />

      {/* Additional Settings */}
      <Box>
        <Typography variant="subtitle1" sx={{ color: theme.palette.text.primary, mb: 2, fontWeight: 500 }}>
          Additional Settings
        </Typography>

        <Box sx={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: 3 }}>
          {/* Points */}
          <Box>
            <Typography variant="body2" sx={{ color: theme.palette.text.secondary, mb: 1 }}>
              Points: {question.points}
            </Typography>
            <Slider
              value={question.points}
              onChange={(_, value) =>
                setQuestion((prev) => ({ ...prev, points: value as number }))
              }
              min={1}
              max={100}
              step={1}
              marks={[
                { value: 1, label: '1' },
                { value: 50, label: '50' },
                { value: 100, label: '100' },
              ]}
              valueLabelDisplay="auto"
            />
          </Box>

          {/* Time Limit */}
          <Box>
            <Typography variant="body2" sx={{ color: theme.palette.text.secondary, mb: 1 }}>
              Time Limit (seconds): {question.timeLimit || 'None'}
            </Typography>
            <Slider
              value={question.timeLimit || 0}
              onChange={(_, value) =>
                setQuestion((prev) => ({ ...prev, timeLimit: value as number || undefined }))
              }
              min={0}
              max={300}
              step={5}
              marks={[
                { value: 0, label: 'None' },
                { value: 60, label: '1m' },
                { value: 180, label: '3m' },
                { value: 300, label: '5m' },
              ]}
              valueLabelDisplay="auto"
            />
          </Box>
        </Box>

        {/* Explanation */}
        <Box sx={{ mt: 3 }}>
          <Typography variant="body2" sx={{ color: theme.palette.text.secondary, mb: 1 }}>
            Explanation (shown after answering)
          </Typography>
          <TextField
            fullWidth
            multiline
            rows={2}
            placeholder="Explain why the answer is correct..."
            value={question.explanation || ''}
            onChange={(e) =>
              setQuestion((prev) => ({ ...prev, explanation: e.target.value }))
            }
          />
        </Box>

        {/* Hint */}
        <Box sx={{ mt: 2 }}>
          <Typography variant="body2" sx={{ color: theme.palette.text.secondary, mb: 1 }}>
            Hint (optional)
          </Typography>
          <TextField
            fullWidth
            placeholder="Give a hint to help users..."
            value={question.hint || ''}
            onChange={(e) => setQuestion((prev) => ({ ...prev, hint: e.target.value }))}
          />
        </Box>
      </Box>

      {/* Auto-save Snackbar */}
      <Snackbar
        open={showSnackbar}
        autoHideDuration={3000}
        onClose={() => setShowSnackbar(false)}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
      >
        <Alert severity="success" onClose={() => setShowSnackbar(false)}>
          Draft auto-saved
        </Alert>
      </Snackbar>
    </Box>
  );
};

export default QuestionBuilder;
