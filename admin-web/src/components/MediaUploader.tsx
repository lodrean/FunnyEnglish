import { useCallback, useEffect, useState } from 'react';
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
  Link,
} from '@mui/material';
import { CloudUpload, Delete, Refresh, InsertDriveFile } from '@mui/icons-material';
import { useMutation } from '@tanstack/react-query';
import { uploadMedia, deleteMedia } from '../api/client';
import { extractVttTranscript } from '../utils/vtt';

type MediaKind = 'image' | 'audio' | 'video' | 'file';

interface MediaUploaderProps {
  value?: string;
  onChange: (url: string | undefined) => void;
  folder?: string;
  /** Раньше: 'image/*' | 'audio/*'. Теперь любая accept-строка ('video/*', '.vtt', ...) */
  accept?: string;
  /** Явный тип для превью; по умолчанию выводится из accept */
  mediaKind?: MediaKind;
  label?: string;
  /** Подпись под dropzone, напр. 'MP4, WebM до 50 МБ' */
  hint?: string;
}

const MAX_FILE_SIZE = 50 * 1024 * 1024; // 50 МБ (nginx client_max_body_size, memory.md №5)

/** Маппинг accept-строки в формат react-dropzone */
const toDropzoneAccept = (accept: string): Record<string, string[]> => {
  if (accept === 'image/*') return { 'image/*': [] };
  if (accept === 'audio/*') return { 'audio/*': [] };
  if (accept === 'video/*') return { 'video/*': [] };
  if (accept === '.vtt') return { 'text/vtt': ['.vtt'] };
  return { '*/*': accept.split(',').map((ext) => ext.trim()).filter(Boolean) };
};

const deriveMediaKind = (accept: string): MediaKind => {
  if (accept === 'image/*') return 'image';
  if (accept === 'audio/*') return 'audio';
  if (accept === 'video/*') return 'video';
  return 'file';
};

const defaultHint = (kind: MediaKind, accept: string): string => {
  switch (kind) {
    case 'image':
      return 'PNG, JPG, GIF до 50 МБ';
    case 'audio':
      return 'MP3, WAV, OGG до 50 МБ';
    case 'video':
      return 'MP4, WebM до 50 МБ';
    default:
      return accept === '.vtt' ? 'WebVTT (.vtt)' : 'Файл до 50 МБ';
  }
};

/** Превью текстового файла (.vtt): полный текст транскрипта — его видит ученик в приложении */
function FilePreview({ url }: { url: string }) {
  const [head, setHead] = useState<string[] | null>(null);
  const [transcript, setTranscript] = useState<string | null>(null);
  const isVtt = url.split('?')[0].toLowerCase().endsWith('.vtt');

  useEffect(() => {
    let cancelled = false;
    fetch(url)
      .then((r) => (r.ok ? r.text() : Promise.reject(new Error(String(r.status)))))
      .then((text) => {
        if (cancelled) return;
        setHead(text.split('\n').slice(0, 3));
        setTranscript(extractVttTranscript(text));
      })
      .catch(() => {
        if (!cancelled) {
          setHead(null);
          setTranscript(null);
        }
      });
    return () => {
      cancelled = true;
    };
  }, [url]);

  return (
    <Box sx={{ p: 2, pt: 5, display: 'flex', alignItems: 'flex-start', gap: 1.5 }}>
      <InsertDriveFile color="action" />
      <Box sx={{ minWidth: 0, flex: 1 }}>
        <Link href={url} target="_blank" rel="noopener" underline="hover">
          {url.split('/').pop()}
        </Link>
        {isVtt && transcript ? (
          <>
            <Typography variant="caption" color="text.secondary" sx={{ mt: 0.5, display: 'block' }}>
              Полный текст видео (из субтитров — показывается ученику):
            </Typography>
            <Typography
              variant="caption"
              component="pre"
              color="text.secondary"
              data-testid="vtt-transcript-preview"
              sx={{
                mt: 0.5,
                whiteSpace: 'pre-wrap',
                wordBreak: 'break-word',
                m: 0,
                maxHeight: 140,
                overflowY: 'auto',
                display: 'block',
              }}
            >
              {transcript}
            </Typography>
          </>
        ) : (
          head && (
            <Typography
              variant="caption"
              component="pre"
              color="text.secondary"
              sx={{ mt: 0.5, whiteSpace: 'pre-wrap', wordBreak: 'break-word', m: 0 }}
            >
              {head.join('\n')}
            </Typography>
          )
        )}
      </Box>
    </Box>
  );
}

export default function MediaUploader({
  value,
  onChange,
  folder = 'media',
  accept = 'image/*',
  mediaKind,
  label,
  hint,
}: MediaUploaderProps) {
  const [error, setError] = useState<string | null>(null);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);

  const kind: MediaKind = mediaKind ?? deriveMediaKind(accept);

  /** Client-side валидация до upload: размер (≤50 МБ) и WebVTT-заголовок для .vtt */
  const validateFile = async (file: File): Promise<string | null> => {
    if (file.size > MAX_FILE_SIZE) {
      return `Файл слишком большой (${Math.ceil(file.size / 1024 / 1024)} МБ). Максимум 50 МБ.`;
    }
    if (accept === '.vtt') {
      if (!file.name.toLowerCase().endsWith('.vtt')) {
        return 'Нужен файл субтитров в формате .vtt';
      }
      const text = await file.text();
      if (!text.startsWith('WEBVTT')) {
        return 'Невалидный WebVTT: файл должен начинаться с «WEBVTT»';
      }
    }
    return null;
  };

  const uploadMutation = useMutation({
    mutationFn: async (file: File) => {
      const validationError = await validateFile(file);
      if (validationError) throw new Error(validationError);
      return uploadMedia(file, folder);
    },
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
    accept: toDropzoneAccept(accept),
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
    input.accept = accept;
    input.onchange = (e) => {
      const file = (e.target as HTMLInputElement).files?.[0];
      if (file) {
        uploadMutation.mutate(file);
      }
    };
    input.click();
  };

  if (value) {
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

          {kind === 'image' && (
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
          )}
          {kind === 'audio' && (
            <Box sx={{ p: 2, pt: label ? 4 : 2 }}>
              <audio controls style={{ width: '100%' }}>
                <source src={value} />
                Ваш браузер не поддерживает аудио.
              </audio>
            </Box>
          )}
          {kind === 'video' && (
            <Box sx={{ p: 2, pt: label ? 4 : 2 }}>
              {/* preload="metadata" — не тянуть 50+ МБ при открытии редактора */}
              <video controls preload="metadata" style={{ width: '100%', maxHeight: 240 }}>
                <source src={value} />
                Ваш браузер не поддерживает видео.
              </video>
            </Box>
          )}
          {kind === 'file' && <FilePreview url={value} />}

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
          {kind !== 'file' && (
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
          )}
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
            {hint ?? defaultHint(kind, accept)}
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
