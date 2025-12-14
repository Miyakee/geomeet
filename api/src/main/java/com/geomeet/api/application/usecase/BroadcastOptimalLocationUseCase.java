package com.geomeet.api.application.usecase;

import com.geomeet.api.application.result.CalculateOptimalLocationResult;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Application service (Use Case) for broadcasting optimal location updates via WebSocket.
 * Orchestrates the optimal location broadcast flow.
 */
@Service
public class BroadcastOptimalLocationUseCase {

    private final SimpMessagingTemplate messagingTemplate;

    public BroadcastOptimalLocationUseCase(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Executes the broadcast optimal location use case.
     * Sends the optimal location result to all subscribers of the session's optimal location topic.
     *
     * @param result the calculate optimal location result
     */
    public void execute(CalculateOptimalLocationResult result) {
        // Broadcast to all subscribers of this session's optimal location topic
        messagingTemplate.convertAndSend(
            "/topic/session/" + result.getSessionIdString() + "/optimal-location",
            result
        );
    }
}

