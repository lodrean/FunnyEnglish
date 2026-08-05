import type { Meta, StoryObj } from '@storybook/react';
import SpeakingTopics from '../SpeakingTopics';
import { allTopicsQuery, librariesQuery, withSpeakingMocks } from '../../stories/speakingMocks';

const meta: Meta<typeof SpeakingTopics> = {
  title: 'Speaking/Screens/SpeakingTopics',
  component: SpeakingTopics,
};
export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  decorators: [withSpeakingMocks({ queries: [librariesQuery(), allTopicsQuery()] })],
};

/** Тем нет вообще → EmptyState с CTA «Сначала создайте тему» */
export const NoLibraries: Story = {
  decorators: [withSpeakingMocks({ queries: [librariesQuery([]), allTopicsQuery([])] })],
};
