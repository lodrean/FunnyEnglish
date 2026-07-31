import { useCallback, useState } from 'react';
import { useDropzone } from 'react-dropzone';
import {
  Box,
  Typography,
  CircularProgress,
  IconButton,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
  Tooltip,
} from '@mui/material';
import { CloudUpload, Delete, Refresh } from '@mui/icons-material';
import { useMutation } from '@tanstack/react-query';
import { uploadMedia, deleteMedia } from '../api/client';

interface MediaUploaderProps {
  value?: string;
  onChange: (url: string | undefined) => void;
  folder?: string;
  accept?: string;
  label?: string;
}

export default function MediaUploader({
  value,
  onChange,
  folder = 'media',
  accept = 'image/*',
  label,
}: MediaUploaderProps) {
  const [error, setError] = useState<string | null>(null);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);

  const uploadMutation = useMutation({
    mutationFn: (file: File) => uploadMedia(file, folder),
    onSuccess: (url) => {
      onChange(url);
      setError(null);
    },
    onError: (err: Error) => {
      setError(err.message || 'Ошибка загрузки');
    },
  });

  const deleteMutation = useMutation({
    mutationFn: deleteMedia,
    onSuccess: () => {
      onChange(undefined);
      setDeleteDialogOpen(false);
    },
  });

  const onDrop = useCallback(
    (acceptedFiles: File[]) => {
      if (acceptedFiles.length > 0) {
        uploadMutation.mutate(acceptedFiles[0]);
      }
    },
    [uploadMutation]
  );

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    accept: accept === 'image/*' ? { 'image/*': [] } : { 'audio/*': [] },
    maxFiles: 1,
    disabled: uploadMutation.isPending,
  });

  const handleDeleteClick = () => {
    setDeleteDialogOpen(true);
  };

  const handleConfirmDelete = () => {
    if (value) {
      deleteMutation.mutate(value);
    }
  };

  const handleCancelDelete = () => {
    setDeleteDialogOpen(false);
  };

  const handleReplace = () => {
    // Just open the file picker by clicking the dropzone
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = accept === 'image/*' ? 'image/*' : 'audio/*';
    input.onchange = (e) => {
      const file = (e.target as HTMLInputElement).files?.[0];
      if (file) {
        uploadMutation.mutate(file);
      }
    };
    input.click();
  };

  if (value) {
    const isImage = accept === 'image/*';
    
    return (
      <>
        <Box
          sx={{
            position: 'relative',
            borderRadius: 2,
            overflow: 'hidden',
            bgcolor: 'grey.100',
            border: '1px solid',
            borderColor: 'divider',
          }}
        >
          {label && (
            <Typography
              variant="caption"
              sx={{
                position: 'absolute',
                top: 8,
                left: 8,
                bgcolor: 'background.paper',
                px: 1,
                py: 0.5,
                borderRadius: 1,
                zIndex: 1,
              }}
            >
              {label}
            </Typography>
          )}

          {isImage ? (
            <Box
              component="img"
              src={value}
              alt="Uploaded"
              sx={{
                width: '100%',
                height: 200,
                objectFit: 'cover',
              }}
            />
          ) : (
            <Box sx={{ p: 2, pt: label ? 4 : 2 }}>
              <audio controls style={{ width: '100%' }}>
                <source src={value} />
                Ваш браузер не поддерживает аудио.
              </audio>
            </Box>
          )}

          {/* Action buttons overlay */}
          <Box
            sx={{
              position: 'absolute',
              top: 8,
              right: 8,
              display: 'flex',
              gap: 1,
            }}
          >
            <Tooltip title="Заменить файл">
              <IconButton
                onClick={handleReplace}
                disabled={uploadMutation.isPending}
                sx={{
                  bgcolor: 'background.paper',
                  '&:hover': { bgcolor: 'primary.light', color: 'white' },
                }}
                size="small"
              >
                {uploadMutation.isPending ? (
                  <CircularProgress size={20} />
                ) : (
                  <Refresh fontSize="small" />
                )}
              </IconButton>
            </Tooltip>

            <Tooltip title="Удалить файл">
              <IconButton
                onClick={handleDeleteClick}
                disabled={deleteMutation.isPending}
                sx={{
                  bgcolor: 'background.paper',
                  '&:hover': { bgcolor: 'error.light', color: 'white' },
                }}
                size="small"
              >
                {deleteMutation.isPending ? (
                  <CircularProgress size={20} />
                ) : (
                  <Delete fontSize="small" />
                )}
              </IconButton>
            </Tooltip>
          </Box>

          {/* File URL display */}
          <Box
            sx={{
              position: 'absolute',
              bottom: 0,
              left: 0,
              right: 0,
              bgcolor: 'rgba(0, 0, 0, 0.6)',
              color: 'white',
              p: 1,
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
            }}
          >
            <Typography
              variant="caption"
              sx={{
                overflow: 'hidden',
                textOverflow: 'ellipsis',
                whiteSpace: 'nowrap',
                maxWidth: '70%',
              }}
            >
              {value.split('/').pop()}
            </Typography>
            <Typography variant="caption" color="success.light">
              ✓ Загружено
            </Typography>
          </Box>
        </Box>

        {/* Delete confirmation dialog */}
        <Dialog
          open={deleteDialogOpen}
          onClose={handleCancelDelete}
          maxWidth="xs"
          fullWidth
        >
          <DialogTitle>Подтвердите удаление</DialogTitle>
          <DialogContent>
            <DialogContentText>
              Вы уверены, что хотите удалить этот файл? Это действие нельзя отменить.
            </DialogContentText>
          </DialogContent>
          <DialogActions>
            <Button onClick={handleCancelDelete}>Отмена</Button>
            <Button
              onClick={handleConfirmDelete}
              color="error"
              variant="contained"
              disabled={deleteMutation.isPending}
            >
              {deleteMutation.isPending ? (
                <CircularProgress size={20} />
              ) : (
                'Удалить'
              )}
            </Button>
          </DialogActions>
        </Dialog>
      </>
    );
  }

  return (
    <Box
      {...getRootProps()}
      sx={{
        border: '2px dashed',
        borderColor: isDragActive ? 'primary.main' : 'grey.300',
        borderRadius: 2,
        p: 3,
        textAlign: 'center',
        cursor: 'pointer',
        bgcolor: isDragActive ? 'primary.light' : 'grey.50',
        transition: 'all 0.2s',
        '&:hover': {
          borderColor: 'primary.main',
          bgcolor: 'primary.light',
        },
      }}
    >
      <input {...getInputProps()} />
      {uploadMutation.isPending ? (
        <CircularProgress size={32} />
      ) : (
        <>
          <CloudUpload sx={{ fontSize: 40, color: 'grey.500', mb: 1 }} />
          <Typography color="text.secondary">
            {isDragActive
              ? 'Отпустите файл'
              : label || 'Перетащите файл или кликните для выбора'}
          </Typography>
          <Typography variant="caption" color="text.secondary" display="block" mt={1}>
            {accept === 'image/*' ? 'PNG, JPG, GIF до 50 МБ' : 'MP3, WAV, OGG до 50 МБ'}
          </Typography>
        </>
      )}
      {error && (
        <Typography color="error" variant="body2" mt={1}>
          {error}
        </Typography>
      )}
    </Box>
  );
}
