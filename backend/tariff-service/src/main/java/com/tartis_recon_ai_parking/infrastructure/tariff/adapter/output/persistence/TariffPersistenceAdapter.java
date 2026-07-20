package com.tartis_recon_ai_parking.infrastructure.tariff.adapter.output.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.tartis_recon_ai_parking.application.tariff.port.output.TariffPersistence;
import com.tartis_recon_ai_parking.domain.tariff.Tariff;

public class TariffPersistenceAdapter implements TariffPersistence {

    private final TariffRepository tariffRepository;
    private final TariffPersistenceMapper tariffPersistenceMapper;

    public TariffPersistenceAdapter(TariffRepository tariffRepository,
            TariffPersistenceMapper tariffPersistenceMapper) {
        this.tariffRepository = tariffRepository;
        this.tariffPersistenceMapper = tariffPersistenceMapper;
    }

    @Override
    public Tariff save(Tariff tariff) {

        TariffEntity entity = tariffPersistenceMapper.toEntity(tariff);
        TariffEntity savedEntity = tariffRepository.save(entity);
        return tariffPersistenceMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Tariff> findByName(String name) {
        return tariffRepository.findByName(name).map(tariffPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsByName(String name) {
        return tariffRepository.existsByName(name);
    }

    @Override
    public List<Tariff> findAll() {
        return tariffRepository.findAll().stream().map(tariffPersistenceMapper::toDomain).toList();
    }

    @Override
    public Optional<Tariff> findById(UUID id) {
        return tariffRepository.findById(id).map(tariffPersistenceMapper::toDomain);

    }

    @Override
    public List<Tariff> findByActive(boolean active) {
        return tariffRepository.findByActive(active).stream().map(tariffPersistenceMapper::toDomain).toList();
    }

}
