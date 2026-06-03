/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{html,ts}",
  ],
  theme: {
    extend: {
      colors: {
        gym: {
          dark: '#0f172a',
          card: '#1e293b',
          accent: '#fbbf24',
          primary: '#e2e8f0',
          secondary: '#94a3b8',
          border: '#334155',
          gold: '#f59e0b',
        }
      }
    },
  },
  plugins: [],
}
