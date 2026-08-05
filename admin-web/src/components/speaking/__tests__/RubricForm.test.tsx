import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import RubricForm from '../RubricForm';
import type { Grade } from '../../../api/speakingApi';

const reviewedGrade: Grade = {
  grammar: 8,
  vocabulary: 7,
  pronunciation: 9,
  fluency: 6,
  totalScore: 7.5,
  comment: 'Хорошая работа!',
  reviewerName: 'Admin',
  gradedAt: '2026-07-29T10:00:00Z',
  updatedAt: '2026-07-29T11:00:00Z',
};

/** Выставить критерий через слайдер (number-input удалён по мокапу frame-grading) */
const setCriterion = (key: string, value: string) => {
  const input = screen.getByTestId(`rubric-slider-${key}`).querySelector('input')!;
  fireEvent.change(input, { target: { value } });
};

describe('RubricForm', () => {
  it('крупное значение справа от подписи (.rubric-head) обновляется при смене слайдера', () => {
    render(<RubricForm isSaving={false} onSave={() => {}} />);
    expect(screen.getByTestId('rubric-value-grammar')).toHaveTextContent('5');
    setCriterion('grammar', '9');
    expect(screen.getByTestId('rubric-value-grammar')).toHaveTextContent('9');
  });

  it('авто-усреднение total: 8/7/9/6 → «7.5», обновление при изменении критерия', () => {
    render(<RubricForm isSaving={false} onSave={() => {}} />);
    setCriterion('grammar', '8');
    setCriterion('vocabulary', '7');
    setCriterion('pronunciation', '9');
    setCriterion('fluency', '6');
    expect(screen.getByTestId('rubric-total')).toHaveTextContent('7.5');

    setCriterion('fluency', '10');
    expect(screen.getByTestId('rubric-total')).toHaveTextContent('8.5');
  });

  it('панель «Общий балл (среднее)» (.avg-box) вместо чипа «авто-усреднение»', () => {
    render(<RubricForm isSaving={false} onSave={() => {}} />);
    const panel = screen.getByTestId('rubric-avg-panel');
    expect(panel).toHaveTextContent('Общий балл (среднее)');
    expect(panel).toHaveTextContent('5.0');
    expect(screen.queryByText('авто-усреднение')).not.toBeInTheDocument();
  });

  it('clamp slider: 0 → 1, 11 → 10', () => {
    render(<RubricForm isSaving={false} onSave={() => {}} />);
    const slider = () => screen.getByTestId('rubric-slider-grammar').querySelector('input')!;
    setCriterion('grammar', '0');
    expect(slider()).toHaveAttribute('aria-valuenow', '1');
    setCriterion('grammar', '11');
    expect(slider()).toHaveAttribute('aria-valuenow', '10');
  });

  it('Save disabled, пока не выставлены все 4 критерия (дефолт — «не выставлен», не auto-5)', () => {
    render(<RubricForm isSaving={false} onSave={() => {}} />);
    const save = screen.getByTestId('save-grade-button');
    expect(save).toBeDisabled();

    setCriterion('grammar', '8');
    setCriterion('vocabulary', '7');
    setCriterion('pronunciation', '9');
    expect(save).toBeDisabled(); // fluency ещё не выставлен

    setCriterion('fluency', '6');
    expect(save).toBeEnabled();
  });

  it('submit вызывает onSave с GradeRequest БЕЗ totalScore', () => {
    const onSave = vi.fn();
    render(<RubricForm isSaving={false} onSave={onSave} />);
    setCriterion('grammar', '8');
    setCriterion('vocabulary', '7');
    setCriterion('pronunciation', '9');
    setCriterion('fluency', '6');
    fireEvent.change(screen.getByTestId('rubric-comment').querySelector('textarea')!, {
      target: { value: 'Комментарий' },
    });
    fireEvent.click(screen.getByTestId('save-grade-button'));

    expect(onSave).toHaveBeenCalledWith({
      grammar: 8,
      vocabulary: 7,
      pronunciation: 9,
      fluency: 6,
      comment: 'Комментарий',
    });
    expect(onSave.mock.calls[0][0]).not.toHaveProperty('totalScore');
  });

  it('режим REVIEWED: prefill + disabled до клика «Edit grade», отображение аудита', () => {
    render(<RubricForm grade={reviewedGrade} isSaving={false} onSave={() => {}} />);

    // prefill
    expect(screen.getByTestId('rubric-value-grammar')).toHaveTextContent('8');
    expect(screen.getByTestId('rubric-total')).toHaveTextContent('7.5');
    // readonly
    expect(screen.getByTestId('rubric-slider-grammar').querySelector('input')).toBeDisabled();
    expect(screen.queryByTestId('save-grade-button')).not.toBeInTheDocument();
    // аудит
    expect(screen.getByText(/updated at/)).toBeInTheDocument();
    expect(screen.getByText(/Admin/)).toBeInTheDocument();

    // Edit grade → поля активны
    fireEvent.click(screen.getByTestId('edit-grade-button'));
    expect(screen.getByTestId('rubric-slider-grammar').querySelector('input')).toBeEnabled();
    expect(screen.getByTestId('save-grade-button')).toBeEnabled();
  });

  it('REVIEWED + edit: submit обновляет оценку', () => {
    const onSave = vi.fn();
    render(<RubricForm grade={reviewedGrade} isSaving={false} onSave={onSave} />);
    fireEvent.click(screen.getByTestId('edit-grade-button'));
    setCriterion('fluency', '10');
    fireEvent.click(screen.getByTestId('save-grade-button'));
    expect(onSave).toHaveBeenCalledWith({
      grammar: 8,
      vocabulary: 7,
      pronunciation: 9,
      fluency: 10,
      comment: 'Хорошая работа!',
    });
  });

  it('«Пропустить» рендерится при onSkip и вызывает его; без onSkip кнопки нет', () => {
    const onSkip = vi.fn();
    const { unmount } = render(<RubricForm isSaving={false} onSave={() => {}} onSkip={onSkip} />);
    fireEvent.click(screen.getByTestId('skip-submission-button'));
    expect(onSkip).toHaveBeenCalledTimes(1);

    unmount();
    render(<RubricForm isSaving={false} onSave={() => {}} />);
    expect(screen.queryByTestId('skip-submission-button')).not.toBeInTheDocument();
  });
});
