import { useState } from 'react';

interface User {
  id: number;
  name: string;
  email: string;
  avatar: string;
  role: 'admin' | 'teacher' | 'student';
  status: 'active' | 'inactive' | 'suspended';
  joinedDate: string;
  testsCompleted: number;
  avgScore: number;
  streak: number;
}

const mockUsers: User[] = [
  { id: 1, name: 'John Doe', email: 'john@example.com', avatar: 'JD', role: 'student', status: 'active', joinedDate: '2024-01-15', testsCompleted: 24, avgScore: 82, streak: 7 },
  { id: 2, name: 'Jane Smith', email: 'jane@example.com', avatar: 'JS', role: 'teacher', status: 'active', joinedDate: '2024-01-10', testsCompleted: 156, avgScore: 0, streak: 0 },
  { id: 3, name: 'Mike Johnson', email: 'mike@example.com', avatar: 'MJ', role: 'student', status: 'active', joinedDate: '2024-01-08', testsCompleted: 18, avgScore: 75, streak: 3 },
  { id: 4, name: 'Sarah Williams', email: 'sarah@example.com', avatar: 'SW', role: 'student', status: 'inactive', joinedDate: '2024-01-05', testsCompleted: 12, avgScore: 68, streak: 0 },
  { id: 5, name: 'Tom Brown', email: 'tom@example.com', avatar: 'TB', role: 'admin', status: 'active', joinedDate: '2023-12-20', testsCompleted: 0, avgScore: 0, streak: 0 },
  { id: 6, name: 'Emily Davis', email: 'emily@example.com', avatar: 'ED', role: 'student', status: 'suspended', joinedDate: '2024-01-01', testsCompleted: 8, avgScore: 45, streak: 0 },
];

export function UsersLayout() {
  const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid');
  const [selectedUser, setSelectedUser] = useState<User | null>(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [roleFilter, setRoleFilter] = useState('');

  const filteredUsers = mockUsers.filter(user => {
    const matchesSearch = user.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
                         user.email.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesRole = !roleFilter || user.role === roleFilter;
    return matchesSearch && matchesRole;
  });

  return (
    <section className="page-section">
      <div className="section-header">
        <h2 className="section-title">Users</h2>
        <p className="section-description">
          Manage users, view progress, and manage permissions.
        </p>
      </div>

      <div className="users-layout">
        {/* Header Actions */}
        <div className="users-header">
          <div className="users-filters">
            <div className="search-input-wrapper">
              <svg className="search-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <circle cx="11" cy="11" r="8" />
                <path d="M21 21l-4.35-4.35" />
              </svg>
              <input 
                type="text" 
                className="form-input search-input"
                placeholder="Search users..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
            </div>
            <div className="form-select-wrapper">
              <select 
                className="form-select"
                value={roleFilter}
                onChange={(e) => setRoleFilter(e.target.value)}
              >
                <option value="">All Roles</option>
                <option value="admin">Admin</option>
                <option value="teacher">Teacher</option>
                <option value="student">Student</option>
              </select>
              <svg className="form-select-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M6 9l6 6 6-6" />
              </svg>
            </div>
          </div>
          <div className="users-actions">
            <div className="view-toggle">
              <button 
                className={`view-toggle-btn ${viewMode === 'grid' ? 'active' : ''}`}
                onClick={() => setViewMode('grid')}
              >
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <rect x="3" y="3" width="7" height="7" />
                  <rect x="14" y="3" width="7" height="7" />
                  <rect x="14" y="14" width="7" height="7" />
                  <rect x="3" y="14" width="7" height="7" />
                </svg>
              </button>
              <button 
                className={`view-toggle-btn ${viewMode === 'list' ? 'active' : ''}`}
                onClick={() => setViewMode('list')}
              >
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <line x1="8" y1="6" x2="21" y2="6" />
                  <line x1="8" y1="12" x2="21" y2="12" />
                  <line x1="8" y1="18" x2="21" y2="18" />
                  <line x1="3" y1="6" x2="3.01" y2="6" />
                  <line x1="3" y1="12" x2="3.01" y2="12" />
                  <line x1="3" y1="18" x2="3.01" y2="18" />
                </svg>
              </button>
            </div>
            <button className="btn btn-primary">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M12 5v14M5 12h14" />
              </svg>
              Add User
            </button>
          </div>
        </div>

        {/* Users Grid */}
        {viewMode === 'grid' ? (
          <div className="users-grid">
            {filteredUsers.map((user) => (
              <div 
                key={user.id} 
                className="user-card"
                onClick={() => setSelectedUser(user)}
              >
                <div className="user-card-header">
                  <div className="user-avatar">
                    <img src={`https://api.dicebear.com/7.x/avataaars/svg?seed=${user.name}`} alt={user.name} />
                  </div>
                  <div className="user-status">
                    <span className={`status-dot status-${user.status}`} />
                  </div>
                </div>
                <div className="user-card-body">
                  <h4 className="user-name">{user.name}</h4>
                  <p className="user-email">{user.email}</p>
                  <span className={`badge badge-soft badge-${user.role === 'admin' ? 'error' : user.role === 'teacher' ? 'warning' : 'primary'}`}>
                    {user.role}
                  </span>
                </div>
                {user.role === 'student' && (
                  <div className="user-card-stats">
                    <div className="user-stat">
                      <span className="user-stat-value">{user.testsCompleted}</span>
                      <span className="user-stat-label">Tests</span>
                    </div>
                    <div className="user-stat">
                      <span className="user-stat-value">{user.avgScore}%</span>
                      <span className="user-stat-label">Avg</span>
                    </div>
                    <div className="user-stat">
                      <span className="user-stat-value">{user.streak}</span>
                      <span className="user-stat-label">Streak</span>
                    </div>
                  </div>
                )}
              </div>
            ))}
          </div>
        ) : (
          <div className="data-table-wrapper">
            <table className="data-table data-table-hover">
              <thead>
                <tr>
                  <th>User</th>
                  <th>Role</th>
                  <th>Status</th>
                  <th>Joined</th>
                  <th>Tests</th>
                  <th>Avg Score</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {filteredUsers.map((user) => (
                  <tr key={user.id} onClick={() => setSelectedUser(user)}>
                    <td>
                      <div className="user-cell">
                        <div className="user-avatar user-avatar-sm">
                          <img src={`https://api.dicebear.com/7.x/avataaars/svg?seed=${user.name}`} alt={user.name} />
                        </div>
                        <div className="user-cell-info">
                          <span className="user-cell-name">{user.name}</span>
                          <span className="user-cell-email">{user.email}</span>
                        </div>
                      </div>
                    </td>
                    <td>
                      <span className={`badge badge-soft badge-${user.role === 'admin' ? 'error' : user.role === 'teacher' ? 'warning' : 'primary'}`}>
                        {user.role}
                      </span>
                    </td>
                    <td>
                      <span className={`badge badge-${user.status === 'active' ? 'success' : user.status === 'inactive' ? 'warning' : 'error'}`}>
                        {user.status}
                      </span>
                    </td>
                    <td>{user.joinedDate}</td>
                    <td>{user.testsCompleted}</td>
                    <td>{user.avgScore > 0 ? `${user.avgScore}%` : '-'}</td>
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
                            <circle cx="12" cy="12" r="1" />
                            <circle cx="19" cy="12" r="1" />
                            <circle cx="5" cy="12" r="1" />
                          </svg>
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {/* User Detail Sidebar */}
        {selectedUser && (
          <div className="user-sidebar-overlay" onClick={() => setSelectedUser(null)}>
            <div className="user-sidebar" onClick={e => e.stopPropagation()}>
              <div className="user-sidebar-header">
                <button className="user-sidebar-close" onClick={() => setSelectedUser(null)}>
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <line x1="18" y1="6" x2="6" y2="18" />
                    <line x1="6" y1="6" x2="18" y2="18" />
                  </svg>
                </button>
              </div>
              <div className="user-sidebar-content">
                <div className="user-sidebar-profile">
                  <div className="user-avatar user-avatar-lg">
                    <img src={`https://api.dicebear.com/7.x/avataaars/svg?seed=${selectedUser.name}`} alt={selectedUser.name} />
                  </div>
                  <h3 className="user-sidebar-name">{selectedUser.name}</h3>
                  <p className="user-sidebar-email">{selectedUser.email}</p>
                  <div className="user-sidebar-badges">
                    <span className={`badge badge-${selectedUser.role === 'admin' ? 'error' : selectedUser.role === 'teacher' ? 'warning' : 'primary'}`}>
                      {selectedUser.role}
                    </span>
                    <span className={`badge badge-${selectedUser.status === 'active' ? 'success' : selectedUser.status === 'inactive' ? 'warning' : 'error'}`}>
                      {selectedUser.status}
                    </span>
                  </div>
                </div>

                {selectedUser.role === 'student' && (
                  <div className="user-sidebar-stats">
                    <div className="user-sidebar-stat">
                      <span className="user-sidebar-stat-value">{selectedUser.testsCompleted}</span>
                      <span className="user-sidebar-stat-label">Tests Completed</span>
                    </div>
                    <div className="user-sidebar-stat">
                      <span className="user-sidebar-stat-value">{selectedUser.avgScore}%</span>
                      <span className="user-sidebar-stat-label">Average Score</span>
                    </div>
                    <div className="user-sidebar-stat">
                      <span className="user-sidebar-stat-value">{selectedUser.streak}</span>
                      <span className="user-sidebar-stat-label">Day Streak</span>
                    </div>
                  </div>
                )}

                <div className="user-sidebar-info">
                  <div className="user-info-item">
                    <span className="user-info-label">Joined</span>
                    <span className="user-info-value">{selectedUser.joinedDate}</span>
                  </div>
                  <div className="user-info-item">
                    <span className="user-info-label">User ID</span>
                    <span className="user-info-value">#{selectedUser.id}</span>
                  </div>
                </div>

                <div className="user-sidebar-actions">
                  <button className="btn btn-primary btn-full">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
                      <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
                    </svg>
                    Edit User
                  </button>
                  <button className="btn btn-ghost btn-full">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                      <circle cx="12" cy="12" r="3" />
                    </svg>
                    View Activity
                  </button>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>
    </section>
  );
}
