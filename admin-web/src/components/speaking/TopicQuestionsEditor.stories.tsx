import type { Meta, StoryObj } from '@storybook/react';
import TopicQuestionsEditor from './TopicQuestionsEditor';
import { questionsQuery, withSpeakingMocks } from '../../stories/speakingMocks';

const meta: Meta<typeof TopicQuestionsEditor> = {
  title: 'Speaking/Components/TopicQuestionsEditor',
  component: TopicQuestionsEditor,
  args: { topicId: 'topic-1' },
};
export default meta;
type Story = StoryObj<typeof meta>;

export const WithQuestions: Story = {
  decorators: [withSpeakingMocks({ queries: [questionsQuery('topic-1')] })],
};

export const Empty: Story = {
  decorators: [withSpeakingMocks({ queries: [questionsQuery('topic-1', [])] })],
};
