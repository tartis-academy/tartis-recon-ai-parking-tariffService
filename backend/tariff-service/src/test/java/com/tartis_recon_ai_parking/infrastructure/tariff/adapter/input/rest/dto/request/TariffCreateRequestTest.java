package com.tartis_recon_ai_parking.infrastructure.tariff.adapter.input.rest.dto.request;

import com.tartis_recon_ai_parking.domain.tariff.VehicleType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TariffCreateRequestTest {

    @Test
    @DisplayName("Debe crear un TariffCreateRequest usando constructores y getters/setters")
    void shouldCreateAndAccessTariffCreateRequest() {
        // QUE HACE:
        // Instancia el DTO con el constructor vacío y usa setters, además de probar el constructor con argumentos
        TariffCreateRequest requestEmpty = new TariffCreateRequest();
        requestEmpty.setName("TestName");
        requestEmpty.setType(VehicleType.CAR);
        requestEmpty.setPricePerMinute(new BigDecimal("0.5"));
        requestEmpty.setBasePrice(new BigDecimal("1.5"));
        requestEmpty.setActive(true);

        TariffCreateRequest requestArgs = new TariffCreateRequest("TestName", VehicleType.CAR, new BigDecimal("0.5"), new BigDecimal("1.5"), true);

        // QUE DEBERIA HACER:
        // Los getters deben devolver los valores correctos asignados
        assertEquals("TestName", requestEmpty.getName());
        assertEquals(VehicleType.CAR, requestEmpty.getType());
        assertEquals(new BigDecimal("0.5"), requestEmpty.getPricePerMinute());
        assertEquals(new BigDecimal("1.5"), requestEmpty.getBasePrice());
        assertEquals(true, requestEmpty.getActive());

        assertEquals("TestName", requestArgs.getName());
        assertEquals(VehicleType.CAR, requestArgs.getType());
        assertEquals(new BigDecimal("0.5"), requestArgs.getPricePerMinute());
        assertEquals(new BigDecimal("1.5"), requestArgs.getBasePrice());
        assertEquals(true, requestArgs.getActive());
    }
}
