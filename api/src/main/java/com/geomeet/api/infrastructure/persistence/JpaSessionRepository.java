package com.geomeet.api.infrastructure.persistence;

import com.geomeet.api.infrastructure.persistence.entity.SessionEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for SessionEntity.
 * This works with JPA entities, not domain entities.
 */
@Repository
public interface JpaSessionRepository extends JpaRepository<SessionEntity, Long> {

    Optional<SessionEntity> findBySessionId(String sessionId);

    @Query("SELECT s FROM SessionEntity s WHERE s.sessionId = :sessionId")
    Optional<SessionEntity> findBySessionIdValue(@Param("sessionId") String sessionId);
}

