export function TypographyScale() {
  const typeStyles = [
    { name: 'H1', class: 'text-style-h1', sample: 'Heading 1 - Main Page Title', size: '36px', weight: '700' },
    { name: 'H2', class: 'text-style-h2', sample: 'Heading 2 - Section Title', size: '30px', weight: '700' },
    { name: 'H3', class: 'text-style-h3', sample: 'Heading 3 - Card Title', size: '24px', weight: '600' },
    { name: 'H4', class: 'text-style-h4', sample: 'Heading 4 - Subsection', size: '20px', weight: '600' },
    { name: 'H5', class: 'text-style-h5', sample: 'Heading 5 - Small Title', size: '18px', weight: '500' },
    { name: 'H6', class: 'text-style-h6', sample: 'Heading 6 - Label', size: '16px', weight: '500' },
    { name: 'Body', class: 'text-style-body', sample: 'Body text - Regular paragraph content for reading and displaying information.', size: '16px', weight: '400' },
    { name: 'Body Small', class: 'text-style-body-sm', sample: 'Body small - Secondary information and helper text.', size: '14px', weight: '400' },
    { name: 'Caption', class: 'text-style-caption', sample: 'Caption - Labels and metadata', size: '12px', weight: '500' },
    { name: 'Button', class: 'text-style-button', sample: 'Button Text', size: '14px', weight: '600' },
  ];

  return (
    <section className="typography-section">
      <div className="section-header">
        <h2 className="section-title">Typography</h2>
        <p className="section-description">
          Type scale using Inter font family. All text styles are available as CSS classes.
        </p>
      </div>

      <div className="typography-content">
        <div className="type-scale-table">
          <div className="type-scale-header">
            <span>Style</span>
            <span>Sample</span>
            <span>Size</span>
            <span>Weight</span>
          </div>
          {typeStyles.map((style) => (
            <div key={style.name} className="type-scale-row">
              <span className="type-scale-name">{style.name}</span>
              <span className={style.class}>{style.sample}</span>
              <span className="type-scale-meta">{style.size}</span>
              <span className="type-scale-meta">{style.weight}</span>
            </div>
          ))}
        </div>

        <div className="typography-examples">
          <div className="typography-example">
            <h4>Font Families</h4>
            <div className="font-family-demo">
              <div className="font-primary-demo">
                <span className="label">Primary (Inter)</span>
                <p style={{ fontFamily: 'var(--font-primary)' }}>
                  The quick brown fox jumps over the lazy dog.
                </p>
              </div>
              <div className="font-mono-demo">
                <span className="label">Mono (JetBrains Mono)</span>
                <code style={{ fontFamily: 'var(--font-mono)' }}>
                  const theme = useTheme();
                </code>
              </div>
            </div>
          </div>

          <div className="typography-example">
            <h4>Line Heights</h4>
            <div className="line-height-demo">
              <div className="line-height-item">
                <span className="label">Tight (1.25)</span>
                <p style={{ lineHeight: 'var(--leading-tight)', maxWidth: '300px' }}>
                  Lorem ipsum dolor sit amet, consectetur adipiscing elit. 
                  Sed do eiusmod tempor incididunt ut labore.
                </p>
              </div>
              <div className="line-height-item">
                <span className="label">Normal (1.5)</span>
                <p style={{ lineHeight: 'var(--leading-normal)', maxWidth: '300px' }}>
                  Lorem ipsum dolor sit amet, consectetur adipiscing elit. 
                  Sed do eiusmod tempor incididunt ut labore.
                </p>
              </div>
              <div className="line-height-item">
                <span className="label">Relaxed (1.625)</span>
                <p style={{ lineHeight: 'var(--leading-relaxed)', maxWidth: '300px' }}>
                  Lorem ipsum dolor sit amet, consectetur adipiscing elit. 
                  Sed do eiusmod tempor incididunt ut labore.
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
