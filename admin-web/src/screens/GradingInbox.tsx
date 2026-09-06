import { useEffect, useState } from 'react';
import {
  Alert,
  Autocomplete,
  Box,
  Button,
  CircularProgress,
  FormControl,
  InputLabel,
  MenuItem,
  Paper,
  Select,
  Skeleton,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TablePagination,
  TableRow,
  TextField,
  Typography,
} from '@mui/material';
import { DatePicker, LocalizationProvider } from '@mui/x-date-pickers';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFnsV3';
import { format } from 'date-fns';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { getAdminUsers } from '../api/client';
import { EmptyState } from '../components/feedback';
import StatusChip from '../components/speaking/StatusChip';
import { useSpeakingTopics, useSubmissions } from '../hooks/useSpeaking';
import type { SpeakingSubmission, SubmissionStatus } from '../api/speakingApi';
import { formatMmSs } from '../utils/format';

const STATUSES: Array<{ value: string; label: string }> = [
  { value: 'ALL', label: 'All' },
  { value: 'NEW', label: 'NEW' },
  { value: 'REVIEWED', label: 'REVIEWED' },
];

const useDebouncedValue = (value: string, delayMs = 300) => {
  const [debounced, setDebounced] = useState(value);
  useEffect(() => {
    const t = setTimeout(() => setDebounced(value), delayMs);
    return () => clearTimeout(t);
  }, [value, delayMs]);
  return debounced;
};

export default function GradingInbox() {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();

  // Фильтры синхронизированы с query string (шэarabельные ссылки, «назад» из детали).
  // 'ALL' — явный токен «все статусы» (пустое значение в URL невозможно отличить от дефолта).
  const status = (searchParams.get('status') ?? 'NEW') as SubmissionStatus | 'ALL';
  const userId = searchParams.get('userId') ?? '';
  const topicId = searchParams.get('topicId') ?? '';
  const from = searchParams.get('from') ?? '';
  const to = searchParams.get('to') ?? '';

  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);

  const setFilter = (key: string, value: string) => {
    const next = new URLSearchParams(searchParams);
    if (value) next.set(key, value);
    else next.delete(key);
    setSearchParams(next, { replace: true });
    setPage(0); // любая смена фильтра → первая страница
  };

  const handleReset = () => {
    setSearchParams(new URLSearchParams(), { replace: true });
    setPage(0);
  };

  // Опции фильтра учеников — поиск по admin users с debounce
  const [studentInput, setStudentInput] = useState('');
  const debouncedStudentQuery = useDebouncedValue(studentInput);
  // bd wy7.6: /admin/users отдаёт Spring Page — серверный поиск, берём content
  const { data: studentPage, isFetching: studentsLoading } = useQuery({
    queryKey: ['admin', 'users', debouncedStudentQuery],
    queryFn: () => getAdminUsers({ query: debouncedStudentQuery || undefined, size: 100 }),
  });
  const studentOptions = studentPage?.content ?? [];

  // Опции фильтра топиков — все топики, клиентский фильтр по вводу
  const { data: topicOptions } = useSpeakingTopics();

  const filters = {
    status: status === 'ALL' ? undefined : (status as SubmissionStatus),
    userId: userId || undefined,
    topicId: topicId || undefined,
    from: from || undefined,
    to: to || undefined,
    page,
    size: pageSize,
  };

  const { data, isLoading, isError, refetch, isPlaceholderData } = useSubmissions(filters);

  const hasActiveFilters = !!(userId || topicId || from || to) || status !== 'NEW';

  const selectedStudent = (studentOptions ?? []).find((u) => u.id === userId) ?? null;
  const selectedTopic = (topicOptions ?? []).find((t) => t.id === topicId) ?? null;

  // Статус-чип — единый StatusChip (токены speaking.status, light + dark).
  const statusChip = (s: SpeakingSubmission) =>
    s.status === 'NEW' ? (
      <StatusChip status="NEW" />
    ) : (
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
        <StatusChip status="REVIEWED" />
        {s.grade && (
          <Typography variant="body2" fontWeight="bold">
            {s.grade.totalScore.toFixed(1)}
          </Typography>
        )}
      </Box>
    );

  return (
    <LocalizationProvider dateAdapter={AdapterDateFns}>
      <Box>
        <Typography variant="h4" data-testid="page-title" sx={{ mb: 3 }}>
          Grading Inbox
        </Typography>

        {/* Фильтры */}
        <Paper sx={{ p: 2, mb: 2 }} data-testid="grading-filters">
          <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap', alignItems: 'center' }}>
            <FormControl size="small" sx={{ minWidth: 140 }}>
              <InputLabel id="filter-status-label">Status</InputLabel>
              <Select
                labelId="filter-status-label"
                label="Status"
                value={status}
                onChange={(e) => setFilter('status', e.target.value)}
                data-testid="filter-status-select"
              >
                {STATUSES.map((s) => (
                  <MenuItem key={s.value} value={s.value}>
                    {s.label}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>

            <Autocomplete
              size="small"
              sx={{ minWidth: 240 }}
              options={studentOptions ?? []}
              getOptionLabel={(u) => `${u.displayName} (${u.email})`}
              value={selectedStudent}
              onChange={(_, v) => setFilter('userId', v?.id ?? '')}
              onInputChange={(_, v) => setStudentInput(v)}
              loading={studentsLoading}
              isOptionEqualToValue={(a, b) => a.id === b.id}
              data-testid="filter-student-autocomplete"
              renderInput={(params) => (
                <TextField
                  {...params}
                  label="Student"
                  InputProps={{
                    ...params.InputProps,
                    endAdornment: (
                      <>
                        {studentsLoading ? <CircularProgress size={16} /> : null}
                        {params.InputProps.endAdornment}
                      </>
                    ),
                  }}
                />
              )}
            />

            <Autocomplete
              size="small"
              sx={{ minWidth: 240 }}
              options={topicOptions ?? []}
              getOptionLabel={(t) => t.name}
              value={selectedTopic}
              onChange={(_, v) => setFilter('topicId', v?.id ?? '')}
              isOptionEqualToValue={(a, b) => a.id === b.id}
              data-testid="filter-topic-autocomplete"
              renderInput={(params) => <TextField {...params} label="Topic" />}
            />

            <DatePicker
              label="From"
              value={from ? new Date(from) : null}
              onChange={(d) => setFilter('from', d ? format(d, 'yyyy-MM-dd') : '')}
              slotProps={{
                textField: { size: 'small', inputProps: { 'data-testid': 'filter-date-from' } },
              }}
            />
            <DatePicker
              label="To"
              value={to ? new Date(to) : null}
              onChange={(d) => setFilter('to', d ? format(d, 'yyyy-MM-dd') : '')}
              slotProps={{
                textField: { size: 'small', inputProps: { 'data-testid': 'filter-date-to' } },
              }}
            />

            <Button onClick={handleReset} data-testid="filters-reset-button">
              Сбросить
            </Button>
          </Box>
        </Paper>

        {isError && (
          <Alert
            severity="error"
            action={
              <Button color="inherit" size="small" onClick={() => refetch()}>
                Retry
              </Button>
            }
          >
            Не удалось загрузить записи. Проверьте соединение и попробуйте снова.
          </Alert>
        )}

        <TableContainer component={Paper} sx={isPlaceholderData ? { opacity: 0.6 } : undefined}>
          <Table data-testid="submissions-table">
            <TableHead>
              <TableRow>
                <TableCell>Student</TableCell>
                <TableCell>Topic</TableCell>
                <TableCell>Date</TableCell>
                <TableCell>Duration</TableCell>
                <TableCell>Status</TableCell>
                <TableCell align="right">Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {isLoading &&
                Array.from({ length: 5 }).map((_, i) => (
                  <TableRow key={i}>
                    {Array.from({ length: 6 }).map((_, j) => (
                      <TableCell key={j}>
                        <Skeleton />
                      </TableCell>
                    ))}
                  </TableRow>
                ))}

              {!isLoading &&
                (data?.content ?? []).map((s) => (
                  <TableRow key={s.id} hover data-testid={`submission-row-${s.id}`}>
                    <TableCell>
                      <Typography variant="body2">{s.student.name}</Typography>
                      <Typography variant="caption" color="text.secondary">
                        {s.student.email}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2">{s.topic.name}</Typography>
                      {s.topic.libraryName && (
                        <Typography variant="caption" color="text.secondary">
                          {s.topic.libraryName}
                        </Typography>
                      )}
                    </TableCell>
                    <TableCell>
                      {s.submittedAt ? format(new Date(s.submittedAt), 'dd.MM.yyyy HH:mm') : '—'}
                    </TableCell>
                    <TableCell>{formatMmSs(s.durationSeconds)}</TableCell>
                    <TableCell>{statusChip(s)}</TableCell>
                    <TableCell align="right">
                      <Button
                        size="small"
                        variant={s.status === 'NEW' ? 'contained' : 'outlined'}
                        onClick={() => navigate(`/grading/submissions/${s.id}`)}
                        data-testid={`review-submission-${s.id}`}
                      >
                        {s.status === 'NEW' ? 'Review' : 'View'}
                      </Button>
                    </TableCell>
                  </TableRow>
                ))}
            </TableBody>
          </Table>

          {!isLoading && !isError && (data?.content.length ?? 0) === 0 && (
            <Box data-testid="submissions-empty" sx={{ p: 2 }}>
              {hasActiveFilters ? (
                <EmptyState
                  title="Записи не найдены"
                  message="Записи не найдены. Измените фильтры."
                  action={{ label: 'Сбросить', onClick: handleReset }}
                />
              ) : (
                <EmptyState
                  title="Всё проверено"
                  message="Новых записей нет. Всё проверено 🎉"
                />
              )}
            </Box>
          )}

          <TablePagination
            component="div"
            count={data?.totalElements ?? 0}
            page={page}
            onPageChange={(_, p) => setPage(p)}
            rowsPerPage={pageSize}
            onRowsPerPageChange={(e) => {
              setPageSize(Number(e.target.value));
              setPage(0);
            }}
            rowsPerPageOptions={[10, 20, 50]}
            data-testid="submissions-pagination"
          />
        </TableContainer>
      </Box>
    </LocalizationProvider>
  );
}
