package com.tartis_recon_ai_parking.infrastructure.customizedexception.adapter.output;

import com.tartis_recon_ai_parking.domain.tariff.exception.InvalidTariffException;
import com.tartis_recon_ai_parking.domain.tariff.exception.TariffNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import com.tartis_recon_ai_parking.application.tariff.exception.ConcurrentModificationConflictException;
import com.tartis_recon_ai_parking.application.tariff.exception.PersistenceFailureException;
import com.tartis_recon_ai_parking.application.tariff.exception.PersistenceUnavailableException;
import com.tartis_recon_ai_parking.domain.tariff.exception.CorruptedTariffDataException;
import com.tartis_recon_ai_parking.domain.tariff.exception.TariffAlreadyExistsException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.transaction.TransactionException;

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

    @ExceptionHandler(com.tartis_recon_ai_parking.domain.tariff.exception.TariffTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(com.tartis_recon_ai_parking.domain.tariff.exception.TariffTypeMismatchException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
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


    // ==================== Escenarios de ruptura de BD ====================

    /**
     * Nombre de tarifa duplicado. Es el unico de este bloque cuyo mensaje
     * se devuelve tal cual al cliente: lo redacta el adapter de
     * persistencia, no la BD, asi que no filtra nada.
     */
    @ExceptionHandler(TariffAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleTariffAlreadyExists(TariffAlreadyExistsException ex,
                                                                   HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    /**
     * Violacion de restricciones del dominio de tarifas (ej. intentar
     * desactivar la unica tarifa activa).
     */
    @ExceptionHandler(com.tartis_recon_ai_parking.domain.tariff.exception.TariffConstraintException.class)
    public ResponseEntity<ErrorResponse> handleTariffConstraint(com.tartis_recon_ai_parking.domain.tariff.exception.TariffConstraintException ex,
                                                                   HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    /**
     * Conflicto de concurrencia (deadlock, lock optimista). El cliente
     * puede reintentar y tiene sentido que lo haga.
     */
    @ExceptionHandler(ConcurrentModificationConflictException.class)
    public ResponseEntity<ErrorResponse> handleConcurrencyConflict(ConcurrentModificationConflictException ex,
                                                                    HttpServletRequest request) {
        log.warn("Conflicto de concurrencia en [{} {}]", request.getMethod(), request.getRequestURI(), ex);
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    /**
     * BD caida o que no responde. 503 + Retry-After: le decimos al
     * frontend que esto es transitorio y cuando volver a intentarlo.
     */
    @ExceptionHandler(PersistenceUnavailableException.class)
    public ResponseEntity<ErrorResponse> handlePersistenceUnavailable(PersistenceUnavailableException ex,
                                                                       HttpServletRequest request) {
        log.error("Persistencia no disponible en [{} {}]", request.getMethod(), request.getRequestURI(), ex);
        return buildRetryableResponse(
                "The service is temporarily unavailable. Please try again shortly.", request);
    }

    /**
     * Datos almacenados que incumplen los invariantes de dominio. Es un
     * fallo del servidor: sin este handler, la InvalidTariffException que
     * lanza Tariff.reconstruct() al leer acabaria en el handler de 400,
     * culpando al cliente de una fila corrupta.
     */
    @ExceptionHandler(CorruptedTariffDataException.class)
    public ResponseEntity<ErrorResponse> handleCorruptedData(CorruptedTariffDataException ex,
                                                              HttpServletRequest request) {
        log.error("Datos inconsistentes en BD en [{} {}]", request.getMethod(), request.getRequestURI(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later.", request);
    }

    /**
     * Fallo de persistencia no transitorio ya traducido por el adapter.
     */
    @ExceptionHandler(PersistenceFailureException.class)
    public ResponseEntity<ErrorResponse> handlePersistenceFailure(PersistenceFailureException ex,
                                                                   HttpServletRequest request) {
        log.error("Fallo de persistencia en [{} {}]", request.getMethod(), request.getRequestURI(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later.", request);
    }

    /**
     * RED DE SEGURIDAD. Cualquier excepcion de BD que esquive la
     * traduccion del adapter (p.ej. una violacion de constraint que
     * aflora en el commit, ya fuera del try/catch del adapter) acaba
     * aqui. NUNCA se devuelve ex.getMessage(): contiene el SQL, el
     * nombre de la constraint y el de la tabla.
     */
    @ExceptionHandler({DataAccessException.class, TransactionException.class})
    public ResponseEntity<ErrorResponse> handleUntranslatedDataAccess(Exception ex, HttpServletRequest request) {
        log.error("Excepcion de persistencia SIN traducir en [{} {}] - revisar TariffPersistenceAdapter",
                request.getMethod(), request.getRequestURI(), ex);
        return buildRetryableResponse(
                "The service is temporarily unavailable. Please try again shortly.", request);
    }

    // ==================== Seguridad ====================

    /**
     * HTTP 401 Unauthorized: El token de autenticación está ausente, es inválido o ha caducado.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(AuthenticationException ex, HttpServletRequest request) {
        log.warn("Autenticación fallida o token inválido en [{} {}]", request.getMethod(), request.getRequestURI());
        return buildResponse(HttpStatus.UNAUTHORIZED,
                "Authentication token is missing, invalid, or expired.", request);
    }

    /**
     * HTTP 403 Forbidden: El token de autenticación es válido pero el usuario no posee el rol necesario.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Acceso denegado en [{} {}]", request.getMethod(), request.getRequestURI());
        return buildResponse(HttpStatus.FORBIDDEN,
                "You do not have permission to perform this action.", request);
    }

    // ==================== Catch-all ====================

    /**
     * RED DE SEGURIDAD FINAL. Cualquier excepcion no prevista (un NPE en
     * un mapper, un fallo de serializacion, una excepcion de terceros no
     * contemplada) acaba aqui. Garantias:
     *   1. El cliente siempre recibe un ErrorResponse conforme al contrato.
     *   2. NUNCA se exponen detalles internos ni stacktraces al exterior.
     *   3. Se registra la excepcion completa en el log del servidor para
     *      depuracion.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception while processing request [{} {}]",
                request.getMethod(), request.getRequestURI(), ex);
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

    private ResponseEntity<ErrorResponse> buildRetryableResponse(String message, HttpServletRequest request) {
        HttpStatus status = HttpStatus.SERVICE_UNAVAILABLE;
        ErrorResponse body = new ErrorResponse(status.value(), status.name(), message, request.getRequestURI());
        return ResponseEntity.status(status)
                .header(HttpHeaders.RETRY_AFTER, "5")
                .body(body);
    }
}
