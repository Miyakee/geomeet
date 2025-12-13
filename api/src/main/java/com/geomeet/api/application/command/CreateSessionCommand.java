package com.geomeet.api.application.command;

import lombok.Builder;
import lombok.Getter;

/**
 * Command object for create session use case.
 * Represents the input for the create session operation.
 */
@Getter
@Builder
public class CreateSessionCommand {

    private final Long initiatorId;

    public CreateSessionCommand(Long initiatorId) {
        if (initiatorId == null) {
            throw new IllegalArgumentException("Initiator ID cannot be null");
        }
        this.initiatorId = initiatorId;
    }

    public static CreateSessionCommand of(Long initiatorId) {
        return CreateSessionCommand.builder()
            .initiatorId(initiatorId)
            .build();
    }
}

