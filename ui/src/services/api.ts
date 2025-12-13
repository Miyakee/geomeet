import axios from 'axios';

// In development, use relative path to leverage Vite proxy
// In production, use the full API URL
const API_BASE_URL = import.meta.env.PROD
  ? (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080')
  : ''; // Empty string means use relative path (via Vite proxy)

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add token to requests if available
apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export interface LoginRequest {
  usernameOrEmail: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  username: string;
  email: string;
  message: string;
}

export interface ErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
}

export interface CreateSessionRequest {
  // Empty - initiator ID comes from JWT token
}

export interface CreateSessionResponse {
  id: number;
  sessionId: string;
  initiatorId: number;
  status: string;
  createdAt: string;
  message: string;
}

// Helper function to ensure type safety
async function postRequest<T>(url: string, data?: unknown): Promise<T> {
  const response = await apiClient.post<T>(url, data);
  return response.data;
}

export const authApi = {
  login: async (credentials: LoginRequest): Promise<LoginResponse> => {
    return postRequest<LoginResponse>('/api/auth/login', credentials);
  },
};

export const sessionApi = {
  createSession: async (): Promise<CreateSessionResponse> => {
    return postRequest<CreateSessionResponse>('/api/sessions', {});
  },
};

export default apiClient;

