package com.tartis_recon_ai_parking.infrastructure.tariff.adapter.input.rest.dto.request;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TariffStatusRequestTest {

    @Test
    @DisplayName("Debe crear un TariffStatusRequest usando constructores y getters/setters")
    void shouldCreateAndAccessTariffStatusRequest() {
        // QUE HACE:
        // Instancia el DTO con el constructor vacío y usa setters, además de probar el constructor con argumentos
        TariffStatusRequest requestEmpty = new TariffStatusRequest();
        requestEmpty.setActive(false);

        TariffStatusRequest requestArgs = new TariffStatusRequest(false);

        // QUE DEBERIA HACER:
        // Los getters deben devolver los valores correctos asignados
        assertEquals(false, requestEmpty.getActive());
        assertEquals(false, requestArgs.getActive());
    }
}
