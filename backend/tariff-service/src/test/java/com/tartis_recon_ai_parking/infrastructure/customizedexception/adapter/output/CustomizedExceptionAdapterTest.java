package com.tartis_recon_ai_parking.infrastructure.customizedexception.adapter.output;

import com.tartis_recon_ai_parking.domain.tariff.exception.InvalidTariffException;
import com.tartis_recon_ai_parking.domain.tariff.exception.TariffNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomizedExceptionAdapterTest {

    private final CustomizedExceptionAdapter exceptionAdapter = new CustomizedExceptionAdapter();

    private MockHttpServletRequest requestTo(String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(uri);
        return request;
    }

    @Test
    @DisplayName("Debe manejar TariffNotFoundException retornando 404 Not Found alineado con openapi.yml")
    void shouldHandleTariffNotFoundException() {
        UUID id = UUID.randomUUID();
        TariffNotFoundException exception = new TariffNotFoundException(id);

        ResponseEntity<ErrorResponse> response =
                exceptionAdapter.handleTariffNotFound(exception, requestTo("/v1/tariffs/" + id));

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getBody().getStatus());
        assertEquals("NOT_FOUND", response.getBody().getError());
        assertEquals(exception.getMessage(), response.getBody().getMessage());
        assertEquals("/v1/tariffs/" + id, response.getBody().getPath());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    @DisplayName("Debe manejar InvalidTariffException retornando 400 Bad Request alineado con openapi.yml")
    void shouldHandleInvalidTariffException() {
        InvalidTariffException exception = new InvalidTariffException("Invalid tariff data");

        ResponseEntity<ErrorResponse> response =
                exceptionAdapter.handleInvalidTariff(exception, requestTo("/v1/tariffs"));

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getStatus());
        assertEquals("BAD_REQUEST", response.getBody().getError());
        assertEquals(exception.getMessage(), response.getBody().getMessage());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    @DisplayName("Debe manejar errores de validacion de @Valid devolviendo el detalle de los campos")
    void shouldHandleValidationErrors() throws NoSuchMethodException {
        Object target = new Object();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "tariffCreateRequest");
        bindingResult.addError(new FieldError("tariffCreateRequest", "name", "must not be blank"));

        MethodParameter methodParameter = new MethodParameter(
                CustomizedExceptionAdapterTest.class.getDeclaredMethod("shouldHandleValidationErrors"), -1);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(methodParameter, bindingResult);

        ResponseEntity<ErrorResponse> response =
                exceptionAdapter.handleValidation(exception, requestTo("/v1/tariffs"));

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("name"));
        assertTrue(response.getBody().getMessage().contains("must not be blank"));
    }

    @Test
    @DisplayName("Debe manejar cualquier excepcion no controlada devolviendo 500 sin exponer detalles internos")
    void shouldHandleUnexpectedException() {
        RuntimeException exception = new RuntimeException("connection refused by database driver XYZ");

        ResponseEntity<ErrorResponse> response =
                exceptionAdapter.handleUnexpected(exception, requestTo("/v1/tariffs"));

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INTERNAL_SERVER_ERROR", response.getBody().getError());
        assertEquals("An unexpected error occurred. Please try again later.", response.getBody().getMessage());
        assertTrue(!response.getBody().getMessage().contains("database driver"));
    }
}