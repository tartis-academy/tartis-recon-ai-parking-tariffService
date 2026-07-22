package com.tartis_recon_ai_parking.infrastructure.customizedexception.adapter.output;

import com.tartis_recon_ai_parking.domain.tariff.exception.InvalidTariffException;
import com.tartis_recon_ai_parking.domain.tariff.exception.TariffNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CustomizedExceptionAdapterTest {

    private final CustomizedExceptionAdapter exceptionAdapter = new CustomizedExceptionAdapter();

    @Test
    @DisplayName("Debe manejar TariffNotFoundException retornando 404 Not Found")
    void shouldHandleTariffNotFoundException() {
        UUID id = UUID.randomUUID();
        TariffNotFoundException exception = new TariffNotFoundException(id);

        ResponseEntity<ErrorResponse> response = exceptionAdapter.handleTariffNotFound(exception);


        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getBody().getStatus());
        assertEquals(exception.getMessage(), response.getBody().getMessage());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    @DisplayName("Debe manejar InvalidTariffException retornando 400 Bad Request")
    void shouldHandleInvalidTariffException() {
        // Genera una InvalidTariffException con un mensaje de error
        InvalidTariffException exception = new InvalidTariffException("Invalid tariff data");

        ResponseEntity<ErrorResponse> response = exceptionAdapter.handleInvalidTariff(exception);


        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getStatus());
        assertEquals(exception.getMessage(), response.getBody().getMessage());
        assertNotNull(response.getBody().getTimestamp());
    }
}
