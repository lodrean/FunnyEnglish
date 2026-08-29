import { useSubmissions } from '../../hooks/useSpeaking';
import StatusChip from './StatusChip';

/**
 * Бейдж количества NEW-записей на пункте меню Grading (G8, мокап frame-grading:
 * «Grading 7 new»). Стиль — единый StatusChip (токены speaking.status,
 * light + dark). При 0 — не рендерится.
 */
export default function GradingNavBadge() {
  const { data } = useSubmissions({ status: 'NEW', page: 0, size: 1 });
  const count = data?.totalElements ?? 0;
  if (count === 0) return null;
  return (
    <StatusChip
      status="NEW"
      label={`${count} new`}
      data-testid="grading-new-badge"
      sx={{ height: 20, ml: 1 }}
    />
  );
}
