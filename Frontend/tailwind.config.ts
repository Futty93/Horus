import type { Config } from "tailwindcss";

const config: Config = {
  content: [
    "./pages/**/*.{js,ts,jsx,tsx,mdx}",
    "./components/**/*.{js,ts,jsx,tsx,mdx}",
    "./app/**/*.{js,ts,jsx,tsx,mdx}",
  ],
  theme: {
    extend: {
      colors: {
        background: "var(--background)",
        foreground: "var(--foreground)",
        // Futuristic ATC Interface Color Palette
        cyber: {
          50: '#f0f9ff',
          100: '#e0f2fe',
          200: '#bae6fd',
          300: '#7dd3fc',
          400: '#38bdf8',
          500: '#0ea5e9',
          600: '#0284c7',
          700: '#0369a1',
          800: '#075985',
          900: '#0c4a6e',
        },
        neon: {
          green: '#00ff88',
          blue: '#00d4ff',
          purple: '#b347d9',
          pink: '#ff1cf7',
          orange: '#ff6b35',
          yellow: '#ffeb3b',
        },
        matrix: {
          dark: '#0d1117',
          medium: '#161b22',
          light: '#21262d',
          accent: '#30363d',
        },
        radar: {
          primary: '#00ff88',
          secondary: '#00d4ff',
          warning: '#ff6b35',
          danger: '#ff1cf7',
          info: '#b347d9',
        }
      },
      backgroundImage: {
        'cyber-gradient': 'linear-gradient(135deg, #0d1117 0%, #161b22 50%, #21262d 100%)',
        'neon-gradient': 'linear-gradient(45deg, #00ff88 0%, #00d4ff 50%, #b347d9 100%)',
        'radar-gradient': 'linear-gradient(90deg, rgba(0,255,136,0.1) 0%, rgba(0,212,255,0.1) 100%)',
        'control-gradient': 'linear-gradient(135deg, rgba(0,255,136,0.05) 0%, rgba(0,212,255,0.05) 100%)',
        'button-gradient': 'linear-gradient(135deg, #00ff88 0%, #00d4ff 100%)',
        'button-hover-gradient': 'linear-gradient(135deg, #00d4ff 0%, #b347d9 100%)',
        'danger-gradient': 'linear-gradient(135deg, #ff1cf7 0%, #ff6b35 100%)',
        'warning-gradient': 'linear-gradient(135deg, #ff6b35 0%, #ffeb3b 100%)',
      },
      boxShadow: {
        'lg-no-offset': '0 0px 15px 0px rgba(0, 0, 0, 0.1)',
        'cyber': '0 0 20px rgba(0, 255, 136, 0.3)',
        'cyber-lg': '0 0 30px rgba(0, 255, 136, 0.4)',
        'neon': '0 0 20px rgba(0, 212, 255, 0.3)',
        'neon-lg': '0 0 30px rgba(0, 212, 255, 0.4)',
        'purple': '0 0 20px rgba(179, 71, 217, 0.3)',
        'purple-lg': '0 0 30px rgba(179, 71, 217, 0.4)',
        'inset-cyber': 'inset 0 0 10px rgba(0, 255, 136, 0.2)',
        'inset-neon': 'inset 0 0 10px rgba(0, 212, 255, 0.2)',
      },
      borderRadius: {
        'cyber': '0.5rem',
        'cyber-lg': '1rem',
        'cyber-xl': '1.5rem',
      },
      animation: {
        'pulse-slow': 'pulse 3s cubic-bezier(0.4, 0, 0.6, 1) infinite',
        'glow': 'glow 2s ease-in-out infinite alternate',
        'scan': 'scan 2s linear infinite',
      },
      keyframes: {
        glow: {
          '0%': {
            'box-shadow': '0 0 5px rgba(0, 255, 136, 0.2), 0 0 10px rgba(0, 255, 136, 0.2)',
          },
          '100%': {
            'box-shadow': '0 0 10px rgba(0, 255, 136, 0.4), 0 0 20px rgba(0, 255, 136, 0.3)',
          },
        },
        scan: {
          '0%': { 'transform': 'translateX(-100%)' },
          '100%': { 'transform': 'translateX(100%)' },
        },
      },
    },
  },
  plugins: [
    function({ addUtilities }: { addUtilities: any }) {
      const newUtilities = {
        '.scrollbar-thin': {
          scrollbarWidth: 'thin',
        },
        '.scrollbar-track-matrix-dark': {
          scrollbarColor: '#00ff88 #0d1117',
        },
        '.scrollbar-thumb-radar-primary': {
          '&::-webkit-scrollbar': {
            width: '8px',
          },
          '&::-webkit-scrollbar-track': {
            background: '#0d1117',
            borderRadius: '4px',
          },
          '&::-webkit-scrollbar-thumb': {
            background: 'linear-gradient(135deg, #00ff88 0%, #00d4ff 100%)',
            borderRadius: '4px',
            border: '1px solid #161b22',
          },
          '&::-webkit-scrollbar-thumb:hover': {
            background: 'linear-gradient(135deg, #00d4ff 0%, #b347d9 100%)',
          },
        },
      };
      addUtilities(newUtilities);
    },
  ],
};
export default config;
