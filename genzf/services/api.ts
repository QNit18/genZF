// API Service for backend communication

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8181/genzf';

export interface ApiBaseResponse<T> {
  code: number;
  message: string;
  result: T;
}

export async function fetchApi<T>(endpoint: string, options?: RequestInit): Promise<T> {
  const url = `${API_BASE_URL}${endpoint}`;
  
  try {
    const response = await fetch(url, {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        ...options?.headers,
      },
    });

    if (!response.ok) {
      throw new Error(`API request failed: ${response.status} ${response.statusText}`);
    }

    const data: ApiBaseResponse<T> = await response.json();
    
    if (data.code !== 1000) {
      throw new Error(data.message || 'API request failed');
    }

    return data.result;
  } catch (error) {
    console.error('API Error:', error);
    throw error;
  }
}
