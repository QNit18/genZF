/// <reference types="vite/client" />

/**
 * Environment variable type definitions for production and development
 * These types ensure TypeScript recognizes Vite environment variables
 */
interface ImportMetaEnv {
  /**
   * API base URL for backend service
   * Example: 'https://api.example.com/genzf' or 'http://localhost:8181/genzf'
   */
  readonly VITE_API_BASE_URL?: string;
  
  /**
   * Gemini API key for AI features
   */
  readonly GEMINI_API_KEY?: string;
  
  /**
   * Production mode flag (set by Vite automatically)
   */
  readonly MODE: string;
  
  /**
   * Development mode flag (set by Vite automatically)
   */
  readonly DEV: boolean;
  
  /**
   * Production mode flag (set by Vite automatically)
   */
  readonly PROD: boolean;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
