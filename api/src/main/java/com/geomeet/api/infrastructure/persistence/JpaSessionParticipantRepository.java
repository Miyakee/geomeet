package com.geomeet.api.infrastructure.persistence;

import com.geomeet.api.infrastructure.persistence.entity.SessionParticipantEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for SessionParticipantEntity.
 */
@Repository
public interface JpaSessionParticipantRepository extends JpaRepository<SessionParticipantEntity, Long> {

    List<SessionParticipantEntity> findBySessionId(Long sessionId);

    Optional<SessionParticipantEntity> findBySessionIdAndUserId(Long sessionId, Long userId);

    @Query("SELECT COUNT(sp) FROM SessionParticipantEntity sp WHERE sp.sessionId = :sessionId")
    long countBySessionId(@Param("sessionId") Long sessionId);

    boolean existsBySessionIdAndUserId(Long sessionId, Long userId);
}

