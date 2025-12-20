package com.geomeet.api.application.usecase.session;

import com.geomeet.api.application.result.UpdateMeetingLocationResult;
import lombok.AllArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Application service (Use Case) for broadcasting meeting location updates via WebSocket.
 * Orchestrates the meeting location broadcast flow.
 */
@Service
@AllArgsConstructor
public class BroadcastMeetingLocationUseCase {

    private final SimpMessagingTemplate messagingTemplate;


    /**
     * Executes the broadcast meeting location update use case.
     * Broadcasts the meeting location update to all subscribers of the session.
     *
     * @param result the meeting location update result to broadcast
     */
    public void execute(UpdateMeetingLocationResult result) {
        // Broadcast to all subscribers of this session's meeting location topic
        String destination = "/topic/session/" + result.getSessionIdString() + "/meeting-location";
        messagingTemplate.convertAndSend(destination, result);
    }
}

