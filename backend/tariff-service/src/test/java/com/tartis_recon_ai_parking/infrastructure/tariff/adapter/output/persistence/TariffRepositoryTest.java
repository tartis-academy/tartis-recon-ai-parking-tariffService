package com.tartis_recon_ai_parking.infrastructure.tariff.adapter.output.persistence;


import com.tartis_recon_ai_parking.domain.tariff.VehicleType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.Optional;
import java.util.UUID;
import java.util.List;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TariffRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TariffRepository tariffRepository;

    @Test
    @DisplayName("Debe retornar True si existe una tarifa con el nombre consultado")
    void shouldReturnTrueWhenNameExists() {
        UUID id = UUID.randomUUID();
        TariffEntity entity = new TariffEntity();
        entity.setUniqueId(id);
        entity.setName("Standard");
        entity.setType(VehicleType.CAR);
        entity.setPricePerMinute(new BigDecimal("0.05"));
        entity.setBasePrice(new BigDecimal("2.0"));
        entity.setActive(true);

        entityManager.persistAndFlush(entity);

        boolean exists = tariffRepository.existsByName("Standard");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Debe retornar False si no existe una tarifa con el nombre consultado")
    void shouldReturnFalseWhenNameDoesNotExist() {
        boolean exists = tariffRepository.existsByName("NonExistent");

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Debe encontrar y retornar una tarifa por su nombre si existe")
    void shouldFindTariffByNameSuccessfully() {
        UUID id = UUID.randomUUID();
        TariffEntity entity = new TariffEntity();
        entity.setUniqueId(id);
        entity.setName("Premium");
        entity.setType(VehicleType.MOTORBIKE);
        entity.setPricePerMinute(new BigDecimal("0.08"));
        entity.setBasePrice(new BigDecimal("3.0"));
        entity.setActive(true);

        entityManager.persistAndFlush(entity);

        Optional<TariffEntity> result = tariffRepository.findByName("Premium");

        assertThat(result).isPresent();
        assertThat(result.get().getUniqueId()).isEqualTo(id);
        assertThat(result.get().getName()).isEqualTo("Premium");
        assertThat(result.get().getType()).isEqualTo(VehicleType.MOTORBIKE);
    }

    @Test
    @DisplayName("Debe retornar un Optional vacio al buscar un nombre que no existe")
    void shouldReturnEmptyOptionalWhenNameNotFound() {
        // Busca una tarifa por el nombre "NonExistent" en una BD vacia.
        Optional<TariffEntity> result = tariffRepository.findByName("NonExistent");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Debe encontrar tarifas por su estado activo")
    void shouldFindTariffsByActive() {
        UUID id = UUID.randomUUID();
        TariffEntity entity = new TariffEntity();
        entity.setUniqueId(id);
        entity.setName("ActiveTariff");
        entity.setType(VehicleType.CAR);
        entity.setPricePerMinute(new BigDecimal("0.05"));
        entity.setBasePrice(new BigDecimal("2.0"));
        entity.setActive(true);

        entityManager.persistAndFlush(entity);

        List<TariffEntity> activeTariffs = tariffRepository.findByActive(true);

        assertThat(activeTariffs).isNotEmpty();
        assertThat(activeTariffs.get(0).getName()).isEqualTo("ActiveTariff");
        assertThat(activeTariffs.get(0).isActive()).isTrue();
    }
    @Test
    @DisplayName("Debe encontrar tarifas activas por tipo de vehiculo")
    void shouldFindActiveTariffsByType() {
        UUID id1 = UUID.randomUUID();
        TariffEntity entity1 = new TariffEntity();
        entity1.setUniqueId(id1);
        entity1.setName("ActiveCarTariff");
        entity1.setType(VehicleType.CAR);
        entity1.setPricePerMinute(new BigDecimal("0.05"));
        entity1.setBasePrice(new BigDecimal("2.0"));
        entity1.setActive(true);

        UUID id2 = UUID.randomUUID();
        TariffEntity entity2 = new TariffEntity();
        entity2.setUniqueId(id2);
        entity2.setName("InactiveCarTariff");
        entity2.setType(VehicleType.CAR);
        entity2.setPricePerMinute(new BigDecimal("0.05"));
        entity2.setBasePrice(new BigDecimal("2.0"));
        entity2.setActive(false);

        UUID id3 = UUID.randomUUID();
        TariffEntity entity3 = new TariffEntity();
        entity3.setUniqueId(id3);
        entity3.setName("ActiveMotoTariff");
        entity3.setType(VehicleType.MOTORBIKE);
        entity3.setPricePerMinute(new BigDecimal("0.08"));
        entity3.setBasePrice(new BigDecimal("3.0"));
        entity3.setActive(true);

        entityManager.persist(entity1);
        entityManager.persist(entity2);
        entityManager.persist(entity3);
        entityManager.flush();

        List<TariffEntity> carTariffs = tariffRepository.findByActiveTrueAndType(VehicleType.CAR);

        assertThat(carTariffs).isNotEmpty().hasSize(1);
        assertThat(carTariffs.get(0).getName()).isEqualTo("ActiveCarTariff");
        assertThat(carTariffs.get(0).isActive()).isTrue();
        assertThat(carTariffs.get(0).getType()).isEqualTo(VehicleType.CAR);
    }
}
