import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';

const { useSubmissionsMock } = vi.hoisted(() => ({
  useSubmissionsMock: vi.fn(),
}));

vi.mock('../../../hooks/useSpeaking', () => ({
  useSubmissions: (filters: unknown) => useSubmissionsMock(filters),
}));

import GradingNavBadge from '../GradingNavBadge';

describe('GradingNavBadge (G8)', () => {
  beforeEach(() => vi.clearAllMocks());

  it('показывает счётчик NEW: «7 new»', () => {
    useSubmissionsMock.mockReturnValue({
      data: { content: [{}], totalElements: 7, totalPages: 7, page: 0, size: 1 },
    });
    render(<GradingNavBadge />);
    expect(screen.getByTestId('grading-new-badge')).toHaveTextContent('7 new');
  });

  it('запрашивает именно NEW-статус', () => {
    useSubmissionsMock.mockReturnValue({ data: undefined });
    render(<GradingNavBadge />);
    expect(useSubmissionsMock).toHaveBeenCalledWith(
      expect.objectContaining({ status: 'NEW' })
    );
  });

  it('при 0 NEW и без данных бейдж не рендерится', () => {
    useSubmissionsMock.mockReturnValue({
      data: { content: [], totalElements: 0, totalPages: 0, page: 0, size: 1 },
    });
    const { unmount } = render(<GradingNavBadge />);
    expect(screen.queryByTestId('grading-new-badge')).not.toBeInTheDocument();

    unmount();
    useSubmissionsMock.mockReturnValue({ data: undefined });
    render(<GradingNavBadge />);
    expect(screen.queryByTestId('grading-new-badge')).not.toBeInTheDocument();
  });
});
