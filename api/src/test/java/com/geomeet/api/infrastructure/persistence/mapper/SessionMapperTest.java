package com.geomeet.api.infrastructure.persistence.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.geomeet.api.domain.entity.Session;
import com.geomeet.api.domain.valueobject.SessionId;
import com.geomeet.api.domain.valueobject.SessionStatus;
import com.geomeet.api.infrastructure.persistence.entity.SessionEntity;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SessionMapperTest {

    private SessionEntity sessionEntity;
    private Session domainSession;
    private Long sessionDbId;
    private SessionId sessionId;

    @BeforeEach
    void setUp() {
        sessionDbId = 100L;
        sessionId = SessionId.generate();

        sessionEntity = SessionEntity.builder()
            .id(sessionDbId)
            .sessionId(sessionId.getValue())
            .initiatorId(1L)
            .status(SessionStatus.ACTIVE.getValue())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .createdBy("admin")
            .updatedBy("admin")
            .build();

        domainSession = Session.reconstruct(
            sessionDbId,
            sessionId,
            1L,
            SessionStatus.ACTIVE,
            LocalDateTime.now(),
            LocalDateTime.now(),
            "admin",
            "admin"
        );
    }

    @Test
    void shouldMapEntityToDomain() {
        SessionMapper mapper = new SessionMapper() {};
        Session mappedSession = mapper.toDomain(sessionEntity);

        assertNotNull(mappedSession);
        assertEquals(sessionEntity.getId(), mappedSession.getId());
        assertEquals(sessionEntity.getSessionId(), mappedSession.getSessionId().getValue());
        assertEquals(sessionEntity.getInitiatorId(), mappedSession.getInitiatorId());
        assertEquals(SessionStatus.ACTIVE, mappedSession.getStatus());
        assertEquals(sessionEntity.getCreatedAt(), mappedSession.getCreatedAt());
        assertEquals(sessionEntity.getUpdatedAt(), mappedSession.getUpdatedAt());
        assertEquals(sessionEntity.getCreatedBy(), mappedSession.getCreatedBy());
        assertEquals(sessionEntity.getUpdatedBy(), mappedSession.getUpdatedBy());
    }

    @Test
    void shouldMapDomainToEntity() {
        SessionMapper mapper = new SessionMapper() {};
        SessionEntity mappedEntity = mapper.toEntity(domainSession);

        assertNotNull(mappedEntity);
        assertEquals(domainSession.getId(), mappedEntity.getId());
        assertEquals(domainSession.getSessionId().getValue(), mappedEntity.getSessionId());
        assertEquals(domainSession.getInitiatorId(), mappedEntity.getInitiatorId());
        assertEquals(domainSession.getStatus().getValue(), mappedEntity.getStatus());
        assertEquals(domainSession.getCreatedAt(), mappedEntity.getCreatedAt());
        assertEquals(domainSession.getUpdatedAt(), mappedEntity.getUpdatedAt());
        assertEquals(domainSession.getCreatedBy(), mappedEntity.getCreatedBy());
        assertEquals(domainSession.getUpdatedBy(), mappedEntity.getUpdatedBy());
    }

    @Test
    void shouldReturnNullWhenMappingNullEntity() {
        SessionMapper mapper = new SessionMapper() {};
        Session mappedSession = mapper.toDomain(null);
        assertNull(mappedSession);
    }

    @Test
    void shouldReturnNullWhenMappingNullDomain() {
        SessionMapper mapper = new SessionMapper() {};
        SessionEntity mappedEntity = mapper.toEntity(null);
        assertNull(mappedEntity);
    }

    @Test
    void shouldMapEntityWithNullAuditFields() {
        sessionEntity.setCreatedBy(null);
        sessionEntity.setUpdatedBy(null);

        SessionMapper mapper = new SessionMapper() {};
        Session mappedSession = mapper.toDomain(sessionEntity);

        assertNotNull(mappedSession);
        assertNull(mappedSession.getCreatedBy());
        assertNull(mappedSession.getUpdatedBy());
    }


    @Test
    void shouldMapEndedSessionStatus() {
        sessionEntity.setStatus(SessionStatus.ENDED.getValue());
        SessionMapper mapper = new SessionMapper() {};
        Session mappedSession = mapper.toDomain(sessionEntity);

        assertNotNull(mappedSession);
        assertEquals(SessionStatus.ENDED, mappedSession.getStatus());
    }
}

