/**
 * Type definitions for session-related data structures.
 */

export interface ParticipantInfo {
  participantId: number | null;
  userId: number;
  username: string;
  email: string;
  joinedAt: string;
}

export interface ParticipantLocationInfo {
  participantId: number | null;
  userId: number;
  latitude: number;
  longitude: number;
  accuracy?: number | null;
  updatedAt: string;
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
  participantLocations?: ParticipantLocationInfo[] | null;
}

export interface ParticipantLocation {
  latitude: number;
  longitude: number;
  accuracy?: number;
  updatedAt: string;
}

