import { useState } from 'react';
import {
  Box,
  Button,
  Card,
  CardContent,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
} from '@mui/material';
import { Download as DownloadIcon } from '@mui/icons-material';
import { useQuery } from '@tanstack/react-query';
import { exportGradingCsv, getGradingAnalytics } from '../../api/speakingGrading';

/**
 * Grading-аналитика (bd h3l.4, PROJECT-REVIEW §4.2.1): средний балл по рубрике,
 * время NEW→REVIEWED, очередь NEW, распределение по топикам + экспорт CSV.
 * Встраивается в страницу Analytics отдельной секцией.
 */

const METRIC_MIN_WIDTH = 120;

export default function GradingAnalyticsSection() {
  const { data, isLoading } = useQuery({
    queryKey: ['speaking', 'grading-analytics'],
    queryFn: getGradingAnalytics,
  });
  const [exporting, setExporting] = useState(false);
  const [exportError, setExportError] = useState(false);

  const handleExport = async () => {
    setExportError(false);
    setExporting(true);
    try {
      await exportGradingCsv();
    } catch {
      setExportError(true);
    } finally {
      setExporting(false);
    }
  };

  const metrics: Array<{ label: string; value: string; testId: string }> = [
    {
      label: 'Средний общий балл',
      value: data?.averages.total != null ? data.averages.total.toFixed(1) : '—',
      testId: 'grading-avg-total',
    },
    {
      label: 'Очередь NEW',
      value: data ? String(data.newCount) : '—',
      testId: 'grading-new-count',
    },
    {
      label: 'Оценено всего',
      value: data ? String(data.reviewedCount) : '—',
      testId: 'grading-reviewed-count',
    },
    {
      label: 'Среднее время проверки',
      value:
        data?.avgReviewTimeMinutes != null
          ? `${data.avgReviewTimeMinutes.toFixed(1)} мин`
          : '—',
      testId: 'grading-avg-review-time',
    },
  ];

  return (
    <Paper sx={{ p: 3, mt: 4 }} data-testid="grading-analytics-section">
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
        <Typography variant="h6">Grading-аналитика (Speaking)</Typography>
        <Button
          variant="outlined"
          startIcon={<DownloadIcon />}
          onClick={handleExport}
          disabled={exporting}
          data-testid="grading-csv-export"
        >
          {exporting ? 'Экспорт…' : 'Экспорт CSV'}
        </Button>
      </Box>

      {exportError && (
        <Typography variant="caption" color="error" data-testid="grading-export-error">
          Не удалось скачать экспорт. Попробуйте ещё раз.
        </Typography>
      )}

      <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap', mb: 2 }}>
        {metrics.map(({ label, value, testId }) => (
          <Card key={label} sx={{ flex: `1 1 ${METRIC_MIN_WIDTH}px` }}>
            <CardContent>
              <Typography variant="caption" color="text.secondary">
                {label}
              </Typography>
              <Typography variant="h5" fontWeight={700} data-testid={testId}>
                {isLoading ? '…' : value}
              </Typography>
            </CardContent>
          </Card>
        ))}
      </Box>

      {data && (
        <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
          <Box sx={{ flex: '1 1 160px' }}>
            <Typography variant="body2" fontWeight={700} sx={{ mb: 1 }}>
              Средние по критериям
            </Typography>
            <Box sx={{ display: 'flex', gap: 2 }} data-testid="grading-criterion-averages">
              {(
                [
                  ['Grammar', data.averages.grammar],
                  ['Vocabulary', data.averages.vocabulary],
                  ['Pronunciation', data.averages.pronunciation],
                  ['Fluency', data.averages.fluency],
                ] as const
              ).map(([label, value]) => (
                <Typography key={label} variant="body2" color="text.secondary">
                  {label}: <strong>{value != null ? value.toFixed(1) : '—'}</strong>
                </Typography>
              ))}
            </Box>
          </Box>
        </Box>
      )}

      {(data?.byTopic.length ?? 0) > 0 && (
        <TableContainer sx={{ mt: 2 }} data-testid="grading-by-topic-table">
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>Топик</TableCell>
                <TableCell align="right">Оценок</TableCell>
                <TableCell align="right">Средний балл</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {data?.byTopic.map((row) => (
                <TableRow key={row.topicId}>
                  <TableCell>{row.topicTitle}</TableCell>
                  <TableCell align="right">{row.gradeCount}</TableCell>
                  <TableCell align="right">
                    {row.avgTotal != null ? row.avgTotal.toFixed(1) : '—'}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      )}
    </Paper>
  );
}
