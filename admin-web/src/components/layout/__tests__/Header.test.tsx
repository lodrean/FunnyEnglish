import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { ThemeProvider } from '../../../theme/ThemeProvider';
import { Header } from '../Header';

const navigateMock = vi.fn();

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual<typeof import('react-router-dom')>('react-router-dom');
  return {
    ...actual,
    useNavigate: () => navigateMock,
  };
});

vi.mock('../../../store/authStore', () => ({
  useAuthStore: () => ({
    user: { displayName: 'Admin User', email: 'admin@sotospeak.com' },
    logout: vi.fn(),
  }),
}));

const renderHeader = (
  initialEntries: string[],
  props: Partial<React.ComponentProps<typeof Header>> = {}
) => {
  return render(
    <ThemeProvider defaultMode="light">
      <MemoryRouter initialEntries={initialEntries}>
        <Routes>
          <Route
            path="*"
            element={
              <Header
                onMenuToggle={vi.fn()}
                showBreadcrumbs={false}
                {...props}
              />
            }
          />
        </Routes>
      </MemoryRouter>
    </ThemeProvider>
  );
};

describe('Header back button', () => {
  beforeEach(() => {
    navigateMock.mockClear();
  });

  it('does not show back button on dashboard', () => {
    renderHeader(['/']);
    expect(screen.queryByTestId('header-back-button')).not.toBeInTheDocument();
  });

  it('does not show back button on list screens', () => {
    renderHeader(['/speaking/libraries']);
    expect(screen.queryByTestId('header-back-button')).not.toBeInTheDocument();
  });

  it('shows back button and title on new library screen', () => {
    renderHeader(['/speaking/libraries/new']);
    expect(screen.getByTestId('header-back-button')).toBeInTheDocument();
    expect(screen.getByText('New Library')).toBeInTheDocument();
  });

  it('shows back button and title on edit library screen', () => {
    renderHeader(['/speaking/libraries/abc-123/edit']);
    expect(screen.getByTestId('header-back-button')).toBeInTheDocument();
    expect(screen.getByText('Edit Library')).toBeInTheDocument();
  });

  it('shows back button and title on new topic screen', () => {
    renderHeader(['/speaking/topics/new']);
    expect(screen.getByTestId('header-back-button')).toBeInTheDocument();
    expect(screen.getByText('New Topic')).toBeInTheDocument();
  });

  it('shows back button and title on edit topic screen', () => {
    renderHeader(['/speaking/topics/topic-1/edit']);
    expect(screen.getByTestId('header-back-button')).toBeInTheDocument();
    expect(screen.getByText('Edit Topic')).toBeInTheDocument();
  });

  it('shows back button and title on grading detail screen', () => {
    renderHeader(['/grading/submissions/sub-1']);
    expect(screen.getByTestId('header-back-button')).toBeInTheDocument();
    expect(screen.getByText('Grading Detail')).toBeInTheDocument();
  });

  it('navigates to parent route when back button is clicked', () => {
    renderHeader(['/speaking/libraries/abc-123/edit']);
    fireEvent.click(screen.getByTestId('header-back-button'));
    expect(navigateMock).toHaveBeenCalledWith('/speaking/libraries');
  });

  it('navigates to grading inbox from grading detail', () => {
    renderHeader(['/grading/submissions/sub-1']);
    fireEvent.click(screen.getByTestId('header-back-button'));
    expect(navigateMock).toHaveBeenCalledWith('/grading');
  });

  it('hides back button when explicitly disabled via prop', () => {
    renderHeader(['/speaking/libraries/new'], { showBackButton: false });
    expect(screen.queryByTestId('header-back-button')).not.toBeInTheDocument();
  });

  it('shows back button with custom title and target when provided', () => {
    renderHeader(['/'], {
      showBackButton: true,
      title: 'Custom Page',
      backTo: '/custom-parent',
    });
    expect(screen.getByTestId('header-back-button')).toBeInTheDocument();
    expect(screen.getByText('Custom Page')).toBeInTheDocument();
    fireEvent.click(screen.getByTestId('header-back-button'));
    expect(navigateMock).toHaveBeenCalledWith('/custom-parent');
  });
});
