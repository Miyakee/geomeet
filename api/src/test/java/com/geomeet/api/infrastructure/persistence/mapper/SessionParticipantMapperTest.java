package com.geomeet.api.infrastructure.persistence.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.geomeet.api.domain.entity.SessionParticipant;
import com.geomeet.api.infrastructure.persistence.entity.SessionParticipantEntity;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SessionParticipantMapperTest {

  private SessionParticipantEntity participantEntity;
  private SessionParticipant domainParticipant;
  private Long participantId;
  private Long sessionId;
  private Long userId;

  private SessionParticipantMapper mapper;

  @BeforeEach
  void setUp() {
    participantId = 1L;
    sessionId = 100L;
    userId = 1L;
    mapper = new SessionParticipantMapperImpl();
    participantEntity = SessionParticipantEntity.builder()
        .id(participantId)
        .sessionId(sessionId)
        .userId(userId)
        .joinedAt(LocalDateTime.now())
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy("admin")
        .updatedBy("admin")
        .build();

    domainParticipant = SessionParticipant.reconstruct(
        participantId,
        sessionId,
        userId,
        LocalDateTime.now(),
        LocalDateTime.now(),
        LocalDateTime.now(),
        "admin",
        "admin"
    );
  }

  @Test
  void shouldMapEntityToDomain() {
    SessionParticipantMapper mapper = new SessionParticipantMapper() {
    };
    SessionParticipant mappedParticipant = mapper.toDomain(participantEntity);

    assertNotNull(mappedParticipant);
    assertEquals(participantEntity.getId(), mappedParticipant.getId());
    assertEquals(participantEntity.getSessionId(), mappedParticipant.getSessionId());
    assertEquals(participantEntity.getUserId(), mappedParticipant.getUserId());
    assertEquals(participantEntity.getJoinedAt(), mappedParticipant.getJoinedAt());
    assertEquals(participantEntity.getCreatedAt(), mappedParticipant.getCreatedAt());
    assertEquals(participantEntity.getUpdatedAt(), mappedParticipant.getUpdatedAt());
    assertEquals(participantEntity.getCreatedBy(), mappedParticipant.getCreatedBy());
    assertEquals(participantEntity.getUpdatedBy(), mappedParticipant.getUpdatedBy());
  }

  @Test
  void shouldMapDomainToEntity() {
    SessionParticipantEntity mappedEntity = mapper.toEntity(domainParticipant);

    assertNotNull(mappedEntity);
    assertEquals(domainParticipant.getId(), mappedEntity.getId());
    assertEquals(domainParticipant.getSessionId(), mappedEntity.getSessionId());
    assertEquals(domainParticipant.getUserId(), mappedEntity.getUserId());
    assertEquals(domainParticipant.getJoinedAt(), mappedEntity.getJoinedAt());
    assertEquals(domainParticipant.getCreatedAt(), mappedEntity.getCreatedAt());
    assertEquals(domainParticipant.getUpdatedAt(), mappedEntity.getUpdatedAt());
    assertEquals(domainParticipant.getCreatedBy(), mappedEntity.getCreatedBy());
    assertEquals(domainParticipant.getUpdatedBy(), mappedEntity.getUpdatedBy());
  }

  @Test
  void shouldReturnNullWhenMappingNullEntity() {
    SessionParticipant mappedParticipant = mapper.toDomain(null);
    assertNull(mappedParticipant);
  }

  @Test
  void shouldReturnNullWhenMappingNullDomain() {
    SessionParticipantEntity mappedEntity = mapper.toEntity(null);
    assertNull(mappedEntity);
  }

  @Test
  void shouldMapEntityWithNullAuditFields() {
    participantEntity.setCreatedBy(null);
    participantEntity.setUpdatedBy(null);

    SessionParticipant mappedParticipant = mapper.toDomain(participantEntity);

    assertNotNull(mappedParticipant);
    assertNull(mappedParticipant.getCreatedBy());
    assertNull(mappedParticipant.getUpdatedBy());
  }

}

