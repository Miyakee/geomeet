/**
 * Type definitions for session-related data structures.
 */

export interface ParticipantInfo {
  participantId: number | null;
  userId: number;
  username: string;
  email: string;
  joinedAt: string;
  // Location information (nullable - participant may not have shared location)
  latitude?: number | null;
  longitude?: number | null;
  accuracy?: number | null;
  locationUpdatedAt?: string | null;
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
  meetingLocationLatitude?: number | null;
  meetingLocationLongitude?: number | null;
}

export interface ParticipantLocation {
  latitude: number;
  longitude: number;
  accuracy?: number;
  updatedAt: string;
}

