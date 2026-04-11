/// <reference types="vite/client" />

// Declare module for JSON imports
declare module '*.json' {
  const value: Record<string, unknown>
  export default value
}
