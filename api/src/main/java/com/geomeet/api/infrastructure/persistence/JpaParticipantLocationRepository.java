package com.geomeet.api.infrastructure.persistence;

import com.geomeet.api.infrastructure.persistence.entity.ParticipantLocationEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for ParticipantLocationEntity.
 */
@Repository
public interface JpaParticipantLocationRepository extends JpaRepository<ParticipantLocationEntity, Long> {

    Optional<ParticipantLocationEntity> findByParticipantId(Long participantId);

    Optional<ParticipantLocationEntity> findBySessionIdAndUserId(Long sessionId, Long userId);

    List<ParticipantLocationEntity> findBySessionId(Long sessionId);

    @Query("SELECT pl FROM ParticipantLocationEntity pl WHERE pl.sessionId = :sessionId")
    List<ParticipantLocationEntity> findBySessionIdQuery(@Param("sessionId") Long sessionId);
}

