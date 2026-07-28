package com.tartis_recon_ai_parking.infrastructure.tariff.adapter.output.persistence;

import com.tartis_recon_ai_parking.domain.tariff.VehicleType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Escenarios de ruptura a nivel de BD: comprobamos que las constraints
 * existen y que salta la excepcion esperada. Complementa a
 * TariffRepositoryTest, que solo cubre el camino feliz.
 */
@DataJpaTest
class TariffRepositoryConstraintTest {
    @Autowired
    private TestEntityManager entityManager;   // añadir al principio de la clase

    @Autowired
    private TariffRepository tariffRepository;

    private TariffEntity buildEntity(String name, VehicleType type, boolean active) {
        TariffEntity entity = new TariffEntity();
        entity.setUniqueId(UUID.randomUUID());
        entity.setName(name);
        entity.setType(type);
        entity.setPricePerMinute(new BigDecimal("0.05"));
        entity.setBasePrice(new BigDecimal("2.0"));
        entity.setActive(active);
        return entity;
    }

    @Test
    @DisplayName("Insertar dos tarifas con el mismo nombre debe violar la constraint UNIQUE")
    void shouldFailOnDuplicateName() {
        tariffRepository.saveAndFlush(buildEntity("Standard", VehicleType.CAR, true));

        assertThatThrownBy(() ->
                tariffRepository.saveAndFlush(buildEntity("Standard", VehicleType.MOTORBIKE, true)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Un nombre mas largo que la columna VARCHAR(255) debe fallar en BD")
    void shouldFailOnNameTooLong() {
        String tooLong = "X".repeat(300);

        assertThatThrownBy(() ->
                tariffRepository.saveAndFlush(buildEntity(tooLong, VehicleType.CAR, true)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Un campo NOT NULL a null debe violar la constraint")
    void shouldFailOnNullMandatoryField() {
        TariffEntity entity = buildEntity("NullPrice", VehicleType.CAR, true);
        entity.setPricePerMinute(null);

        assertThatThrownBy(() -> tariffRepository.saveAndFlush(entity))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("HALLAZGO: nada impide dos tarifas activas del mismo tipo, y el calculo de precio depende de cual devuelva primero la BD")
    void shouldCurrentlyAllowTwoActiveTariffsOfSameType() {
        tariffRepository.saveAndFlush(buildEntity("CarA", VehicleType.CAR, true));

        assertThatCode(() ->
                tariffRepository.saveAndFlush(buildEntity("CarB", VehicleType.CAR, true)))
                .doesNotThrowAnyException();

        assertThat(tariffRepository.findByActiveTrueAndType(VehicleType.CAR)).hasSize(2);
    }

    @Test
    @DisplayName("HALLAZGO: la escala de la columna redondea el precio por minuto; en prod (NUMERIC sin escala) no lo haria")
    void shouldExposeDecimalScaleDivergence() {
        TariffEntity entity = buildEntity("Precision", VehicleType.CAR, true);
        entity.setPricePerMinute(new BigDecimal("0.055"));
        tariffRepository.saveAndFlush(entity);

        // CLAVE: vacia el persistence context. Sin esto, Hibernate
        // devuelve la instancia que ya tiene en memoria y el test
        // comprueba el objeto Java, no lo que la columna guardo.
        entityManager.clear();

        BigDecimal stored = tariffRepository.findByName("Precision")
                .orElseThrow()
                .getPricePerMinute();

        // Documenta la divergencia detectada (ver ticket de seguimiento):
        // la entidad no declara precision/scale, asi que Hibernate aplica
        // su default numeric(38,2) y REDONDEA a 2 decimales. El schema.sql
        // de prod usa NUMERIC sin escala (precision arbitraria en
        // Postgres), asi que el mismo insert conserva 0.055 alli.
        // Si este test empieza a fallar es que se corrigio la escala:
        // actualiza el valor esperado y cierra el ticket.
        assertThat(stored).isEqualByComparingTo(new BigDecimal("0.06"));
    }
}