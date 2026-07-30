package com.tartis_recon_ai_parking.infrastructure.customizedexception.adapter.output;

import com.tartis_recon_ai_parking.application.tariff.exception.ConcurrentModificationConflictException;
import com.tartis_recon_ai_parking.application.tariff.exception.PersistenceFailureException;
import com.tartis_recon_ai_parking.application.tariff.exception.PersistenceUnavailableException;
import com.tartis_recon_ai_parking.application.tariff.usecase.*;
import com.tartis_recon_ai_parking.domain.tariff.exception.CorruptedTariffDataException;
import com.tartis_recon_ai_parking.domain.tariff.exception.TariffAlreadyExistsException;
import com.tartis_recon_ai_parking.infrastructure.tariff.adapter.input.rest.TariffRestMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

/**
 * Gemelo de TariffClientErrorScenariosTest, pero para el otro extremo:
 * fallos de la capa de persistencia. Verifica el contrato HTTP completo
 * que ve el frontend y, sobre todo, que ningun detalle de BD (SQL,
 * constraint, tabla, host) sobrevive hasta la respuesta.
 *
 * Desde SEC-07 todas las llamadas llevan .with(jwt()): sin token, el
 * SecurityFilterChain corta con 401 antes de que el caso de uso mockeado
 * llegue a lanzar la excepcion que cada test quiere comprobar.
 */
@SpringBootTest
@AutoConfigureMockMvc
class TariffDatabaseErrorScenariosTest {

    private static final String VALID_CREATE_BODY = """
            {"name":"Standard","type":"CAR","pricePerMinute":0.05,"basePrice":2.0,"active":true}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private CreateTariffUseCase createTariffUseCase;
    @MockitoBean private GetTariffUseCase getTariffUseCase;
    @MockitoBean private GetAllTariffsUseCase getAllTariffsUseCase;
    @MockitoBean private GetActiveTariffUseCase getActiveTariffUseCase;
    @MockitoBean private UpdateTariffUseCase updateTariffUseCase;
    @MockitoBean private ActivateTariffUseCase activateTariffUseCase;
    @MockitoBean private DeactivateTariffUseCase deactivateTariffUseCase;
    @MockitoBean private TariffRestMapper mapper;
    @MockitoBean private PriceCalculateUseCase priceCalculator;

    @Test
    @DisplayName("Nombre duplicado debe devolver 409 con el contrato ErrorResponse")
    void shouldReturnConflictOnDuplicateName() throws Exception {
        when(createTariffUseCase.execute(any()))
                .thenThrow(new TariffAlreadyExistsException("A tariff with that name already exists."));

        mockMvc.perform(post("/v1/tariffs")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_CREATE_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value("A tariff with that name already exists."))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/v1/tariffs"));
    }

    @Test
    @DisplayName("Conflicto de concurrencia debe devolver 409 sugiriendo reintento")
    void shouldReturnConflictOnConcurrentModification() throws Exception {
        when(getAllTariffsUseCase.execute())
                .thenThrow(new ConcurrentModificationConflictException(
                        "The tariff was modified by another request. Please retry.", null));

        mockMvc.perform(get("/v1/tariffs").with(jwt()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("BD no disponible debe devolver 503 con Retry-After, no 500")
    void shouldReturnServiceUnavailableWhenDatabaseIsDown() throws Exception {
        when(getAllTariffsUseCase.execute())
                .thenThrow(new PersistenceUnavailableException(
                        "The database is currently unavailable.", null));

        mockMvc.perform(get("/v1/tariffs").with(jwt()))
                .andExpect(status().isServiceUnavailable())
                .andExpect(header().string("Retry-After", "5"))
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.error").value("SERVICE_UNAVAILABLE"))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("database"))));
    }

    @Test
    @DisplayName("Fallo de persistencia no transitorio debe devolver 500 generico sin filtrar el esquema")
    void shouldReturnGenericErrorOnPersistenceFailure() throws Exception {
        when(getAllTariffsUseCase.execute())
                .thenThrow(new PersistenceFailureException(
                        "relation \"tariff.tariffs\" does not exist", null));

        mockMvc.perform(get("/v1/tariffs").with(jwt()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred. Please try again later."))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("relation"))));
    }

    @Test
    @DisplayName("Fila corrupta en BD debe devolver 500, NUNCA 400: el cliente no tiene la culpa")
    void shouldReturnServerErrorOnCorruptedRow() throws Exception {
        UUID id = UUID.randomUUID();
        when(getTariffUseCase.execute(id))
                .thenThrow(new CorruptedTariffDataException("Stored tariff data is inconsistent.", null));

        mockMvc.perform(get("/v1/tariffs/{id}", id).with(jwt()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }

    @Test
    @DisplayName("RED DE SEGURIDAD: una excepcion de BD sin traducir tampoco debe filtrar el SQL")
    void shouldNotLeakSqlWhenExceptionEscapesTranslation() throws Exception {
        when(getAllTariffsUseCase.execute()).thenThrow(new DataIntegrityViolationException(
                "could not execute statement [ERROR: duplicate key value violates unique constraint "
                + "\"tariffs_name_key\"] [insert into tariffs (name) values (?)]"));

        mockMvc.perform(get("/v1/tariffs").with(jwt()))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("tariffs_name_key"))))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("insert into"))));
    }
}
