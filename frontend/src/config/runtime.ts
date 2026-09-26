// Showcase builds run deterministically without a backend by default.
// Set VITE_DEMO_MODE=false to enable live project and test APIs.
export const forceDemoMode = import.meta.env.VITE_DEMO_MODE !== 'false'
