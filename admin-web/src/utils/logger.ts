/**
 * Централизованный логгер admin-web (OpenSpec add-client-logging).
 *
 * WARN/ERROR дублируются в консоль и складываются в in-memory буфер,
 * который best-effort уходит на backend (`POST /api/public/logs`, platform=admin-web).
 * Отправка — голым fetch (НЕ axios-instance), чтобы не ловить рекурсию
 * с response-interceptor'ом, который сам пишет в этот логгер.
 */

export type LogLevel = 'WARN' | 'ERROR';

export interface LogEntry {
  timestamp: string; // ISO-8601 с зоной
  level: LogLevel;
  tag: string;
  message: string;
  stackTrace?: string;
  platform: string;
  appVersion?: string;
}

const API_BASE_URL = import.meta.env.VITE_API_URL || '/api';
const MAX_BUFFER = 100;
const FLUSH_THRESHOLD = 10;
const FLUSH_INTERVAL_MS = 10_000;
const MAX_BATCH = 50;

const buffer: LogEntry[] = [];
let flushTimer: ReturnType<typeof setInterval> | null = null;
let flushing = false;

function enqueue(level: LogLevel, tag: string, message: string, error?: unknown): void {
  const entry: LogEntry = {
    timestamp: new Date().toISOString(),
    level,
    tag: tag.slice(0, 100),
    message,
    platform: 'admin-web',
    appVersion: '1.0.0-admin',
  };
  if (error instanceof Error && error.stack) {
    entry.stackTrace = error.stack;
  }
  buffer.push(entry);
  if (buffer.length > MAX_BUFFER) buffer.shift(); // FIFO-вытеснение

  ensureFlushTimer();
  if (buffer.length >= FLUSH_THRESHOLD) {
    void flushLogs();
  }
}

function ensureFlushTimer(): void {
  if (flushTimer === null) {
    flushTimer = setInterval(() => void flushLogs(), FLUSH_INTERVAL_MS);
  }
}

/** Отправить накопленное батчами ≤50; при ошибке — остаток остаётся в буфере */
export async function flushLogs(): Promise<void> {
  if (flushing) return;
  flushing = true;
  try {
    while (buffer.length > 0) {
      const batch = buffer.slice(0, MAX_BATCH);
      try {
        const token = localStorage.getItem('token');
        const response = await fetch(`${API_BASE_URL}/public/logs`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            ...(token ? { Authorization: `Bearer ${token}` } : {}),
          },
          body: JSON.stringify({ logs: batch }),
        });
        if (!response.ok) return; // остаток в буфере, попробуем позже
        buffer.splice(0, batch.length);
      } catch {
        return; // сеть недоступна — молча, логирование не должно влиять на UX
      }
    }
  } finally {
    flushing = false;
  }
}

/** Размер буфера (для тестов и диагностики) */
export function getPendingLogsCount(): number {
  return buffer.length;
}

export const logger = {
  warn(tag: string, message: string, error?: unknown): void {
    console.warn(`[WARN] [${tag}]`, message, error ?? '');
    enqueue('WARN', tag, message, error);
  },
  error(tag: string, message: string, error?: unknown): void {
    console.error(`[ERROR] [${tag}]`, message, error ?? '');
    enqueue('ERROR', tag, message, error);
  },
};

// Best-effort отправка при закрытии страницы
if (typeof window !== 'undefined') {
  window.addEventListener('beforeunload', () => void flushLogs());
}
