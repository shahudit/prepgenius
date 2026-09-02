
export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      colors: {
        ink: '#12131C',
        surface: '#F6F5F9',
        card: '#FFFFFF',
        line: '#E6E4EE',
        primary: {
          50: '#F1EEFE',
          100: '#E2DBFD',
          200: '#C4B7FB',
          300: '#A692F8',
          400: '#8A70F4',
          500: '#6C4EEF',
          600: '#5636D6',
          700: '#4128A8',
          800: '#2E1D79',
          900: '#1D1150'
        },
        teal: {
          400: '#2DD4BF',
          500: '#14B8A6',
          600: '#0D9488'
        },
        amber: {
          400: '#FBBF63',
          500: '#F59E0B'
        },
        coral: {
          400: '#FB7768',
          500: '#F0523F'
        }
      },
      fontFamily: {
        display: ['"Sora"', 'sans-serif'],
        body: ['"Inter"', 'sans-serif']
      },
      boxShadow: {
        soft: '0 1px 2px rgba(18,19,28,0.04), 0 8px 24px -12px rgba(18,19,28,0.12)',
        pop: '0 4px 14px -4px rgba(108,78,239,0.35)'
      },
      borderRadius: {
        xl2: '1.25rem'
      }
    }
  },
  plugins: []
}
