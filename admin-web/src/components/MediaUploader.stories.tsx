import type { Meta, StoryObj } from '@storybook/react';
import { action } from '@storybook/addon-actions';
import MediaUploader from './MediaUploader';

const meta: Meta<typeof MediaUploader> = {
  title: 'Speaking/Components/MediaUploader',
  component: MediaUploader,
  args: { onChange: action('onChange') },
};
export default meta;
type Story = StoryObj<typeof meta>;

/** Новая ветка: видео (video/*, mediaKind=video) — dropzone */
export const VideoDropzone: Story = {
  args: {
    accept: 'video/*',
    mediaKind: 'video',
    folder: 'speaking/videos',
    label: 'Видео топика',
    hint: 'MP4, WebM до 50 МБ',
  },
};

/** Видео загружено — превью <video preload="metadata"> */
export const VideoPreview: Story = {
  args: {
    ...VideoDropzone.args,
    value: 'https://example.com/videos/intro.mp4',
  },
};

/** Новая ветка: субтитры (.vtt, mediaKind=file) — dropzone */
export const SubtitlesDropzone: Story = {
  args: {
    accept: '.vtt',
    mediaKind: 'file',
    folder: 'speaking/subtitles',
    label: 'Субтитры (WebVTT)',
    hint: 'WebVTT (.vtt)',
  },
};

/** Существующее поведение: аудио (регрессия) */
export const AudioDropzone: Story = {
  args: {
    accept: 'audio/*',
    folder: 'media',
  },
};
