package com.tartis_recon_ai_parking.infrastructure.tariff.adapter.input.rest.dto.request;

import com.tartis_recon_ai_parking.domain.tariff.VehicleType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TariffPriceRequestTest {

    @Test
    @DisplayName("El constructor vacio y los setters deben dejar cada getter con el valor fijado")
    void noArgsConstructorAndSettersRoundTrip() {
        TariffPriceRequest request = new TariffPriceRequest();

        request.setVehicleType(VehicleType.MOTORBIKE);
        request.setMinutes(45);

        assertThat(request.getVehicleType()).isEqualTo(VehicleType.MOTORBIKE);
        assertThat(request.getMinutes()).isEqualTo(45);
    }
}
