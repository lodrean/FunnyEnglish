import { useState, useRef } from 'react';
import {
  Box,
  TextField,
  Slider,
  Typography,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Button,
  LinearProgress,
  Alert,
  Paper,
} from '@mui/material';
import { Upload as UploadIcon } from '@mui/icons-material';
import { useQuery } from '@tanstack/react-query';
import { AudioTestDetail } from '../../api/audioTestApi';
import { audioTestApi } from '../../api/audioTestApi';
import { categoryApi } from '../../api/categoryApi';
import { formatDuration } from '../../utils/format';

interface Props {
  data: Partial<AudioTestDetail>;
  onChange: (field: string, value: any) => void;
}

export default function AudioTestInfoForm({ data, onChange }: Props) {
  const [uploadProgress, setUploadProgress] = useState(0);
  const [uploadError, setUploadError] = useState<string | null>(null);
  const [isUploading, setIsUploading] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const { data: categories } = useQuery({
    queryKey: ['categories'],
    queryFn: () => categoryApi.getAll(),
  });

  const handleFileSelect = async (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (!file) return;

    // Validate file type
    if (!file.type.startsWith('audio/')) {
      setUploadError('Please select an audio file (MP3, WAV, M4A)');
      return;
    }

    // Validate file size (max 50MB)
    if (file.size > 50 * 1024 * 1024) {
      setUploadError('File size must be less than 50MB');
      return;
    }

    setIsUploading(true);
    setUploadError(null);
    setUploadProgress(0);

    try {
      const response = await audioTestApi.uploadAudio(file, setUploadProgress);
      onChange('audioFileUrl', response.data.url);
      if (response.data.durationSeconds) {
        onChange('durationSeconds', response.data.durationSeconds);
      }
    } catch (error: any) {
      setUploadError(error.response?.data?.message || 'Upload failed');
    } finally {
      setIsUploading(false);
    }
  };

  return (
    <Box sx={{ maxWidth: 800 }}>
      <input
        type="file"
        accept="audio/*"
        hidden
        ref={fileInputRef}
        onChange={handleFileSelect}
      />

      {uploadError && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {uploadError}
        </Alert>
      )}

      {/* Audio Upload Section */}
      <Paper sx={{ p: 3, mb: 3, textAlign: 'center' }}>
        {data.audioFileUrl ? (
          <Box>
            <Typography variant="subtitle1" gutterBottom>
              Audio File
            </Typography>
            <audio controls src={data.audioFileUrl} style={{ width: '100%' }} />
            <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
              Duration: {formatDuration(data.durationSeconds || 0)}
            </Typography>
            <Button
              variant="outlined"
              size="small"
              onClick={() => fileInputRef.current?.click()}
              sx={{ mt: 1 }}
            >
              Replace Audio
            </Button>
          </Box>
        ) : (
          <Box>
            <Button
              variant="outlined"
              startIcon={<UploadIcon />}
              onClick={() => fileInputRef.current?.click()}
              disabled={isUploading}
              size="large"
            >
              {isUploading ? 'Uploading...' : 'Upload Audio File'}
            </Button>
            <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
              Supported formats: MP3, WAV, M4A (max 50MB)
            </Typography>
          </Box>
        )}
        {isUploading && (
          <Box sx={{ mt: 2 }}>
            <LinearProgress variant="determinate" value={uploadProgress} />
            <Typography variant="body2" sx={{ mt: 1 }}>
              {uploadProgress}%
            </Typography>
          </Box>
        )}
      </Paper>

      <TextField
        fullWidth
        label="Title"
        value={data.title || ''}
        onChange={(e) => onChange('title', e.target.value)}
        margin="normal"
        required
      />

      <TextField
        fullWidth
        label="Description"
        value={data.description || ''}
        onChange={(e) => onChange('description', e.target.value)}
        margin="normal"
        multiline
        rows={3}
      />

      <Box sx={{ display: 'flex', gap: 2, mt: 2 }}>
        <FormControl fullWidth margin="normal">
          <InputLabel>Category</InputLabel>
          <Select
            value={data.category?.id || ''}
            onChange={(e) => {
              const category = categories?.data.find((c: any) => c.id === e.target.value);
              onChange('category', category || null);
            }}
            label="Category"
          >
            <MenuItem value="">
              <em>None</em>
            </MenuItem>
            {categories?.data.map((category: any) => (
              <MenuItem key={category.id} value={category.id}>
                {category.name}
              </MenuItem>
            ))}
          </Select>
        </FormControl>

        <FormControl fullWidth margin="normal">
          <InputLabel>Difficulty</InputLabel>
          <Select
            value={data.difficulty || 1}
            onChange={(e) => onChange('difficulty', e.target.value)}
            label="Difficulty"
          >
            {[1, 2, 3, 4, 5].map((level) => (
              <MenuItem key={level} value={level}>
                Level {level}
              </MenuItem>
            ))}
          </Select>
        </FormControl>
      </Box>

      <Box sx={{ mt: 3 }}>
        <Typography gutterBottom>
          Plays Limit (optional)
        </Typography>
        <Slider
          value={data.playsLimit || 0}
          onChange={(_, value) => onChange('playsLimit', value === 0 ? undefined : value)}
          min={0}
          max={20}
          step={1}
          marks={[
            { value: 0, label: 'Unlimited' },
            { value: 5, label: '5' },
            { value: 10, label: '10' },
            { value: 20, label: '20' },
          ]}
          valueLabelDisplay="auto"
        />
      </Box>
    </Box>
  );
}
