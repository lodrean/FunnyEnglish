import type { Meta, StoryObj } from '@storybook/react';
import SpeakingTopicEditor from '../SpeakingTopicEditor';
import {
  allTopicsQuery,
  librariesQuery,
  questionsQuery,
  withSpeakingMocks,
} from '../../stories/speakingMocks';

const meta: Meta<typeof SpeakingTopicEditor> = {
  title: 'Speaking/Screens/SpeakingTopicEditor',
  component: SpeakingTopicEditor,
};
export default meta;
type Story = StoryObj<typeof meta>;

export const Create: Story = {
  decorators: [
    withSpeakingMocks({
      queries: [librariesQuery()],
      routePath: '/speaking/topics/new',
      initialEntry: '/speaking/topics/new',
    }),
  ],
};

/** Вкладки Details/Questions — Questions активна, вопросы topic-1 предзаполнены */
export const Edit: Story = {
  decorators: [
    withSpeakingMocks({
      queries: [librariesQuery(), allTopicsQuery(), questionsQuery('topic-1')],
      routePath: '/speaking/topics/:id/edit',
      initialEntry: '/speaking/topics/topic-1/edit',
    }),
  ],
};
