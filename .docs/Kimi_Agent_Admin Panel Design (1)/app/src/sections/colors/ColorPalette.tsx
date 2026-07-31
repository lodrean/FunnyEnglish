import { useTheme } from '@/design-system/components/ThemeProvider';

interface ColorSwatchProps {
  name: string;
  variable: string;
  value: string;
}

function ColorSwatch({ name, variable, value }: ColorSwatchProps) {
  return (
    <div className="color-swatch">
      <div 
        className="color-swatch-preview"
        style={{ backgroundColor: value }}
      />
      <div className="color-swatch-info">
        <span className="color-swatch-name">{name}</span>
        <code className="color-swatch-variable">{variable}</code>
        <span className="color-swatch-value">{value}</span>
      </div>
    </div>
  );
}

interface ColorScaleProps {
  title: string;
  prefix: string;
  shades: { name: string; value: string }[];
}

function ColorScale({ title, prefix, shades }: ColorScaleProps) {
  return (
    <div className="color-scale">
      <h4 className="color-scale-title">{title}</h4>
      <div className="color-scale-grid">
        {shades.map(({ name, value }) => (
          <ColorSwatch
            key={name}
            name={name}
            variable={`--color-${prefix}-${name}`}
            value={value}
          />
        ))}
      </div>
    </div>
  );
}

export function ColorPalette() {
  const { theme } = useTheme();
  
  const primaryShades = theme === 'light' 
    ? [
        { name: '50', value: '#E3F2FD' },
        { name: '100', value: '#BBDEFB' },
        { name: '200', value: '#90CAF9' },
        { name: '300', value: '#64B5F6' },
        { name: '400', value: '#42A5F5' },
        { name: '500', value: '#4A90D9' },
        { name: '600', value: '#3B82F6' },
        { name: '700', value: '#2E5A8C' },
        { name: '800', value: '#1E3A5F' },
        { name: '900', value: '#0F1F33' },
      ]
    : [
        { name: '50', value: '#0F172A' },
        { name: '100', value: '#1E293B' },
        { name: '200', value: '#334155' },
        { name: '300', value: '#475569' },
        { name: '400', value: '#64748B' },
        { name: '500', value: '#60A5FA' },
        { name: '600', value: '#93C5FD' },
        { name: '700', value: '#BFDBFE' },
        { name: '800', value: '#DBEAFE' },
        { name: '900', value: '#EFF6FF' },
      ];

  const semanticColors = theme === 'light'
    ? [
        { name: 'Background', value: '#F5F5F5', variable: '--bg-primary' },
        { name: 'Surface', value: '#FFFFFF', variable: '--bg-surface' },
        { name: 'Surface Elevated', value: '#FFFFFF', variable: '--bg-surface-elevated' },
        { name: 'Text Primary', value: '#212121', variable: '--text-primary' },
        { name: 'Text Secondary', value: '#757575', variable: '--text-secondary' },
        { name: 'Border', value: '#E0E0E0', variable: '--border-primary' },
      ]
    : [
        { name: 'Background', value: '#0A1929', variable: '--bg-primary' },
        { name: 'Surface', value: '#1E293B', variable: '--bg-surface' },
        { name: 'Surface Elevated', value: '#334155', variable: '--bg-surface-elevated' },
        { name: 'Text Primary', value: '#F8FAFC', variable: '--text-primary' },
        { name: 'Text Secondary', value: '#94A3B8', variable: '--text-secondary' },
        { name: 'Border', value: '#334155', variable: '--border-primary' },
      ];

  const statusColors = [
    { name: 'Success', value: theme === 'light' ? '#43A047' : '#4ADE80', variable: '--color-success-500' },
    { name: 'Warning', value: theme === 'light' ? '#FB8C00' : '#FBBF24', variable: '--color-warning-500' },
    { name: 'Error', value: theme === 'light' ? '#E53935' : '#F87171', variable: '--color-error-500' },
    { name: 'Info', value: theme === 'light' ? '#2196F3' : '#60A5FA', variable: '--color-info-500' },
  ];

  return (
    <section className="color-palette-section">
      <div className="section-header">
        <h2 className="section-title">Color Palette</h2>
        <p className="section-description">
          Complete color system for {theme === 'light' ? 'Light' : 'Dark'} theme.
          All colors are available as CSS custom properties.
        </p>
      </div>

      <div className="color-palette-content">
        {/* Primary Colors */}
        <ColorScale title="Primary Colors" prefix="primary" shades={primaryShades} />

        {/* Semantic Colors */}
        <div className="semantic-colors">
          <h4 className="color-scale-title">Semantic Colors</h4>
          <div className="color-scale-grid">
            {semanticColors.map(({ name, value, variable }) => (
              <ColorSwatch key={name} name={name} variable={variable} value={value} />
            ))}
          </div>
        </div>

        {/* Status Colors */}
        <div className="status-colors">
          <h4 className="color-scale-title">Status Colors</h4>
          <div className="color-scale-grid">
            {statusColors.map(({ name, value, variable }) => (
              <ColorSwatch key={name} name={name} variable={variable} value={value} />
            ))}
          </div>
        </div>

        {/* Surface Examples */}
        <div className="surface-examples">
          <h4 className="color-scale-title">Surface Hierarchy</h4>
          <div className="surface-cards">
            <div className="surface-card surface-card-1">
              <span className="surface-card-label">Base Layer</span>
              <code>--bg-primary</code>
            </div>
            <div className="surface-card surface-card-2">
              <span className="surface-card-label">Surface Layer</span>
              <code>--bg-surface</code>
            </div>
            <div className="surface-card surface-card-3">
              <span className="surface-card-label">Elevated Layer</span>
              <code>--bg-surface-elevated</code>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
