import React from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Grid,
} from '@mui/material';
import {
  TextFields,
  Image,
  Audiotrack,
  DragIndicator,
  ShortText,
  TouchApp,
} from '@mui/icons-material';
import { QuestionTypeV2 } from '../../types/questions';

interface QuestionType {
  type: QuestionTypeV2;
  title: string;
  description: string;
  icon: React.ReactNode;
}

const questionTypes: QuestionType[] = [
  {
    type: 'TEXT_SELECT',
    title: 'Выбор из текста',
    description: 'Ученик выбирает правильный ответ из нескольких вариантов',
    icon: <TextFields sx={{ fontSize: 40 }} />,
  },
  {
    type: 'IMAGE_SELECT',
    title: 'Выбор из картинок',
    description: 'Ученик выбирает правильное изображение из нескольких вариантов',
    icon: <Image sx={{ fontSize: 40 }} />,
  },
  {
    type: 'AUDIO_SELECT',
    title: 'Аудирование',
    description: 'Ученик слушает аудио и выбирает правильный ответ',
    icon: <Audiotrack sx={{ fontSize: 40 }} />,
  },
  {
    type: 'DRAG_DROP_MATCH',
    title: 'Сопоставление',
    description: 'Ученик соединяет элементы пары перетаскиванием',
    icon: <DragIndicator sx={{ fontSize: 40 }} />,
  },
  {
    type: 'FILL_BLANK',
    title: 'Заполнить пропуск',
    description: 'Ученик выбирает слово для вставки в пропуск',
    icon: <ShortText sx={{ fontSize: 40 }} />,
  },
  {
    type: 'IMAGE_WORD_MATCH',
    title: 'Найди предмет',
    description: 'Ученик перетаскивает слова к областям на изображении',
    icon: <TouchApp sx={{ fontSize: 40 }} />,
  },
];

interface QuestionTypeSelectorProps {
  selectedType?: QuestionTypeV2;
  onSelect: (type: QuestionTypeV2) => void;
}

export const QuestionTypeSelector: React.FC<QuestionTypeSelectorProps> = ({
  selectedType,
  onSelect,
}) => {
  return (
    <Box>
      <Typography variant="h6" gutterBottom>
        Выберите тип вопроса
      </Typography>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
        Каждый тип подходит для разных задач обучения английскому языку
      </Typography>

      <Grid container spacing={2}>
        {questionTypes.map((qt) => (
          <Grid item xs={12} sm={6} key={qt.type}>
            <Card
              onClick={() => onSelect(qt.type)}
              sx={{
                cursor: 'pointer',
                transition: 'all 0.2s',
                border: selectedType === qt.type ? 2 : 0,
                borderColor: 'primary.main',
                '&:hover': {
                  boxShadow: 4,
                  transform: 'translateY(-2px)',
                },
              }}
            >
              <CardContent sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                <Box
                  sx={{
                    p: 1.5,
                    borderRadius: 2,
                    bgcolor: 'primary.light',
                    color: 'primary.contrastText',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                  }}
                >
                  {qt.icon}
                </Box>
                <Box flex={1}>
                  <Typography variant="h6" component="h3" gutterBottom>
                    {qt.title}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    {qt.description}
                  </Typography>
                </Box>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>
    </Box>
  );
};
