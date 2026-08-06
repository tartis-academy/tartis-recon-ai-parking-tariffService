package com.tartis_recon_ai_parking.infrastructure.tariff.adapter.input.rest.dto.request;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TariffPriceRequestTest {

    @Test
    @DisplayName("El constructor vacio y los setters deben dejar cada getter con el valor fijado")
    void noArgsConstructorAndSettersRoundTrip() {
        TariffPriceRequest request = new TariffPriceRequest();
        UUID id = UUID.randomUUID();

        request.setTariffId(id);
        request.setMinutes(45);

        assertThat(request.getTariffId()).isEqualTo(id);
        assertThat(request.getMinutes()).isEqualTo(45);
    }
}
