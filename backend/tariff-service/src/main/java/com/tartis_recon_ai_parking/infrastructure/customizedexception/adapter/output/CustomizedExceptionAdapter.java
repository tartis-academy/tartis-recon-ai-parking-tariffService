package com.tartis_recon_ai_parking.infrastructure.customizedexception.adapter.output;

import com.tartis_recon_ai_parking.domain.tariff.exception.InvalidTariffException;
import com.tartis_recon_ai_parking.domain.tariff.exception.TariffNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import com.tartis_recon_ai_parking.domain.tariff.exception.TariffConcurrentModificationException;
import java.util.stream.Collectors;

/**
 * Punto unico de traduccion de excepciones de dominio/aplicacion a
 * respuestas HTTP (IN-36). El cliente (frontend) siempre recibe el mismo
 * contrato ErrorResponse (alineado con el schema "ErrorResponse" de
 * openapi.yml: timestamp, status, error, message, path), nunca una
 * excepcion cruda de red/HTTP.
 */
@RestControllerAdvice
public class CustomizedExceptionAdapter {

    private static final Logger log = LoggerFactory.getLogger(CustomizedExceptionAdapter.class);

    @ExceptionHandler(TariffNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTariffNotFound(TariffNotFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidTariffException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTariff(InvalidTariffException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }
     /**
     * Condicion de carrera detectada (TAR-1780): dos operaciones concurrentes
     * intentaron modificar la misma tarifa. Se devuelve 409 Conflict para que
     * el cliente sepa que debe recargar el recurso y reintentar, en vez de un
     * 500 generico.
     */
    @ExceptionHandler(TariffConcurrentModificationException.class)
    public ResponseEntity<ErrorResponse> handleConcurrentModification(TariffConcurrentModificationException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }


    /**
     * Falla la validacion de un @Valid @RequestBody (ej. un TariffCreateRequest
     * con un campo obligatorio ausente o invalido desde el panel de admin).
     *
     * Usa getAllErrors() (no solo getFieldErrors()) para no perder los
     * errores de validacion a nivel de clase/objeto completo (ObjectError
     * sin campo asociado), que con getFieldErrors() quedaban descartados
     * y caian en el mensaje generico de respaldo.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getAllErrors().stream()
                .map(this::formatValidationError)
                .collect(Collectors.joining("; "));

        if (message.isBlank()) {
            message = "Invalid request payload.";
        }

        return buildResponse(HttpStatus.BAD_REQUEST, message, request);
    }

    /**
     * Cuerpo de la peticion no legible: JSON mal formado, o un valor que
     * no se puede deserializar al tipo esperado (ej. un VehicleType que
     * no existe en el enum, como "type": "BUS"). Sin este handler, Spring
     * lo dejaba caer en el catch-all de Exception -> 500, ocultando que
     * el problema es del payload enviado por el cliente, no del servidor.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMalformedRequest(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST,
                "The request body is missing, malformed, or contains a value that cannot be parsed (e.g. an unrecognized value for an enum field).",
                request);
    }

    /**
     * Un parametro de ruta o de query no tiene el tipo esperado (ej. un
     * UUID mal formado en /v1/tariffs/{id}, o un VehicleType invalido en
     * ?type=). Mismo motivo que el handler anterior: sin esto caia en el
     * catch-all como 500 en vez de 400.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String requiredType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "expected type";
        String message = "Invalid value '" + ex.getValue() + "' for parameter '" + ex.getName() + "'. Expected " + requiredType + ".";
        return buildResponse(HttpStatus.BAD_REQUEST, message, request);
    }

    /**
     * Falta un query param obligatorio (ej. GET /v1/tariffs/active sin
     * ?type=).
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(MissingServletRequestParameterException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    /**
     * Catch-all: cualquier excepcion no controlada explicitamente
     * (errores de infraestructura, NullPointerException, fallos de BD,
     * etc.). Se registra el detalle completo en el log del servidor, pero
     * al cliente solo se le devuelve un ErrorResponse generico y seguro,
     * evitando exponer detalles internos o stacktraces.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception while processing request [{} {}]", request.getMethod(), request.getRequestURI(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later.", request);
    }

    private String formatValidationError(ObjectError error) {
        if (error instanceof FieldError fieldError) {
            return fieldError.getField() + ": " + fieldError.getDefaultMessage();
        }
        return error.getObjectName() + ": " + error.getDefaultMessage();
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String message, HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(status.value(), status.name(), message, request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}