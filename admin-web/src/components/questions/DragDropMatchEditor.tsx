import React from 'react';
import {
  Box,
  TextField,
  IconButton,
  Button,
  Typography,
  Paper,
  Grid,
} from '@mui/material';
import { Delete, Add, CloudUpload } from '@mui/icons-material';
import { DragDropMatchContent, DragItem, DropTarget } from '../../types/questions';
import { uploadMedia } from '../../api/client';

interface DragDropMatchEditorProps {
  content: DragDropMatchContent;
  onChange: (content: DragDropMatchContent) => void;
}

export const DragDropMatchEditor: React.FC<DragDropMatchEditorProps> = ({
  content,
  onChange,
}) => {
  const handleTextChange = (text: string) => {
    onChange({ ...content, text });
  };

  const handleItemChange = (index: number, field: keyof DragItem, value: any) => {
    const newItems = [...content.items];
    newItems[index] = { ...newItems[index], [field]: value };
    onChange({ ...content, items: newItems });
  };

  const handleTargetChange = (index: number, field: keyof DropTarget, value: any) => {
    const newTargets = [...content.targets];
    newTargets[index] = { ...newTargets[index], [field]: value };
    onChange({ ...content, targets: newTargets });
  };

  const handleTargetImageUpload = async (index: number, file: File) => {
    try {
      const url = await uploadMedia(file, 'targets');
      handleTargetChange(index, 'imageUrl', url);
    } catch (error) {
      console.error('Failed to upload image:', error);
    }
  };

  const handleAddPair = () => {
    const newId = `pair_${Date.now()}`;
    const newItem: DragItem = {
      id: `item_${newId}`,
      text: '',
      targetId: `target_${newId}`,
    };
    const newTarget: DropTarget = {
      id: `target_${newId}`,
      text: '',
    };
    onChange({
      ...content,
      items: [...content.items, newItem],
      targets: [...content.targets, newTarget],
    });
  };

  const handleDeletePair = (index: number) => {
    const targetId = content.items[index].targetId;
    onChange({
      ...content,
      items: content.items.filter((_, i) => i !== index),
      targets: content.targets.filter((t) => t.id !== targetId),
    });
  };

  return (
    <Box>
      <TextField
        fullWidth
        label="Инструкция"
        value={content.text}
        onChange={(e) => handleTextChange(e.target.value)}
        sx={{ mb: 3 }}
        placeholder="Например: Соедините слова с картинками"
      />

      <Typography variant="subtitle1" gutterBottom>
        Пары для сопоставления
      </Typography>

      {content.items.map((item, index) => {
        const target = content.targets.find((t) => t.id === item.targetId);
        if (!target) return null;

        return (
          <Paper key={item.id} sx={{ p: 2, mb: 2 }}>
            <Grid container spacing={2} alignItems="center">
              <Grid item xs={12} sm={5}>
                <TextField
                  fullWidth
                  label="Слово / Элемент"
                  value={item.text}
                  onChange={(e) => handleItemChange(index, 'text', e.target.value)}
                />
              </Grid>
              <Grid item xs={12} sm={5}>
                <Box>
                  {target.imageUrl ? (
                    <Box
                      component="img"
                      src={target.imageUrl}
                      alt="Target"
                      sx={{ width: 80, height: 80, objectFit: 'cover', borderRadius: 1, mb: 1 }}
                    />
                  ) : (
                    <TextField
                      fullWidth
                      label="Эмодзи или текст"
                      value={target.emoji || target.text || ''}
                      onChange={(e) => {
                        const value = e.target.value;
                        if (value.length <= 2) {
                          handleTargetChange(index, 'emoji', value);
                        } else {
                          handleTargetChange(index, 'text', value);
                        }
                      }}
                    />
                  )}
                  <Button
                    component="label"
                    size="small"
                    startIcon={<CloudUpload />}
                    sx={{ mt: 1 }}
                  >
                    {target.imageUrl ? 'Заменить' : 'Загрузить картинку'}
                    <input
                      type="file"
                      hidden
                      accept="image/*"
                      onChange={(e) => {
                        const file = e.target.files?.[0];
                        if (file) handleTargetImageUpload(index, file);
                      }}
                    />
                  </Button>
                </Box>
              </Grid>
              <Grid item xs={12} sm={2}>
                <IconButton
                  onClick={() => handleDeletePair(index)}
                  disabled={content.items.length <= 2}
                  color="error"
                >
                  <Delete />
                </IconButton>
              </Grid>
            </Grid>
          </Paper>
        );
      })}

      <Button
        startIcon={<Add />}
        onClick={handleAddPair}
        disabled={content.items.length >= 6}
        variant="outlined"
      >
        Добавить пару
      </Button>
    </Box>
  );
};
