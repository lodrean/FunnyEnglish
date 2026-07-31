/**
 * ImageUploader Component - Drag & drop image upload with preview
 * Design System 2.0
 */

import React, { useState, useCallback, useEffect } from 'react';
import {
  Box,
  Button,
  Typography,
  IconButton,
  LinearProgress,
  alpha,
  useTheme,
} from '@mui/material';
import {
  CloudUpload as CloudUploadIcon,
  Delete as DeleteIcon,
  Image as ImageIcon,
  CheckCircle as CheckCircleIcon,
} from '@mui/icons-material';
import { useDropzone } from 'react-dropzone';

// =============================================================================
// TYPES
// =============================================================================

export interface ImageUploaderProps {
  value?: File | string;
  onChange: (file: File | null) => void;
  onPreviewChange?: (preview: string | null) => void;
  accept?: Record<string, string[]>;
  maxSize?: number; // MB
  minSize?: number; // MB
  label?: string;
  helperText?: string;
  disabled?: boolean;
  readOnly?: boolean;
  aspectRatio?: number; // e.g., 16/9, 4/3, 1
  previewWidth?: number;
  previewHeight?: number;
  showPreview?: boolean;
  uploadProgress?: number;
}

// =============================================================================
// MAIN COMPONENT
// =============================================================================

export const ImageUploader: React.FC<ImageUploaderProps> = ({
  value,
  onChange,
  onPreviewChange,
  accept = { 'image/*': ['.png', '.jpg', '.jpeg', '.gif', '.webp'] },
  maxSize = 5,
  minSize = 0,
  label = 'Upload Image',
  helperText,
  disabled = false,
  readOnly = false,
  aspectRatio,
  previewWidth = 200,
  previewHeight = 200,
  showPreview = true,
  uploadProgress,
}) => {
  const theme = useTheme();
  const [preview, setPreview] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  // Initialize preview from value
  useEffect(() => {
    if (typeof value === 'string') {
      setPreview(value);
    } else if (value instanceof File) {
      const reader = new FileReader();
      reader.onloadend = () => {
        setPreview(reader.result as string);
      };
      reader.readAsDataURL(value);
    } else {
      setPreview(null);
    }
  }, [value]);

  const onDrop = useCallback(
    (acceptedFiles: File[], rejectedFiles: any[]) => {
      setError(null);

      if (rejectedFiles.length > 0) {
        const rejection = rejectedFiles[0];
        if (rejection.errors[0].code === 'file-too-large') {
          setError(`File is too large. Max size is ${maxSize}MB.`);
        } else if (rejection.errors[0].code === 'file-too-small') {
          setError(`File is too small. Min size is ${minSize}MB.`);
        } else if (rejection.errors[0].code === 'file-invalid-type') {
          setError('Invalid file type. Please upload an image.');
        } else {
          setError(rejection.errors[0].message);
        }
        return;
      }

      const file = acceptedFiles[0];
      if (file) {
        onChange(file);
        const reader = new FileReader();
        reader.onloadend = () => {
          const previewUrl = reader.result as string;
          setPreview(previewUrl);
          onPreviewChange?.(previewUrl);
        };
        reader.readAsDataURL(file);
      }
    },
    [maxSize, minSize, onChange, onPreviewChange]
  );

  const { getRootProps, getInputProps, isDragActive, isDragAccept, isDragReject } =
    useDropzone({
      onDrop,
      accept,
      maxSize: maxSize * 1024 * 1024,
      minSize: minSize * 1024 * 1024,
      multiple: false,
      disabled: disabled || readOnly,
    });

  const handleRemove = () => {
    onChange(null);
    setPreview(null);
    setError(null);
    onPreviewChange?.(null);
  };

  // Preview mode
  if (preview && showPreview) {
    return (
      <Box
        sx={{
          position: 'relative',
          display: 'inline-flex',
          flexDirection: 'column',
          alignItems: 'flex-start',
        }}
      >
        <Box
          sx={{
            position: 'relative',
            width: previewWidth,
            height: aspectRatio
              ? previewWidth / aspectRatio
              : previewHeight,
            borderRadius: 2,
            overflow: 'hidden',
            boxShadow: theme.shadows[2],
            border: `2px solid ${
              error ? theme.palette.error.main : alpha(theme.palette.divider, 0.5)
            }`,
          }}
        >
          <img
            src={preview}
            alt="Preview"
            style={{
              width: '100%',
              height: '100%',
              objectFit: 'cover',
            }}
          />

          {/* Upload Progress */}
          {uploadProgress !== undefined && uploadProgress < 100 && (
            <Box
              sx={{
                position: 'absolute',
                bottom: 0,
                left: 0,
                right: 0,
                p: 1,
                backgroundColor: alpha(theme.palette.background.paper, 0.9),
              }}
            >
              <LinearProgress
                variant="determinate"
                value={uploadProgress}
                sx={{
                  height: 6,
                  borderRadius: 1,
                }}
              />
              <Typography
                variant="caption"
                sx={{
                  mt: 0.5,
                  display: 'block',
                  textAlign: 'center',
                }}
              >
                {uploadProgress}%
              </Typography>
            </Box>
          )}

          {/* Success indicator */}
          {uploadProgress === 100 && (
            <Box
              sx={{
                position: 'absolute',
                top: 8,
                right: 8,
                color: theme.palette.success.main,
                backgroundColor: alpha(theme.palette.background.paper, 0.9),
                borderRadius: '50%',
                p: 0.5,
              }}
            >
              <CheckCircleIcon fontSize="small" />
            </Box>
          )}
        </Box>

        {/* Actions */}
        {!readOnly && (
          <Box
            sx={{
              display: 'flex',
              gap: 1,
              mt: 1.5,
            }}
          >
            <Button
              variant="outlined"
              size="small"
              onClick={() => {
                const input = document.createElement('input');
                input.type = 'file';
                input.accept = Object.values(accept).flat().join(',');
                input.onchange = (e) => {
                  const file = (e.target as HTMLInputElement).files?.[0];
                  if (file) {
                    onDrop([file], []);
                  }
                };
                input.click();
              }}
            >
              Change
            </Button>
            <Button
              variant="outlined"
              size="small"
              color="error"
              onClick={handleRemove}
              startIcon={<DeleteIcon />}
            >
              Remove
            </Button>
          </Box>
        )}

        {/* Error */}
        {error && (
          <Typography
            variant="caption"
            color="error"
            sx={{ mt: 1 }}
          >
            {error}
          </Typography>
        )}
      </Box>
    );
  }

  // Upload mode
  return (
    <Box>
      <Box
        {...getRootProps()}
        sx={{
          border: '2px dashed',
          borderColor: isDragAccept
            ? 'success.main'
            : isDragReject
            ? 'error.main'
            : isDragActive
            ? 'primary.main'
            : alpha(theme.palette.divider, 0.5),
          borderRadius: 2,
          p: 4,
          textAlign: 'center',
          cursor: disabled ? 'not-allowed' : 'pointer',
          backgroundColor: isDragActive
            ? alpha(theme.palette.primary.main, 0.05)
            : alpha(theme.palette.action.hover, 0.02),
          opacity: disabled ? 0.5 : 1,
          transition: 'all 0.2s ease-in-out',
          '&:hover': !disabled && {
            borderColor: 'primary.main',
            backgroundColor: alpha(theme.palette.primary.main, 0.05),
          },
          minWidth: 280,
          minHeight: 180,
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
        }}
      >
        <input {...getInputProps()} />
        <CloudUploadIcon
          sx={{
            fontSize: 48,
            color: isDragActive ? 'primary.main' : 'text.secondary',
            mb: 2,
            transition: 'color 0.2s',
          }}
        />
        <Typography
          variant="h6"
          gutterBottom
          sx={{
            color: isDragActive ? 'primary.main' : 'text.primary',
            fontWeight: 500,
          }}
        >
          {isDragActive ? 'Drop the image here' : label}
        </Typography>
        <Typography variant="body2" color="text.secondary">
          Drag & drop or click to browse
        </Typography>
        <Typography
          variant="caption"
          color="text.disabled"
          display="block"
          sx={{ mt: 1 }}
        >
          Max size: {maxSize}MB
          {aspectRatio && ` • Aspect ratio: ${aspectRatio.toFixed(2)}:1`}
        </Typography>
      </Box>

      {/* Error */}
      {error && (
        <Typography variant="caption" color="error" sx={{ mt: 1, display: 'block' }}>
          {error}
        </Typography>
      )}

      {/* Helper text */}
      {helperText && !error && (
        <Typography
          variant="caption"
          color="text.secondary"
          sx={{ mt: 1, display: 'block' }}
        >
          {helperText}
        </Typography>
      )}
    </Box>
  );
};

export default ImageUploader;
