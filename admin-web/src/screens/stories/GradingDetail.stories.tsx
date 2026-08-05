import React from 'react';
import type { Meta, StoryObj } from '@storybook/react';
import GradingDetail from '../GradingDetail';
import {
  mockSubmissionNew,
  mockSubmissionReviewed,
  newSubmissionsQuery,
  questionsQuery,
  submissionsQuery,
  withSpeakingMocks,
} from '../../stories/speakingMocks';

/** Показывает реальную ошибку рендера вместо generic «configuration issue» Storybook */
class ErrorBoundary extends React.Component<
  { children: React.ReactNode },
  { error: Error | null }
> {
  state = { error: null as Error | null };
  static getDerivedStateFromError(error: Error) {
    return { error };
  }
  render() {
    if (this.state.error) {
      return (
        <pre style={{ color: 'red', whiteSpace: 'pre-wrap' }}>
          {String(this.state.error.stack || this.state.error)}
        </pre>
      );
    }
    return this.props.children;
  }
}

const withErrorBoundary = (Story: React.ComponentType) => (
  <ErrorBoundary>
    <Story />
  </ErrorBoundary>
);

const meta: Meta<typeof GradingDetail> = {
  title: 'Speaking/Screens/GradingDetail',
  component: GradingDetail,
};
export default meta;
type Story = StoryObj<typeof meta>;

/** status NEW — рубрика пустая, Save disabled пока не выставлены все критерии */
export const New: Story = {
  decorators: [
    withErrorBoundary,
    withSpeakingMocks({
      queries: [
        submissionsQuery([mockSubmissionNew]),
        newSubmissionsQuery([mockSubmissionNew, mockSubmissionReviewed]),
        questionsQuery('topic-1'),
      ],
      routePath: '/grading/submissions/:id',
      initialEntry: '/grading/submissions/sub-1',
    }),
  ],
};

/** status REVIEWED — форма предзаполнена и readonly до «Edit grade» */
export const Reviewed: Story = {
  decorators: [
    withErrorBoundary,
    withSpeakingMocks({
      queries: [
        submissionsQuery([mockSubmissionReviewed]),
        newSubmissionsQuery([mockSubmissionNew]),
        questionsQuery('topic-1'),
      ],
      routePath: '/grading/submissions/:id',
      initialEntry: '/grading/submissions/sub-2',
    }),
  ],
};
