import { describe, it, expect } from 'vitest';
import { z } from 'zod';
import { adminUserPageSchema, parseApi, prdMetricsSchema, ApiContractError, userSchema } from '../api/schemas';

describe('parseApi — валидация ответов на границе API (bd b85.4)', () => {
  const validUser = {
    id: 'u1', email: 'a@b.c', displayName: 'A B', level: 1, totalPoints: 10,
    currentStreak: 0, role: 'USER', createdAt: '2026-09-01T00:00:00Z',
  };

  it('валидный ответ проходит и возвращает данные', () => {
    const data = parseApi<{ email: string; avatarUrl?: string | null }>(userSchema, validUser, 'users/me');
    expect(data.email).toBe('a@b.c');
  });

  it('extra-поля ответа разрешены (обратная совместимость)', () => {
    const data = parseApi<{ id: string }>(userSchema, { ...validUser, newBackendField: 42 }, 'users/me');
    expect(data.id).toBe('u1');
  });

  it('null в опциональном поле допустим (Jackson шлёт null)', () => {
    const data = parseApi<{ avatarUrl: string | null }>(userSchema, { ...validUser, avatarUrl: null }, 'users/me');
    expect(data.avatarUrl).toBeNull();
  });

  it('дрейф контракта (нет обязательного поля) → ApiContractError', () => {
    const bad = { ...validUser };
    delete (bad as Record<string, unknown>).displayName;
    expect(() => parseApi(userSchema, bad, 'users/me')).toThrow(ApiContractError);
  });

  it('дрейф типов (string вместо number) → ApiContractError', () => {
    expect(() =>
      parseApi(prdMetricsSchema, { ...{ practiceSubmissionsLast7d: 'many' } }, 'prd-metrics')
    ).toThrow(ApiContractError);
  });

  it('adminUserPage: Page-контракт /admin/users', () => {
    const page = {
      content: [validUser ? { ...validUser, stats: { testsCompleted: 1, totalStars: 3, perfectScores: 0, currentLevel: 1, pointsToNextLevel: 90 } } : null],
      totalElements: 1, totalPages: 1, number: 0, size: 10,
    };
    const data = parseApi<{ content: unknown[]; totalElements: number }>(adminUserPageSchema, page, 'admin/users');
    expect(data.content).toHaveLength(1);
    expect(data.totalElements).toBe(1);
  });

  it('ApiContractError содержит endpoint и путь проблемного поля', () => {
    try {
      parseApi(z.object({ a: z.number() }), { a: 'x' }, 'some/endpoint');
      expect.unreachable('должен был бросить');
    } catch (e) {
      expect(e).toBeInstanceOf(ApiContractError);
      expect((e as ApiContractError).endpoint).toBe('some/endpoint');
      expect((e as ApiContractError).issues.length).toBeGreaterThan(0);
    }
  });
});
