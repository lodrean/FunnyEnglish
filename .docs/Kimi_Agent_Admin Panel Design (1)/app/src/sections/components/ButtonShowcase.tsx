import { useState } from 'react';

export function ButtonShowcase() {
  const [loading, setLoading] = useState(false);

  const handleLoadingClick = () => {
    setLoading(true);
    setTimeout(() => setLoading(false), 2000);
  };

  return (
    <section className="component-section">
      <div className="section-header">
        <h2 className="section-title">Buttons</h2>
        <p className="section-description">
          Button components with multiple variants, sizes, and states.
        </p>
      </div>

      <div className="component-grid">
        {/* Primary Buttons */}
        <div className="component-group">
          <h4 className="component-group-title">Primary Buttons</h4>
          <div className="button-row">
            <button className="btn btn-primary btn-sm">Small</button>
            <button className="btn btn-primary">Default</button>
            <button className="btn btn-primary btn-lg">Large</button>
          </div>
          <div className="button-row">
            <button className="btn btn-primary" disabled>Disabled</button>
            <button className="btn btn-primary" onClick={handleLoadingClick}>
              {loading ? (
                <>
                  <span className="btn-spinner" />
                  Loading...
                </>
              ) : 'Click to Load'}
            </button>
          </div>
        </div>

        {/* Secondary Buttons */}
        <div className="component-group">
          <h4 className="component-group-title">Secondary Buttons</h4>
          <div className="button-row">
            <button className="btn btn-secondary btn-sm">Small</button>
            <button className="btn btn-secondary">Default</button>
            <button className="btn btn-secondary btn-lg">Large</button>
          </div>
          <div className="button-row">
            <button className="btn btn-secondary" disabled>Disabled</button>
          </div>
        </div>

        {/* Outlined Buttons */}
        <div className="component-group">
          <h4 className="component-group-title">Outlined Buttons</h4>
          <div className="button-row">
            <button className="btn btn-outlined btn-sm">Small</button>
            <button className="btn btn-outlined">Default</button>
            <button className="btn btn-outlined btn-lg">Large</button>
          </div>
          <div className="button-row">
            <button className="btn btn-outlined" disabled>Disabled</button>
          </div>
        </div>

        {/* Ghost Buttons */}
        <div className="component-group">
          <h4 className="component-group-title">Ghost Buttons</h4>
          <div className="button-row">
            <button className="btn btn-ghost btn-sm">Small</button>
            <button className="btn btn-ghost">Default</button>
            <button className="btn btn-ghost btn-lg">Large</button>
          </div>
          <div className="button-row">
            <button className="btn btn-ghost" disabled>Disabled</button>
          </div>
        </div>

        {/* Danger Buttons */}
        <div className="component-group">
          <h4 className="component-group-title">Danger Buttons</h4>
          <div className="button-row">
            <button className="btn btn-danger btn-sm">Small</button>
            <button className="btn btn-danger">Default</button>
            <button className="btn btn-danger btn-lg">Large</button>
          </div>
          <div className="button-row">
            <button className="btn btn-danger" disabled>Disabled</button>
            <button className="btn btn-danger-outlined">Outlined</button>
          </div>
        </div>

        {/* Icon Buttons */}
        <div className="component-group">
          <h4 className="component-group-title">Icon Buttons</h4>
          <div className="button-row">
            <button className="btn btn-icon btn-primary">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M12 5v14M5 12h14" />
              </svg>
            </button>
            <button className="btn btn-icon btn-secondary">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
                <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
              </svg>
            </button>
            <button className="btn btn-icon btn-danger">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M3 6h18M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
              </svg>
            </button>
          </div>
          <div className="button-row">
            <button className="btn btn-primary">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M12 5v14M5 12h14" />
              </svg>
              Add New
            </button>
            <button className="btn btn-secondary">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
                <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
              </svg>
              Edit
            </button>
          </div>
        </div>
      </div>
    </section>
  );
}
