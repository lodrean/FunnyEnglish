import { 
  BarChart, Bar, XAxis, YAxis, CartesianGrid, 
  Tooltip, ResponsiveContainer, AreaChart, Area, PieChart, Pie, Cell 
} from 'recharts';
import { useTheme } from '@/design-system/components/ThemeProvider';

const lineData = [
  { name: 'Mon', users: 400, tests: 240 },
  { name: 'Tue', users: 300, tests: 139 },
  { name: 'Wed', users: 200, tests: 980 },
  { name: 'Thu', users: 278, tests: 390 },
  { name: 'Fri', users: 189, tests: 480 },
  { name: 'Sat', users: 239, tests: 380 },
  { name: 'Sun', users: 349, tests: 430 },
];

const barData = [
  { name: 'Grammar', completed: 400, started: 650 },
  { name: 'Vocabulary', completed: 300, started: 500 },
  { name: 'Listening', completed: 200, started: 350 },
  { name: 'Reading', completed: 278, started: 450 },
  { name: 'Writing', completed: 189, started: 300 },
];

const pieData = [
  { name: 'Beginner', value: 400 },
  { name: 'Intermediate', value: 300 },
  { name: 'Advanced', value: 300 },
  { name: 'Expert', value: 200 },
];

const recentActivity = [
  { id: 1, user: 'John Doe', action: 'completed', target: 'Basic Grammar Test', time: '2 min ago' },
  { id: 2, user: 'Jane Smith', action: 'started', target: 'Vocabulary 101', time: '5 min ago' },
  { id: 3, user: 'Mike Johnson', action: 'created', target: 'New Test', time: '15 min ago' },
  { id: 4, user: 'Sarah Williams', action: 'completed', target: 'Listening Exercise', time: '1 hour ago' },
  { id: 5, user: 'Tom Brown', action: 'earned', target: 'Achievement: First 100', time: '2 hours ago' },
];

export function DashboardLayout() {
  const { theme } = useTheme();
  const isDark = theme === 'dark';
  
  const chartColors = {
    primary: isDark ? '#60A5FA' : '#4A90D9',
    secondary: isDark ? '#4ADE80' : '#43A047',
    tertiary: isDark ? '#FBBF24' : '#FB8C00',
    quaternary: isDark ? '#F87171' : '#E53935',
    grid: isDark ? '#334155' : '#E0E0E0',
    text: isDark ? '#94A3B8' : '#757575',
  };

  const pieColors = [chartColors.primary, chartColors.secondary, chartColors.tertiary, chartColors.quaternary];

  return (
    <section className="page-section">
      <div className="section-header">
        <h2 className="section-title">Dashboard</h2>
        <p className="section-description">
          Main dashboard with analytics, charts, and recent activity.
        </p>
      </div>

      <div className="dashboard-layout">
        {/* Stats Cards */}
        <div className="stats-row">
          <div className="stats-card stats-card-large">
            <div className="stats-card-icon stats-card-icon-primary">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
                <circle cx="9" cy="7" r="4" />
                <path d="M23 21v-2a4 4 0 0 0-3-3.87" />
                <path d="M16 3.13a4 4 0 0 1 0 7.75" />
              </svg>
            </div>
            <div className="stats-card-content">
              <span className="stats-card-value">12,345</span>
              <span className="stats-card-label">Total Users</span>
              <span className="stats-card-trend stats-card-trend-up">
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <path d="M23 6l-9.5 9.5-5-5L1 18" />
                  <path d="M17 6h6v6" />
                </svg>
                +12.5% from last month
              </span>
            </div>
          </div>

          <div className="stats-card stats-card-large">
            <div className="stats-card-icon stats-card-icon-success">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                <polyline points="14 2 14 8 20 8" />
                <line x1="16" y1="13" x2="8" y2="13" />
                <line x1="16" y1="17" x2="8" y2="17" />
                <polyline points="10 9 9 9 8 9" />
              </svg>
            </div>
            <div className="stats-card-content">
              <span className="stats-card-value">856</span>
              <span className="stats-card-label">Active Tests</span>
              <span className="stats-card-trend stats-card-trend-up">
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <path d="M23 6l-9.5 9.5-5-5L1 18" />
                  <path d="M17 6h6v6" />
                </svg>
                +8.2% from last month
              </span>
            </div>
          </div>

          <div className="stats-card stats-card-large">
            <div className="stats-card-icon stats-card-icon-warning">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <circle cx="12" cy="12" r="10" />
                <polyline points="12 6 12 12 16 14" />
              </svg>
            </div>
            <div className="stats-card-content">
              <span className="stats-card-value">2.4h</span>
              <span className="stats-card-label">Avg. Session</span>
              <span className="stats-card-trend stats-card-trend-down">
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <path d="M23 18l-9.5-9.5-5 5L1 6" />
                  <path d="M17 18h6v-6" />
                </svg>
                -3.1% from last month
              </span>
            </div>
          </div>

          <div className="stats-card stats-card-large">
            <div className="stats-card-icon stats-card-icon-info">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z" />
              </svg>
            </div>
            <div className="stats-card-content">
              <span className="stats-card-value">94.2%</span>
              <span className="stats-card-label">Completion Rate</span>
              <span className="stats-card-trend stats-card-trend-up">
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <path d="M23 6l-9.5 9.5-5-5L1 18" />
                  <path d="M17 6h6v6" />
                </svg>
                +5.4% from last month
              </span>
            </div>
          </div>
        </div>

        {/* Charts Row */}
        <div className="charts-row">
          <div className="chart-card">
            <div className="chart-header">
              <h4 className="chart-title">User Activity</h4>
              <div className="chart-legend">
                <span className="legend-item">
                  <span className="legend-dot" style={{ background: chartColors.primary }} />
                  Active Users
                </span>
                <span className="legend-item">
                  <span className="legend-dot" style={{ background: chartColors.secondary }} />
                  Tests Taken
                </span>
              </div>
            </div>
            <div className="chart-body">
              <ResponsiveContainer width="100%" height={250}>
                <AreaChart data={lineData}>
                  <defs>
                    <linearGradient id="colorUsers" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor={chartColors.primary} stopOpacity={0.3}/>
                      <stop offset="95%" stopColor={chartColors.primary} stopOpacity={0}/>
                    </linearGradient>
                    <linearGradient id="colorTests" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor={chartColors.secondary} stopOpacity={0.3}/>
                      <stop offset="95%" stopColor={chartColors.secondary} stopOpacity={0}/>
                    </linearGradient>
                  </defs>
                  <CartesianGrid strokeDasharray="3 3" stroke={chartColors.grid} />
                  <XAxis dataKey="name" stroke={chartColors.text} fontSize={12} />
                  <YAxis stroke={chartColors.text} fontSize={12} />
                  <Tooltip 
                    contentStyle={{ 
                      backgroundColor: isDark ? '#1E293B' : '#FFFFFF',
                      border: `1px solid ${chartColors.grid}`,
                      borderRadius: '8px'
                    }}
                  />
                  <Area type="monotone" dataKey="users" stroke={chartColors.primary} fillOpacity={1} fill="url(#colorUsers)" />
                  <Area type="monotone" dataKey="tests" stroke={chartColors.secondary} fillOpacity={1} fill="url(#colorTests)" />
                </AreaChart>
              </ResponsiveContainer>
            </div>
          </div>

          <div className="chart-card">
            <div className="chart-header">
              <h4 className="chart-title">Test Completion by Category</h4>
            </div>
            <div className="chart-body">
              <ResponsiveContainer width="100%" height={250}>
                <BarChart data={barData}>
                  <CartesianGrid strokeDasharray="3 3" stroke={chartColors.grid} />
                  <XAxis dataKey="name" stroke={chartColors.text} fontSize={12} />
                  <YAxis stroke={chartColors.text} fontSize={12} />
                  <Tooltip 
                    contentStyle={{ 
                      backgroundColor: isDark ? '#1E293B' : '#FFFFFF',
                      border: `1px solid ${chartColors.grid}`,
                      borderRadius: '8px'
                    }}
                  />
                  <Bar dataKey="completed" fill={chartColors.primary} radius={[4, 4, 0, 0]} />
                  <Bar dataKey="started" fill={chartColors.tertiary} radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </div>
        </div>

        {/* Bottom Row */}
        <div className="dashboard-bottom-row">
          <div className="chart-card">
            <div className="chart-header">
              <h4 className="chart-title">User Levels Distribution</h4>
            </div>
            <div className="chart-body">
              <ResponsiveContainer width="100%" height={200}>
                <PieChart>
                  <Pie
                    data={pieData}
                    cx="50%"
                    cy="50%"
                    innerRadius={60}
                    outerRadius={80}
                    paddingAngle={5}
                    dataKey="value"
                  >
                    {pieData.map((_entry, index) => (
                      <Cell key={`cell-${index}`} fill={pieColors[index % pieColors.length]} />
                    ))}
                  </Pie>
                  <Tooltip 
                    contentStyle={{ 
                      backgroundColor: isDark ? '#1E293B' : '#FFFFFF',
                      border: `1px solid ${chartColors.grid}`,
                      borderRadius: '8px'
                    }}
                  />
                </PieChart>
              </ResponsiveContainer>
              <div className="pie-legend">
                {pieData.map((entry, index) => (
                  <span key={entry.name} className="legend-item">
                    <span className="legend-dot" style={{ background: pieColors[index] }} />
                    {entry.name}
                  </span>
                ))}
              </div>
            </div>
          </div>

          <div className="activity-card">
            <div className="card-header">
              <h4 className="card-title">Recent Activity</h4>
              <button className="btn btn-ghost btn-sm">View All</button>
            </div>
            <div className="activity-list">
              {recentActivity.map((activity) => (
                <div key={activity.id} className="activity-item">
                  <div className="activity-avatar">
                    <img src={`https://api.dicebear.com/7.x/avataaars/svg?seed=${activity.user}`} alt={activity.user} />
                  </div>
                  <div className="activity-content">
                    <p className="activity-text">
                      <strong>{activity.user}</strong> {activity.action} <strong>{activity.target}</strong>
                    </p>
                    <span className="activity-time">{activity.time}</span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Quick Actions */}
        <div className="quick-actions">
          <h4 className="quick-actions-title">Quick Actions</h4>
          <div className="quick-actions-grid">
            <button className="quick-action-btn">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M12 5v14M5 12h14" />
              </svg>
              Create Test
            </button>
            <button className="quick-action-btn">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
                <circle cx="9" cy="7" r="4" />
                <path d="M23 21v-2a4 4 0 0 0-3-3.87" />
                <path d="M16 3.13a4 4 0 0 1 0 7.75" />
              </svg>
              Add User
            </button>
            <button className="quick-action-btn">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M21.21 15.89A10 10 0 1 1 8 2.83" />
                <path d="M22 12A10 10 0 0 0 12 2v10z" />
              </svg>
              View Reports
            </button>
            <button className="quick-action-btn">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <circle cx="12" cy="12" r="3" />
                <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z" />
              </svg>
              Settings
            </button>
          </div>
        </div>
      </div>
    </section>
  );
}
