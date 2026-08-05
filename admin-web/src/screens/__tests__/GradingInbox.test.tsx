import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { MemoryRouter } from 'react-router-dom';
import { ThemeProvider } from '../../theme/ThemeProvider';

const { useSubmissionsMock, useSpeakingTopicsMock, getAdminUsersMock } = vi.hoisted(() => ({
  useSubmissionsMock: vi.fn(),
  useSpeakingTopicsMock: vi.fn(),
  getAdminUsersMock: vi.fn(),
}));

vi.mock('../../hooks/useSpeaking', () => ({
  useSubmissions: (filters: unknown) => useSubmissionsMock(filters),
  useSpeakingTopics: () => useSpeakingTopicsMock(),
}));

vi.mock('../../api/client', () => ({
  getAdminUsers: (opts: unknown) => getAdminUsersMock(opts),
}));

import GradingInbox from '../GradingInbox';
import type { SpeakingSubmission } from '../../api/speakingApi';

const submissionNew: SpeakingSubmission = {
  id: 'sub-1',
  student: { id: 'u-1', name: 'Demo Student', email: 'demo@x.app' },
  topic: { id: 't-1', name: 'Знакомство' },
  audioUrl: 'http://media/a.m4a',
  durationSeconds: 30,
  status: 'NEW',
  submittedAt: '2026-07-30T14:05:00Z',
};

const submissionReviewed: SpeakingSubmission = {
  ...submissionNew,
  id: 'sub-2',
  status: 'REVIEWED',
  grade: {
    grammar: 8,
    vocabulary: 7,
    pronunciation: 9,
    fluency: 6,
    totalScore: 7.5,
    reviewerName: 'Admin',
  },
};

const paged = (content: SpeakingSubmission[]) => ({
  data: { content, totalElements: content.length, totalPages: 1, page: 0, size: 20 },
  isLoading: false,
  isError: false,
  refetch: vi.fn(),
  isPlaceholderData: false,
});

const renderInbox = (initialEntry = '/grading') => {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return render(
    <QueryClientProvider client={client}>
      <ThemeProvider>
        <MemoryRouter initialEntries={[initialEntry]}>
          <GradingInbox />
        </MemoryRouter>
      </ThemeProvider>
    </QueryClientProvider>
  );
};

beforeEach(() => {
  vi.clearAllMocks();
  useSpeakingTopicsMock.mockReturnValue({ data: [] });
  getAdminUsersMock.mockResolvedValue([]);
});

describe('GradingInbox', () => {
  it('рендер строк из getSubmissions: student, topic, duration, действие Review', () => {
    useSubmissionsMock.mockReturnValue(paged([submissionNew]));
    renderInbox();
    expect(screen.getByText('Demo Student')).toBeInTheDocument();
    expect(screen.getByText('demo@x.app')).toBeInTheDocument();
    expect(screen.getByText('Знакомство')).toBeInTheDocument();
    expect(screen.getByText('0:30')).toBeInTheDocument();
    expect(screen.getByTestId('review-submission-sub-1')).toHaveTextContent('Review');
  });

  it('Chip NEW / REVIEWED (+ totalScore)', () => {
    useSubmissionsMock.mockReturnValue(paged([submissionNew, submissionReviewed]));
    renderInbox();
    const table = screen.getByTestId('submissions-table');
    expect(table).toHaveTextContent('NEW');
    expect(table).toHaveTextContent('REVIEWED');
    expect(screen.getByText('7.5')).toBeInTheDocument();
  });

  it('дефолтный фильтр — NEW (учитель открывает inbox ради непроверенных)', () => {
    useSubmissionsMock.mockReturnValue(paged([]));
    renderInbox();
    expect(useSubmissionsMock).toHaveBeenCalledWith(
      expect.objectContaining({ status: 'NEW', page: 0, size: 20 })
    );
  });

  it('начальные фильтры из searchParams (?status=REVIEWED&userId=u1)', () => {
    useSubmissionsMock.mockReturnValue(paged([]));
    renderInbox('/grading?status=REVIEWED&userId=u1');
    expect(useSubmissionsMock).toHaveBeenCalledWith(
      expect.objectContaining({ status: 'REVIEWED', userId: 'u1' })
    );
  });

  it('empty state без активных фильтров (дефолт NEW): «Всё проверено 🎉»', () => {
    useSubmissionsMock.mockReturnValue(paged([]));
    renderInbox();
    expect(screen.getByTestId('submissions-empty')).toHaveTextContent('Всё проверено');
  });

  it('empty state с фильтрами: «Записи не найдены» + Reset', () => {
    useSubmissionsMock.mockReturnValue(paged([]));
    renderInbox('/grading?userId=u1');
    const empty = screen.getByTestId('submissions-empty');
    expect(empty).toHaveTextContent('Записи не найдены');
  });

  it('смена status-фильтра обновляет query string и фильтры запроса', () => {
    useSubmissionsMock.mockReturnValue(paged([]));
    renderInbox();
    const select = screen.getByTestId('filter-status-select');
    // MUI Select — открываем и выбираем REVIEWED
    fireEvent.mouseDown(select.querySelector('[role="combobox"]')!);
    fireEvent.click(screen.getByRole('option', { name: 'REVIEWED' }));
    expect(useSubmissionsMock).toHaveBeenLastCalledWith(
      expect.objectContaining({ status: 'REVIEWED' })
    );
  });
});
