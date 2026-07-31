export function DataDisplayShowcase() {
  const tableData = [
    { id: 1, name: 'Basic Grammar', category: 'Grammar', questions: 25, status: 'active' },
    { id: 2, name: 'Vocabulary 101', category: 'Vocabulary', questions: 40, status: 'active' },
    { id: 3, name: 'Listening Test', category: 'Listening', questions: 15, status: 'draft' },
    { id: 4, name: 'Reading Comprehension', category: 'Reading', questions: 30, status: 'archived' },
  ];

  return (
    <section className="component-section">
      <div className="section-header">
        <h2 className="section-title">Data Display</h2>
        <p className="section-description">
          Cards, badges, tables, and other components for displaying data.
        </p>
      </div>

      <div className="component-grid">
        {/* Cards */}
        <div className="component-group">
          <h4 className="component-group-title">Cards</h4>
          <div className="card-grid">
            <div className="card">
              <div className="card-header">
                <h5 className="card-title">Basic Card</h5>
                <span className="card-subtitle">With header and content</span>
              </div>
              <div className="card-body">
                <p>This is a basic card component with header, body, and optional footer.</p>
              </div>
              <div className="card-footer">
                <button className="btn btn-ghost btn-sm">Cancel</button>
                <button className="btn btn-primary btn-sm">Save</button>
              </div>
            </div>

            <div className="card card-elevated">
              <div className="card-body">
                <h5 className="card-title">Elevated Card</h5>
                <p>This card has elevated shadow for emphasis.</p>
              </div>
            </div>

            <div className="card card-bordered">
              <div className="card-body">
                <h5 className="card-title">Bordered Card</h5>
                <p>This card has a visible border.</p>
              </div>
            </div>

            <div className="card card-hover">
              <div className="card-body">
                <h5 className="card-title">Hover Card</h5>
                <p>This card has hover effect.</p>
              </div>
            </div>
          </div>
        </div>

        {/* Stats Cards */}
        <div className="component-group">
          <h4 className="component-group-title">Stats Cards</h4>
          <div className="stats-grid">
            <div className="stats-card">
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
                  +12.5%
                </span>
              </div>
            </div>

            <div className="stats-card">
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
                  +8.2%
                </span>
              </div>
            </div>

            <div className="stats-card">
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
                  -3.1%
                </span>
              </div>
            </div>
          </div>
        </div>

        {/* Badges */}
        <div className="component-group">
          <h4 className="component-group-title">Badges</h4>
          <div className="badge-row">
            <span className="badge badge-default">Default</span>
            <span className="badge badge-primary">Primary</span>
            <span className="badge badge-success">Success</span>
            <span className="badge badge-warning">Warning</span>
            <span className="badge badge-error">Error</span>
            <span className="badge badge-info">Info</span>
          </div>
          <div className="badge-row">
            <span className="badge badge-outline">Outline</span>
            <span className="badge badge-outline-primary">Primary</span>
            <span className="badge badge-outline-success">Success</span>
            <span className="badge badge-soft badge-primary">Soft</span>
            <span className="badge badge-soft badge-success">Soft</span>
          </div>
        </div>

        {/* Avatars */}
        <div className="component-group">
          <h4 className="component-group-title">Avatars</h4>
          <div className="avatar-row">
            <div className="avatar avatar-xs">
              <img src="https://api.dicebear.com/7.x/avataaars/svg?seed=1" alt="User" />
            </div>
            <div className="avatar avatar-sm">
              <img src="https://api.dicebear.com/7.x/avataaars/svg?seed=2" alt="User" />
            </div>
            <div className="avatar">
              <img src="https://api.dicebear.com/7.x/avataaars/svg?seed=3" alt="User" />
            </div>
            <div className="avatar avatar-lg">
              <img src="https://api.dicebear.com/7.x/avataaars/svg?seed=4" alt="User" />
            </div>
            <div className="avatar avatar-xl">
              <img src="https://api.dicebear.com/7.x/avataaars/svg?seed=5" alt="User" />
            </div>
          </div>
          <div className="avatar-row">
            <div className="avatar avatar-primary">JD</div>
            <div className="avatar avatar-secondary">AB</div>
            <div className="avatar avatar-success">+5</div>
          </div>
        </div>

        {/* Progress */}
        <div className="component-group">
          <h4 className="component-group-title">Progress</h4>
          <div className="progress-row">
            <div className="progress">
              <div className="progress-bar" style={{ width: '25%' }} />
            </div>
            <span className="progress-value">25%</span>
          </div>
          <div className="progress-row">
            <div className="progress">
              <div className="progress-bar progress-bar-success" style={{ width: '60%' }} />
            </div>
            <span className="progress-value">60%</span>
          </div>
          <div className="progress-row">
            <div className="progress">
              <div className="progress-bar progress-bar-warning" style={{ width: '85%' }} />
            </div>
            <span className="progress-value">85%</span>
          </div>
        </div>

        {/* Data Table */}
        <div className="component-group">
          <h4 className="component-group-title">Data Table</h4>
          <div className="data-table-wrapper">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Category</th>
                  <th>Questions</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {tableData.map((row) => (
                  <tr key={row.id}>
                    <td>{row.name}</td>
                    <td>{row.category}</td>
                    <td>{row.questions}</td>
                    <td>
                      <span className={`badge badge-${row.status === 'active' ? 'success' : row.status === 'draft' ? 'warning' : 'default'}`}>
                        {row.status}
                      </span>
                    </td>
                    <td>
                      <div className="table-actions">
                        <button className="btn btn-icon btn-ghost btn-sm">
                          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                            <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
                            <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
                          </svg>
                        </button>
                        <button className="btn btn-icon btn-ghost btn-sm">
                          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                            <path d="M3 6h18M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
                          </svg>
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </section>
  );
}
