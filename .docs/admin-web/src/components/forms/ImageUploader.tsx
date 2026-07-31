import React, { useCallback, useState } from 'react';
import { useDropzone } from 'react-dropzone';
import {
  Box,
  Paper,
  Typography,
  Button,
  IconButton,
  LinearProgress,
  Fade,
  Tooltip,
} from '@mui/material';
import {
  CloudUpload,
  Delete,
  Image as ImageIcon,
  Refresh,
  Warning,
} from '@mui/icons-material';
import { Controller, Control, FieldValues, Path } from 'react-hook-form';

// Design System Colors
const colors = {
  primary: '#4A90D9',
  error: '#E53935',
  warning: '#FB8C00',
  success: '#43A047',
  textPrimary: '#212121',
  textSecondary: '#757575',
  background: '#F5F5F5',
  card: '#FFFFFF',
  border: '#E0E0E0',
};

// Image uploader props
export interface ImageUploaderProps<T extends FieldValues> {
  /** Field name - must match form schema */
  name: Path<T>;
  /** react-hook-form control instance */
  control: Control<T>;
  /** Field label */
  label?: string;
  /** Accepted file types */
  accept?: Record<string, string[]>;
  /** Maximum file size in MB */
  maxSizeMB?: number;
  /** Image preview height */
  previewHeight?: number | string;
  /** Image preview width */
  previewWidth?: number | string;
  /** Helper text displayed below uploader */
  helperText?: string;
  /** Whether field is required */
  required?: boolean;
  /** Whether field is disabled */
  disabled?: boolean;
  /** Custom class name */
  className?: string;
  /** Callback when image is uploaded */
  onUpload?: (file: File, previewUrl: string) => void | Promise<void>;
  /** Callback when image is removed */
  onRemove?: () => void;
}

/**
 * Image Uploader Component
 * 
 * A drag-and-drop image upload component with preview functionality.
 * Validates file size and type, shows upload progress.
 * 
 * @example
 * ```tsx
 * <ImageUploader
 *   name="avatar"
 *   control={control}
 *   label="Profile Picture"
 *   maxSizeMB={5}
 *   previewHeight={200}
 *   onUpload={(file, url) => console.log('Uploaded:', file.name)}
 * />
 * ```
 */
export function ImageUploader<T extends FieldValues>({
  name,
  control,
  label = 'Upload Image',
  accept = { 'image/*': [] },
  maxSizeMB = 5,
  previewHeight = 200,
  previewWidth = '100%',
  helperText,
  required = false,
  disabled = false,
  className,
  onUpload,
  onRemove,
}: ImageUploaderProps<T>): React.ReactElement {
  
  const maxSizeBytes = maxSizeMB * 1024 * 1024;

  return (
    <Controller
      name={name}
      control={control}
      rules={{
        required: required ? `${label} is required` : false,
      }}
      render={({ field, fieldState: { error } }) => (
        <Box className={className}>
          {label && (
            <Typography
              variant="subtitle2"
              component="label"
              sx={{
                display: 'block',
                mb: 1,
                color: error ? colors.error : colors.textPrimary,
                fontWeight: 500,
              }}
            >
              {label}
              {required && (
                <Typography
                  component="span"
                  sx={{ color: colors.error, ml: 0.5 }}
                >
                  *
                </Typography>
              )}
            </Typography>
          )}

          <ImageUploaderContent
            value={field.value}
            onChange={field.onChange}
            accept={accept}
            maxSizeBytes={maxSizeBytes}
            maxSizeMB={maxSizeMB}
            previewHeight={previewHeight}
            previewWidth={previewWidth}
            disabled={disabled}
            error={error?.message}
            onUpload={onUpload}
            onRemove={onRemove}
          />

          {helperText && !error && (
            <Typography
              variant="caption"
              sx={{
                display: 'block',
                mt: 1,
                color: colors.textSecondary,
              }}
            >
              {helperText}
            </Typography>
          )}

          {error && (
            <Typography
              variant="caption"
              sx={{
                display: 'flex',
                alignItems: 'center',
                gap: 0.5,
                mt: 1,
                color: colors.error,
              }}
            >
              <Warning fontSize="small" />
              {error.message}
            </Typography>
          )}
        </Box>
      )}
    />
  );
}

// Internal component for uploader content
interface ImageUploaderContentProps {
  value: File | string | null | undefined;
  onChange: (value: File | string | null) => void;
  accept: Record<string, string[]>;
  maxSizeBytes: number;
  maxSizeMB: number;
  previewHeight: number | string;
  previewWidth: number | string;
  disabled: boolean;
  error?: string;
  onUpload?: (file: File, previewUrl: string) => void | Promise<void>;
  onRemove?: () => void;
}

function ImageUploaderContent({
  value,
  onChange,
  accept,
  maxSizeBytes,
  maxSizeMB,
  previewHeight,
  previewWidth,
  disabled,
  error,
  onUpload,
  onRemove,
}: ImageUploaderContentProps): React.ReactElement {
  
  const [previewUrl, setPreviewUrl] = useState<string | null>(
    typeof value === 'string' ? value : null
  );
  const [isUploading, setIsUploading] = useState(false);
  const [uploadProgress, setUploadProgress] = useState(0);
  const [fileError, setFileError] = useState<string | null>(null);

  // Create preview URL for File objects
  React.useEffect(() => {
    if (value instanceof File) {
      const url = URL.createObjectURL(value);
      setPreviewUrl(url);
      return () => URL.revokeObjectURL(url);
    } else if (typeof value === 'string') {
      setPreviewUrl(value);
    } else {
      setPreviewUrl(null);
    }
  }, [value]);

  const onDrop = useCallback(async (acceptedFiles: File[], rejectedFiles: unknown[]) => {
    setFileError(null);

    // Handle rejected files
    if (rejectedFiles.length > 0) {
      const rejected = rejectedFiles as Array<{ file: File; errors: Array<{ code: string; message: string }> }>;
      const firstError = rejected[0]?.errors[0];
      if (firstError?.code === 'file-too-large') {
        setFileError(`File is too large. Maximum size is ${maxSizeMB}MB.`);
      } else if (firstError?.code === 'file-invalid-type') {
        setFileError('Invalid file type. Please upload an image.');
      } else {
        setFileError(firstError?.message || 'Invalid file');
      }
      return;
    }

    if (acceptedFiles.length === 0) return;

    const file = acceptedFiles[0];
    
    // Simulate upload progress
    setIsUploading(true);
    setUploadProgress(0);

    const progressInterval = setInterval(() => {
      setUploadProgress((prev) => {
        if (prev >= 90) {
          clearInterval(progressInterval);
          return 90;
        }
        return prev + 10;
      });
    }, 100);

    try {
      // Create preview URL
      const url = URL.createObjectURL(file);
      
      // Call onUpload callback if provided
      if (onUpload) {
        await onUpload(file, url);
      }

      // Update form value
      onChange(file);
      setPreviewUrl(url);
      
      setUploadProgress(100);
      
      // Reset progress after a delay
      setTimeout(() => {
        setIsUploading(false);
        setUploadProgress(0);
      }, 500);
    } catch (err) {
      setFileError('Upload failed. Please try again.');
      setIsUploading(false);
      setUploadProgress(0);
    } finally {
      clearInterval(progressInterval);
    }
  }, [maxSizeMB, onChange, onUpload]);

  const { getRootProps, getInputProps, isDragActive, isDragAccept, isDragReject } = useDropzone({
    onDrop,
    accept,
    maxSize: maxSizeBytes,
    multiple: false,
    disabled: disabled || isUploading,
  });

  const handleRemove = useCallback(() => {
    if (previewUrl && value instanceof File) {
      URL.revokeObjectURL(previewUrl);
    }
    onChange(null);
    setPreviewUrl(null);
    setFileError(null);
    onRemove?.();
  }, [onChange, onRemove, previewUrl, value]);

  const handleReplace = useCallback(() => {
    // Trigger file input click
    const input = document.getElementById('image-replace-input') as HTMLInputElement;
    input?.click();
  }, []);

  const displayError = error || fileError;

  // Show preview if image is uploaded
  if (previewUrl) {
    return (
      <Paper
        variant="outlined"
        sx={{
          position: 'relative',
          overflow: 'hidden',
          borderColor: displayError ? colors.error : colors.border,
          borderWidth: displayError ? 2 : 1,
        }}
      >
        {/* Image Preview */}
        <Box
          sx={{
            width: previewWidth,
            height: previewHeight,
            position: 'relative',
            backgroundColor: colors.background,
          }}
        >
          <img
            src={previewUrl}
            alt="Preview"
            style={{
              width: '100%',
              height: '100%',
              objectFit: 'contain',
            }}
          />

          {/* Overlay Actions */}
          <Fade in>
            <Box
              sx={{
                position: 'absolute',
                top: 0,
                left: 0,
                right: 0,
                bottom: 0,
                backgroundColor: 'rgba(0, 0, 0, 0.5)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                gap: 2,
                opacity: 0,
                transition: 'opacity 0.2s',
                '&:hover': {
                  opacity: 1,
                },
              }}
            >
              <Tooltip title="Replace Image">
                <IconButton
                  onClick={handleReplace}
                  disabled={disabled}
                  sx={{
                    backgroundColor: colors.card,
                    color: colors.primary,
                    '&:hover': {
                      backgroundColor: colors.primary,
                      color: colors.card,
                    },
                  }}
                >
                  <Refresh />
                </IconButton>
              </Tooltip>
              <Tooltip title="Remove Image">
                <IconButton
                  onClick={handleRemove}
                  disabled={disabled}
                  sx={{
                    backgroundColor: colors.card,
                    color: colors.error,
                    '&:hover': {
                      backgroundColor: colors.error,
                      color: colors.card,
                    },
                  }}
                >
                  <Delete />
                </IconButton>
              </Tooltip>
            </Box>
          </Fade>
        </Box>

        {/* Hidden input for replace functionality */}
        <input
          id="image-replace-input"
          type="file"
          accept="image/*"
          style={{ display: 'none' }}
          onChange={(e) => {
            const file = e.target.files?.[0];
            if (file) {
              onDrop([file], []);
            }
          }}
        />

        {/* File Info */}
        <Box
          sx={{
            p: 1.5,
            backgroundColor: colors.background,
            borderTop: `1px solid ${colors.border}`,
          }}
        >
          <Typography variant="caption" color={colors.textSecondary}>
            {value instanceof File ? value.name : 'Uploaded image'}
          </Typography>
        </Box>
      </Paper>
    );
  }

  // Show dropzone when no image
  return (
    <Paper
      {...getRootProps()}
      variant="outlined"
      sx={{
        p: 4,
        textAlign: 'center',
        cursor: disabled ? 'not-allowed' : 'pointer',
        borderColor: displayError
          ? colors.error
          : isDragAccept
          ? colors.success
          : isDragReject
          ? colors.error
          : isDragActive
          ? colors.primary
          : colors.border,
        borderWidth: displayError || isDragAccept || isDragReject || isDragActive ? 2 : 1,
        borderStyle: 'dashed',
        backgroundColor: isDragActive
          ? 'rgba(74, 144, 217, 0.05)'
          : disabled
          ? colors.background
          : colors.card,
        transition: 'all 0.2s ease',
        opacity: disabled ? 0.6 : 1,
        '&:hover': {
          borderColor: disabled ? colors.border : colors.primary,
          backgroundColor: disabled ? colors.background : 'rgba(74, 144, 217, 0.05)',
        },
      }}
    >
      <input {...getInputProps()} />

      <CloudUpload
        sx={{
          fontSize: 48,
          color: isDragActive
            ? colors.primary
            : displayError
            ? colors.error
            : colors.textSecondary,
          mb: 2,
        }}
      />

      <Typography
        variant="body1"
        sx={{
          color: isDragActive ? colors.primary : colors.textPrimary,
          fontWeight: 500,
          mb: 1,
        }}
      >
        {isDragActive
          ? 'Drop the image here...'
          : 'Drag & drop an image here, or click to select'}
      </Typography>

      <Typography variant="caption" color={colors.textSecondary}>
        Maximum file size: {maxSizeMB}MB
      </Typography>

      {isUploading && (
        <Box sx={{ mt: 2 }}>
          <LinearProgress
            variant="determinate"
            value={uploadProgress}
            sx={{
              height: 6,
              borderRadius: 3,
              backgroundColor: colors.border,
              '& .MuiLinearProgress-bar': {
                backgroundColor: colors.primary,
                borderRadius: 3,
              },
            }}
          />
          <Typography variant="caption" color={colors.textSecondary} sx={{ mt: 0.5, display: 'block' }}>
            Uploading... {uploadProgress}%
          </Typography>
        </Box>
      )}
    </Paper>
  );
}

export default ImageUploader;
