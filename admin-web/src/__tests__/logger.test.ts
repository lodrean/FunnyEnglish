import { describe, it, expect, vi, afterEach } from 'vitest';
import { logger, flushLogs, getPendingLogsCount } from '../utils/logger';

/**
 * Тесты admin-web логгера (OpenSpec add-client-logging):
 * буферизация WARN/ERROR, batch-отправка на /public/logs, сохранение буфера при сбое.
 * Буфер модульный — тесты идут последовательно и учитывают накопленное состояние.
 */
describe('utils/logger', () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('буферизует WARN/ERROR и отправляет батч на /public/logs', async () => {
    const fetchMock = vi.fn().mockResolvedValue({ ok: true });
    vi.stubGlobal('fetch', fetchMock);

    logger.error('TestTag', 'something broke', new Error('boom'));
    expect(getPendingLogsCount()).toBeGreaterThan(0);

    await flushLogs();

    expect(fetchMock).toHaveBeenCalledTimes(1);
    const [url, init] = fetchMock.mock.calls[0] as [string, RequestInit];
    expect(url).toContain('/public/logs');
    const body = JSON.parse(init.body as string);
    expect(body.logs).toHaveLength(1);
    expect(body.logs[0]).toMatchObject({
      level: 'ERROR',
      tag: 'TestTag',
      message: 'something broke',
      platform: 'admin-web',
    });
    expect(body.logs[0].stackTrace).toContain('boom');
    expect(getPendingLogsCount()).toBe(0);
  });

  it('при сбое сети записи остаются в буфере', async () => {
    const fetchMock = vi.fn().mockRejectedValue(new Error('network down'));
    vi.stubGlobal('fetch', fetchMock);

    logger.warn('TestTag', 'warn before outage');
    const before = getPendingLogsCount();
    expect(before).toBeGreaterThan(0);

    await flushLogs();
    expect(getPendingLogsCount()).toBe(before); // ничего не потеряли

    // сеть восстановилась — буфер уходит
    fetchMock.mockResolvedValue({ ok: true });
    await flushLogs();
    expect(getPendingLogsCount()).toBe(0);
  });

  it('при HTTP-ошибке (not ok) записи остаются в буфере', async () => {
    const fetchMock = vi.fn().mockResolvedValue({ ok: false, status: 500 });
    vi.stubGlobal('fetch', fetchMock);

    logger.warn('TestTag', 'warn before 500');
    const before = getPendingLogsCount();

    await flushLogs();
    expect(getPendingLogsCount()).toBe(before);

    fetchMock.mockResolvedValue({ ok: true });
    await flushLogs();
    expect(getPendingLogsCount()).toBe(0);
  });
});
