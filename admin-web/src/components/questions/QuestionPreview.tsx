import React from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Radio,
  RadioGroup,
  FormControlLabel,
  FormControl,
  Paper,
  Chip,
} from '@mui/material';
import {
  VolumeUp,
  Image as ImageIcon,
  TextFields,
  DragIndicator,
  ShortText,
  ImageSearch,
} from '@mui/icons-material';
import {
  QuestionV2,
  QuestionTypeV2,
  TextSelectContent,
  ImageSelectContent,
  AudioSelectContent,
  DragDropMatchContent,
  FillBlankContent,
} from '../../types/questions';

interface QuestionPreviewProps {
  question: QuestionV2;
}

const typeIcons: Record<QuestionTypeV2, React.ReactNode> = {
  TEXT_SELECT: <TextFields />,
  IMAGE_SELECT: <ImageIcon />,
  AUDIO_SELECT: <VolumeUp />,
  DRAG_DROP_MATCH: <DragIndicator />,
  DRAG_DROP_SORT: <DragIndicator />,
  FILL_BLANK: <ShortText />,
  IMAGE_WORD_MATCH: <ImageSearch />,
};

const typeLabels: Record<QuestionTypeV2, string> = {
  TEXT_SELECT: 'Выбор из текста',
  IMAGE_SELECT: 'Выбор изображения',
  AUDIO_SELECT: 'Аудирование',
  DRAG_DROP_MATCH: 'Сопоставление',
  DRAG_DROP_SORT: 'Сортировка',
  FILL_BLANK: 'Заполнить пропуск',
  IMAGE_WORD_MATCH: 'Сопоставление изображения и слова',
};

export const QuestionPreview: React.FC<QuestionPreviewProps> = ({ question }) => {
  const renderContent = () => {
    switch (question.type) {
      case 'TEXT_SELECT':
        return <TextSelectPreview content={question.content as TextSelectContent} />;
      case 'IMAGE_SELECT':
        return <ImageSelectPreview content={question.content as ImageSelectContent} />;
      case 'AUDIO_SELECT':
        return <AudioSelectPreview content={question.content as AudioSelectContent} />;
      case 'DRAG_DROP_MATCH':
        return <DragDropMatchPreview content={question.content as DragDropMatchContent} />;
      case 'FILL_BLANK':
        return <FillBlankPreview content={question.content as FillBlankContent} />;
      default:
        return <Typography>Preview not implemented for this type</Typography>;
    }
  };

  return (
    <Card variant="outlined">
      <CardContent>
        <Box display="flex" alignItems="center" gap={1} mb={2}>
          {typeIcons[question.type]}
          <Typography variant="subtitle2" color="text.secondary">
            {typeLabels[question.type]}
          </Typography>
          <Chip
            label={`${question.points} балл${question.points !== 1 ? 'ов' : ''}`}
            size="small"
            sx={{ ml: 'auto' }}
          />
        </Box>

        <Typography variant="h6" gutterBottom>
          {question.title}
        </Typography>

        {question.hint && (
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            💡 Подсказка: {question.hint}
          </Typography>
        )}

        {renderContent()}
      </CardContent>
    </Card>
  );
};

const TextSelectPreview: React.FC<{ content: TextSelectContent }> = ({ content }) => (
  <Box>
    <Typography variant="body1" gutterBottom>
      {content.text}
    </Typography>
    <FormControl component="fieldset">
      <RadioGroup>
        {content.answers.map((answer) => (
          <FormControlLabel
            key={answer.id}
            value={answer.id}
            control={<Radio />}
            label={answer.text}
          />
        ))}
      </RadioGroup>
    </FormControl>
  </Box>
);

const ImageSelectPreview: React.FC<{ content: ImageSelectContent }> = ({ content }) => (
  <Box>
    {content.text && (
      <Typography variant="body1" gutterBottom>
        {content.text}
      </Typography>
    )}
    <Box display="flex" gap={2} flexWrap="wrap">
      {content.answers.map((answer) => (
        <Paper
          key={answer.id}
          sx={{
            p: 1,
            width: 120,
            textAlign: 'center',
            cursor: 'pointer',
            '&:hover': { boxShadow: 2 },
          }}
        >
          {answer.imageUrl ? (
            <Box
              component="img"
              src={answer.imageUrl}
              alt={answer.text || 'Option'}
              sx={{ width: '100%', height: 80, objectFit: 'cover', borderRadius: 1 }}
            />
          ) : answer.emoji ? (
            <Typography variant="h2">{answer.emoji}</Typography>
          ) : (
            <Box sx={{ height: 80, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <Typography variant="body2" color="text.secondary">Нет изображения</Typography>
            </Box>
          )}
          {answer.text && (
            <Typography variant="caption" display="block" sx={{ mt: 1 }}>
              {answer.text}
            </Typography>
          )}
        </Paper>
      ))}
    </Box>
  </Box>
);

const AudioSelectPreview: React.FC<{ content: AudioSelectContent }> = ({ content }) => (
  <Box>
    <Box sx={{ mb: 2 }}>
      <audio controls style={{ width: '100%' }}>
        <source src={content.audioUrl} />
        Ваш браузер не поддерживает аудио.
      </audio>
    </Box>
    {content.text && (
      <Typography variant="body1" gutterBottom>
        {content.text}
      </Typography>
    )}
    <FormControl component="fieldset">
      <RadioGroup>
        {content.answers.map((answer) => (
          <FormControlLabel
            key={answer.id}
            value={answer.id}
            control={<Radio />}
            label={answer.text}
          />
        ))}
      </RadioGroup>
    </FormControl>
  </Box>
);

const DragDropMatchPreview: React.FC<{ content: DragDropMatchContent }> = ({ content }) => (
  <Box>
    <Typography variant="body1" gutterBottom>
      {content.text}
    </Typography>
    <Box display="flex" gap={4}>
      <Box>
        <Typography variant="subtitle2" gutterBottom>Элементы:</Typography>
        {content.items.map((item) => (
          <Paper key={item.id} sx={{ p: 1, mb: 1, minWidth: 100 }}>
            <Typography>{item.text}</Typography>
          </Paper>
        ))}
      </Box>
      <Box>
        <Typography variant="subtitle2" gutterBottom>Цели:</Typography>
        {content.targets.map((target) => (
          <Paper key={target.id} sx={{ p: 1, mb: 1, minWidth: 100, bgcolor: 'grey.100' }}>
            {target.imageUrl ? (
              <Box
                component="img"
                src={target.imageUrl}
                alt={target.text || 'Target'}
                sx={{ width: 40, height: 40, objectFit: 'cover' }}
              />
            ) : target.emoji ? (
              <Typography variant="h4">{target.emoji}</Typography>
            ) : (
              <Typography>{target.text}</Typography>
            )}
          </Paper>
        ))}
      </Box>
    </Box>
  </Box>
);

const FillBlankPreview: React.FC<{ content: FillBlankContent }> = ({ content }) => (
  <Box>
    <Typography variant="body1" gutterBottom>
      {content.textBefore}{' '}
      <Box
        component="span"
        sx={{
          display: 'inline-block',
          minWidth: 80,
          borderBottom: '2px solid',
          borderColor: 'primary.main',
          textAlign: 'center',
        }}
      >
        ???
      </Box>{' '}
      {content.textAfter}
    </Typography>
    <FormControl component="fieldset">
      <RadioGroup row>
        {content.answers.map((answer) => (
          <FormControlLabel
            key={answer.id}
            value={answer.id}
            control={<Radio />}
            label={answer.text}
          />
        ))}
      </RadioGroup>
    </FormControl>
  </Box>
);
