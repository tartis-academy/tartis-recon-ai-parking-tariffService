package com.tartis_recon_ai_parking.domain.tariff;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


import com.tartis_recon_ai_parking.domain.tariff.exception.InvalidTariffException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.UUID;

class TariffTest {

    @Test
    @DisplayName("Debe crear una tarifa si los argumentos son validos")
    void shouldCreateTariffWhenValidArguments() {
        Tariff tariff = Tariff.create("Standard Car", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("1.50"), true);
        
        assertNotNull(tariff.getUniqueId());
        assertEquals("Standard Car", tariff.getName());
        assertEquals(VehicleType.CAR, tariff.getType());
        assertEquals(new BigDecimal("0.05"), tariff.getPricePerMinute());
        assertEquals(new BigDecimal("1.50"), tariff.getBasePrice());
        assertTrue(tariff.isActive());
    }

    @Test
    @DisplayName("Debe reconstruir una tarifa existente usando todos sus argumentos")
    void shouldReconstructTariffWhenValidArguments() {
        UUID id = UUID.randomUUID();
        Tariff tariff = Tariff.reconstruct(id, "Standard Car", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("1.50"), false);
        
        assertEquals(id, tariff.getUniqueId());
        assertEquals("Standard Car", tariff.getName());
        assertEquals(VehicleType.CAR, tariff.getType());
        assertEquals(new BigDecimal("0.05"), tariff.getPricePerMinute());
        assertEquals(new BigDecimal("1.50"), tariff.getBasePrice());
        assertFalse(tariff.isActive());
    }

    @Test
    @DisplayName("Debe actualizar una tarifa devolviendo una nueva instancia con los datos modificados")
    void shouldUpdateTariff() {
        Tariff tariff = Tariff.create("Standard Car", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("1.50"), true);
        Tariff updated = tariff.update("Updated Car", new BigDecimal("0.08"), new BigDecimal("2.00"));
        
        assertEquals(tariff.getUniqueId(), updated.getUniqueId());
        assertEquals("Updated Car", updated.getName());
        assertEquals(VehicleType.CAR, updated.getType());
        assertEquals(new BigDecimal("0.08"), updated.getPricePerMinute());
        assertEquals(new BigDecimal("2.00"), updated.getBasePrice());
        assertTrue(updated.isActive());
    }

    @Test
    @DisplayName("Debe activar y desactivar una tarifa devolviendo una nueva instancia con el estado cambiado")
    void shouldActivateAndDeactivateTariff() {
        Tariff tariff = Tariff.create("Standard Car", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("1.50"), true);
        assertTrue(tariff.isActive());
        
        Tariff deactivated = tariff.deactivate();
        Tariff activated = deactivated.activate();
        
        assertFalse(deactivated.isActive());
        assertEquals(tariff.getUniqueId(), deactivated.getUniqueId());
        assertTrue(activated.isActive());
    }

    @Test
    @DisplayName("Debe lanzar InvalidTariffException si el nombre es nulo")
    void shouldThrowExceptionWhenNameIsNull() {
        InvalidTariffException exception = assertThrows(InvalidTariffException.class, () -> {
            Tariff.create(null, VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("1.50"), true);
        });
        
        assertEquals("Tariff name is null.", exception.getMessage());
    }

    @Test
    @DisplayName("Debe lanzar InvalidTariffException si el nombre esta en blanco")
    void shouldThrowExceptionWhenNameIsBlank() {
        InvalidTariffException exception = assertThrows(InvalidTariffException.class, () -> {
            Tariff.create("   ", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("1.50"), true);
        });
        
        assertEquals("Tariff name is null.", exception.getMessage());
    }

    @Test
    @DisplayName("Debe lanzar InvalidTariffException si el tipo de vehiculo es nulo")
    void shouldThrowExceptionWhenTypeIsNull() {
        InvalidTariffException exception = assertThrows(InvalidTariffException.class, () -> {
            Tariff.create("Standard Car", null, new BigDecimal("0.05"), new BigDecimal("1.50"), true);
        });
        
        assertEquals("Vehicle type is null.", exception.getMessage());
    }

    @Test
    @DisplayName("Debe lanzar InvalidTariffException si el precio por minuto es cero")
    void shouldThrowExceptionWhenPricePerMinuteIsZero() {
        InvalidTariffException exception = assertThrows(InvalidTariffException.class, () -> {
            Tariff.create("Standard Car", VehicleType.CAR, BigDecimal.ZERO, new BigDecimal("1.50"), true);
        });
        
        assertEquals("PricePerMinute must be greater than 0.", exception.getMessage());
    }

    @Test
    @DisplayName("Debe lanzar InvalidTariffException si el precio por minuto es negativo")
    void shouldThrowExceptionWhenPricePerMinuteIsNegative() {
        InvalidTariffException exception = assertThrows(InvalidTariffException.class, () -> {
            Tariff.create("Standard Car", VehicleType.CAR, new BigDecimal("-0.05"), new BigDecimal("1.50"), true);
        });
        
        assertEquals("PricePerMinute must be greater than 0.", exception.getMessage());
    }

    @Test
    @DisplayName("Debe lanzar InvalidTariffException si el precio base es negativo")
    void shouldThrowExceptionWhenBasePriceIsNegative() {
        InvalidTariffException exception = assertThrows(InvalidTariffException.class, () -> {
            Tariff.create("Standard Car", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("-1.50"), true);
        });
        
        assertEquals("BasePrice must be a positive number.", exception.getMessage());
    }
    
    @Test
    @DisplayName("Debe permitir un precio base de cero sin lanzar excepcion")
    void shouldAllowBasePriceZero() {
        Tariff tariff = Tariff.create("Standard Car", VehicleType.CAR, new BigDecimal("0.05"), BigDecimal.ZERO, true);
        
        assertEquals(BigDecimal.ZERO, tariff.getBasePrice());
    }
}