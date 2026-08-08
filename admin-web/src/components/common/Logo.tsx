/**
 * So to speak — Waveform Connected Logo v2.0
 *
 * Theme-aware inline SVG logo. The wordmark uses the current MUI text color,
 * so it works in both light and dark modes without a background rectangle.
 */

import React from 'react';
import { styled, useTheme } from '@mui/material/styles';
import type { SxProps, Theme } from '@mui/material/styles';

const SvgRoot = styled('svg')({});

interface LogoProps extends React.SVGProps<SVGSVGElement> {
  /** Horizontal (header) or Primary Full (splash/login) composition */
  variant?: 'horizontal' | 'full';
  /** Logo height in px; width is derived from the viewBox aspect ratio */
  height?: number;
  /** MUI sx prop (used by callers for layout) */
  sx?: SxProps<Theme>;
}

export const Logo: React.FC<LogoProps> = ({
  variant = 'horizontal',
  height = 28,
  ...props
}) => {
  const theme = useTheme();
  const textFill = theme.palette.text.primary;
  const gradientId = `logo-wave-gradient-${variant}`;

  if (variant === 'horizontal') {
    const width = height * (235 / 48);
    return (
      <SvgRoot
        viewBox="0 0 235 48"
        height={height}
        width={width}
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
        aria-label="So to speak"
        {...props}
      >
        <defs>
          <linearGradient id={gradientId} x1="0" y1="0" x2="1" y2="0">
            <stop offset="0" stopColor="#5B8DEF" />
            <stop offset="0.5" stopColor="#9B7EDE" />
            <stop offset="1" stopColor="#FF9F6B" />
          </linearGradient>
        </defs>
        {/* Microphone */}
        <rect x="12" y="4" width="12" height="22" rx="6" fill="#FF9F6B" />
        <path
          d="M6 20a12 12 0 0 0 24 0"
          fill="none"
          stroke="#FF9F6B"
          strokeWidth="3.5"
          strokeLinecap="round"
        />
        <line x1="18" y1="32" x2="18" y2="37" stroke="#FF9F6B" strokeWidth="3.5" strokeLinecap="round" />
        <line x1="12" y1="41" x2="24" y2="41" stroke="#FF9F6B" strokeWidth="3.5" strokeLinecap="round" />
        {/* Waveform */}
        <g fill={`url(#${gradientId})`}>
          <rect x="38" y="20" width="5" height="8" rx="2.5" />
          <rect x="46" y="15" width="5" height="18" rx="2.5" />
          <rect x="54" y="10" width="5" height="28" rx="2.5" />
          <rect x="62" y="17" width="5" height="14" rx="2.5" />
          <rect x="70" y="19" width="5" height="10" rx="2.5" />
        </g>
        {/* Wordmark */}
        <text
          x="88"
          y="33"
          fontFamily="Nunito, -apple-system, 'Segoe UI', sans-serif"
          fontSize="26"
          fontWeight="800"
          fill={textFill}
          letterSpacing="-0.5"
        >
          SoToSpeak
        </text>
      </SvgRoot>
    );
  }

  // Primary Full
  const width = height * (320 / 96);
  return (
    <SvgRoot
      viewBox="0 0 320 96"
      height={height}
      width={width}
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      aria-label="So to speak"
      {...props}
    >
      <defs>
        <linearGradient id={gradientId} x1="0" y1="0" x2="1" y2="0">
          <stop offset="0" stopColor="#5B8DEF" />
          <stop offset="0.5" stopColor="#9B7EDE" />
          <stop offset="1" stopColor="#FF9F6B" />
        </linearGradient>
      </defs>
      {/* Microphone */}
      <rect x="14" y="14" width="18" height="32" rx="9" fill="#FF9F6B" />
      <path
        d="M5 38a18 18 0 0 0 36 0"
        fill="none"
        stroke="#FF9F6B"
        strokeWidth="5"
        strokeLinecap="round"
      />
      <line x1="23" y1="56" x2="23" y2="63" stroke="#FF9F6B" strokeWidth="5" strokeLinecap="round" />
      <line x1="14" y1="69" x2="32" y2="69" stroke="#FF9F6B" strokeWidth="5" strokeLinecap="round" />
      {/* Waveform */}
      <g fill={`url(#${gradientId})`}>
        <rect x="50" y="40" width="7" height="12" rx="3.5" />
        <rect x="62" y="32" width="7" height="28" rx="3.5" />
        <rect x="74" y="24" width="7" height="44" rx="3.5" />
        <rect x="86" y="34" width="7" height="20" rx="3.5" />
        <rect x="98" y="38" width="7" height="14" rx="3.5" />
      </g>
      {/* Wordmark */}
      <text
        x="122"
        y="64"
        fontFamily="Nunito, -apple-system, 'Segoe UI', sans-serif"
        fontSize="40"
        fontWeight="800"
        fill={textFill}
        letterSpacing="-1"
      >
        SoToSpeak
      </text>
    </SvgRoot>
  );
};

export default Logo;
