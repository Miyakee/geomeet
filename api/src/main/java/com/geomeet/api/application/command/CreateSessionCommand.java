package com.geomeet.api.application.command;

/**
 * Command object for create session use case.
 * Represents the input for the create session operation.
 */
public class CreateSessionCommand {

    private final Long initiatorId;

    public CreateSessionCommand(Long initiatorId) {
        if (initiatorId == null) {
            throw new IllegalArgumentException("Initiator ID cannot be null");
        }
        this.initiatorId = initiatorId;
    }

    public Long getInitiatorId() {
        return initiatorId;
    }
}

