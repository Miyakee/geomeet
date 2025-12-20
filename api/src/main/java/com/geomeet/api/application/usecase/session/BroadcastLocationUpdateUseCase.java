package com.geomeet.api.application.usecase.session;

import com.geomeet.api.application.result.UpdateLocationResult;
import lombok.AllArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Application service (Use Case) for broadcasting location updates via WebSocket.
 * Orchestrates the location update broadcast flow.
 */
@Service
@AllArgsConstructor
public class BroadcastLocationUpdateUseCase {

    private final SimpMessagingTemplate messagingTemplate;


    /**
     * Executes the broadcast location update use case.
     * Broadcasts the location update to all subscribers of the session.
     *
     * @param result the location update result to broadcast
     */
    public void execute(UpdateLocationResult result) {
        // Broadcast to all subscribers of this session's location updates
        String destination = "/topic/session/" + result.getSessionIdString() + "/locations";
        messagingTemplate.convertAndSend(destination, result);
    }
}

