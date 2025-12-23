import { SessionDetailResponse } from '../types/session';

// In development, use relative path to leverage Vite proxy
// In production, use relative path to leverage Nginx proxy (same domain)
// Only use absolute URL if VITE_API_BASE_URL is explicitly set
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '';

// Helper function to get auth token
function getAuthToken(): string | null {
  return localStorage.getItem('token');
}

// Helper function to build full URL
function buildUrl(path: string): string {
  if (API_BASE_URL) {
    return `${API_BASE_URL}${path}`;
  }
  return path;
}

// Helper function to handle errors
export class ApiError extends Error {
  public response?: {
    status: number;
    data?: ErrorResponse;
  };

  constructor(
    message: string,
    public status: number,
    responseData?: ErrorResponse | any,
  ) {
    super(message);
    this.name = 'ApiError';
    this.response = {
      status,
      data: responseData as ErrorResponse,
    };
  }
}

// Interceptor types
type RequestInterceptor = (config: RequestInit) => RequestInit | Promise<RequestInit>;
type ResponseInterceptor = <T>(data: T) => T | Promise<T>;
type ErrorInterceptor = (error: ApiError) => void | Promise<void>;

// Interceptor storage
const requestInterceptors: RequestInterceptor[] = [];
const responseInterceptors: ResponseInterceptor[] = [];
const errorInterceptors: ErrorInterceptor[] = [];

// Add interceptor functions
export const apiInterceptors = {
  request: {
    use: (interceptor: RequestInterceptor) => {
      requestInterceptors.push(interceptor);
      return () => {
        const index = requestInterceptors.indexOf(interceptor);
        if (index > -1) {
          requestInterceptors.splice(index, 1);
        }
      };
    },
  },
  response: {
    use: (interceptor: ResponseInterceptor) => {
      responseInterceptors.push(interceptor);
      return () => {
        const index = responseInterceptors.indexOf(interceptor);
        if (index > -1) {
          responseInterceptors.splice(index, 1);
        }
      };
    },
  },
  error: {
    use: (interceptor: ErrorInterceptor) => {
      errorInterceptors.push(interceptor);
      return () => {
        const index = errorInterceptors.indexOf(interceptor);
        if (index > -1) {
          errorInterceptors.splice(index, 1);
        }
      };
    },
  },
};

// Helper function to make HTTP requests with interceptors
async function request<T>(
  url: string,
  options: RequestInit = {},
): Promise<T> {
  try {
    // Apply request interceptors
    let requestConfig: RequestInit = { ...options };
    for (const interceptor of requestInterceptors) {
      requestConfig = await interceptor(requestConfig);
    }

    // Build headers
    const token = getAuthToken();
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
      ...(requestConfig.headers as Record<string, string> || {}),
    };

    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const fullUrl = buildUrl(url);
    const response = await fetch(fullUrl, {
      ...requestConfig,
      headers,
    });

    // Handle error responses
    if (!response.ok) {
      let errorData: ErrorResponse;
      try {
        errorData = await response.json();
      } catch {
        // If response is not JSON, create a standard ErrorResponse structure
        errorData = {
          timestamp: new Date().toISOString(),
          status: response.status,
          error: response.statusText || 'Error',
          message: response.statusText || `HTTP ${response.status}`,
          path: fullUrl,
        };
      }

      // Ensure errorData has the standard ErrorResponse structure
      if (!errorData.status) {
        errorData.status = response.status;
      }
      if (!errorData.timestamp) {
        errorData.timestamp = new Date().toISOString();
      }

      // Use message from ErrorResponse, fallback to error field, then statusText
      const errorMessage = errorData.message || errorData.error || `HTTP ${response.status}: ${response.statusText}`;
      
      const error = new ApiError(
        errorMessage,
        errorData.status,
        errorData,
      );

      // Apply error interceptors
      for (const interceptor of errorInterceptors) {
        try {
          await interceptor(error);
        } catch (interceptedError) {
          // If interceptor throws, use that error
          throw interceptedError;
        }
      }

      // Re-throw the original error after interceptors have processed it
      throw error;
    }

    // Handle successful responses
    const contentType = response.headers.get('content-type');
    let data: T;
    
    if (contentType && contentType.includes('application/json')) {
      data = await response.json();
    } else {
      // For non-JSON responses, return empty object
      data = {} as T;
    }

    // Apply response interceptors
    for (const interceptor of responseInterceptors) {
      data = await interceptor<T>(data);
    }

    return data;
  } catch (error) {
    // Re-throw ApiError as-is
    if (error instanceof ApiError) {
      throw error;
    }
    // Wrap other errors
    throw new ApiError(
      error instanceof Error ? error.message : 'Unknown error occurred',
      0,
      { originalError: error },
    );
  }
}

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

// SessionDetailResponse is imported from types/session.ts

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

export interface EndSessionResponse {
  sessionId: number;
  sessionIdString: string;
  status: string;
  endedAt: string;
  message: string;
}

// Interceptors are set up in apiInterceptors.ts
// Helper function to ensure type safety
async function postRequest<T>(url: string, data?: unknown): Promise<T> {
  return request<T>(url, {
    method: 'POST',
    body: data ? JSON.stringify(data) : undefined,
  });
}

export interface RegisterRequest {
  username: string;
  password: string;
  email: string;
  verificationCode: string;
}

export interface RegisterResponse {
  token: string;
  username: string;
  email: string;
  message: string;
}

export const authApi = {
  login: async (credentials: LoginRequest): Promise<LoginResponse> => {
    return postRequest<LoginResponse>('/api/auth/login', credentials);
  },
  register: async (credentials: RegisterRequest): Promise<RegisterResponse> => {
    return postRequest<RegisterResponse>('/api/auth/register', credentials);
  },
};

// Helper function for GET requests
async function getRequest<T>(url: string): Promise<T> {
  return request<T>(url, {
    method: 'GET',
  });
}

// Helper function for PUT requests
async function putRequest<T>(url: string, data?: unknown): Promise<T> {
  return request<T>(url, {
    method: 'PUT',
    body: data ? JSON.stringify(data) : undefined,
  });
}

// Helper function for DELETE requests
async function deleteRequest<T>(url: string): Promise<T> {
  return request<T>(url, {
    method: 'DELETE',
  });
}

export const sessionApi = {
  createSession: async (): Promise<CreateSessionResponse> => {
    return postRequest<CreateSessionResponse>('/api/sessions', {});
  },
  generateInviteLink: async (sessionId: string): Promise<InviteLinkResponse> => {
    return getRequest<InviteLinkResponse>(`/api/sessions/${sessionId}/invite`);
  },
  joinSession: async (sessionId: string, inviteCode: string): Promise<JoinSessionResponse> => {
    return postRequest<JoinSessionResponse>('/api/sessions/join', { sessionId, inviteCode });
  },
  getSessionDetails: async (sessionId: string): Promise<SessionDetailResponse> => {
    return getRequest<SessionDetailResponse>(`/api/sessions/${sessionId}`);
  },
  updateLocation: async (
    sessionId: string,
    location: UpdateLocationRequest,
  ): Promise<UpdateLocationResponse> => {
    return putRequest<UpdateLocationResponse>(`/api/sessions/${sessionId}/location`, location);
  },
  calculateOptimalLocation: async (
    sessionId: string,
  ): Promise<CalculateOptimalLocationResponse> => {
    return postRequest<CalculateOptimalLocationResponse>(
      `/api/sessions/${sessionId}/optimal-location`,
      {},
    );
  },
  updateMeetingLocation: async (
    sessionId: string,
    location: UpdateMeetingLocationRequest,
  ): Promise<UpdateMeetingLocationResponse> => {
    return putRequest<UpdateMeetingLocationResponse>(
      `/api/sessions/${sessionId}/meeting-location`,
      location,
    );
  },
  endSession: async (sessionId: string): Promise<EndSessionResponse> => {
    return deleteRequest<EndSessionResponse>(`/api/sessions/${sessionId}`);
  },
};
