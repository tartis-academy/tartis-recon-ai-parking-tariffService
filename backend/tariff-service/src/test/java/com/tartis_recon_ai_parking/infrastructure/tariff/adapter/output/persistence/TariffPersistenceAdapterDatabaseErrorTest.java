package com.tartis_recon_ai_parking.infrastructure.tariff.adapter.output.persistence;

import com.tartis_recon_ai_parking.application.tariff.exception.ConcurrentModificationConflictException;
import com.tartis_recon_ai_parking.application.tariff.exception.PersistenceFailureException;
import com.tartis_recon_ai_parking.application.tariff.exception.PersistenceUnavailableException;
import com.tartis_recon_ai_parking.domain.tariff.Tariff;
import com.tartis_recon_ai_parking.domain.tariff.VehicleType;
import com.tartis_recon_ai_parking.domain.tariff.exception.CorruptedTariffDataException;
import com.tartis_recon_ai_parking.domain.tariff.exception.InvalidTariffException;
import com.tartis_recon_ai_parking.domain.tariff.exception.TariffAlreadyExistsException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.QueryTimeoutException;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Escenarios de ruptura de BD: verifica que TariffPersistenceAdapter
 * traduce la jerarquia DataAccessException de Spring a excepciones de
 * dominio/aplicacion, y que ningun detalle tecnico (SQL, nombre de
 * constraint o de tabla) sobrevive a la traduccion.
 */
@ExtendWith(MockitoExtension.class)
class TariffPersistenceAdapterDatabaseErrorTest {

    /** Mensaje realista de Postgres, con todo lo que NO debe filtrarse. */
    private static final String LEAKY_MESSAGE =
            "could not execute statement [ERROR: duplicate key value violates unique constraint "
            + "\"tariffs_name_key\" Detail: Key (name)=(Standard) already exists.] "
            + "[insert into tariffs (active,base_price,name,price_per_minute,type,unique_id) values (?,?,?,?,?,?)]";

    @Mock
    private TariffRepository tariffRepository;

    @Mock
    private TariffPersistenceMapper tariffPersistenceMapper;

    @InjectMocks
    private TariffPersistenceAdapter adapter;

    private Tariff sampleTariff() {
        return Tariff.reconstruct(UUID.randomUUID(), "Standard", VehicleType.CAR,
                new BigDecimal("0.05"), new BigDecimal("2.0"), true);
    }

    /** Prepara el camino de save() hasta justo antes de que la BD falle. */
    private Tariff stubSaveFailingWith(RuntimeException dbException) {
        Tariff tariff = sampleTariff();
        TariffEntity entity = new TariffEntity();
        when(tariffPersistenceMapper.toEntity(tariff)).thenReturn(entity);
        when(tariffRepository.save(entity)).thenThrow(dbException);
        return tariff;
    }

    @Test
    @DisplayName("Un nombre duplicado debe traducirse a TariffAlreadyExistsException sin filtrar el SQL ni la constraint")
    void shouldTranslateDuplicateKey() {
        Tariff tariff = stubSaveFailingWith(new DuplicateKeyException(LEAKY_MESSAGE));

        assertThatThrownBy(() -> adapter.save(tariff))
                .isInstanceOf(TariffAlreadyExistsException.class)
                .hasMessage("A tariff with that name already exists.")
                .hasMessageNotContaining("constraint")
                .hasMessageNotContaining("tariffs")
                .hasMessageNotContaining("insert into");
    }

    @Test
    @DisplayName("Un fallo de lock optimista debe traducirse a conflicto de concurrencia conservando la causa para el log")
    void shouldTranslateOptimisticLocking() {
        Tariff tariff = stubSaveFailingWith(new OptimisticLockingFailureException("Row was updated by another transaction"));

        assertThatThrownBy(() -> adapter.save(tariff))
                .isInstanceOf(ConcurrentModificationConflictException.class)
                .hasMessageNotContaining("transaction")
                .hasCauseInstanceOf(OptimisticLockingFailureException.class);
    }

    @Test
    @DisplayName("Un deadlock o lock no adquirido debe traducirse a conflicto de concurrencia")
    void shouldTranslateCannotAcquireLock() {
        Tariff tariff = stubSaveFailingWith(new CannotAcquireLockException("deadlock detected"));

        assertThatThrownBy(() -> adapter.save(tariff))
                .isInstanceOf(ConcurrentModificationConflictException.class)
                .hasCauseInstanceOf(CannotAcquireLockException.class);
    }

    @Test
    @DisplayName("Un timeout de consulta debe traducirse a PersistenceUnavailableException")
    void shouldTranslateQueryTimeout() {
        when(tariffRepository.findAll()).thenThrow(new QueryTimeoutException("statement timeout after 30s"));

        assertThatThrownBy(() -> adapter.findAll())
                .isInstanceOf(PersistenceUnavailableException.class)
                .hasMessageNotContaining("timeout after");
    }

    @Test
    @DisplayName("Una BD caida debe traducirse a PersistenceUnavailableException sin exponer host ni puerto")
    void shouldTranslateResourceFailure() {
        when(tariffRepository.findAll()).thenThrow(new DataAccessResourceFailureException(
                "Connection to tariff-db.internal:5432 refused"));

        assertThatThrownBy(() -> adapter.findAll())
                .isInstanceOf(PersistenceUnavailableException.class)
                .hasMessageNotContaining("5432")
                .hasMessageNotContaining("tariff-db.internal");
    }

    @Test
    @DisplayName("Una violacion de integridad que no sea duplicado debe traducirse a PersistenceFailureException")
    void shouldTranslateNonDuplicateIntegrityViolation() {
        Tariff tariff = stubSaveFailingWith(new DataIntegrityViolationException(
                "NULL not allowed for column \"PRICE_PER_MINUTE\""));

        assertThatThrownBy(() -> adapter.save(tariff))
                .isInstanceOf(PersistenceFailureException.class)
                .hasMessageNotContaining("PRICE_PER_MINUTE");
    }

    @Test
    @DisplayName("Un esquema desalineado (ddl-auto=validate) debe traducirse a PersistenceFailureException sin exponer el esquema")
    void shouldTranslateSchemaMismatch() {
        when(tariffRepository.findAll()).thenThrow(new InvalidDataAccessResourceUsageException(
                "relation \"tariff.tariffs\" does not exist"));

        assertThatThrownBy(() -> adapter.findAll())
                .isInstanceOf(PersistenceFailureException.class)
                .hasMessageNotContaining("relation")
                .hasMessageNotContaining("tariff.tariffs");
    }

    @Test
    @DisplayName("Una fila corrupta en BD debe dar CorruptedTariffDataException (500), NUNCA InvalidTariffException (400)")
    void shouldTranslateCorruptedRowOnRead() {
        UUID id = UUID.randomUUID();
        TariffEntity entity = new TariffEntity();
        when(tariffRepository.findById(id)).thenReturn(Optional.of(entity));
        when(tariffPersistenceMapper.toDomain(entity))
                .thenThrow(new InvalidTariffException("PricePerMinute must be greater than 0."));

        assertThatThrownBy(() -> adapter.findById(id))
                .isInstanceOf(CorruptedTariffDataException.class)
                .isNotInstanceOf(InvalidTariffException.class);
    }
}