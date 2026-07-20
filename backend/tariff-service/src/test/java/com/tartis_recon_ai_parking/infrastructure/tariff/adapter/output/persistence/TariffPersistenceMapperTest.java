package com.tartis_recon_ai_parking.infrastructure.tariff.adapter.output.persistence;

import com.tartis_recon_ai_parking.domain.tariff.Tariff;
import com.tartis_recon_ai_parking.domain.tariff.VehicleType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class TariffPersistenceMapperTest {

    // Utiliza el cargador de MapStruct Mappers en lugar de instanciar directamente para evitar problemas de sincronizacion del compilador de la IDE.
    private final TariffPersistenceMapper mapper = Mappers.getMapper(TariffPersistenceMapper.class);

    @Test
    @DisplayName("Debe mapear un objeto de dominio Tariff a una entidad TariffEntity de forma correcta")
    void shouldMapTariffToEntity() {
        // QUE HACE:
        // Instancia un objeto Tariff de dominio completo con datos especificos, llama al metodo toEntity del mapeador.
        UUID id = UUID.randomUUID();
        Tariff tariff = new Tariff("Standard", VehicleType.CAR, 0.05f, 2.0f, true);
        tariff.setUniqueId(id);

        TariffEntity entity = mapper.toEntity(tariff);

        // QUE DEBERIA HACER:
        // Debe retornar un objeto TariffEntity no nulo y comprobar mediante aserciones 
        // de AssertJ que cada campo de persistencia coincida exactamente con el de origen.
        assertThat(entity).isNotNull();
        assertThat(entity.getUniqueId()).isEqualTo(id);
        assertThat(entity.getType()).isEqualTo(VehicleType.CAR);
        assertThat(entity.getName()).isEqualTo("Standard");
        assertThat(entity.getPricePerMinute()).isEqualTo(0.05f);
        assertThat(entity.getBasePrice()).isEqualTo(2.0f);
        assertThat(entity.isActive()).isTrue();
    }

    @Test
    @DisplayName("Debe retornar null al mapear una tarifa de dominio nula a entidad")
    void shouldReturnNullWhenMappingNullTariff() {
        // QUE HACE:
        // Llama al mapper pasando un parametro nulo.
        TariffEntity entity = mapper.toEntity(null);

        // QUE DEBERIA HACER:
        // El mapper debe retornar null de forma segura y sin lanzar excepciones.
        assertThat(entity).isNull();
    }

    @Test
    @DisplayName("Debe mapear una entidad TariffEntity a un objeto de dominio Tariff de forma correcta")
    void shouldMapEntityToTariff() {
        // QUE HACE:
        // - Instancia y rellena un TariffEntity con datos de prueba especificos, llama al metodo toDomain del mapeador.
        UUID id = UUID.randomUUID();
        TariffEntity entity = new TariffEntity();
        entity.setUniqueId(id);
        entity.setType(VehicleType.MOTORBIKE);
        entity.setName("Premium");
        entity.setPricePerMinute(0.08f);
        entity.setBasePrice(3.0f);
        entity.setActive(true);

        Tariff tariff = mapper.toDomain(entity);

        // QUE DEBERIA HACER:
        // Debe retornar un objeto de dominio Tariff no nulo y comprobar mediante aserciones
        // que todos los campos del dominio se correspondan de forma exacta con los de la entidad.
        assertThat(tariff).isNotNull();
        assertThat(tariff.getUniqueId()).isEqualTo(id);
        assertThat(tariff.getType()).isEqualTo(VehicleType.MOTORBIKE);
        assertThat(tariff.getName()).isEqualTo("Premium");
        assertThat(tariff.getPricePerMinute()).isEqualTo(0.08f);
        assertThat(tariff.getBasePrice()).isEqualTo(3.0f);
        assertThat(tariff.isActive()).isTrue();
    }

    @Test
    @DisplayName("Debe retornar null al mapear una entidad nula a dominio")
    void shouldReturnNullWhenMappingNullEntity() {
        // QUE HACE:
        // Llama al mapper pasando una entidad nula.
        Tariff tariff = mapper.toDomain(null);

        // QUE DEBERIA HACER:
        // El mapper debe retornar null de forma segura y sin lanzar excepciones.
        assertThat(tariff).isNull();
    }
}
