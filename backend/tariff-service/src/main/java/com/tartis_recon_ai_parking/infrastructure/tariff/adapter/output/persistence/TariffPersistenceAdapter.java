package com.tartis_recon_ai_parking.infrastructure.tariff.adapter.output.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Repository;

import com.tartis_recon_ai_parking.application.tariff.port.output.TariffPersistence;
import com.tartis_recon_ai_parking.domain.tariff.Tariff;
import com.tartis_recon_ai_parking.domain.tariff.VehicleType;
import com.tartis_recon_ai_parking.domain.tariff.exception.TariffConcurrentModificationException;


@Repository
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
        try {
            TariffEntity savedEntity = tariffRepository.save(entity);
            return tariffPersistenceMapper.toDomain(savedEntity);
        } catch (OptimisticLockingFailureException ex) {
            // Otra transaccion actualizo esta misma tarifa entre el
            // findById() del caso de uso y este save() (TAR-1780).
            throw new TariffConcurrentModificationException(tariff.getUniqueId());
        }
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

    @Override
    public List<Tariff> findActiveByType(VehicleType type) {
        return tariffRepository.findByActiveTrueAndType(type).stream().map(tariffPersistenceMapper::toDomain).toList();
    }

}
