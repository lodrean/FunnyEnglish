import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import type { ReactElement } from 'react';

const {
  useTopicQuestionsMock,
  useSaveQuestionMock,
  useDeleteQuestionMock,
  useReorderQuestionsMock,
} = vi.hoisted(() => ({
  useTopicQuestionsMock: vi.fn(),
  useSaveQuestionMock: vi.fn(),
  useDeleteQuestionMock: vi.fn(),
  useReorderQuestionsMock: vi.fn(),
}));

vi.mock('../../../hooks/useSpeaking', () => ({
  useTopicQuestions: (topicId: string) => useTopicQuestionsMock(topicId),
  useSaveQuestion: (topicId: string) => useSaveQuestionMock(topicId),
  useDeleteQuestion: (topicId: string) => useDeleteQuestionMock(topicId),
  useReorderQuestions: (topicId: string) => useReorderQuestionsMock(topicId),
}));

vi.mock('../../../hooks', () => ({
  useToast: () => ({ success: vi.fn(), error: vi.fn(), warning: vi.fn(), info: vi.fn() }),
  useConfirm: () => ({
    confirm: vi.fn().mockResolvedValue(true),
    confirmState: { isOpen: false, title: '', message: '', danger: false },
    handleConfirm: vi.fn(),
    handleCancel: vi.fn(),
  }),
}));

import TopicQuestionsEditor from '../TopicQuestionsEditor';

const questions = [
  { id: 'q-1', text: 'First question', displayOrder: 0 },
  { id: 'q-2', text: 'Second question', displayOrder: 1 },
];

const renderWithClient = (ui: ReactElement) => {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return render(<QueryClientProvider client={client}>{ui}</QueryClientProvider>);
};

const mutationStub = () => ({ mutate: vi.fn(), mutateAsync: vi.fn(), isPending: false });

let saveMutation: ReturnType<typeof mutationStub>;
let deleteMutation: ReturnType<typeof mutationStub>;
let reorderMutation: ReturnType<typeof mutationStub>;

beforeEach(() => {
  vi.clearAllMocks();
  saveMutation = mutationStub();
  deleteMutation = mutationStub();
  reorderMutation = mutationStub();
  useSaveQuestionMock.mockReturnValue(saveMutation);
  useDeleteQuestionMock.mockReturnValue(deleteMutation);
  useReorderQuestionsMock.mockReturnValue(reorderMutation);
  useTopicQuestionsMock.mockReturnValue({
    data: questions,
    isLoading: false,
    isError: false,
    refetch: vi.fn(),
  });
});

describe('TopicQuestionsEditor', () => {
  it('добавление вопроса: displayOrder = max + 1', () => {
    renderWithClient(<TopicQuestionsEditor topicId="topic-1" />);
    const input = screen.getByPlaceholderText('New question text…');
    fireEvent.change(input, { target: { value: 'Third question' } });
    fireEvent.click(screen.getByTestId('add-question-button'));

    expect(saveMutation.mutate).toHaveBeenCalledWith(
      { data: { text: 'Third question', displayOrder: 2 } },
      expect.objectContaining({ onSuccess: expect.any(Function) })
    );
  });

  it('trim-валидация: пустой текст не отправляется', () => {
    renderWithClient(<TopicQuestionsEditor topicId="topic-1" />);
    fireEvent.change(screen.getByPlaceholderText('New question text…'), {
      target: { value: '   ' },
    });
    fireEvent.click(screen.getByTestId('add-question-button'));
    expect(saveMutation.mutate).not.toHaveBeenCalled();
  });

  it('inline-edit: Edit → текст превращается в поле → Save вызывает mutation', async () => {
    renderWithClient(<TopicQuestionsEditor topicId="topic-1" />);
    const editButtons = screen.getAllByRole('button', { name: '' });
    // Клик по Edit в первом item (иконка-карандаш — по tooltip)
    const item = screen.getByTestId('question-item-q-1');
    const editBtn = Array.from(item.querySelectorAll('button')).find((b) =>
      b.getAttribute('aria-label') === null
    );
    // надёжнее: по порядку кнопок up/down/edit/delete — edit третья
    const buttons = item.querySelectorAll('button');
    fireEvent.click(buttons[2]);

    const textarea = await screen.findByDisplayValue('First question');
    fireEvent.change(textarea, { target: { value: 'First question (edited)' } });
    fireEvent.click(screen.getByRole('button', { name: 'Save' }));

    expect(saveMutation.mutate).toHaveBeenCalledWith(
      { id: 'q-1', data: { text: 'First question (edited)', displayOrder: 0 } },
      expect.any(Object)
    );
    void editButtons;
    void editBtn;
  });

  it('delete с подтверждением вызывает deleteMutation', async () => {
    renderWithClient(<TopicQuestionsEditor topicId="topic-1" />);
    const item = screen.getByTestId('question-item-q-1');
    const buttons = item.querySelectorAll('button');
    fireEvent.click(buttons[3]); // delete

    await waitFor(() => expect(deleteMutation.mutate).toHaveBeenCalledWith('q-1', expect.any(Object)));
  });

  it('reorder ↑/↓ → «Save order» вызывает reorder с новым порядком', () => {
    renderWithClient(<TopicQuestionsEditor topicId="topic-1" />);
    fireEvent.click(screen.getByTestId('question-down-q-1'));

    const saveOrder = screen.getByTestId('save-order-button');
    fireEvent.click(saveOrder);

    expect(reorderMutation.mutate).toHaveBeenCalledWith(
      [
        { id: 'q-2', text: 'Second question', displayOrder: 0 },
        { id: 'q-1', text: 'First question', displayOrder: 1 },
      ],
      expect.any(Object)
    );
  });

  it('empty state', () => {
    useTopicQuestionsMock.mockReturnValue({
      data: [],
      isLoading: false,
      isError: false,
      refetch: vi.fn(),
    });
    renderWithClient(<TopicQuestionsEditor topicId="topic-1" />);
    expect(screen.getByTestId('questions-empty')).toBeInTheDocument();
  });
});
