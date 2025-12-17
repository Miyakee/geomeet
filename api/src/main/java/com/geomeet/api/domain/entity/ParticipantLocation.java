package com.geomeet.api.domain.entity;

import com.geomeet.api.domain.valueobject.Location;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * Participant Location entity.
 * Represents a participant's location in a session.
 * This is not an aggregate root, but a part of the SessionParticipant aggregate.
 */
@Getter
@Setter(AccessLevel.PACKAGE)
public class ParticipantLocation {

    private Long id;
    private Long participantId;
    private Long sessionId;
    private Long userId;
    private Location location; // Value Object containing latitude, longitude, accuracy
    private LocalDateTime updatedAt;
    private LocalDateTime createdAt;
    private String createdBy;
    private String updatedBy;

    // Private constructor for reconstruction
    private ParticipantLocation() {
    }

    /**
     * Factory method to create a new ParticipantLocation.
     *
     * @param participantId the participant ID
     * @param sessionId the session ID
     * @param userId the user ID
     * @param location the location value object
     * @return a new ParticipantLocation
     */
    public static ParticipantLocation create(
        Long participantId,
        Long sessionId,
        Long userId,
        Location location
    ) {
        ParticipantLocation participantLocation = new ParticipantLocation();
        participantLocation.participantId = participantId;
        participantLocation.sessionId = sessionId;
        participantLocation.userId = userId;
        participantLocation.location = location;
        LocalDateTime now = LocalDateTime.now();
        participantLocation.updatedAt = now;
        participantLocation.createdAt = now;
        return participantLocation;
    }

    /**
     * Factory method to reconstruct ParticipantLocation from persistence.
     */
    public static ParticipantLocation reconstruct(
        Long id,
        Long participantId,
        Long sessionId,
        Long userId,
        Double latitude,
        Double longitude,
        Double accuracy,
        LocalDateTime updatedAt,
        LocalDateTime createdAt,
        String createdBy,
        String updatedBy
    ) {
        ParticipantLocation participantLocation = new ParticipantLocation();
        participantLocation.id = id;
        participantLocation.participantId = participantId;
        participantLocation.sessionId = sessionId;
        participantLocation.userId = userId;
        participantLocation.location = Location.of(latitude, longitude, accuracy);
        participantLocation.updatedAt = updatedAt;
        participantLocation.createdAt = createdAt;
        participantLocation.createdBy = createdBy;
        participantLocation.updatedBy = updatedBy;
        return participantLocation;
    }

    /**
     * Business method: Update location.
     *
     * @param newLocation the new location
     */
    public void updateLocation(Location newLocation) {
        if (newLocation == null) {
            throw new IllegalArgumentException("Location cannot be null");
        }
        this.location = newLocation;
        this.updatedAt = LocalDateTime.now();
    }
}

