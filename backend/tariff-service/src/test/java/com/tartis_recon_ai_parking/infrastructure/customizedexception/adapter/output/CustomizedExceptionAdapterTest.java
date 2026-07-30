package com.tartis_recon_ai_parking.infrastructure.customizedexception.adapter.output;

import com.tartis_recon_ai_parking.domain.tariff.exception.InvalidTariffException;
import com.tartis_recon_ai_parking.domain.tariff.exception.TariffNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

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
    @DisplayName("Debe manejar errores de validacion a nivel de clase/objeto (ObjectError sin campo asociado), no solo FieldErrors")
    void shouldHandleClassLevelValidationErrors() throws NoSuchMethodException {
        Object target = new Object();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "tariffCreateRequest");
        // ObjectError "puro": sin campo asociado, como el que produce una
        // validacion a nivel de clase completa (ej. @ScriptAssert o un
        // validador cruzado entre varios campos del DTO).
        bindingResult.addError(new ObjectError("tariffCreateRequest", "basePrice must not exceed pricePerMinute limits"));

        MethodParameter methodParameter = new MethodParameter(
                CustomizedExceptionAdapterTest.class.getDeclaredMethod("shouldHandleClassLevelValidationErrors"), -1);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(methodParameter, bindingResult);

        ResponseEntity<ErrorResponse> response =
                exceptionAdapter.handleValidation(exception, requestTo("/v1/tariffs"));

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        // Antes de la correccion, este mensaje se perdia y caia en el
        // texto generico "Invalid request payload." porque getFieldErrors()
        // ignora los ObjectError sin campo asociado.
        assertTrue(response.getBody().getMessage().contains("basePrice must not exceed pricePerMinute limits"));
    }

    @Test
    @DisplayName("Debe manejar un body JSON malformado o con un valor de enum no reconocido devolviendo 400, no el catch-all de 500")
    void shouldHandleMalformedRequestBody() {
        HttpMessageNotReadableException exception =
                new HttpMessageNotReadableException(
                        "JSON parse error: Cannot deserialize value of type VehicleType from String \"BUS\"",
                        (HttpInputMessage) null);

        ResponseEntity<ErrorResponse> response =
                exceptionAdapter.handleMalformedRequest(exception, requestTo("/v1/tariffs"));

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("BAD_REQUEST", response.getBody().getError());
        // El mensaje debe ser propio, sin filtrar el detalle crudo de Jackson.
        assertTrue(!response.getBody().getMessage().contains("Jackson"));
    }

    @Test
    @DisplayName("Debe manejar un path variable o query param con tipo incorrecto (ej. UUID mal formado) devolviendo 400, no el catch-all de 500")
    void shouldHandleTypeMismatch() throws NoSuchMethodException {
        MethodParameter methodParameter = new MethodParameter(
                CustomizedExceptionAdapterTest.class.getDeclaredMethod("shouldHandleTypeMismatch"), -1);
        MethodArgumentTypeMismatchException exception =
                new MethodArgumentTypeMismatchException("not-a-uuid", java.util.UUID.class, "id", methodParameter, new IllegalArgumentException());

        ResponseEntity<ErrorResponse> response =
                exceptionAdapter.handleTypeMismatch(exception, requestTo("/v1/tariffs/not-a-uuid"));

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("id"));
        assertTrue(response.getBody().getMessage().contains("UUID"));
    }

    @Test
    @DisplayName("Debe manejar un query param obligatorio ausente devolviendo 400, no el catch-all de 500")
    void shouldHandleMissingParameter() {
        MissingServletRequestParameterException exception =
                new MissingServletRequestParameterException("type", "VehicleType");

        ResponseEntity<ErrorResponse> response =
                exceptionAdapter.handleMissingParameter(exception, requestTo("/v1/tariffs/active"));

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("BAD_REQUEST", response.getBody().getError());
    }

    @Test
    @DisplayName("Debe manejar AccessDeniedException devolviendo 403 Forbidden con ErrorResponse conforme al contrato")
    void shouldHandleAccessDeniedException() {
        org.springframework.security.access.AccessDeniedException exception =
                new org.springframework.security.access.AccessDeniedException("Access is denied");

        ResponseEntity<ErrorResponse> response =
                exceptionAdapter.handleAccessDenied(exception, requestTo("/v1/tariffs"));

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.FORBIDDEN.value(), response.getBody().getStatus());
        assertEquals("FORBIDDEN", response.getBody().getError());
        assertEquals("You do not have permission to perform this action.", response.getBody().getMessage());
        assertEquals("/v1/tariffs", response.getBody().getPath());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    @DisplayName("Debe manejar cualquier excepcion no controlada devolviendo 500 sin exponer detalles internos")
    void shouldHandleUnexpectedException() {
        RuntimeException exception = new RuntimeException("Simulated internal failure: database driver crashed");

        ResponseEntity<ErrorResponse> response =
                exceptionAdapter.handleUnexpected(exception, requestTo("/v1/tariffs"));

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getBody().getStatus());
        assertEquals("INTERNAL_SERVER_ERROR", response.getBody().getError());
        assertEquals("An unexpected error occurred. Please try again later.", response.getBody().getMessage());
        // La propiedad clave: el mensaje interno NUNCA se filtra al cliente.
        assertTrue(!response.getBody().getMessage().contains("database driver"));
    }
}
