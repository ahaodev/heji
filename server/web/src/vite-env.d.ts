/// <reference types="vite/client" />

// Declare module for JSON imports
declare module '*.json' {
  const value: any;
  export default value;
}
