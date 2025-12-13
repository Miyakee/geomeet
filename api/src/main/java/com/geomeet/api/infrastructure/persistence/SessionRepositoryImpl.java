package com.geomeet.api.infrastructure.persistence;

import com.geomeet.api.application.usecase.SessionRepository;
import com.geomeet.api.domain.entity.Session;
import com.geomeet.api.domain.valueobject.SessionId;
import com.geomeet.api.infrastructure.persistence.entity.SessionEntity;
import com.geomeet.api.infrastructure.persistence.mapper.SessionMapper;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Implementation of SessionRepository using JPA.
 * This adapter converts between Domain Session and JPA SessionEntity.
 */
@Component
public class SessionRepositoryImpl implements SessionRepository {

    private final JpaSessionRepository jpaSessionRepository;

    public SessionRepositoryImpl(JpaSessionRepository jpaSessionRepository) {
        this.jpaSessionRepository = jpaSessionRepository;
    }

    @Override
    public Session save(Session session) {
        SessionEntity entity = SessionMapper.toEntity(session);
        SessionEntity savedEntity = jpaSessionRepository.save(entity);
        return SessionMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Session> findBySessionId(SessionId sessionId) {
        return jpaSessionRepository.findBySessionId(sessionId.getValue())
            .map(SessionMapper::toDomain);
    }

    @Override
    public Optional<Session> findById(Long id) {
        return jpaSessionRepository.findById(id)
            .map(SessionMapper::toDomain);
    }
}

