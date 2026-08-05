import { Chip } from '@mui/material';
import { useSubmissions } from '../../hooks/useSpeaking';

/**
 * Бейдж количества NEW-записей на пункте меню Grading (G8, мокап frame-grading:
 * «Grading 7 new»). Стиль chip-new: bg #FFE0B2 (--color-status-new-container),
 * текст #8a5200. При 0 — не рендерится.
 */
export default function GradingNavBadge() {
  const { data } = useSubmissions({ status: 'NEW', page: 0, size: 1 });
  const count = data?.totalElements ?? 0;
  if (count === 0) return null;
  return (
    <Chip
      label={`${count} new`}
      size="small"
      data-testid="grading-new-badge"
      sx={{
        bgcolor: '#FFE0B2',
        color: '#8a5200',
        fontWeight: 700,
        height: 20,
        ml: 1,
      }}
    />
  );
}
