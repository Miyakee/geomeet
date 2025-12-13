package com.geomeet.api.application.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class CreateSessionCommandTest {

    @Test
    void shouldCreateCommandSuccessfully() {
        // Given
        Long initiatorId = 1L;

        // When
        CreateSessionCommand command = CreateSessionCommand.of(initiatorId);

        // Then
        assertNotNull(command);
        assertEquals(initiatorId, command.getInitiatorId());
    }

    @Test
    void shouldCreateCommandUsingBuilder() {
        // Given
        Long initiatorId = 1L;

        // When
        CreateSessionCommand command = CreateSessionCommand.builder()
            .initiatorId(initiatorId)
            .build();

        // Then
        assertNotNull(command);
        assertEquals(initiatorId, command.getInitiatorId());
    }

    @Test
    void shouldThrowExceptionWhenInitiatorIdIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new CreateSessionCommand(null)
        );

        assertEquals("Initiator ID cannot be null", exception.getMessage());
    }
}

