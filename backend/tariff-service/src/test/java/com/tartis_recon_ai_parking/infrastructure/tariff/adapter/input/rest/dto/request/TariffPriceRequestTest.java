package com.tartis_recon_ai_parking.infrastructure.tariff.adapter.input.rest.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TariffPriceRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("El constructor vacio y los setters deben dejar cada getter con el valor fijado")
    void noArgsConstructorAndSettersRoundTrip() {
        TariffPriceRequest request = new TariffPriceRequest();
        UUID id = UUID.randomUUID();

        request.setTariffId(id);
        request.setType(com.tartis_recon_ai_parking.domain.tariff.VehicleType.CAR);
        request.setMinutes(45);

        assertThat(request.getTariffId()).isEqualTo(id);
        assertThat(request.getType()).isEqualTo(com.tartis_recon_ai_parking.domain.tariff.VehicleType.CAR);
        assertThat(request.getMinutes()).isEqualTo(45);
    }

    @Test
    void testValidRequest() {
        TariffPriceRequest request = new TariffPriceRequest(UUID.randomUUID(), com.tartis_recon_ai_parking.domain.tariff.VehicleType.CAR, 60);

        Set<ConstraintViolation<TariffPriceRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "No debería haber violaciones para una petición válida.");
    }

    @Test
    void testNullTariffId() {
        TariffPriceRequest request = new TariffPriceRequest(null, com.tartis_recon_ai_parking.domain.tariff.VehicleType.CAR, 60);

        Set<ConstraintViolation<TariffPriceRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Debería haber una violación cuando el tariffId es null.");
    }

    @Test
    void testNullVehicleType() {
        TariffPriceRequest request = new TariffPriceRequest(UUID.randomUUID(), null, 60);

        Set<ConstraintViolation<TariffPriceRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Debería haber una violación cuando el type es null.");
    }

    @Test
    void testNullMinutes() {
        TariffPriceRequest request = new TariffPriceRequest(UUID.randomUUID(), com.tartis_recon_ai_parking.domain.tariff.VehicleType.CAR, null);

        Set<ConstraintViolation<TariffPriceRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Debería haber una violación cuando los minutos son null.");
    }

    @Test
    void testNegativeMinutes() {
        TariffPriceRequest request = new TariffPriceRequest(UUID.randomUUID(), com.tartis_recon_ai_parking.domain.tariff.VehicleType.CAR, -5);

        Set<ConstraintViolation<TariffPriceRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Debería haber una violación cuando los minutos son negativos.");
    }
}
