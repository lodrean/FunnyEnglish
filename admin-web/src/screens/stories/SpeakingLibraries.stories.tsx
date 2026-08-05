import type { Meta, StoryObj } from '@storybook/react';
import SpeakingLibraries from '../SpeakingLibraries';
import { librariesQuery, withSpeakingMocks } from '../../stories/speakingMocks';

const meta: Meta<typeof SpeakingLibraries> = {
  title: 'Speaking/Screens/SpeakingLibraries',
  component: SpeakingLibraries,
};
export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  decorators: [withSpeakingMocks({ queries: [librariesQuery()] })],
};

export const Empty: Story = {
  decorators: [withSpeakingMocks({ queries: [librariesQuery([])] })],
};
