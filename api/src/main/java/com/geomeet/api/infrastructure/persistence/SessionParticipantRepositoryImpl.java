package com.geomeet.api.infrastructure.persistence;

import com.geomeet.api.application.usecase.SessionParticipantRepository;
import com.geomeet.api.domain.entity.SessionParticipant;
import com.geomeet.api.infrastructure.persistence.entity.SessionParticipantEntity;
import com.geomeet.api.infrastructure.persistence.mapper.SessionParticipantMapper;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Implementation of SessionParticipantRepository using JPA.
 * This adapter converts between Domain SessionParticipant and JPA SessionParticipantEntity.
 */
@Component
public class SessionParticipantRepositoryImpl implements SessionParticipantRepository {

    private final JpaSessionParticipantRepository jpaSessionParticipantRepository;
    private final SessionParticipantMapper sessionParticipantMapper;

    public SessionParticipantRepositoryImpl(
        JpaSessionParticipantRepository jpaSessionParticipantRepository,
        SessionParticipantMapper sessionParticipantMapper
    ) {
        this.jpaSessionParticipantRepository = jpaSessionParticipantRepository;
        this.sessionParticipantMapper = sessionParticipantMapper;
    }

    @Override
    public SessionParticipant save(SessionParticipant participant) {
        SessionParticipantEntity entity = sessionParticipantMapper.toEntity(participant);
        SessionParticipantEntity savedEntity = jpaSessionParticipantRepository.save(entity);
        return sessionParticipantMapper.toDomain(savedEntity);
    }

    @Override
    public List<SessionParticipant> findBySessionId(Long sessionId) {
        return jpaSessionParticipantRepository.findBySessionId(sessionId).stream()
            .map(sessionParticipantMapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public Optional<SessionParticipant> findBySessionIdAndUserId(Long sessionId, Long userId) {
        return jpaSessionParticipantRepository.findBySessionIdAndUserId(sessionId, userId)
            .map(sessionParticipantMapper::toDomain);
    }

    @Override
    public boolean existsBySessionIdAndUserId(Long sessionId, Long userId) {
        return jpaSessionParticipantRepository.existsBySessionIdAndUserId(sessionId, userId);
    }

    @Override
    public long countBySessionId(Long sessionId) {
        return jpaSessionParticipantRepository.countBySessionId(sessionId);
    }
}

