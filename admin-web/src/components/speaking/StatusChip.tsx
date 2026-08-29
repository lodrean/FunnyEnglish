import { Chip, useTheme } from '@mui/material';
import type { ChipProps, SxProps, Theme } from '@mui/material';

interface StatusChipProps extends Omit<ChipProps, 'color' | 'label'> {
  status: 'NEW' | 'REVIEWED';
  label?: string;
  sx?: SxProps<Theme>;
}

/**
 * Единый статус-чип отправки (NEW/REVIEWED) — admin-web.
 * Цвета — ТОЛЬКО из theme.palette.speaking.status (light + dark-токены 1:1
 * с composeApp DarkSpeakingColors): container-фон + text.primary.
 * WCAG AA в обеих темах: белый/акцентный текст на warning/success = FAIL,
 * поэтому текст — text.primary (9.2/8.7:1 на light-контейнерах).
 */
export default function StatusChip({ status, label, sx, ...rest }: StatusChipProps) {
  const { speaking } = useTheme().palette;
  return (
    <Chip
      label={label ?? status}
      size="small"
      sx={[
        {
          bgcolor:
            status === 'NEW'
              ? speaking.status.newContainer
              : speaking.status.reviewedContainer,
          color: 'text.primary',
          fontWeight: 700,
        },
        ...(Array.isArray(sx) ? sx : [sx]),
      ]}
      {...rest}
    />
  );
}
