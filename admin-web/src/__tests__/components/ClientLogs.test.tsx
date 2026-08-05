import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
// App ThemeProvider обязателен в тестовых обёртках (memory.md №47: кастомная палитра)
import { ThemeProvider } from '../../theme/ThemeProvider';
import ClientLogs from '../../screens/ClientLogs';
import type { ClientLogsPage } from '../../api/client';

vi.mock('../../api/client', () => ({
  getClientLogs: vi.fn(),
}));

import { getClientLogs } from '../../api/client';

const emptyPage: ClientLogsPage = {
  content: [],
  totalElements: 0,
  totalPages: 0,
  number: 0,
  size: 20,
};

const oneLogPage: ClientLogsPage = {
  ...emptyPage,
  totalElements: 1,
  totalPages: 1,
  content: [
    {
      id: '11111111-1111-1111-1111-111111111111',
      anonymousId: null,
      level: 'ERROR',
      tag: 'HttpClient',
      message: 'HTTP call failed',
      stackTrace: null,
      platform: 'android',
      appVersion: '1.0.0-qa',
      clientTimestamp: '2026-08-02T10:00:00Z',
      createdAt: '2026-08-02T10:00:05Z',
    },
  ],
};

function renderPage() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });
  return render(
    <QueryClientProvider client={queryClient}>
      <ThemeProvider>
        <ClientLogs />
      </ThemeProvider>
    </QueryClientProvider>
  );
}

describe('ClientLogs screen (OpenSpec add-client-logging)', () => {
  it('рендерит заголовок и пустое состояние', async () => {
    vi.mocked(getClientLogs).mockResolvedValue(emptyPage);
    renderPage();

    expect(screen.getByTestId('page-title')).toHaveTextContent('Client Logs');
    expect(await screen.findByText('No logs found')).toBeInTheDocument();
  });

  it('рендерит строку лога с уровнем и платформой', async () => {
    vi.mocked(getClientLogs).mockResolvedValue(oneLogPage);
    renderPage();

    expect(await screen.findByText('HTTP call failed')).toBeInTheDocument();
    expect(screen.getByText('ERROR')).toBeInTheDocument();
    expect(screen.getByText('android')).toBeInTheDocument();
    expect(screen.getByText('1.0.0-qa')).toBeInTheDocument();
  });

  it('передаёт фильтры по датам как ISO-интервал (from — начало дня, to — конец дня)', async () => {
    vi.mocked(getClientLogs).mockResolvedValue(emptyPage);
    renderPage();

    fireEvent.change(screen.getByTestId('logs-filter-from').querySelector('input')!, {
      target: { value: '2026-08-01' },
    });
    fireEvent.change(screen.getByTestId('logs-filter-to').querySelector('input')!, {
      target: { value: '2026-08-03' },
    });

    await waitFor(() => {
      expect(getClientLogs).toHaveBeenCalledWith(
        expect.objectContaining({
          from: '2026-08-01T00:00:00Z',
          to: '2026-08-03T23:59:59Z',
        })
      );
    });
  });
});
