package com.tartis_recon_ai_parking.tariff_service;

import com.tartis_recon_ai_parking.domain.tariff.Tariff;
import com.tartis_recon_ai_parking.domain.tariff.VehicleType;
import com.tartis_recon_ai_parking.domain.tariff.exception.InvalidTariffException;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class TariffTest {


    // Prueba la creación de una tarifa nueva utilizando el método 'create'.
    // Resultado esperado: Una instancia válida con un UUID auto-generado y activa por defecto.
    @Test
    void shouldCreateTariffWhenValidArguments() {
        Tariff tariff = Tariff.create("Standard Car", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("1.50"), true);
        assertNotNull(tariff.getUniqueId());
        assertEquals("Standard Car", tariff.getName());
        assertEquals(VehicleType.CAR, tariff.getType());
        assertEquals(new BigDecimal("0.05"), tariff.getPricePerMinute());
        assertEquals(new BigDecimal("1.50"), tariff.getBasePrice());
        assertTrue(tariff.isActive());
    }


    // Prueba la reconstrucción de una tarifa que ya existe (ej. desde base de datos).
    // Resultado esperado: Una instancia con los mismos valores exactos que se le pasan, incluyendo su estado y UUID original.
    @Test
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


    // Prueba el método de actualización de datos de la tarifa.
    // Resultado esperado: Una nueva instancia con los datos actualizados y el UUID y tipo originales (inmutabilidad).
    @Test
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


    // Prueba el cambio de estado de la tarifa (activar y desactivar).
    // Resultado esperado: Instancias nuevas con la bandera 'active' cambiada y el resto de los datos intactos.
    @Test
    void shouldActivateAndDeactivateTariff() {
        Tariff tariff = Tariff.create("Standard Car", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("1.50"), true);
        assertTrue(tariff.isActive());
        Tariff deactivated = tariff.deactivate();
        assertFalse(deactivated.isActive());
        assertEquals(tariff.getUniqueId(), deactivated.getUniqueId());
        Tariff activated = deactivated.activate();
        assertTrue(activated.isActive());
    }


    // Prueba la validación de regla de negocio: el nombre de la tarifa no puede ser nulo.
    // Resultado esperado: Se lanza la excepción InvalidTariffException indicando que el nombre es nulo.
    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        InvalidTariffException exception = assertThrows(InvalidTariffException.class, () -> {
            Tariff.create(null, VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("1.50"), true);
        });
        assertEquals("Tariff name is null.", exception.getMessage());
    }


    // Prueba la validación de regla de negocio: el nombre no puede contener únicamente espacios en blanco.
    // Resultado esperado: Se lanza la excepción InvalidTariffException indicando que el nombre es inválido.
    @Test
    void shouldThrowExceptionWhenNameIsBlank() {
        InvalidTariffException exception = assertThrows(InvalidTariffException.class, () -> {
            Tariff.create("   ", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("1.50"), true);
        });
        assertEquals("Tariff name is null.", exception.getMessage());
    }


    // Prueba la validación de regla de negocio: el tipo de vehículo no puede ser nulo.
    // Resultado esperado: Se lanza la excepción InvalidTariffException indicando que el tipo de vehículo es nulo.
    @Test
    void shouldThrowExceptionWhenTypeIsNull() {
        InvalidTariffException exception = assertThrows(InvalidTariffException.class, () -> {
            Tariff.create("Standard Car", null, new BigDecimal("0.05"), new BigDecimal("1.50"), true);
        });
        assertEquals("Vehicle type is null.", exception.getMessage());
    }


    // Prueba la validación de regla de negocio (IN-09): el precio por minuto no puede ser 0.
    // Resultado esperado: Se lanza la excepción InvalidTariffException indicando el error en el precio.
    @Test
    void shouldThrowExceptionWhenPricePerMinuteIsZero() {
        InvalidTariffException exception = assertThrows(InvalidTariffException.class, () -> {
            Tariff.create("Standard Car", VehicleType.CAR, BigDecimal.ZERO, new BigDecimal("1.50"), true);
        });
        assertEquals("PricePerMinute must be greater than 0.", exception.getMessage());
    }


    // Prueba la validación de regla de negocio (IN-09): el precio por minuto no puede ser negativo.
    // Resultado esperado: Se lanza la excepción InvalidTariffException indicando el error en el precio.
    @Test
    void shouldThrowExceptionWhenPricePerMinuteIsNegative() {
        InvalidTariffException exception = assertThrows(InvalidTariffException.class, () -> {
            Tariff.create("Standard Car", VehicleType.CAR, new BigDecimal("-0.05"), new BigDecimal("1.50"), true);
        });
        assertEquals("PricePerMinute must be greater than 0.", exception.getMessage());
    }


    // Prueba la validación de regla de negocio: el precio base no puede ser negativo.
    // Resultado esperado: Se lanza la excepción InvalidTariffException indicando que debe ser un valor positivo.
    @Test
    void shouldThrowExceptionWhenBasePriceIsNegative() {
        InvalidTariffException exception = assertThrows(InvalidTariffException.class, () -> {
            Tariff.create("Standard Car", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("-1.50"), true);
        });
        assertEquals("BasePrice must be a positive number.", exception.getMessage());
    }

    
    // Prueba un límite en la validación: el precio base sí puede ser exactamente 0.
    // Resultado esperado: La creación es exitosa y el precio base de la tarifa generada es 0.
    @Test
    void shouldAllowBasePriceZero() {
        Tariff tariff = Tariff.create("Standard Car", VehicleType.CAR, new BigDecimal("0.05"), BigDecimal.ZERO, true);
        assertEquals(BigDecimal.ZERO, tariff.getBasePrice());
    }
}