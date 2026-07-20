package com.tartis_recon_ai_parking.tariff_service;

import com.tartis_recon_ai_parking.domain.tariff.Tariff;
import com.tartis_recon_ai_parking.domain.tariff.VehicleType;
import com.tartis_recon_ai_parking.domain.tariff.exception.InvalidTariffException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class TariffTest {

    @Test
    @DisplayName("Debe crear una tarifa si los argumentos son validos")
    void shouldCreateTariffWhenValidArguments() {
        // QUE HACE:
        // Prueba la creación de una tarifa nueva utilizando el método 'create'.
        Tariff tariff = Tariff.create("Standard Car", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("1.50"), true);
        
        // QUE DEBERIA HACER:
        // Una instancia válida con un UUID auto-generado y activa por defecto.
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
        // QUE HACE:
        // Prueba la reconstrucción de una tarifa que ya existe (ej. desde base de datos).
        UUID id = UUID.randomUUID();
        Tariff tariff = Tariff.reconstruct(id, "Standard Car", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("1.50"), false);
        
        // QUE DEBERIA HACER:
        // Una instancia con los mismos valores exactos que se le pasan, incluyendo su estado y UUID original.
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
        // QUE HACE:
        // Prueba el método de actualización de datos de la tarifa.
        Tariff tariff = Tariff.create("Standard Car", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("1.50"), true);
        Tariff updated = tariff.update("Updated Car", new BigDecimal("0.08"), new BigDecimal("2.00"));
        
        // QUE DEBERIA HACER:
        // Una nueva instancia con los datos actualizados y el UUID y tipo originales (inmutabilidad).
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
        // QUE HACE:
        // Prueba el cambio de estado de la tarifa (activar y desactivar).
        Tariff tariff = Tariff.create("Standard Car", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("1.50"), true);
        assertTrue(tariff.isActive());
        
        Tariff deactivated = tariff.deactivate();
        Tariff activated = deactivated.activate();
        
        // QUE DEBERIA HACER:
        // Instancias nuevas con la bandera 'active' cambiada y el resto de los datos intactos.
        assertFalse(deactivated.isActive());
        assertEquals(tariff.getUniqueId(), deactivated.getUniqueId());
        assertTrue(activated.isActive());
    }

    @Test
    @DisplayName("Debe lanzar InvalidTariffException si el nombre es nulo")
    void shouldThrowExceptionWhenNameIsNull() {
        // QUE HACE:
        // Prueba la validación de regla de negocio: el nombre de la tarifa no puede ser nulo.
        InvalidTariffException exception = assertThrows(InvalidTariffException.class, () -> {
            Tariff.create(null, VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("1.50"), true);
        });
        
        // QUE DEBERIA HACER:
        // Se lanza la excepción InvalidTariffException indicando que el nombre es nulo.
        assertEquals("Tariff name is null.", exception.getMessage());
    }

    @Test
    @DisplayName("Debe lanzar InvalidTariffException si el nombre esta en blanco")
    void shouldThrowExceptionWhenNameIsBlank() {
        // QUE HACE:
        // Prueba la validación de regla de negocio: el nombre no puede contener únicamente espacios en blanco.
        InvalidTariffException exception = assertThrows(InvalidTariffException.class, () -> {
            Tariff.create("   ", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("1.50"), true);
        });
        
        // QUE DEBERIA HACER:
        // Se lanza la excepción InvalidTariffException indicando que el nombre es inválido.
        assertEquals("Tariff name is null.", exception.getMessage());
    }

    @Test
    @DisplayName("Debe lanzar InvalidTariffException si el tipo de vehiculo es nulo")
    void shouldThrowExceptionWhenTypeIsNull() {
        // QUE HACE:
        // Prueba la validación de regla de negocio: el tipo de vehículo no puede ser nulo.
        InvalidTariffException exception = assertThrows(InvalidTariffException.class, () -> {
            Tariff.create("Standard Car", null, new BigDecimal("0.05"), new BigDecimal("1.50"), true);
        });
        
        // QUE DEBERIA HACER:
        // Se lanza la excepción InvalidTariffException indicando que el tipo de vehículo es nulo.
        assertEquals("Vehicle type is null.", exception.getMessage());
    }

    @Test
    @DisplayName("Debe lanzar InvalidTariffException si el precio por minuto es cero")
    void shouldThrowExceptionWhenPricePerMinuteIsZero() {
        // QUE HACE:
        // Prueba la validación de regla de negocio (IN-09): el precio por minuto no puede ser 0.
        InvalidTariffException exception = assertThrows(InvalidTariffException.class, () -> {
            Tariff.create("Standard Car", VehicleType.CAR, BigDecimal.ZERO, new BigDecimal("1.50"), true);
        });
        
        // QUE DEBERIA HACER:
        // Se lanza la excepción InvalidTariffException indicando el error en el precio.
        assertEquals("PricePerMinute must be greater than 0.", exception.getMessage());
    }

    @Test
    @DisplayName("Debe lanzar InvalidTariffException si el precio por minuto es negativo")
    void shouldThrowExceptionWhenPricePerMinuteIsNegative() {
        // QUE HACE:
        // Prueba la validación de regla de negocio (IN-09): el precio por minuto no puede ser negativo.
        InvalidTariffException exception = assertThrows(InvalidTariffException.class, () -> {
            Tariff.create("Standard Car", VehicleType.CAR, new BigDecimal("-0.05"), new BigDecimal("1.50"), true);
        });
        
        // QUE DEBERIA HACER:
        // Se lanza la excepción InvalidTariffException indicando el error en el precio.
        assertEquals("PricePerMinute must be greater than 0.", exception.getMessage());
    }

    @Test
    @DisplayName("Debe lanzar InvalidTariffException si el precio base es negativo")
    void shouldThrowExceptionWhenBasePriceIsNegative() {
        // QUE HACE:
        // Prueba la validación de regla de negocio: el precio base no puede ser negativo.
        InvalidTariffException exception = assertThrows(InvalidTariffException.class, () -> {
            Tariff.create("Standard Car", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("-1.50"), true);
        });
        
        // QUE DEBERIA HACER:
        // Se lanza la excepción InvalidTariffException indicando que debe ser un valor positivo.
        assertEquals("BasePrice must be a positive number.", exception.getMessage());
    }
    
    @Test
    @DisplayName("Debe permitir un precio base de cero sin lanzar excepcion")
    void shouldAllowBasePriceZero() {
        // QUE HACE:
        // Prueba un límite en la validación: el precio base sí puede ser exactamente 0.
        Tariff tariff = Tariff.create("Standard Car", VehicleType.CAR, new BigDecimal("0.05"), BigDecimal.ZERO, true);
        
        // QUE DEBERIA HACER:
        // La creación es exitosa y el precio base de la tarifa generada es 0.
        assertEquals(BigDecimal.ZERO, tariff.getBasePrice());
    }
}