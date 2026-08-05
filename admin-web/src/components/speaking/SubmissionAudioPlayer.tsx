import { useEffect, useRef, useState } from 'react';
import {
  Alert,
  Box,
  IconButton,
  MenuItem,
  Paper,
  Select,
  Typography,
  useTheme,
} from '@mui/material';
import {
  Download as DownloadIcon,
  Pause as PauseIcon,
  PlayArrow as PlayIcon,
  Replay as ReplayIcon,
} from '@mui/icons-material';
import { formatMmSs } from '../../utils/format';

const SPEEDS = [0.75, 1, 1.25, 1.5];

/**
 * Декоративный waveform (мокап frame-grading, .player-wave): 56 баров,
 * высоты детерминированы той же формулой, что и в mockups.html (adminWave).
 */
const WAVE_BARS = 56;
const WAVE_HEIGHTS = Array.from(
  { length: WAVE_BARS },
  (_, i) => 6 + Math.round(34 * Math.abs(Math.sin(i * 0.45 + 1)))
);
// tokens.css --color-surface-variant (невоспроизведённые бары)
const WAVE_BAR_UNPLAYED = '#D8E2FA';

interface SubmissionAudioPlayerProps {
  audioUrl: string;
  /** Длительность из API-ответа (НЕ из audio.duration — может быть Infinity/NaN до loadedmetadata) */
  durationSeconds: number;
}

/**
 * Плеер записи ученика (Grading): скрытый HTMLAudioElement + waveform-плеер
 * по мокапу frame-grading (56 баров, played/unplayed, метки current/total).
 * AAC/m4a играют Chrome/Edge/Safari из коробки (спека §6.3); скачивание — иконкой.
 */
export default function SubmissionAudioPlayer({
  audioUrl,
  durationSeconds,
}: SubmissionAudioPlayerProps) {
  const theme = useTheme();
  const audioRef = useRef<HTMLAudioElement | null>(null);
  const [isPlaying, setIsPlaying] = useState(false);
  const [currentTime, setCurrentTime] = useState(0);
  const [speed, setSpeed] = useState(1);
  const [error, setError] = useState(false);
  // cache-buster при retry (спека §6.3)
  const [retrySeed, setRetrySeed] = useState(0);

  const src = retrySeed > 0 ? `${audioUrl}${audioUrl.includes('?') ? '&' : '?'}t=${retrySeed}` : audioUrl;

  useEffect(() => {
    const audio = audioRef.current;
    if (!audio) return;

    const onTimeUpdate = () => setCurrentTime(audio.currentTime);
    const onEnded = () => {
      setIsPlaying(false);
      setCurrentTime(0);
    };
    const onError = () => {
      setIsPlaying(false);
      setError(true);
    };

    audio.addEventListener('timeupdate', onTimeUpdate);
    audio.addEventListener('ended', onEnded);
    audio.addEventListener('error', onError);
    return () => {
      audio.removeEventListener('timeupdate', onTimeUpdate);
      audio.removeEventListener('ended', onEnded);
      audio.removeEventListener('error', onError);
      audio.pause();
    };
  }, [src]);

  const togglePlay = () => {
    const audio = audioRef.current;
    if (!audio) return;
    if (isPlaying) {
      audio.pause();
      setIsPlaying(false);
    } else {
      audio
        .play()
        .then(() => setIsPlaying(true))
        .catch(() => setError(true));
    }
  };

  const handleWaveSeek = (e: React.MouseEvent<HTMLDivElement>) => {
    const audio = audioRef.current;
    if (!audio || durationSeconds <= 0) return;
    const rect = e.currentTarget.getBoundingClientRect();
    const ratio = Math.min(1, Math.max(0, (e.clientX - rect.left) / rect.width));
    const next = ratio * durationSeconds;
    audio.currentTime = next;
    setCurrentTime(next);
  };

  const handleSpeed = (value: number) => {
    setSpeed(value);
    if (audioRef.current) audioRef.current.playbackRate = value;
  };

  const handleRetry = () => {
    setError(false);
    setCurrentTime(0);
    setRetrySeed((s) => s + 1);
  };

  // played-бары по прогрессу (мокап: i < playedCount → .played)
  const playedBars =
    durationSeconds > 0 ? Math.floor((currentTime / durationSeconds) * WAVE_BARS) : 0;

  return (
    <Paper sx={{ p: 2 }} data-testid="submission-audio-player">
      <audio ref={audioRef} src={src} preload="metadata" />

      {error ? (
        <Alert
          severity="error"
          action={
            <IconButton color="inherit" size="small" onClick={handleRetry} aria-label="retry">
              <ReplayIcon />
            </IconButton>
          }
        >
          Не удалось загрузить аудио. Попробуйте ещё раз или скачайте запись.
        </Alert>
      ) : (
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          {/* .player-play: круг 56px primary */}
          <IconButton
            onClick={togglePlay}
            data-testid="audio-play-button"
            aria-label={isPlaying ? 'Pause' : 'Play'}
            sx={{
              width: 56,
              height: 56,
              flexShrink: 0,
              bgcolor: 'primary.main',
              color: '#fff',
              '&:hover': { bgcolor: 'primary.dark' },
            }}
          >
            {isPlaying ? <PauseIcon /> : <PlayIcon />}
          </IconButton>

          {/* .player-track: waveform 40px + метки времени */}
          <Box sx={{ flex: 1, minWidth: 0 }}>
            <Box
              onClick={handleWaveSeek}
              data-testid="audio-waveform"
              sx={{
                display: 'flex',
                alignItems: 'center',
                gap: '2px',
                height: 40,
                cursor: 'pointer',
              }}
            >
              {WAVE_HEIGHTS.map((h, i) => (
                <Box
                  key={i}
                  component="span"
                  data-played={i < playedBars ? 'true' : undefined}
                  sx={{
                    flex: 1,
                    height: h,
                    borderRadius: '2px',
                    bgcolor:
                      i < playedBars
                        ? theme.palette.speaking.waveformPlayback
                        : WAVE_BAR_UNPLAYED,
                    transition: 'background-color 0.15s ease',
                  }}
                />
              ))}
            </Box>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', mt: 0.5 }}>
              <Typography
                variant="caption"
                color="text.secondary"
                data-testid="audio-current-time"
              >
                {formatMmSs(currentTime)}
              </Typography>
              <Typography variant="caption" color="text.secondary">
                {formatMmSs(durationSeconds)}
              </Typography>
            </Box>
          </Box>

          <Select
            value={speed}
            onChange={(e) => handleSpeed(Number(e.target.value))}
            size="small"
            data-testid="audio-speed-select"
            sx={{ minWidth: 76 }}
          >
            {SPEEDS.map((s) => (
              <MenuItem key={s} value={s}>
                {s}×
              </MenuItem>
            ))}
          </Select>

          {/* Скачивание — ненавязчиво, иконкой (мокап не показывает download-link) */}
          <IconButton
            component="a"
            href={audioUrl}
            download
            aria-label="Download audio"
            data-testid="download-audio-button"
            size="small"
            sx={{ color: 'text.secondary' }}
          >
            <DownloadIcon fontSize="small" />
          </IconButton>
        </Box>
      )}
    </Paper>
  );
}
