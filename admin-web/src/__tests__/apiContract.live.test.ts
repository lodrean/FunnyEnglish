/**
 * Живой контракт-тест: схемы b85.4 против реального backend.
 * Запуск опционально (нужен поднятый backend):
 *   LIVE_API=http://localhost:8901/api npx vitest run src/__tests__/apiContract.live.test.ts
 * В обычном CI скипается (проверка контрактов — на живых гейтах с реальным стеком).
 */
import { describe, it, expect, beforeAll } from 'vitest';
import { z } from 'zod';
import {
  adminAnalyticsSchema,
  adminMessageSchema,
  adminSettingsSchema,
  adminUserDetailSchema,
  adminUserPageSchema,
  backendLibrarySchema,
  backendSubmissionSchema,
  backendTopicSchema,
  clientLogsPageSchema,
  gradeAuditEntrySchema,
  gradingAnalyticsSchema,
  groupProgressSummarySchema,
  parseApi,
  prdMetricsSchema,
  recentActivityItemSchema,
  springPageSchema,
  userSchema,
} from '../api/schemas';

const LIVE = process.env.LIVE_API;
const submissionPageSchema = springPageSchema(backendSubmissionSchema);
const d = describe.skipIf(!LIVE);

let adminToken = '';
let demoToken = '';

async function get(path: string, token: string): Promise<unknown> {
  const resp = await fetch(`${LIVE}${path}`, {
    headers: { Authorization: `Bearer ${token}` },
  });
  const body = await resp.text();
  expect(resp.status, `${path} → ${resp.status}: ${body.slice(0, 200)}`).toBeLessThan(500);
  return JSON.parse(body);
}

d('API contract (live backend)', () => {
  beforeAll(async () => {
    const login = async (email: string, password: string) => {
      const r = await fetch(`${LIVE}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password }),
      });
      return (await r.json()).token as string;
    };
    adminToken = await login('admin@sotospeak.com', process.env.TEST_ADMIN_PASSWORD || 'admin123');
    demoToken = await login(process.env.LIVE_DEMO_EMAIL || 'demo@sotospeak.app', 'demo123');
  });

  it('auth/me: User', async () => {
    const data = parseApi<z.infer<typeof userSchema>>(userSchema, await get('/users/me', adminToken), 'users/me');
    expect(data.email).toBeTruthy();
  });

  it('admin/users: Page<AdminUserSummary>', async () => {
    const data = parseApi<z.infer<typeof adminUserPageSchema>>(adminUserPageSchema, await get('/admin/users?page=0&size=10', adminToken), 'admin/users');
    expect(data.totalElements).toBeGreaterThan(0);
  });

  it('admin/users/{id}: AdminUserDetail', async () => {
    const page = parseApi<z.infer<typeof adminUserPageSchema>>(adminUserPageSchema, await get('/admin/users?page=0&size=1', adminToken), 'admin/users');
    const id = page.content[0].id;
    const data = parseApi<z.infer<typeof adminUserDetailSchema>>(adminUserDetailSchema, await get(`/admin/users/${id}`, adminToken), `admin/users/${id}`);
    expect(data.user.id).toBeTruthy();
  });

  it('admin/analytics: AdminAnalytics', async () => {
    const data = parseApi<z.infer<typeof adminAnalyticsSchema>>(adminAnalyticsSchema, await get('/admin/analytics', adminToken), 'admin/analytics');
    expect(data.totalUsers).toBeGreaterThan(0);
  });

  it('admin/analytics/prd-metrics: PrdMetrics', async () => {
    const data = parseApi<z.infer<typeof prdMetricsSchema>>(prdMetricsSchema, await get('/admin/analytics/prd-metrics', adminToken), 'prd-metrics');
    expect(data.totalGuests).toBeGreaterThanOrEqual(0);
  });

  it('admin/analytics/recent-activity: RecentActivityItem[]', async () => {
    const data = parseApi<Array<z.infer<typeof recentActivityItemSchema>>>(
      z.array(recentActivityItemSchema),
      await get('/admin/analytics/recent-activity?limit=3', adminToken),
      'recent-activity'
    );
    expect(Array.isArray(data)).toBe(true);
  });

  it('admin/settings: AdminSettings', async () => {
    const data = parseApi<z.infer<typeof adminSettingsSchema>>(adminSettingsSchema, await get('/admin/settings', adminToken), 'admin/settings');
    expect(data.s3Bucket).toBeTruthy();
  });

  it('admin/logs: ClientLogsPage', async () => {
    const data = parseApi<z.infer<typeof clientLogsPageSchema>>(clientLogsPageSchema, await get('/admin/logs?size=5', adminToken), 'admin/logs');
    expect(data.size).toBe(5);
  });

  it('admin/users/{id}/messages: AdminMessage[]', async () => {
    const page = parseApi<z.infer<typeof adminUserPageSchema>>(adminUserPageSchema, await get('/admin/users?page=0&size=1', adminToken), 'admin/users');
    const id = page.content[0].id;
    const data = parseApi<Array<z.infer<typeof adminMessageSchema>>>(
      z.array(adminMessageSchema),
      await get(`/admin/users/${id}/messages`, adminToken),
      'messages'
    );
    expect(Array.isArray(data)).toBe(true);
  });

  it('messages: AdminMessage (проверка admin-токена проходит схему юзера)', async () => {
    const data = parseApi<z.infer<typeof userSchema>>(userSchema, await get('/users/me', demoToken), 'users/me (demo)');
    expect(data.email).toContain('@');
  });

  it('groups/progress: GroupProgressSummary (первая группа, если есть)', async () => {
    const resp = await fetch(`${LIVE}/groups/teacher/my-groups`, {
      headers: { Authorization: `Bearer ${adminToken}` },
    });
    if (resp.status !== 200) return; // группы — опциональный контур
    const groups = (await resp.json()) as Array<{ id: string }>;
    if (groups.length === 0) return;
    const data = parseApi<z.infer<typeof groupProgressSummarySchema>>(
      groupProgressSummarySchema,
      await get(`/groups/${groups[0].id}/progress`, adminToken),
      'groups/:id/progress'
    );
    expect(data.groupId).toBeTruthy();
  });

  // ==================== Speaking (bd b85.4) ====================

  it('admin/speaking/libraries: BackendLibrary[]', async () => {
    const data = parseApi<Array<z.infer<typeof backendLibrarySchema>>>(
      z.array(backendLibrarySchema),
      await get('/admin/speaking/libraries', adminToken),
      'admin/speaking/libraries'
    );
    expect(Array.isArray(data)).toBe(true);
  });

  it('admin/speaking/topics: BackendTopic[] (первая тема, если есть)', async () => {
    const libraries = parseApi<Array<z.infer<typeof backendLibrarySchema>>>(
      z.array(backendLibrarySchema),
      await get('/admin/speaking/libraries', adminToken),
      'admin/speaking/libraries'
    );
    if (libraries.length === 0) return;
    const data = parseApi<Array<z.infer<typeof backendTopicSchema>>>(
      z.array(backendTopicSchema),
      await get(`/admin/speaking/topics?libraryId=${libraries[0].id}`, adminToken),
      'admin/speaking/topics'
    );
    expect(Array.isArray(data)).toBe(true);
  });

  it('admin/speaking/submissions: Spring Page<AdminSubmissionResponse>', async () => {
    const data = parseApi<z.infer<typeof submissionPageSchema>>(
      submissionPageSchema,
      await get('/admin/speaking/submissions?size=5', adminToken),
      'admin/speaking/submissions'
    );
    expect(data.size).toBe(5);
  });

  it('admin/speaking/grading/analytics: GradingAnalytics', async () => {
    const data = parseApi<z.infer<typeof gradingAnalyticsSchema>>(
      gradingAnalyticsSchema,
      await get('/admin/speaking/grading/analytics', adminToken),
      'admin/speaking/grading/analytics'
    );
    expect(data.newCount).toBeGreaterThanOrEqual(0);
  });

  it('grade history: GradeAuditEntry[] (для REVIEWED, если есть)', async () => {
    const page = parseApi<z.infer<typeof submissionPageSchema>>(
      submissionPageSchema,
      await get('/admin/speaking/submissions?status=REVIEWED&size=1', adminToken),
      'admin/speaking/submissions'
    );
    if (page.content.length === 0) return;
    const data = parseApi<Array<z.infer<typeof gradeAuditEntrySchema>>>(
      z.array(gradeAuditEntrySchema),
      await get(`/admin/speaking/submissions/${page.content[0].id}/grade/history`, adminToken),
      'grade/history'
    );
    expect(Array.isArray(data)).toBe(true);
  });
});
