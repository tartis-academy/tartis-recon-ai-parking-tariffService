package com.tartis_recon_ai_parking.domain.tariff.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TariffNotFoundExceptionTest {

    @Test
    @DisplayName("Debe crear la excepcion TariffNotFoundException con el mensaje correcto")
    void shouldCreateExceptionWithMessage() {
        // QUE HACE:
        // Instancia la excepcion usando el constructor que recibe un UUID
        UUID id = UUID.randomUUID();
        TariffNotFoundException exception = new TariffNotFoundException(id);

        // QUE DEBERIA HACER:
        // La excepcion no debe ser nula y el mensaje debe contener el UUID proporcionado
        assertNotNull(exception);
        assertEquals("Tariff not found with id: " + id, exception.getMessage());
    }

    @Test
    @DisplayName("Debe crear la excepcion TariffNotFoundException con un mensaje de texto plano")
    void shouldCreateExceptionWithStringMessage() {
        // QUE HACE:
        // Instancia la excepcion usando el constructor que recibe un String
        TariffNotFoundException exception = new TariffNotFoundException("Custom error");

        // QUE DEBERIA HACER:
        // La excepcion no debe ser nula y el mensaje debe contener el texto proporcionado
        assertNotNull(exception);
        assertEquals("Custom error", exception.getMessage());
    }
}
