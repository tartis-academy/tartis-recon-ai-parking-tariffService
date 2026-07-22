package com.tartis_recon_ai_parking.infrastructure.tariff.adapter.output.persistence;

import com.tartis_recon_ai_parking.domain.tariff.VehicleType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

class TariffEntityTest {

    @Test
    @DisplayName("Debe crear TariffEntity usando constructor vacio y setters")
    void shouldCreateEntityWithEmptyConstructorAndSetters() {
        // QUE HACE:
        // Instancia un TariffEntity usando el constructor por defecto y establece los campos mediante setters
        TariffEntity entity = new TariffEntity();
        
        UUID id = UUID.randomUUID();
        entity.setUniqueId(id);
        entity.setName("TestName");
        entity.setType(VehicleType.CAR);
        entity.setPricePerMinute(new BigDecimal("0.1"));
        entity.setBasePrice(new BigDecimal("1.0"));
        entity.setActive(true);

        // QUE DEBERIA HACER:
        // Los getters deben devolver los valores previamente asignados
        assertEquals(id, entity.getUniqueId());
        assertEquals("TestName", entity.getName());
        assertEquals(VehicleType.CAR, entity.getType());
        assertEquals(new BigDecimal("0.1"), entity.getPricePerMinute());
        assertEquals(new BigDecimal("1.0"), entity.getBasePrice());
        assertTrue(entity.isActive());
    }

    @Test
    @DisplayName("Debe crear TariffEntity usando constructor con argumentos")
    void shouldCreateEntityWithAllArgsConstructor() {
        // QUE HACE:
        // Instancia un TariffEntity usando el constructor con todos los argumentos
        UUID id = UUID.randomUUID();
        TariffEntity entity = new TariffEntity(id, "AllArgs", VehicleType.MOTORBIKE, new BigDecimal("0.2"), new BigDecimal("2.0"), false);

        // QUE DEBERIA HACER:
        // La entidad debe contener los valores inicializados en el constructor
        assertEquals(id, entity.getUniqueId());
        assertEquals("AllArgs", entity.getName());
        assertEquals(VehicleType.MOTORBIKE, entity.getType());
        assertEquals(new BigDecimal("0.2"), entity.getPricePerMinute());
        assertEquals(new BigDecimal("2.0"), entity.getBasePrice());
        assertFalse(entity.isActive());
    }
}
