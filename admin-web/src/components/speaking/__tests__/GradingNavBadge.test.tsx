import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';

const { useSubmissionsMock } = vi.hoisted(() => ({
  useSubmissionsMock: vi.fn(),
}));

vi.mock('../../../hooks/useSpeaking', () => ({
  useSubmissions: (filters: unknown) => useSubmissionsMock(filters),
}));

import GradingNavBadge from '../GradingNavBadge';
import { ThemeProvider } from '../../../theme/ThemeProvider';

// StatusChip читает кастомную палитру theme.palette.speaking (augmentation) —
// без app ThemeProvider тест падает (грабля №47)
const renderBadge = (ui: React.ReactElement) => render(<ThemeProvider>{ui}</ThemeProvider>);

describe('GradingNavBadge (G8)', () => {
  beforeEach(() => vi.clearAllMocks());

  it('показывает счётчик NEW: «7 new»', () => {
    useSubmissionsMock.mockReturnValue({
      data: { content: [{}], totalElements: 7, totalPages: 7, page: 0, size: 1 },
    });
    renderBadge(<GradingNavBadge />);
    expect(screen.getByTestId('grading-new-badge')).toHaveTextContent('7 new');
  });

  it('запрашивает именно NEW-статус', () => {
    useSubmissionsMock.mockReturnValue({ data: undefined });
    renderBadge(<GradingNavBadge />);
    expect(useSubmissionsMock).toHaveBeenCalledWith(
      expect.objectContaining({ status: 'NEW' })
    );
  });

  it('при 0 NEW и без данных бейдж не рендерится', () => {
    useSubmissionsMock.mockReturnValue({
      data: { content: [], totalElements: 0, totalPages: 0, page: 0, size: 1 },
    });
    const { unmount } = renderBadge(<GradingNavBadge />);
    expect(screen.queryByTestId('grading-new-badge')).not.toBeInTheDocument();

    unmount();
    useSubmissionsMock.mockReturnValue({ data: undefined });
    renderBadge(<GradingNavBadge />);
    expect(screen.queryByTestId('grading-new-badge')).not.toBeInTheDocument();
  });
});
