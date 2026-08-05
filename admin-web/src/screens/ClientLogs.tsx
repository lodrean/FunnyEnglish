import React, { useState } from 'react';
import {
  Box,
  Typography,
  TextField,
  Paper,
  MenuItem,
  FormControl,
  InputLabel,
  Select,
  Alert,
  Chip,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TablePagination,
  Collapse,
  IconButton,
  InputAdornment,
} from '@mui/material';
import {
  Search as SearchIcon,
  KeyboardArrowDown as ExpandMoreIcon,
  KeyboardArrowUp as ExpandLessIcon,
} from '@mui/icons-material';
import { useQuery, keepPreviousData } from '@tanstack/react-query';
import { getClientLogs } from '../api/client';
import type { ClientLogEntry } from '../api/client';

/**
 * Просмотр клиентских логов WARN/ERROR с устройств пользователей
 * (OpenSpec add-client-logging). Backend: GET /admin/logs (ROLE_ADMIN).
 */
const ClientLogs: React.FC = () => {
  const [level, setLevel] = useState('');
  const [platform, setPlatform] = useState('');
  const [query, setQuery] = useState('');
  const [searchInput, setSearchInput] = useState('');
  const [dateFrom, setDateFrom] = useState('');
  const [dateTo, setDateTo] = useState('');
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(20);
  const [expandedId, setExpandedId] = useState<string | null>(null);

  const { data, isLoading, error } = useQuery({
    queryKey: ['client-logs', level, platform, query, dateFrom, dateTo, page, rowsPerPage],
    queryFn: () =>
      getClientLogs({
        level: level || undefined,
        platform: platform || undefined,
        q: query || undefined,
        // Backend ждёт ISO Instant: from — начало дня, to — конец дня (UTC)
        from: dateFrom ? `${dateFrom}T00:00:00Z` : undefined,
        to: dateTo ? `${dateTo}T23:59:59Z` : undefined,
        page,
        size: rowsPerPage,
      }),
    placeholderData: keepPreviousData,
  });

  const logs = data?.content ?? [];

  const applySearch = () => {
    setPage(0);
    setQuery(searchInput);
  };

  return (
    <Box>
      <Typography variant="h4" data-testid="page-title" sx={{ mb: 3 }}>
        Client Logs
      </Typography>

      {/* Фильтры */}
      <Paper sx={{ p: 2, mb: 2, display: 'flex', gap: 2, flexWrap: 'wrap', alignItems: 'center' }}>
        <FormControl size="small" sx={{ minWidth: 140 }}>
          <InputLabel>Level</InputLabel>
          <Select
            label="Level"
            value={level}
            data-testid="logs-filter-level"
            onChange={(e) => {
              setPage(0);
              setLevel(e.target.value);
            }}
          >
            <MenuItem value="">All</MenuItem>
            <MenuItem value="WARN">WARN</MenuItem>
            <MenuItem value="ERROR">ERROR</MenuItem>
          </Select>
        </FormControl>

        <FormControl size="small" sx={{ minWidth: 160 }}>
          <InputLabel>Platform</InputLabel>
          <Select
            label="Platform"
            value={platform}
            data-testid="logs-filter-platform"
            onChange={(e) => {
              setPage(0);
              setPlatform(e.target.value);
            }}
          >
            <MenuItem value="">All</MenuItem>
            <MenuItem value="android">android</MenuItem>
            <MenuItem value="desktop">desktop</MenuItem>
            <MenuItem value="wasm">wasm</MenuItem>
            <MenuItem value="admin-web">admin-web</MenuItem>
          </Select>
        </FormControl>

        <TextField
          size="small"
          type="date"
          label="From"
          value={dateFrom}
          data-testid="logs-filter-from"
          onChange={(e) => {
            setPage(0);
            setDateFrom(e.target.value);
          }}
          InputLabelProps={{ shrink: true }}
        />

        <TextField
          size="small"
          type="date"
          label="To"
          value={dateTo}
          data-testid="logs-filter-to"
          onChange={(e) => {
            setPage(0);
            setDateTo(e.target.value);
          }}
          InputLabelProps={{ shrink: true }}
        />

        <TextField
          size="small"
          label="Search in message"
          value={searchInput}
          data-testid="logs-filter-search"
          onChange={(e) => setSearchInput(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && applySearch()}
          InputProps={{
            endAdornment: (
              <InputAdornment position="end">
                <IconButton size="small" onClick={applySearch} aria-label="search">
                  <SearchIcon />
                </IconButton>
              </InputAdornment>
            ),
          }}
          sx={{ minWidth: 240 }}
        />
      </Paper>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          Failed to load logs: {(error as Error).message}
        </Alert>
      )}

      <TableContainer component={Paper}>
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell width={40} />
              <TableCell>Time</TableCell>
              <TableCell>Level</TableCell>
              <TableCell>Platform</TableCell>
              <TableCell>Tag</TableCell>
              <TableCell>Message</TableCell>
              <TableCell>Version</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {isLoading && (
              <TableRow>
                <TableCell colSpan={7} align="center">
                  Loading…
                </TableCell>
              </TableRow>
            )}
            {!isLoading && logs.length === 0 && (
              <TableRow>
                <TableCell colSpan={7} align="center">
                  No logs found
                </TableCell>
              </TableRow>
            )}
            {logs.map((log: ClientLogEntry) => (
              <React.Fragment key={log.id}>
                <TableRow hover data-testid="log-row">
                  <TableCell>
                    {log.stackTrace && (
                      <IconButton
                        size="small"
                        onClick={() => setExpandedId(expandedId === log.id ? null : log.id)}
                        aria-label="toggle stack trace"
                      >
                        {expandedId === log.id ? <ExpandLessIcon /> : <ExpandMoreIcon />}
                      </IconButton>
                    )}
                  </TableCell>
                  <TableCell sx={{ whiteSpace: 'nowrap' }}>
                    {new Date(log.createdAt).toLocaleString()}
                  </TableCell>
                  <TableCell>
                    <Chip
                      size="small"
                      label={log.level}
                      color={log.level === 'ERROR' ? 'error' : 'warning'}
                      variant="outlined"
                    />
                  </TableCell>
                  <TableCell>{log.platform}</TableCell>
                  <TableCell>{log.tag}</TableCell>
                  <TableCell sx={{ maxWidth: 420, wordBreak: 'break-word' }}>{log.message}</TableCell>
                  <TableCell>{log.appVersion ?? '—'}</TableCell>
                </TableRow>
                {log.stackTrace && (
                  <TableRow>
                    <TableCell colSpan={7} sx={{ py: 0, borderBottom: expandedId === log.id ? undefined : 0 }}>
                      <Collapse in={expandedId === log.id} unmountOnExit>
                        <Box
                          component="pre"
                          sx={{
                            p: 2,
                            m: 1,
                            fontSize: '0.75rem',
                            overflow: 'auto',
                            maxHeight: 300,
                            bgcolor: 'action.hover',
                            borderRadius: 1,
                          }}
                        >
                          {log.stackTrace}
                        </Box>
                      </Collapse>
                    </TableCell>
                  </TableRow>
                )}
              </React.Fragment>
            ))}
          </TableBody>
        </Table>
        <TablePagination
          component="div"
          count={data?.totalElements ?? 0}
          page={page}
          onPageChange={(_, newPage) => setPage(newPage)}
          rowsPerPage={rowsPerPage}
          onRowsPerPageChange={(e) => {
            setPage(0);
            setRowsPerPage(parseInt(e.target.value, 10));
          }}
          rowsPerPageOptions={[10, 20, 50, 100]}
        />
      </TableContainer>
    </Box>
  );
};

export default ClientLogs;
