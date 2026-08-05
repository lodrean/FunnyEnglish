import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { MemoryRouter } from 'react-router-dom';

const { useSpeakingLibrariesMock, usePublishLibraryMock, useDeleteLibraryMock, confirmMock } =
  vi.hoisted(() => ({
    useSpeakingLibrariesMock: vi.fn(),
    usePublishLibraryMock: vi.fn(),
    useDeleteLibraryMock: vi.fn(),
    confirmMock: vi.fn(),
  }));

vi.mock('../../hooks/useSpeaking', () => ({
  useSpeakingLibraries: () => useSpeakingLibrariesMock(),
  usePublishLibrary: () => usePublishLibraryMock(),
  useDeleteLibrary: () => useDeleteLibraryMock(),
}));

vi.mock('../../hooks', () => ({
  useToast: () => ({ success: vi.fn(), error: vi.fn(), warning: vi.fn(), info: vi.fn() }),
  useConfirm: () => ({
    confirm: confirmMock,
    confirmState: { isOpen: false, title: '', message: '', danger: false },
    handleConfirm: vi.fn(),
    handleCancel: vi.fn(),
  }),
}));

import SpeakingLibraries from '../SpeakingLibraries';

const libraries = [
  {
    id: 'lib-1',
    name: 'Everyday English',
    description: 'desc',
    displayOrder: 0,
    isPublished: true,
    topicsCount: 2,
  },
  {
    id: 'lib-2',
    name: 'Business English',
    description: 'desc',
    displayOrder: 1,
    isPublished: false,
    topicsCount: 0,
  },
];

let publishMutation: { mutate: ReturnType<typeof vi.fn>; isPending: boolean };
let deleteMutation: { mutate: ReturnType<typeof vi.fn>; isPending: boolean };

const renderScreen = () => {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return render(
    <QueryClientProvider client={client}>
      <MemoryRouter>
        <SpeakingLibraries />
      </MemoryRouter>
    </QueryClientProvider>
  );
};

beforeEach(() => {
  vi.clearAllMocks();
  publishMutation = { mutate: vi.fn(), isPending: false };
  deleteMutation = { mutate: vi.fn(), isPending: false };
  usePublishLibraryMock.mockReturnValue(publishMutation);
  useDeleteLibraryMock.mockReturnValue(deleteMutation);
  useSpeakingLibrariesMock.mockReturnValue({
    data: libraries,
    isLoading: false,
    isError: false,
    refetch: vi.fn(),
  });
  confirmMock.mockResolvedValue(true);
});

describe('SpeakingLibraries', () => {
  it('поиск фильтрует строки', () => {
    renderScreen();
    fireEvent.change(screen.getByTestId('search-libraries').querySelector('input')!, {
      target: { value: 'business' },
    });
    expect(screen.queryByText('Everyday English')).not.toBeInTheDocument();
    expect(screen.getByText('Business English')).toBeInTheDocument();
  });

  it('publish toggle → publishMutation(id, !isPublished)', () => {
    renderScreen();
    const switchEl = screen.getByTestId('publish-switch-lib-1').querySelector('input')!;
    fireEvent.click(switchEl);
    expect(publishMutation.mutate).toHaveBeenCalledWith(
      { id: 'lib-1', isPublished: false },
      expect.any(Object)
    );
  });

  it('delete → confirm → deleteMutation', async () => {
    renderScreen();
    fireEvent.click(screen.getByTestId('delete-library-lib-2'));
    await waitFor(() => expect(deleteMutation.mutate).toHaveBeenCalledWith('lib-2', expect.any(Object)));
    expect(confirmMock).toHaveBeenCalledWith(
      expect.objectContaining({ danger: true })
    );
  });

  it('delete отменённый в диалоге не вызывает mutation', async () => {
    confirmMock.mockResolvedValue(false);
    renderScreen();
    fireEvent.click(screen.getByTestId('delete-library-lib-1'));
    await waitFor(() => expect(confirmMock).toHaveBeenCalled());
    expect(deleteMutation.mutate).not.toHaveBeenCalled();
  });

  it('empty state', () => {
    useSpeakingLibrariesMock.mockReturnValue({
      data: [],
      isLoading: false,
      isError: false,
      refetch: vi.fn(),
    });
    renderScreen();
    expect(screen.getByTestId('libraries-empty')).toBeInTheDocument();
  });
});
