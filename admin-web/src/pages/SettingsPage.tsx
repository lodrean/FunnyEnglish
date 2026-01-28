import { Box, Card, CardContent, Chip, CircularProgress, Grid, Typography } from '@mui/material';
import { useQuery } from '@tanstack/react-query';
import { getAdminSettings } from '../api/client';

interface SettingRowProps {
  label: string;
  value: string;
}

function SettingRow({ label, value }: SettingRowProps) {
  return (
    <Box sx={{ display: 'flex', justifyContent: 'space-between', py: 1 }}>
      <Typography variant="body2" color="text.secondary">
        {label}
      </Typography>
      <Typography variant="body2" fontWeight="medium" sx={{ textAlign: 'right', ml: 2 }}>
        {value}
      </Typography>
    </Box>
  );
}

export default function SettingsPage() {
  const { data, isLoading } = useQuery({
    queryKey: ['admin-settings'],
    queryFn: getAdminSettings,
  });

  if (isLoading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
        <CircularProgress />
      </Box>
    );
  }

  if (!data) {
    return (
      <Box>
        <Typography variant="h5" fontWeight="bold" mb={3}>
          Настройки
        </Typography>
        <Typography color="text.secondary">Нет данных для отображения</Typography>
      </Box>
    );
  }

  return (
    <Box>
      <Typography variant="h5" fontWeight="bold" mb={3}>
        Настройки
      </Typography>

      <Grid container spacing={3}>
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" mb={2}>
                Хранилище
              </Typography>
              <SettingRow label="Endpoint" value={data.s3Endpoint} />
              <SettingRow label="Bucket" value={data.s3Bucket} />
              <SettingRow label="Регион" value={data.s3Region} />
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" mb={2}>
                Загрузка файлов
              </Typography>
              <SettingRow label="Макс. размер файла" value={data.maxFileSize} />
              <SettingRow label="Макс. размер запроса" value={data.maxRequestSize} />
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12}>
          <Card>
            <CardContent>
              <Typography variant="h6" mb={2}>
                CORS
              </Typography>
              <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                {data.corsAllowedOrigins.length > 0 ? (
                  data.corsAllowedOrigins.map((origin) => (
                    <Chip key={origin} label={origin} size="small" />
                  ))
                ) : (
                  <Typography color="text.secondary">Не задано</Typography>
                )}
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
}
