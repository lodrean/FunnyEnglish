import type { Meta, StoryObj } from '@storybook/react';
import GradingInbox from '../GradingInbox';
import {
  adminUsersQuery,
  allTopicsQuery,
  librariesQuery,
  mockSubmissionNew,
  submissionsQuery,
  withSpeakingMocks,
} from '../../stories/speakingMocks';

const meta: Meta<typeof GradingInbox> = {
  title: 'Speaking/Screens/GradingInbox',
  component: GradingInbox,
};
export default meta;
type Story = StoryObj<typeof meta>;

const baseQueries = [librariesQuery(), allTopicsQuery(), adminUsersQuery];

export const Default: Story = {
  decorators: [
    withSpeakingMocks({
      queries: [...baseQueries, submissionsQuery([mockSubmissionNew])],
      initialEntry: '/grading',
    }),
  ],
};

export const Empty: Story = {
  decorators: [
    withSpeakingMocks({
      queries: [...baseQueries, submissionsQuery([])],
      initialEntry: '/grading',
    }),
  ],
};
