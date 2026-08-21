import type { Config } from 'tailwindcss';

/**
 * Dark theme matching the Book Worm wireframes (plan §1 palette).
 * Colors are exposed as Tailwind semantic tokens so components use
 * bg-surface / text-body / bg-accent instead of hard-coded hex.
 */
export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        bg:       '#0f1114',
        surface:  '#171a20',
        surface2: '#1f232b',
        border:   '#2a2f3a',
        body:     '#e6e8ec',
        muted:    '#9aa3b2',
        accent: {
          DEFAULT: '#3b82f6',
          hover:   '#2563eb',
        },
        success: '#22c55e',
        warn:    '#f59e0b',
        danger:  '#ef4444',
        star:    '#facc15',
      },
      fontFamily: {
        sans: ['"IBM Plex Sans"', 'Inter', 'system-ui', 'sans-serif'],
      },
      borderRadius: {
        DEFAULT: '0.5rem',
      },
    },
  },
  plugins: [],
} satisfies Config;
