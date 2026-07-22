package com.tartis_recon_ai_parking.infrastructure.tariff.adapter.input.rest.dto.request;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TariffUpdateRequestTest {

    @Test
    @DisplayName("Debe crear un TariffUpdateRequest usando constructores y getters/setters")
    void shouldCreateAndAccessTariffUpdateRequest() {
        // QUE HACE:
        // Instancia el DTO con el constructor vacío y usa setters, además de probar el constructor con argumentos
        TariffUpdateRequest requestEmpty = new TariffUpdateRequest();
        requestEmpty.setName("UpdatedName");
        requestEmpty.setPricePerMinute(new BigDecimal("0.6"));
        requestEmpty.setBasePrice(new BigDecimal("2.0"));

        TariffUpdateRequest requestArgs = new TariffUpdateRequest("UpdatedName", new BigDecimal("0.6"), new BigDecimal("2.0"));

        // QUE DEBERIA HACER:
        // Los getters deben devolver los valores correctos asignados
        assertEquals("UpdatedName", requestEmpty.getName());
        assertEquals(new BigDecimal("0.6"), requestEmpty.getPricePerMinute());
        assertEquals(new BigDecimal("2.0"), requestEmpty.getBasePrice());

        assertEquals("UpdatedName", requestArgs.getName());
        assertEquals(new BigDecimal("0.6"), requestArgs.getPricePerMinute());
        assertEquals(new BigDecimal("2.0"), requestArgs.getBasePrice());
    }
}
