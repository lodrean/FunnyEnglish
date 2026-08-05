import type { Meta, StoryObj } from '@storybook/react';
import { action } from '@storybook/addon-actions';
import RubricForm from './RubricForm';

const meta: Meta<typeof RubricForm> = {
  title: 'Speaking/Components/RubricForm',
  component: RubricForm,
  args: {
    isSaving: false,
    onSave: action('onSave'),
  },
};
export default meta;
type Story = StoryObj<typeof meta>;

/** NEW: критерии «не выставлены» — Save disabled до явного изменения всех 4 */
export const Empty: Story = {};

/** REVIEWED: предзаполнена и readonly, включается кнопкой «Edit grade» */
export const Reviewed: Story = {
  args: {
    grade: {
      grammar: 8,
      vocabulary: 7,
      pronunciation: 9,
      fluency: 6,
      totalScore: 7.5,
      comment: 'Хорошая работа! Обратите внимание на темп речи.',
      reviewerName: 'Admin',
      gradedAt: '2026-07-29T10:00:00Z',
      updatedAt: '2026-07-29T11:00:00Z',
    },
  },
};

export const Saving: Story = {
  args: { isSaving: true },
};
