import type { Meta, StoryObj } from '@storybook/react';
import SpeakingLibraryEditor from '../SpeakingLibraryEditor';
import { librariesQuery, withSpeakingMocks } from '../../stories/speakingMocks';

const meta: Meta<typeof SpeakingLibraryEditor> = {
  title: 'Speaking/Screens/SpeakingLibraryEditor',
  component: SpeakingLibraryEditor,
};
export default meta;
type Story = StoryObj<typeof meta>;

export const Create: Story = {
  decorators: [
    withSpeakingMocks({
      queries: [librariesQuery()],
      routePath: '/speaking/libraries/new',
      initialEntry: '/speaking/libraries/new',
    }),
  ],
};

export const Edit: Story = {
  decorators: [
    withSpeakingMocks({
      queries: [librariesQuery()],
      routePath: '/speaking/libraries/:id/edit',
      initialEntry: '/speaking/libraries/lib-1/edit',
    }),
  ],
};
