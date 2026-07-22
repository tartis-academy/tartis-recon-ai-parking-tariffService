package com.tartis_recon_ai_parking.infrastructure.tariff.adapter.output.persistence;


import com.tartis_recon_ai_parking.domain.tariff.Tariff;
import com.tartis_recon_ai_parking.domain.tariff.VehicleType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class TariffPersistenceMapperTest {

    private final TariffPersistenceMapper mapper = Mappers.getMapper(TariffPersistenceMapper.class);

    @Test
    @DisplayName("Debe mapear un objeto de dominio Tariff a una entidad TariffEntity de forma correcta")
    void shouldMapTariffToEntity() {
        UUID id = UUID.randomUUID();
        Tariff tariff = Tariff.reconstruct(id, "Standard", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("2.0"), true);

        TariffEntity entity = mapper.toEntity(tariff);

        assertThat(entity).isNotNull();
        assertThat(entity.getUniqueId()).isEqualTo(id);
        assertThat(entity.getType()).isEqualTo(VehicleType.CAR);
        assertThat(entity.getName()).isEqualTo("Standard");
        assertThat(entity.getPricePerMinute()).isEqualTo(new BigDecimal("0.05"));
        assertThat(entity.getBasePrice()).isEqualTo(new BigDecimal("2.0"));
        assertThat(entity.isActive()).isTrue();
    }

    @Test
    @DisplayName("Debe retornar null al mapear una tarifa de dominio nula a entidad")
    void shouldReturnNullWhenMappingNullTariff() {
        TariffEntity entity = mapper.toEntity(null);
        assertThat(entity).isNull();
    }

    @Test
    @DisplayName("Debe mapear una entidad TariffEntity a un objeto de dominio Tariff de forma correcta")
    void shouldMapEntityToTariff() {
        UUID id = UUID.randomUUID();
        TariffEntity entity = new TariffEntity();
        entity.setUniqueId(id);
        entity.setType(VehicleType.MOTORBIKE);
        entity.setName("Premium");
        entity.setPricePerMinute(new BigDecimal("0.08"));
        entity.setBasePrice(new BigDecimal("3.0"));
        entity.setActive(true);

        Tariff tariff = mapper.toDomain(entity);

        assertThat(tariff).isNotNull();
        assertThat(tariff.getUniqueId()).isEqualTo(id);
        assertThat(tariff.getType()).isEqualTo(VehicleType.MOTORBIKE);
        assertThat(tariff.getName()).isEqualTo("Premium");
        assertThat(tariff.getPricePerMinute()).isEqualTo(new BigDecimal("0.08"));
        assertThat(tariff.getBasePrice()).isEqualTo(new BigDecimal("3.0"));
        assertThat(tariff.isActive()).isTrue();
    }

    @Test
    @DisplayName("Debe retornar null al mapear una entidad nula a dominio")
    void shouldReturnNullWhenMappingNullEntity() {
        Tariff tariff = mapper.toDomain(null);
        assertThat(tariff).isNull();
        assertThat(mapper.create(null)).isNull();
    }
}
