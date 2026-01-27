import { useCallback, useState } from 'react';
import { useDropzone } from 'react-dropzone';
import {
  Box,
  Typography,
  CircularProgress,
  IconButton,
} from '@mui/material';
import { CloudUpload, Delete } from '@mui/icons-material';
import { useMutation } from '@tanstack/react-query';
import { uploadMedia, deleteMedia } from '../api/client';

interface MediaUploaderProps {
  value?: string;
  onChange: (url: string | undefined) => void;
  folder?: string;
  accept?: string;
}

export default function MediaUploader({
  value,
  onChange,
  folder = 'media',
  accept = 'image/*',
}: MediaUploaderProps) {
  const [error, setError] = useState<string | null>(null);

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

  const handleDelete = () => {
    if (value) {
      deleteMutation.mutate(value);
    }
  };

  if (value) {
    return (
      <Box
        sx={{
          position: 'relative',
          borderRadius: 2,
          overflow: 'hidden',
          bgcolor: 'grey.100',
        }}
      >
        {accept === 'image/*' ? (
          <Box
            component="img"
            src={value}
            alt="Uploaded"
            sx={{
              width: '100%',
              height: 150,
              objectFit: 'cover',
            }}
          />
        ) : (
          <Box
            component="audio"
            controls
            src={value}
            sx={{ width: '100%', p: 2 }}
          />
        )}
        <IconButton
          onClick={handleDelete}
          disabled={deleteMutation.isPending}
          sx={{
            position: 'absolute',
            top: 8,
            right: 8,
            bgcolor: 'background.paper',
            '&:hover': { bgcolor: 'error.light', color: 'white' },
          }}
        >
          {deleteMutation.isPending ? (
            <CircularProgress size={20} />
          ) : (
            <Delete />
          )}
        </IconButton>
      </Box>
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
              : 'Перетащите файл или кликните для выбора'}
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
