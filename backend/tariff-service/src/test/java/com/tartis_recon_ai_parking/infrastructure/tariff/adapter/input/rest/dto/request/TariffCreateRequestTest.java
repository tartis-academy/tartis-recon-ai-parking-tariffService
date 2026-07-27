package com.tartis_recon_ai_parking.infrastructure.tariff.adapter.input.rest.dto.request;

import com.tartis_recon_ai_parking.domain.tariff.VehicleType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class TariffCreateRequestTest {

    @Test
    @DisplayName("El constructor vacio y los setters deben dejar cada getter con el valor fijado")
    void noArgsConstructorAndSettersRoundTrip() {
        TariffCreateRequest request = new TariffCreateRequest();

        request.setName("Standard");
        request.setType(VehicleType.CAR);
        request.setPricePerMinute(new BigDecimal("0.05"));
        request.setBasePrice(new BigDecimal("2.0"));
        request.setActive(true);

        assertThat(request.getName()).isEqualTo("Standard");
        assertThat(request.getType()).isEqualTo(VehicleType.CAR);
        assertThat(request.getPricePerMinute()).isEqualTo(new BigDecimal("0.05"));
        assertThat(request.getBasePrice()).isEqualTo(new BigDecimal("2.0"));
        assertThat(request.getActive()).isTrue();
    }
}
