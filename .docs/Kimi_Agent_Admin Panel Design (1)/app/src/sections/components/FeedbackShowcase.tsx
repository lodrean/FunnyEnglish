import { useState } from 'react';

export function FeedbackShowcase() {
  const [showModal, setShowModal] = useState(false);
  const [toasts, setToasts] = useState<{ id: number; type: string; message: string }[]>([]);

  const addToast = (type: string, message: string) => {
    const id = Date.now();
    setToasts(prev => [...prev, { id, type, message }]);
    setTimeout(() => {
      setToasts(prev => prev.filter(t => t.id !== id));
    }, 3000);
  };

  return (
    <section className="component-section">
      <div className="section-header">
        <h2 className="section-title">Feedback Components</h2>
        <p className="section-description">
          Toast notifications, modals, loading states, and other feedback elements.
        </p>
      </div>

      <div className="component-grid">
        {/* Toast Notifications */}
        <div className="component-group">
          <h4 className="component-group-title">Toast Notifications</h4>
          <div className="button-row">
            <button className="btn btn-success" onClick={() => addToast('success', 'Changes saved successfully!')}>
              Show Success
            </button>
            <button className="btn btn-error" onClick={() => addToast('error', 'Something went wrong!')}>
              Show Error
            </button>
            <button className="btn btn-warning" onClick={() => addToast('warning', 'Please review your input.')}>
              Show Warning
            </button>
            <button className="btn btn-info" onClick={() => addToast('info', 'New update available.')}>
              Show Info
            </button>
          </div>

          {/* Toast Container */}
          <div className="toast-container">
            {toasts.map(toast => (
              <div key={toast.id} className={`toast toast-${toast.type}`}>
                <div className="toast-icon">
                  {toast.type === 'success' && (
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" />
                      <polyline points="22 4 12 14.01 9 11.01" />
                    </svg>
                  )}
                  {toast.type === 'error' && (
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <circle cx="12" cy="12" r="10" />
                      <line x1="15" y1="9" x2="9" y2="15" />
                      <line x1="9" y1="9" x2="15" y2="15" />
                    </svg>
                  )}
                  {toast.type === 'warning' && (
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" />
                      <line x1="12" y1="9" x2="12" y2="13" />
                      <line x1="12" y1="17" x2="12.01" y2="17" />
                    </svg>
                  )}
                  {toast.type === 'info' && (
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <circle cx="12" cy="12" r="10" />
                      <line x1="12" y1="16" x2="12" y2="12" />
                      <line x1="12" y1="8" x2="12.01" y2="8" />
                    </svg>
                  )}
                </div>
                <span className="toast-message">{toast.message}</span>
                <button className="toast-close" onClick={() => setToasts(prev => prev.filter(t => t.id !== toast.id))}>
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <line x1="18" y1="6" x2="6" y2="18" />
                    <line x1="6" y1="6" x2="18" y2="18" />
                  </svg>
                </button>
              </div>
            ))}
          </div>
        </div>

        {/* Modal */}
        <div className="component-group">
          <h4 className="component-group-title">Modal Dialog</h4>
          <button className="btn btn-primary" onClick={() => setShowModal(true)}>
            Open Modal
          </button>

          {showModal && (
            <div className="modal-overlay" onClick={() => setShowModal(false)}>
              <div className="modal" onClick={e => e.stopPropagation()}>
                <div className="modal-header">
                  <h4 className="modal-title">Confirm Action</h4>
                  <button className="modal-close" onClick={() => setShowModal(false)}>
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <line x1="18" y1="6" x2="6" y2="18" />
                      <line x1="6" y1="6" x2="18" y2="18" />
                    </svg>
                  </button>
                </div>
                <div className="modal-body">
                  <p>Are you sure you want to delete this item? This action cannot be undone.</p>
                </div>
                <div className="modal-footer">
                  <button className="btn btn-ghost" onClick={() => setShowModal(false)}>Cancel</button>
                  <button className="btn btn-danger" onClick={() => setShowModal(false)}>Delete</button>
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Loading States */}
        <div className="component-group">
          <h4 className="component-group-title">Loading States</h4>
          <div className="loading-row">
            <div className="spinner" />
            <span>Default Spinner</span>
          </div>
          <div className="loading-row">
            <div className="spinner spinner-sm" />
            <span>Small Spinner</span>
          </div>
          <div className="loading-row">
            <div className="spinner spinner-lg" />
            <span>Large Spinner</span>
          </div>
          <div className="loading-row">
            <div className="dots-loader">
              <span /><span /><span />
            </div>
            <span>Dots Loader</span>
          </div>
        </div>

        {/* Skeleton Loading */}
        <div className="component-group">
          <h4 className="component-group-title">Skeleton Loading</h4>
          <div className="skeleton-card">
            <div className="skeleton skeleton-avatar" />
            <div className="skeleton-content">
              <div className="skeleton skeleton-text skeleton-text-lg" />
              <div className="skeleton skeleton-text" />
              <div className="skeleton skeleton-text skeleton-text-sm" />
            </div>
          </div>
        </div>

        {/* Empty State */}
        <div className="component-group">
          <h4 className="component-group-title">Empty State</h4>
          <div className="empty-state">
            <div className="empty-state-icon">
              <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
                <path d="M13 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V9z" />
                <polyline points="13 2 13 9 20 9" />
              </svg>
            </div>
            <h5 className="empty-state-title">No tests found</h5>
            <p className="empty-state-description">Get started by creating your first test.</p>
            <button className="btn btn-primary">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M12 5v14M5 12h14" />
              </svg>
              Create Test
            </button>
          </div>
        </div>

        {/* Alert */}
        <div className="component-group">
          <h4 className="component-group-title">Alerts</h4>
          <div className="alert alert-info">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <circle cx="12" cy="12" r="10" />
              <line x1="12" y1="16" x2="12" y2="12" />
              <line x1="12" y1="8" x2="12.01" y2="8" />
            </svg>
            <span>This is an informational alert.</span>
          </div>
          <div className="alert alert-success">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" />
              <polyline points="22 4 12 14.01 9 11.01" />
            </svg>
            <span>Your changes have been saved successfully!</span>
          </div>
          <div className="alert alert-warning">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" />
              <line x1="12" y1="9" x2="12" y2="13" />
              <line x1="12" y1="17" x2="12.01" y2="17" />
            </svg>
            <span>Please review your input before continuing.</span>
          </div>
          <div className="alert alert-error">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <circle cx="12" cy="12" r="10" />
              <line x1="15" y1="9" x2="9" y2="15" />
              <line x1="9" y1="9" x2="15" y2="15" />
            </svg>
            <span>An error occurred while processing your request.</span>
          </div>
        </div>
      </div>
    </section>
  );
}
