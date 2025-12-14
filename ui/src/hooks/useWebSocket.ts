import { useEffect, useRef } from 'react';
import { UpdateLocationResponse, CalculateOptimalLocationResponse, UpdateMeetingLocationResponse } from '../services/api';
import { reverseGeocode } from '../services/geocodingService';
import { SessionDetailResponse, ParticipantLocation } from '../types/session';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

export interface SessionEndNotification {
  sessionId: number;
  sessionIdString: string;
  status: string;
  endedAt: string;
  message: string;
  hasMeetingLocation: boolean;
  meetingLocationLatitude?: number | null;
  meetingLocationLongitude?: number | null;
}

interface UseWebSocketProps {
  sessionId: string | undefined;
  onSessionUpdate: (session: SessionDetailResponse) => void;
  onLocationUpdate: (location: ParticipantLocation, userId: number) => void;
  onAddressUpdate: (address: string, userId: number) => void;
  onOptimalLocationUpdate?: (optimalLocation: CalculateOptimalLocationResponse) => void;
  onMeetingLocationUpdate?: (meetingLocation: UpdateMeetingLocationResponse) => void;
  onSessionEnd?: (notification: SessionEndNotification) => void;
}

export const useWebSocket = ({
  sessionId,
  onSessionUpdate,
  onLocationUpdate,
  onAddressUpdate,
  onOptimalLocationUpdate,
  onMeetingLocationUpdate,
  onSessionEnd,
}: UseWebSocketProps) => {
  const stompClientRef = useRef<Client | null>(null);

  useEffect(() => {
    if (!sessionId) return;

    const setupWebSocket = () => {
      const token = localStorage.getItem('token');
      if (!token) {
        console.error('No token found for WebSocket connection');
        return;
      }

      if (stompClientRef.current) {
        stompClientRef.current.deactivate();
        stompClientRef.current = null;
      }

      const wsUrl = import.meta.env.PROD
        ? (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080').replace(/^http/, 'ws')
        : '';

      const socket = new SockJS(wsUrl ? `${wsUrl}/ws` : '/ws');
      const client = new Client({
        webSocketFactory: () => socket as any,
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
        onConnect: (frame) => {
          console.log('WebSocket connected, frame:', frame);
          
          client.subscribe(`/topic/session/${sessionId}`, (message) => {
            try {
              const updatedSession: SessionDetailResponse = JSON.parse(message.body);
              console.log('Parsed session update:', updatedSession);
              onSessionUpdate(updatedSession);
            } catch (err) {
              console.error('Failed to parse WebSocket message:', err);
            }
          });

          client.subscribe(`/topic/session/${sessionId}/locations`, (message) => {
            try {
              const locationUpdate: UpdateLocationResponse = JSON.parse(message.body);
              console.log('Parsed location update:', locationUpdate);
              
              onLocationUpdate(
                {
                  latitude: locationUpdate.latitude,
                  longitude: locationUpdate.longitude,
                  accuracy: locationUpdate.accuracy,
                  updatedAt: locationUpdate.updatedAt,
                },
                locationUpdate.userId
              );
              
              reverseGeocode(locationUpdate.latitude, locationUpdate.longitude).then((address) => {
                if (address) {
                  onAddressUpdate(address, locationUpdate.userId);
                }
              });
            } catch (err) {
              console.error('Failed to parse location update message:', err);
            }
          });

          // Subscribe to optimal location updates
          if (onOptimalLocationUpdate) {
            client.subscribe(`/topic/session/${sessionId}/optimal-location`, (message) => {
              try {
                const optimalLocation: CalculateOptimalLocationResponse = JSON.parse(message.body);
                console.log('Parsed optimal location update:', optimalLocation);
                onOptimalLocationUpdate(optimalLocation);
              } catch (err) {
                console.error('Failed to parse optimal location message:', err);
              }
            });
          }

          // Subscribe to meeting location updates
          if (onMeetingLocationUpdate) {
            client.subscribe(`/topic/session/${sessionId}/meeting-location`, (message) => {
              try {
                const meetingLocation: UpdateMeetingLocationResponse = JSON.parse(message.body);
                console.log('Parsed meeting location update:', meetingLocation);
                onMeetingLocationUpdate(meetingLocation);
              } catch (err) {
                console.error('Failed to parse meeting location message:', err);
              }
            });
          }

          // Subscribe to session end notifications
          if (onSessionEnd) {
            client.subscribe(`/topic/session/${sessionId}/end`, (message) => {
              try {
                const sessionEndNotification: SessionEndNotification = JSON.parse(message.body);
                console.log('Parsed session end notification:', sessionEndNotification);
                onSessionEnd(sessionEndNotification);
              } catch (err) {
                console.error('Failed to parse session end message:', err);
              }
            });
          }
        },
        onStompError: (frame) => {
          console.error('WebSocket STOMP error:', frame);
        },
        onWebSocketError: (event) => {
          console.error('WebSocket error:', event);
        },
        onDisconnect: () => {
          console.log('WebSocket disconnected');
        },
      });

      stompClientRef.current = client;
      client.activate();
    };

    const wsTimer = setTimeout(() => {
      setupWebSocket();
    }, 500);

    return () => {
      clearTimeout(wsTimer);
      if (stompClientRef.current) {
        console.log('Cleaning up WebSocket connection');
        stompClientRef.current.deactivate();
        stompClientRef.current = null;
      }
    };
  }, [sessionId, onSessionUpdate, onLocationUpdate, onAddressUpdate, onOptimalLocationUpdate, onMeetingLocationUpdate, onSessionEnd]);
};

