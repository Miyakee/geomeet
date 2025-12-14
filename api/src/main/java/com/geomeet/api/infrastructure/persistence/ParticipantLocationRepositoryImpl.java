package com.geomeet.api.infrastructure.persistence;

import com.geomeet.api.application.usecase.ParticipantLocationRepository;
import com.geomeet.api.domain.entity.ParticipantLocation;
import com.geomeet.api.infrastructure.persistence.entity.ParticipantLocationEntity;
import com.geomeet.api.infrastructure.persistence.mapper.ParticipantLocationMapper;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Implementation of ParticipantLocationRepository using JPA.
 * This adapter converts between Domain ParticipantLocation and JPA ParticipantLocationEntity.
 */
@Component
public class ParticipantLocationRepositoryImpl implements ParticipantLocationRepository {

    private final JpaParticipantLocationRepository jpaParticipantLocationRepository;
    private final ParticipantLocationMapper participantLocationMapper;

    public ParticipantLocationRepositoryImpl(
        JpaParticipantLocationRepository jpaParticipantLocationRepository,
        ParticipantLocationMapper participantLocationMapper
    ) {
        this.jpaParticipantLocationRepository = jpaParticipantLocationRepository;
        this.participantLocationMapper = participantLocationMapper;
    }

    @Override
    public ParticipantLocation save(ParticipantLocation location) {
        ParticipantLocationEntity entity = participantLocationMapper.toEntity(location);
        ParticipantLocationEntity savedEntity = jpaParticipantLocationRepository.save(entity);
        return participantLocationMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<ParticipantLocation> findByParticipantId(Long participantId) {
        return jpaParticipantLocationRepository.findByParticipantId(participantId)
            .map(participantLocationMapper::toDomain);
    }

    @Override
    public Optional<ParticipantLocation> findBySessionIdAndUserId(Long sessionId, Long userId) {
        return jpaParticipantLocationRepository.findBySessionIdAndUserId(sessionId, userId)
            .map(participantLocationMapper::toDomain);
    }

    @Override
    public List<ParticipantLocation> findBySessionId(Long sessionId) {
        return jpaParticipantLocationRepository.findBySessionId(sessionId).stream()
            .map(participantLocationMapper::toDomain)
            .collect(Collectors.toList());
    }
}

