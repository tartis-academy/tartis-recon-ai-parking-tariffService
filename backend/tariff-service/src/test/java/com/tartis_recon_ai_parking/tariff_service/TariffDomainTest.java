package com.tartis_recon_ai_parking.tariff_service;

import com.tartis_recon_ai_parking.domain.tariff.Tariff;
import com.tartis_recon_ai_parking.domain.tariff.VehicleType;
import com.tartis_recon_ai_parking.domain.tariff.exception.InvalidTariffException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TariffDomainTest {

    @Test
    void shouldCreateTariffWhenValidArguments() {
        Tariff tariff = new Tariff("Standard Car", VehicleType.CAR, 0.05f, 1.50f, true);

        assertNotNull(tariff.getUniqueId());
        assertEquals("Standard Car", tariff.getName());
        assertEquals(VehicleType.CAR, tariff.getType());
        assertEquals(0.05f, tariff.getPricePerMinute());
        assertEquals(1.50f, tariff.getBasePrice());
        assertTrue(tariff.isActive());
    }

    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        InvalidTariffException exception = assertThrows(InvalidTariffException.class, () -> {
            new Tariff(null, VehicleType.CAR, 0.05f, 1.50f, true);
        });
        assertEquals("Tariff name is null.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenNameIsBlank() {
        InvalidTariffException exception = assertThrows(InvalidTariffException.class, () -> {
            new Tariff("   ", VehicleType.CAR, 0.05f, 1.50f, true);
        });
        assertEquals("Tariff name is null.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenTypeIsNull() {
        InvalidTariffException exception = assertThrows(InvalidTariffException.class, () -> {
            new Tariff("Standard Car", null, 0.05f, 1.50f, true);
        });
        assertEquals("Vehicle type is null.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenPricePerMinuteIsZero() {
        InvalidTariffException exception = assertThrows(InvalidTariffException.class, () -> {
            new Tariff("Standard Car", VehicleType.CAR, 0.0f, 1.50f, true);
        });
        assertEquals("PricePerMinute must be greater than 0.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenPricePerMinuteIsNegative() {
        InvalidTariffException exception = assertThrows(InvalidTariffException.class, () -> {
            new Tariff("Standard Car", VehicleType.CAR, -0.05f, 1.50f, true);
        });
        assertEquals("PricePerMinute must be greater than 0.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenBasePriceIsNegative() {
        InvalidTariffException exception = assertThrows(InvalidTariffException.class, () -> {
            new Tariff("Standard Car", VehicleType.CAR, 0.05f, -1.50f, true);
        });
        assertEquals("BasePrice must be a positive number.", exception.getMessage());
    }

    @Test
    void shouldAllowBasePriceZero() {
        Tariff tariff = new Tariff("Standard Car", VehicleType.CAR, 0.05f, 0.0f, true);
        assertEquals(0.0f, tariff.getBasePrice());
    }
}
