package com.tartis_recon_ai_parking.infrastructure.tariff.adapter.output.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.stereotype.Repository;

import com.tartis_recon_ai_parking.application.tariff.exception.ConcurrentModificationConflictException;
import com.tartis_recon_ai_parking.application.tariff.exception.PersistenceFailureException;
import com.tartis_recon_ai_parking.application.tariff.exception.PersistenceUnavailableException;
import com.tartis_recon_ai_parking.application.tariff.port.output.TariffPersistence;
import com.tartis_recon_ai_parking.domain.tariff.Tariff;
import com.tartis_recon_ai_parking.domain.tariff.VehicleType;
import com.tartis_recon_ai_parking.domain.tariff.exception.CorruptedTariffDataException;
import com.tartis_recon_ai_parking.domain.tariff.exception.InvalidTariffException;
import com.tartis_recon_ai_parking.domain.tariff.exception.TariffAlreadyExistsException;

/**
 * Frontera de traduccion: aqui muere la jerarquia DataAccessException de
 * Spring. Hacia arriba solo viajan excepciones de dominio/aplicacion, de
 * modo que ni los casos de uso ni el frontend ven nunca detalles de BD.
 */
@Repository
public class TariffPersistenceAdapter implements TariffPersistence {

    private static final Logger log = LoggerFactory.getLogger(TariffPersistenceAdapter.class);

    private final TariffRepository tariffRepository;
    private final TariffPersistenceMapper tariffPersistenceMapper;

    public TariffPersistenceAdapter(TariffRepository tariffRepository,
            TariffPersistenceMapper tariffPersistenceMapper) {
        this.tariffRepository = tariffRepository;
        this.tariffPersistenceMapper = tariffPersistenceMapper;
    }

    @Override
    public Tariff save(Tariff tariff) {
        try {
            return execute("save", () -> {
                TariffEntity entity = tariffPersistenceMapper.toEntity(tariff);
                TariffEntity saved = tariffRepository.save(entity);
                return tariffPersistenceMapper.toDomain(saved);
            });
        } catch (ConcurrentModificationConflictException ex) {
            // Reenriquecemos el mensaje con el id de la tarifa: es el
            // detalle que aportaba TariffConcurrentModificationException
            // de TAR-1780, que estamos consolidando en esta unica excepcion.
            throw new ConcurrentModificationConflictException(
                    "The tariff " + tariff.getUniqueId() + " was modified by another request. Please retry.",
                    ex.getCause());
        }
    }

    @Override
    public Optional<Tariff> findByName(String name) {
        return execute("findByName",
                () -> tariffRepository.findByName(name).map(tariffPersistenceMapper::toDomain));
    }

    @Override
    public boolean existsByName(String name) {
        return execute("existsByName", () -> tariffRepository.existsByName(name));
    }

    @Override
    public List<Tariff> findAll() {
        return execute("findAll",
                () -> tariffRepository.findAll().stream().map(tariffPersistenceMapper::toDomain).toList());
    }

    @Override
    public Optional<Tariff> findById(UUID id) {
        return execute("findById",
                () -> tariffRepository.findById(id).map(tariffPersistenceMapper::toDomain));
    }

    @Override
    public List<Tariff> findByActive(boolean active) {
        return execute("findByActive",
                () -> tariffRepository.findByActive(active).stream().map(tariffPersistenceMapper::toDomain).toList());
    }

    @Override
    public List<Tariff> findActiveByType(VehicleType type) {
        return execute("findActiveByType",
                () -> tariffRepository.findByActiveTrueAndType(type).stream()
                        .map(tariffPersistenceMapper::toDomain).toList());
    }

    @Override
    public List<Tariff> findActiveByTypeForUpdate(VehicleType type) {
        return execute("findActiveByTypeForUpdate",
                () -> tariffRepository.findByActiveTrueAndTypeForUpdate(type).stream()
                        .map(tariffPersistenceMapper::toDomain).toList());
    }

    /**
     * El orden de los catch importa: Java exige subclase antes que
     * superclase, y ademas queremos distinguir lo reintentable (409/503)
     * de lo definitivo (500).
     */
    private <T> T execute(String operation, Supplier<T> action) {
        try {
            return action.get();

        } catch (DuplicateKeyException ex) {
            // Nombre duplicado. Es la carrera que el chequeo previo del
            // caso de uso no puede cerrar: la BD es el arbitro final.
            log.warn("Violacion de unicidad en '{}'", operation, ex);
            throw new TariffAlreadyExistsException("A tariff with that name already exists.");

        } catch (OptimisticLockingFailureException ex) {
            log.warn("Conflicto de lock optimista en '{}'", operation, ex);
            throw new ConcurrentModificationConflictException(
                    "The tariff was modified by another request. Please retry.", ex);

        } catch (PessimisticLockingFailureException ex) {
            // Cubre CannotAcquireLockException y DeadlockLoserDataAccessException.
            log.warn("Deadlock o lock no adquirido en '{}'", operation, ex);
            throw new ConcurrentModificationConflictException(
                    "The operation conflicted with another in-flight request. Please retry.", ex);

        } catch (QueryTimeoutException ex) {
            log.error("Timeout de consulta en '{}'", operation, ex);
            throw new PersistenceUnavailableException("The database did not respond in time.", ex);

        } catch (DataAccessResourceFailureException ex) {
            // BD caida / sin conexion. Ojo: NO es TransientDataAccessException.
            log.error("Fallo de conexion con la BD en '{}'", operation, ex);
            throw new PersistenceUnavailableException("The database is currently unavailable.", ex);

        } catch (TransientDataAccessException ex) {
            log.error("Fallo transitorio de BD en '{}'", operation, ex);
            throw new PersistenceUnavailableException("Temporary database failure.", ex);

        } catch (DataIntegrityViolationException ex) {
            // Integridad no-duplicado (NOT NULL, longitud, CHECK). Si llega
            // aqui es que falta una validacion de entrada: bug nuestro, 500.
            log.error("Violacion de integridad no clasificada en '{}'", operation, ex);
            throw new PersistenceFailureException("The data could not be persisted.", ex);

        } catch (InvalidTariffException ex) {
            // Tariff.reconstruct() valida invariantes AL LEER. Si una fila
            // de BD los incumple, no es culpa del cliente: no puede acabar
            // en un 400.
            log.error("Fila de BD que incumple invariantes de dominio en '{}'", operation, ex);
            throw new CorruptedTariffDataException("Stored tariff data is inconsistent.", ex);

        } catch (DataAccessException ex) {
            // Catch-all: esquema desalineado, SQL invalido, etc.
            log.error("Fallo de acceso a datos en '{}'", operation, ex);
            throw new PersistenceFailureException("Unexpected persistence failure.", ex);
        }
    }
}