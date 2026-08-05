import { describe, it, expect, vi, beforeAll, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { ThemeProvider } from '../../theme/ThemeProvider';

const {
  useSubmissionMock,
  useTopicQuestionsMock,
  useSaveGradeMock,
  useSubmissionsMock,
} = vi.hoisted(() => ({
  useSubmissionMock: vi.fn(),
  useTopicQuestionsMock: vi.fn(),
  useSaveGradeMock: vi.fn(),
  useSubmissionsMock: vi.fn(),
}));

vi.mock('../../hooks/useSpeaking', async (importOriginal) => {
  const actual = await importOriginal<typeof import('../../hooks/useSpeaking')>();
  return {
    ...actual,
    useSubmission: (id?: string) => useSubmissionMock(id),
    useTopicQuestions: () => useTopicQuestionsMock(),
    useSaveGrade: () => useSaveGradeMock(),
    useSubmissions: (filters: unknown) => useSubmissionsMock(filters),
  };
});

vi.mock('../../hooks', () => ({
  useToast: () => ({ success: vi.fn(), error: vi.fn() }),
}));

import GradingDetail from '../GradingDetail';
import type { SpeakingSubmission } from '../../api/speakingApi';

const sub1: SpeakingSubmission = {
  id: 'sub-1',
  student: { id: 'u-1', name: 'Анна Смирнова', email: 'anna@x.app' },
  topic: { id: 't-1', name: 'At the airport', libraryName: 'Travel & Holidays' },
  audioUrl: 'http://media/a1.m4a',
  durationSeconds: 30,
  status: 'NEW',
  submittedAt: '2026-07-29T21:14:00',
};

const sub2: SpeakingSubmission = {
  ...sub1,
  id: 'sub-2',
  student: { id: 'u-2', name: 'Ivan Petrov', email: 'ivan@x.app' },
};

const sub3: SpeakingSubmission = {
  ...sub1,
  id: 'sub-3',
  student: { id: 'u-3', name: 'Maria Sidorova', email: 'maria@x.app' },
};

const byId: Record<string, SpeakingSubmission> = {
  'sub-1': sub1,
  'sub-2': sub2,
  'sub-3': sub3,
};

const newList = (content: SpeakingSubmission[]) => ({
  data: { content, totalElements: content.length, totalPages: 1, page: 0, size: 100 },
});

const renderDetail = (entry: string) => {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return render(
    <QueryClientProvider client={client}>
      <ThemeProvider>
        <MemoryRouter initialEntries={[entry]}>
          <Routes>
            <Route path="/grading/submissions/:id" element={<GradingDetail />} />
            <Route path="/grading" element={<div data-testid="inbox-page">inbox</div>} />
          </Routes>
        </MemoryRouter>
      </ThemeProvider>
    </QueryClientProvider>
  );
};

beforeAll(() => {
  // jsdom не реализует HTMLMediaElement — плеер вызывает pause() при unmount
  vi.spyOn(window.HTMLMediaElement.prototype, 'play').mockImplementation(() =>
    Promise.resolve()
  );
  vi.spyOn(window.HTMLMediaElement.prototype, 'pause').mockImplementation(() => {});
});

beforeEach(() => {
  vi.clearAllMocks();
  useSubmissionMock.mockImplementation((id?: string) => ({
    data: id ? byId[id] : undefined,
    isLoading: false,
    isError: false,
    error: null,
  }));
  useTopicQuestionsMock.mockReturnValue({
    data: [
      { id: 'q-1', text: 'Where do you usually fly to on holidays?', displayOrder: 0 },
      { id: 'q-2', text: 'Window or aisle?', displayOrder: 1 },
    ],
  });
  useSaveGradeMock.mockReturnValue({ mutateAsync: vi.fn(), isPending: false });
  useSubmissionsMock.mockReturnValue(newList([sub1, sub2, sub3]));
});

describe('GradingDetail', () => {
  it('карточка студента (G5): аватар с инициалами, имя, мета «Library → Topic · отправлено <date>», чип NEW', () => {
    renderDetail('/grading/submissions/sub-1');

    expect(screen.getByTestId('student-avatar')).toHaveTextContent('АС');
    expect(screen.getByTestId('student-name')).toHaveTextContent('Анна Смирнова');
    expect(screen.getByTestId('student-meta')).toHaveTextContent(
      'Travel & Holidays → At the airport · отправлено 29.07.2026 21:14'
    );
    expect(screen.getByTestId('submission-status-chip')).toHaveTextContent('NEW');
    // плашка записи «Запись · 0:30» и waveform-плеер
    expect(screen.getByTestId('recording-title')).toHaveTextContent('Запись · 0:30');
    expect(screen.getByTestId('audio-waveform')).toBeInTheDocument();
    // старого заголовка «Submission: …» больше нет
    expect(screen.queryByText(/Submission:/)).not.toBeInTheDocument();
  });

  it('«Пропустить» (G4): переход к следующей NEW-записи без сохранения оценки', () => {
    renderDetail('/grading/submissions/sub-1');
    fireEvent.click(screen.getByTestId('skip-submission-button'));

    expect(screen.getByTestId('student-name')).toHaveTextContent('Ivan Petrov');
    expect(useSaveGradeMock().mutateAsync).not.toHaveBeenCalled();
  });

  it('«Пропустить» на последней NEW-записи возвращает в inbox', () => {
    renderDetail('/grading/submissions/sub-3');
    fireEvent.click(screen.getByTestId('skip-submission-button'));

    expect(screen.getByTestId('inbox-page')).toBeInTheDocument();
  });

  it('«Пропустить» без NEW-записей в ленте возвращает в inbox', () => {
    useSubmissionsMock.mockReturnValue(newList([]));
    renderDetail('/grading/submissions/sub-1');
    fireEvent.click(screen.getByTestId('skip-submission-button'));

    expect(screen.getByTestId('inbox-page')).toBeInTheDocument();
  });
});
