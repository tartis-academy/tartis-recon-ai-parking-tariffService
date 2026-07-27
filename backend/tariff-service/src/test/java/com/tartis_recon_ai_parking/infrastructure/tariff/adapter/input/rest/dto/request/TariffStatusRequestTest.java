package com.tartis_recon_ai_parking.infrastructure.tariff.adapter.input.rest.dto.request;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TariffStatusRequestTest {

    @Test
    @DisplayName("El constructor vacio y el setter deben dejar el getter con el valor fijado")
    void noArgsConstructorAndSetterRoundTrip() {
        TariffStatusRequest request = new TariffStatusRequest();

        request.setActive(true);

        assertThat(request.getActive()).isTrue();
    }
}
