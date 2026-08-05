import { describe, it, expect, vi, beforeAll } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import SubmissionAudioPlayer from '../SubmissionAudioPlayer';
import { ThemeProvider } from '../../../theme/ThemeProvider';

const renderPlayer = (durationSeconds = 30) =>
  render(
    <ThemeProvider>
      <SubmissionAudioPlayer audioUrl="http://media/a.m4a" durationSeconds={durationSeconds} />
    </ThemeProvider>
  );

const getAudio = () => document.querySelector('audio')!;

beforeAll(() => {
  // jsdom не реализует HTMLMediaElement.play/pause
  vi.spyOn(window.HTMLMediaElement.prototype, 'play').mockResolvedValue(undefined);
  vi.spyOn(window.HTMLMediaElement.prototype, 'pause').mockImplementation(() => {});
});

describe('SubmissionAudioPlayer (waveform, мокап frame-grading)', () => {
  it('рендер: 56 баров waveform, метки 0:00 / 0:30, play-кнопка, скачивание иконкой', () => {
    renderPlayer();
    const wave = screen.getByTestId('audio-waveform');
    expect(wave.children).toHaveLength(56);
    expect(screen.getByTestId('audio-current-time')).toHaveTextContent('0:00');
    expect(screen.getByTestId('submission-audio-player')).toHaveTextContent('0:30');
    expect(screen.getByTestId('audio-play-button')).toHaveAttribute('aria-label', 'Play');

    const download = screen.getByTestId('download-audio-button');
    expect(download).toHaveAttribute('href', 'http://media/a.m4a');
    expect(download).toHaveAttribute('download');
    // старого текстового «Download audio»-линка и MUI-слайдера больше нет
    expect(screen.queryByTestId('download-audio-link')).not.toBeInTheDocument();
    expect(screen.queryByTestId('audio-seek-slider')).not.toBeInTheDocument();
  });

  it('play/pause переключает aria-label и вызывает HTMLAudioElement', async () => {
    renderPlayer();
    const button = screen.getByTestId('audio-play-button');
    fireEvent.click(button);
    expect(window.HTMLMediaElement.prototype.play).toHaveBeenCalled();
    expect(await screen.findByLabelText('Pause')).toBeInTheDocument();

    fireEvent.click(button);
    expect(window.HTMLMediaElement.prototype.pause).toHaveBeenCalled();
    expect(screen.getByTestId('audio-play-button')).toHaveAttribute('aria-label', 'Play');
  });

  it('прогресс окрашивает played-бары (0:12/0:30 → 22 бара) и обновляет метку времени', () => {
    renderPlayer();
    const audio = getAudio();
    Object.defineProperty(audio, 'currentTime', { value: 12, configurable: true });
    fireEvent(audio, new Event('timeupdate'));

    expect(screen.getByTestId('audio-current-time')).toHaveTextContent('0:12');
    const wave = screen.getByTestId('audio-waveform');
    expect(wave.querySelectorAll('[data-played="true"]')).toHaveLength(22);
  });

  it('ошибка загрузки: Alert + retry', () => {
    renderPlayer();
    fireEvent(getAudio(), new Event('error'));
    expect(screen.getByText(/Не удалось загрузить аудио/)).toBeInTheDocument();
    expect(screen.getByLabelText('retry')).toBeInTheDocument();
  });
});
