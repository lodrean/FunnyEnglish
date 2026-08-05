import type { Meta, StoryObj } from '@storybook/react';
import SubmissionAudioPlayer from './SubmissionAudioPlayer';

const meta: Meta<typeof SubmissionAudioPlayer> = {
  title: 'Speaking/Components/SubmissionAudioPlayer',
  component: SubmissionAudioPlayer,
  args: {
    audioUrl: 'https://example.com/audio/sub-1.m4a',
    durationSeconds: 30,
  },
};
export default meta;
type Story = StoryObj<typeof meta>;

/** URL недоступен в Storybook → при нажатии Play покажется error + retry + Download (это и есть fallback) */
export const Default: Story = {};
