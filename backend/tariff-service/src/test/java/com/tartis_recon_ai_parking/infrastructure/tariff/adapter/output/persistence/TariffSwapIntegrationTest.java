package com.tartis_recon_ai_parking.infrastructure.tariff.adapter.output.persistence;

import com.tartis_recon_ai_parking.domain.tariff.Tariff;
import com.tartis_recon_ai_parking.domain.tariff.VehicleType;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

// Regresion del swap de IN-17 contra un Postgres real.
//
// Por que no vale un test unitario: los tests de ActivateTariffUseCase mockean
// TariffPersistence, asi que el indice ux_tariffs_one_active_per_type nunca
// entra en juego. Por eso la suite estaba en verde mientras el swap devolvia
// 503 en la demo: el indice es PARCIAL (WHERE active) y ni H2 ni un mock lo
// reproducen.
//
// Piezas de la configuracion:
// - @AutoConfigureTestDatabase(replace = NONE): sin esto @DataJpaTest cambiaria
//   el DataSource por H2 y el contenedor no se usaria.
// - flyway.enabled + ddl-auto=none: el src/test/resources/application.properties
//   apaga Flyway y deja que Hibernate cree el esquema. Aqui hace falta lo
//   contrario: el indice unico solo existe en la migracion V4, no en el modelo.
// - @Transactional(NOT_SUPPORTED): @DataJpaTest envolveria cada test en una
//   transaccion con rollback, y la violacion del indice se ve en el COMMIT.
//   Sin commit real no hay nada que comprobar. La limpieza va a mano.
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Import({TariffPersistenceAdapter.class, TariffPersistenceMapperImpl.class})
@TestPropertySource(properties = {
        "spring.flyway.enabled=true",
        "spring.jpa.hibernate.ddl-auto=none"
})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class TariffSwapIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.18-alpine");

    @Autowired
    private TariffPersistenceAdapter adapter;

    @Autowired
    private TariffRepository repository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @AfterEach
    void limpiar() {
        repository.deleteAll();
    }

    private Tariff nueva(String name, VehicleType type, boolean active) {
        return Tariff.reconstruct(UUID.randomUUID(), name, type,
                new BigDecimal("0.05"), new BigDecimal("1.00"), active);
    }

    private List<Tariff> activasDe(VehicleType type) {
        return adapter.findAll().stream()
                .filter(t -> t.getType() == type && t.isActive())
                .toList();
    }

    @Test
    @DisplayName("Activar una tarifa releva a la vigente del mismo tipo sin violar el indice unico")
    void swapNoVulneraElIndiceUnico() {
        Tariff vigente = adapter.save(nueva("CAR vigente", VehicleType.CAR, true));
        Tariff relevo = adapter.save(nueva("CAR relevo", VehicleType.CAR, false));

        // Misma secuencia que ActivateTariffUseCase, en una transaccion real:
        // el fallo que se persigue solo aparece al hacer commit.
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            adapter.findActiveByTypeForUpdate(VehicleType.CAR);
            adapter.deactivateActiveByType(VehicleType.CAR, relevo.getUniqueId());
            adapter.save(relevo.activate());
        });

        List<Tariff> activas = activasDe(VehicleType.CAR);
        assertThat(activas).hasSize(1);
        assertThat(activas.get(0).getUniqueId()).isEqualTo(relevo.getUniqueId());
        assertThat(adapter.findById(vigente.getUniqueId()).orElseThrow().isActive()).isFalse();
    }

    @Test
    @DisplayName("El alta de una tarifa activa releva a la vigente de su tipo")
    void altaActivaRelevaALaVigente() {
        Tariff vigente = adapter.save(nueva("CAR vigente", VehicleType.CAR, true));
        Tariff alta = nueva("CAR alta", VehicleType.CAR, true);

        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            adapter.findActiveByTypeForUpdate(VehicleType.CAR);
            adapter.deactivateActiveByType(VehicleType.CAR, null);
            adapter.save(alta);
        });

        List<Tariff> activas = activasDe(VehicleType.CAR);
        assertThat(activas).hasSize(1);
        assertThat(activas.get(0).getUniqueId()).isEqualTo(alta.getUniqueId());
        assertThat(adapter.findById(vigente.getUniqueId()).orElseThrow().isActive()).isFalse();
    }

    @Test
    @DisplayName("El swap no toca las tarifas activas de otro tipo de vehiculo")
    void noTocaOtrosTipos() {
        Tariff moto = adapter.save(nueva("MOTO vigente", VehicleType.MOTORBIKE, true));
        adapter.save(nueva("CAR vigente", VehicleType.CAR, true));
        Tariff relevo = adapter.save(nueva("CAR relevo", VehicleType.CAR, false));

        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            adapter.deactivateActiveByType(VehicleType.CAR, relevo.getUniqueId());
            adapter.save(relevo.activate());
        });

        assertThat(adapter.findById(moto.getUniqueId()).orElseThrow().isActive()).isTrue();
        assertThat(activasDe(VehicleType.MOTORBIKE)).hasSize(1);
    }

    @Test
    @DisplayName("El update masivo incrementa @Version: sin eso el lock optimista se rompe en silencio")
    void elUpdateMasivoIncrementaLaVersion() {
        Tariff vigente = adapter.save(nueva("CAR vigente", VehicleType.CAR, true));
        long versionInicial = repository.findById(vigente.getUniqueId()).orElseThrow().getVersion();

        new TransactionTemplate(transactionManager).executeWithoutResult(status ->
                adapter.deactivateActiveByType(VehicleType.CAR, null));

        long versionFinal = repository.findById(vigente.getUniqueId()).orElseThrow().getVersion();
        assertThat(versionFinal).isEqualTo(versionInicial + 1);
    }
}
