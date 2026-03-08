import type { Config } from "tailwindcss";

interface ScrollbarPluginApi {
  addUtilities: (
    utilities: Record<string, Record<string, string | Record<string, string>>>
  ) => void;
}

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
        atc: {
          bg: "#0d1117",
          surface: "#161b22",
          "surface-elevated": "#21262d",
          border: "#30363d",
          text: "#c9d1d9",
          "text-muted": "#8b949e",
          accent: "#238636",
          "accent-hover": "#2ea043",
          warning: "#9e6a03",
          danger: "#cf2222",
        },
      },
      backgroundImage: {
        "atc-bg": "linear-gradient(180deg, #0d1117 0%, #161b22 100%)",
      },
      animation: {
        "fade-in": "fadeIn 0.5s ease-out both",
      },
      keyframes: {
        fadeIn: {
          "0%": { opacity: "0", transform: "translateY(12px)" },
          "100%": { opacity: "1", transform: "translateY(0)" },
        },
      },
    },
  },
  plugins: [
    function scrollbarPlugin({ addUtilities }: ScrollbarPluginApi) {
      const newUtilities = {
        ".scrollbar-thin": {
          scrollbarWidth: "thin",
        },
        ".scrollbar-track-atc": {
          scrollbarColor: "#30363d #0d1117",
        },
        ".scrollbar-thumb-atc": {
          "&::-webkit-scrollbar": {
            width: "6px",
          },
          "&::-webkit-scrollbar-track": {
            background: "#0d1117",
            borderRadius: "3px",
          },
          "&::-webkit-scrollbar-thumb": {
            background: "#30363d",
            borderRadius: "3px",
          },
          "&::-webkit-scrollbar-thumb:hover": {
            background: "#484f58",
          },
        },
      };
      addUtilities(newUtilities);
    },
  ],
};
export default config;
