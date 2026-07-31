import React, { useState } from 'react';
import {
  Box,
  Typography,
  Button,
  Paper,
  Grid,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Switch,
  FormControlLabel,
  Divider,
  Alert,
  Snackbar,
  Avatar,
  Chip,
  Tabs,
  Tab,
  InputAdornment,
  IconButton,
  Tooltip,
  Card,
  CardContent,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  ListItemSecondaryAction,
} from '@mui/material';
import {
  Save as SaveIcon,
  Image as ImageIcon,
  Email as EmailIcon,
  Notifications as NotificationsIcon,
  Palette as PaletteIcon,
  Security as SecurityIcon,
  Language as LanguageIcon,
  Visibility as VisibilityIcon,
  VisibilityOff as VisibilityOffIcon,
  CheckCircle as CheckCircleIcon,
  Info as InfoIcon,
  Settings as SettingsIcon,
} from '@mui/icons-material';
import { useMutation } from '@tanstack/react-query';

// Design System Colors
const COLORS = {
  primary: '#4A90D9',
  success: '#43A047',
  error: '#E53935',
  warning: '#FB8C00',
  info: '#2196F3',
  background: '#F5F5F5',
  card: '#FFFFFF',
  textPrimary: '#212121',
  textSecondary: '#757575',
};

// Types
interface GeneralSettings {
  siteName: string;
  siteDescription: string;
  logoUrl: string;
  faviconUrl: string;
  contactEmail: string;
  supportPhone: string;
}

interface EmailSettings {
  smtpHost: string;
  smtpPort: number;
  smtpUsername: string;
  smtpPassword: string;
  fromEmail: string;
  fromName: string;
  enableSsl: boolean;
}

interface NotificationSettings {
  emailOnRegistration: boolean;
  emailOnTestCompletion: boolean;
  emailOnNewUser: boolean;
  emailOnReport: boolean;
  pushNotifications: boolean;
  dailyDigest: boolean;
  weeklyReport: boolean;
}

interface ThemeSettings {
  primaryColor: string;
  fontFamily: string;
  borderRadius: number;
  darkMode: boolean;
  compactMode: boolean;
}

interface SettingsData {
  general: GeneralSettings;
  email: EmailSettings;
  notifications: NotificationSettings;
  theme: ThemeSettings;
}

// Tab Panel Component
interface TabPanelProps {
  children: React.ReactNode;
  value: number;
  index: number;
}

const TabPanel: React.FC<TabPanelProps> = ({ children, value, index }) => (
  <Box role="tabpanel" hidden={value !== index} sx={{ py: 3 }}>
    {value === index && children}
  </Box>
);

// Default settings
const defaultSettings: SettingsData = {
  general: {
    siteName: 'FunnyEnglish',
    siteDescription: 'Learn English with fun and interactive tests',
    logoUrl: '',
    faviconUrl: '',
    contactEmail: 'support@funnyenglish.com',
    supportPhone: '+1 (555) 123-4567',
  },
  email: {
    smtpHost: 'smtp.gmail.com',
    smtpPort: 587,
    smtpUsername: 'noreply@funnyenglish.com',
    smtpPassword: '',
    fromEmail: 'noreply@funnyenglish.com',
    fromName: 'FunnyEnglish',
    enableSsl: true,
  },
  notifications: {
    emailOnRegistration: true,
    emailOnTestCompletion: false,
    emailOnNewUser: true,
    emailOnReport: true,
    pushNotifications: true,
    dailyDigest: false,
    weeklyReport: true,
  },
  theme: {
    primaryColor: '#4A90D9',
    fontFamily: 'Roboto',
    borderRadius: 8,
    darkMode: false,
    compactMode: false,
  },
};

// Mock API
const saveSettings = async (settings: SettingsData): Promise<SettingsData> => {
  await new Promise((resolve) => setTimeout(resolve, 1000));
  return settings;
};

const Settings: React.FC = () => {
  const [activeTab, setActiveTab] = useState(0);
  const [settings, setSettings] = useState<SettingsData>(defaultSettings);
  const [hasChanges, setHasChanges] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' as 'success' | 'error' });

  const saveMutation = useMutation({
    mutationFn: saveSettings,
    onSuccess: () => {
      setHasChanges(false);
      setSnackbar({ open: true, message: 'Settings saved successfully!', severity: 'success' });
    },
    onError: () => {
      setSnackbar({ open: true, message: 'Failed to save settings.', severity: 'error' });
    },
  });

  const handleChange = (section: keyof SettingsData, field: string, value: any) => {
    setSettings((prev) => ({
      ...prev,
      [section]: {
        ...prev[section],
        [field]: value,
      },
    }));
    setHasChanges(true);
  };

  const handleSave = () => {
    saveMutation.mutate(settings);
  };

  const handleReset = () => {
    setSettings(defaultSettings);
    setHasChanges(true);
  };

  const colorOptions = [
    { name: 'Blue', value: '#4A90D9' },
    { name: 'Green', value: '#43A047' },
    { name: 'Purple', value: '#7B1FA2' },
    { name: 'Orange', value: '#FB8C00' },
    { name: 'Red', value: '#E53935' },
    { name: 'Teal', value: '#00897B' },
  ];

  const fontOptions = ['Roboto', 'Open Sans', 'Lato', 'Montserrat', 'Poppins'];

  return (
    <Box p={{ xs: 2, md: 3 }}>
      {/* Header */}
      <Box display="flex" flexDirection={{ xs: 'column', sm: 'row' }} justifyContent="space-between" alignItems={{ xs: 'flex-start', sm: 'center' }} mb={3} gap={2}>
        <Typography variant="h4" fontWeight="bold" color={COLORS.textPrimary}>
          Settings
        </Typography>
        <Box display="flex" gap={1}>
          <Button variant="outlined" onClick={handleReset}>
            Reset
          </Button>
          <Button
            variant="contained"
            startIcon={<SaveIcon />}
            onClick={handleSave}
            disabled={!hasChanges || saveMutation.isPending}
            sx={{ backgroundColor: COLORS.primary }}
          >
            {saveMutation.isPending ? 'Saving...' : 'Save Changes'}
          </Button>
        </Box>
      </Box>

      {hasChanges && (
        <Alert severity="info" sx={{ mb: 3 }}>
          You have unsaved changes. Don&apos;t forget to save!
        </Alert>
      )}

      {/* Tabs */}
      <Paper sx={{ mb: 3 }}>
        <Tabs
          value={activeTab}
          onChange={(_, v) => setActiveTab(v)}
          variant="scrollable"
          scrollButtons="auto"
        >
          <Tab icon={<SettingsIcon fontSize="small" />} iconPosition="start" label="General" />
          <Tab icon={<EmailIcon fontSize="small" />} iconPosition="start" label="Email" />
          <Tab icon={<NotificationsIcon fontSize="small" />} iconPosition="start" label="Notifications" />
          <Tab icon={<PaletteIcon fontSize="small" />} iconPosition="start" label="Theme" />
        </Tabs>

        {/* General Settings */}
        <TabPanel value={activeTab} index={0}>
          <Box px={3} pb={3}>
            <Grid container spacing={3}>
              <Grid size={{ xs: 12, md: 6 }}>
                <Typography variant="h6" gutterBottom>
                  Site Information
                </Typography>
                
                <TextField
                  label="Site Name"
                  fullWidth
                  margin="normal"
                  value={settings.general.siteName}
                  onChange={(e) => handleChange('general', 'siteName', e.target.value)}
                />
                
                <TextField
                  label="Site Description"
                  fullWidth
                  margin="normal"
                  multiline
                  rows={3}
                  value={settings.general.siteDescription}
                  onChange={(e) => handleChange('general', 'siteDescription', e.target.value)}
                />
                
                <TextField
                  label="Contact Email"
                  fullWidth
                  margin="normal"
                  type="email"
                  value={settings.general.contactEmail}
                  onChange={(e) => handleChange('general', 'contactEmail', e.target.value)}
                />
                
                <TextField
                  label="Support Phone"
                  fullWidth
                  margin="normal"
                  value={settings.general.supportPhone}
                  onChange={(e) => handleChange('general', 'supportPhone', e.target.value)}
                />
              </Grid>

              <Grid size={{ xs: 12, md: 6 }}>
                <Typography variant="h6" gutterBottom>
                  Branding
                </Typography>
                
                <Box mb={3}>
                  <Typography variant="subtitle2" gutterBottom>
                    Site Logo
                  </Typography>
                  <Box
                    sx={{
                      width: 200,
                      height: 100,
                      backgroundColor: '#F5F5F5',
                      borderRadius: 1,
                      display: 'flex',
                      flexDirection: 'column',
                      alignItems: 'center',
                      justifyContent: 'center',
                      border: '2px dashed #E0E0E0',
                      cursor: 'pointer',
                      '&:hover': {
                        borderColor: COLORS.primary,
                        backgroundColor: 'rgba(74, 144, 217, 0.05)',
                      },
                    }}
                  >
                    <ImageIcon sx={{ fontSize: 32, color: COLORS.textSecondary, mb: 1 }} />
                    <Typography variant="caption" color="text.secondary">
                      Click to upload logo
                    </Typography>
                  </Box>
                </Box>

                <Box>
                  <Typography variant="subtitle2" gutterBottom>
                    Favicon
                  </Typography>
                  <Box
                    sx={{
                      width: 64,
                      height: 64,
                      backgroundColor: '#F5F5F5',
                      borderRadius: 1,
                      display: 'flex',
                      flexDirection: 'column',
                      alignItems: 'center',
                      justifyContent: 'center',
                      border: '2px dashed #E0E0E0',
                      cursor: 'pointer',
                      '&:hover': {
                        borderColor: COLORS.primary,
                        backgroundColor: 'rgba(74, 144, 217, 0.05)',
                      },
                    }}
                  >
                    <ImageIcon sx={{ fontSize: 24, color: COLORS.textSecondary }} />
                  </Box>
                </Box>
              </Grid>
            </Grid>
          </Box>
        </TabPanel>

        {/* Email Settings */}
        <TabPanel value={activeTab} index={1}>
          <Box px={3} pb={3}>
            <Grid container spacing={3}>
              <Grid size={{ xs: 12, md: 6 }}>
                <Typography variant="h6" gutterBottom>
                  SMTP Configuration
                </Typography>
                
                <TextField
                  label="SMTP Host"
                  fullWidth
                  margin="normal"
                  value={settings.email.smtpHost}
                  onChange={(e) => handleChange('email', 'smtpHost', e.target.value)}
                />
                
                <TextField
                  label="SMTP Port"
                  fullWidth
                  margin="normal"
                  type="number"
                  value={settings.email.smtpPort}
                  onChange={(e) => handleChange('email', 'smtpPort', parseInt(e.target.value))}
                />
                
                <TextField
                  label="SMTP Username"
                  fullWidth
                  margin="normal"
                  value={settings.email.smtpUsername}
                  onChange={(e) => handleChange('email', 'smtpUsername', e.target.value)}
                />
                
                <TextField
                  label="SMTP Password"
                  fullWidth
                  margin="normal"
                  type={showPassword ? 'text' : 'password'}
                  value={settings.email.smtpPassword}
                  onChange={(e) => handleChange('email', 'smtpPassword', e.target.value)}
                  InputProps={{
                    endAdornment: (
                      <InputAdornment position="end">
                        <IconButton onClick={() => setShowPassword(!showPassword)} edge="end">
                          {showPassword ? <VisibilityOffIcon /> : <VisibilityIcon />}
                        </IconButton>
                      </InputAdornment>
                    ),
                  }}
                />
                
                <FormControlLabel
                  control={
                    <Switch
                      checked={settings.email.enableSsl}
                      onChange={(e) => handleChange('email', 'enableSsl', e.target.checked)}
                    />
                  }
                  label="Enable SSL/TLS"
                  sx={{ mt: 2 }}
                />
              </Grid>

              <Grid size={{ xs: 12, md: 6 }}>
                <Typography variant="h6" gutterBottom>
                  Sender Information
                </Typography>
                
                <TextField
                  label="From Email"
                  fullWidth
                  margin="normal"
                  type="email"
                  value={settings.email.fromEmail}
                  onChange={(e) => handleChange('email', 'fromEmail', e.target.value)}
                />
                
                <TextField
                  label="From Name"
                  fullWidth
                  margin="normal"
                  value={settings.email.fromName}
                  onChange={(e) => handleChange('email', 'fromName', e.target.value)}
                />

                <Box mt={3}>
                  <Button
                    variant="outlined"
                    startIcon={<EmailIcon />}
                    onClick={() => setSnackbar({ open: true, message: 'Test email sent!', severity: 'success' })}
                  >
                    Send Test Email
                  </Button>
                </Box>
              </Grid>
            </Grid>
          </Box>
        </TabPanel>

        {/* Notification Settings */}
        <TabPanel value={activeTab} index={2}>
          <Box px={3} pb={3}>
            <Typography variant="h6" gutterBottom>
              Email Notifications
            </Typography>
            
            <List>
              <ListItem>
                <ListItemIcon>
                  <EmailIcon sx={{ color: COLORS.primary }} />
                </ListItemIcon>
                <ListItemText
                  primary="User Registration"
                  secondary="Send email when a new user registers"
                />
                <ListItemSecondaryAction>
                  <Switch
                    checked={settings.notifications.emailOnRegistration}
                    onChange={(e) => handleChange('notifications', 'emailOnRegistration', e.target.checked)}
                  />
                </ListItemSecondaryAction>
              </ListItem>
              
              <ListItem>
                <ListItemIcon>
                  <CheckCircleIcon sx={{ color: COLORS.success }} />
                </ListItemIcon>
                <ListItemText
                  primary="Test Completion"
                  secondary="Send email when a user completes a test"
                />
                <ListItemSecondaryAction>
                  <Switch
                    checked={settings.notifications.emailOnTestCompletion}
                    onChange={(e) => handleChange('notifications', 'emailOnTestCompletion', e.target.checked)}
                  />
                </ListItemSecondaryAction>
              </ListItem>
              
              <ListItem>
                <ListItemIcon>
                  <NotificationsIcon sx={{ color: COLORS.info }} />
                </ListItemIcon>
                <ListItemText
                  primary="New User Alert"
                  secondary="Notify admins of new user registrations"
                />
                <ListItemSecondaryAction>
                  <Switch
                    checked={settings.notifications.emailOnNewUser}
                    onChange={(e) => handleChange('notifications', 'emailOnNewUser', e.target.checked)}
                  />
                </ListItemSecondaryAction>
              </ListItem>
              
              <ListItem>
                <ListItemIcon>
                  <InfoIcon sx={{ color: COLORS.warning }} />
                </ListItemIcon>
                <ListItemText
                  primary="Reports & Alerts"
                  secondary="Send system reports and alerts"
                />
                <ListItemSecondaryAction>
                  <Switch
                    checked={settings.notifications.emailOnReport}
                    onChange={(e) => handleChange('notifications', 'emailOnReport', e.target.checked)}
                  />
                </ListItemSecondaryAction>
              </ListItem>
            </List>

            <Divider sx={{ my: 3 }} />

            <Typography variant="h6" gutterBottom>
              Digest & Reports
            </Typography>
            
            <List>
              <ListItem>
                <ListItemText
                  primary="Daily Digest"
                  secondary="Receive a daily summary of activities"
                />
                <ListItemSecondaryAction>
                  <Switch
                    checked={settings.notifications.dailyDigest}
                    onChange={(e) => handleChange('notifications', 'dailyDigest', e.target.checked)}
                  />
                </ListItemSecondaryAction>
              </ListItem>
              
              <ListItem>
                <ListItemText
                  primary="Weekly Report"
                  secondary="Receive a weekly analytics report"
                />
                <ListItemSecondaryAction>
                  <Switch
                    checked={settings.notifications.weeklyReport}
                    onChange={(e) => handleChange('notifications', 'weeklyReport', e.target.checked)}
                  />
                </ListItemSecondaryAction>
              </ListItem>
              
              <ListItem>
                <ListItemText
                  primary="Push Notifications"
                  secondary="Enable browser push notifications"
                />
                <ListItemSecondaryAction>
                  <Switch
                    checked={settings.notifications.pushNotifications}
                    onChange={(e) => handleChange('notifications', 'pushNotifications', e.target.checked)}
                  />
                </ListItemSecondaryAction>
              </ListItem>
            </List>
          </Box>
        </TabPanel>

        {/* Theme Settings */}
        <TabPanel value={activeTab} index={3}>
          <Box px={3} pb={3}>
            <Grid container spacing={3}>
              <Grid size={{ xs: 12, md: 6 }}>
                <Typography variant="h6" gutterBottom>
                  Appearance
                </Typography>
                
                <Box mb={3}>
                  <Typography variant="subtitle2" gutterBottom>
                    Primary Color
                  </Typography>
                  <Box display="flex" gap={1} flexWrap="wrap">
                    {colorOptions.map((color) => (
                      <Tooltip key={color.value} title={color.name}>
                        <Box
                          onClick={() => handleChange('theme', 'primaryColor', color.value)}
                          sx={{
                            width: 40,
                            height: 40,
                            borderRadius: 1,
                            backgroundColor: color.value,
                            cursor: 'pointer',
                            border: settings.theme.primaryColor === color.value ? '3px solid #212121' : '3px solid transparent',
                            '&:hover': {
                              transform: 'scale(1.1)',
                            },
                            transition: 'all 0.2s',
                          }}
                        />
                      </Tooltip>
                    ))}
                  </Box>
                </Box>

                <FormControl fullWidth margin="normal">
                  <InputLabel>Font Family</InputLabel>
                  <Select
                    value={settings.theme.fontFamily}
                    label="Font Family"
                    onChange={(e) => handleChange('theme', 'fontFamily', e.target.value)}
                  >
                    {fontOptions.map((font) => (
                      <MenuItem key={font} value={font} style={{ fontFamily: font }}>
                        {font}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>

                <Box mt={3}>
                  <Typography variant="subtitle2" gutterBottom>
                    Border Radius: {settings.theme.borderRadius}px
                  </Typography>
                  <input
                    type="range"
                    min={0}
                    max={24}
                    value={settings.theme.borderRadius}
                    onChange={(e) => handleChange('theme', 'borderRadius', parseInt(e.target.value))}
                    style={{ width: '100%' }}
                  />
                </Box>
              </Grid>

              <Grid size={{ xs: 12, md: 6 }}>
                <Typography variant="h6" gutterBottom>
                  Display Options
                </Typography>
                
                <Card sx={{ mb: 2 }}>
                  <CardContent>
                    <FormControlLabel
                      control={
                        <Switch
                          checked={settings.theme.darkMode}
                          onChange={(e) => handleChange('theme', 'darkMode', e.target.checked)}
                        />
                      }
                      label="Dark Mode"
                    />
                    <Typography variant="body2" color="text.secondary">
                      Enable dark theme for the admin panel
                    </Typography>
                  </CardContent>
                </Card>

                <Card>
                  <CardContent>
                    <FormControlLabel
                      control={
                        <Switch
                          checked={settings.theme.compactMode}
                          onChange={(e) => handleChange('theme', 'compactMode', e.target.checked)}
                        />
                      }
                      label="Compact Mode"
                    />
                    <Typography variant="body2" color="text.secondary">
                      Reduce spacing for a more compact view
                    </Typography>
                  </CardContent>
                </Card>

                <Box mt={3}>
                  <Typography variant="subtitle2" gutterBottom>
                    Preview
                  </Typography>
                  <Paper
                    sx={{
                      p: 2,
                      backgroundColor: settings.theme.darkMode ? '#1E1E1E' : COLORS.card,
                      borderRadius: `${settings.theme.borderRadius}px`,
                    }}
                  >
                    <Button
                      variant="contained"
                      sx={{
                        backgroundColor: settings.theme.primaryColor,
                        borderRadius: `${settings.theme.borderRadius}px`,
                        fontFamily: settings.theme.fontFamily,
                        mr: 1,
                      }}
                    >
                      Primary Button
                    </Button>
                    <Button
                      variant="outlined"
                      sx={{
                        borderColor: settings.theme.primaryColor,
                        color: settings.theme.primaryColor,
                        borderRadius: `${settings.theme.borderRadius}px`,
                        fontFamily: settings.theme.fontFamily,
                      }}
                    >
                      Secondary
                    </Button>
                  </Paper>
                </Box>
              </Grid>
            </Grid>
          </Box>
        </TabPanel>
      </Paper>

      {/* Snackbar */}
      <Snackbar
        open={snackbar.open}
        autoHideDuration={6000}
        onClose={() => setSnackbar({ ...snackbar, open: false })}
      >
        <Alert
          severity={snackbar.severity}
          onClose={() => setSnackbar({ ...snackbar, open: false })}
        >
          {snackbar.message}
        </Alert>
      </Snackbar>
    </Box>
  );
};

export default Settings;
