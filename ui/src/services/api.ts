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

export interface InviteLinkResponse {
  sessionId: string;
  inviteLink: string;
  inviteCode: string;
  message: string;
}

export interface JoinSessionRequest {
  sessionId: string;
}

export interface JoinSessionResponse {
  participantId: number;
  sessionId: number;
  sessionIdString: string;
  userId: number;
  joinedAt: string;
  message: string;
}

export interface ParticipantInfo {
  participantId: number;
  userId: number;
  username: string;
  email: string;
  joinedAt: string;
}

export interface SessionDetailResponse {
  id: number;
  sessionId: string;
  initiatorId: number;
  initiatorUsername: string;
  status: string;
  createdAt: string;
  participants: ParticipantInfo[];
  participantCount: number;
}

export interface UpdateLocationRequest {
  latitude: number;
  longitude: number;
  accuracy?: number;
}

export interface UpdateLocationResponse {
  participantId: number;
  sessionId: number;
  sessionIdString: string;
  userId: number;
  latitude: number;
  longitude: number;
  accuracy?: number;
  updatedAt: string;
  message: string;
}

export interface CalculateOptimalLocationResponse {
  sessionId: number;
  sessionIdString: string;
  optimalLatitude: number;
  optimalLongitude: number;
  totalTravelDistance: number;
  participantCount: number;
  message: string;
}

export interface UpdateMeetingLocationRequest {
  latitude: number;
  longitude: number;
}

export interface UpdateMeetingLocationResponse {
  sessionId: number;
  sessionIdString: string;
  latitude: number;
  longitude: number;
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

// Helper function for GET requests
async function getRequest<T>(url: string): Promise<T> {
  const response = await apiClient.get<T>(url);
  return response.data;
}

// Helper function for PUT requests
async function putRequest<T>(url: string, data?: unknown): Promise<T> {
  const response = await apiClient.put<T>(url, data);
  return response.data;
}

export const sessionApi = {
  createSession: async (): Promise<CreateSessionResponse> => {
    return postRequest<CreateSessionResponse>('/api/sessions', {});
  },
  generateInviteLink: async (sessionId: string): Promise<InviteLinkResponse> => {
    return getRequest<InviteLinkResponse>(`/api/sessions/${sessionId}/invite`);
  },
  joinSession: async (sessionId: string): Promise<JoinSessionResponse> => {
    return postRequest<JoinSessionResponse>('/api/sessions/join', { sessionId });
  },
  getSessionDetails: async (sessionId: string): Promise<SessionDetailResponse> => {
    return getRequest<SessionDetailResponse>(`/api/sessions/${sessionId}`);
  },
  updateLocation: async (
    sessionId: string,
    location: UpdateLocationRequest
  ): Promise<UpdateLocationResponse> => {
    return putRequest<UpdateLocationResponse>(`/api/sessions/${sessionId}/location`, location);
  },
  calculateOptimalLocation: async (
    sessionId: string
  ): Promise<CalculateOptimalLocationResponse> => {
    return postRequest<CalculateOptimalLocationResponse>(
      `/api/sessions/${sessionId}/optimal-location`,
      {}
    );
  },
  updateMeetingLocation: async (
    sessionId: string,
    location: UpdateMeetingLocationRequest
  ): Promise<UpdateMeetingLocationResponse> => {
    return putRequest<UpdateMeetingLocationResponse>(
      `/api/sessions/${sessionId}/meeting-location`,
      location
    );
  },
};

export default apiClient;

