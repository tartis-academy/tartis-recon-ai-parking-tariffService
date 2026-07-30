package com.tartis_recon_ai_parking.infrastructure.customizedexception.adapter.output;

import com.tartis_recon_ai_parking.application.tariff.usecase.*;
import com.tartis_recon_ai_parking.infrastructure.tariff.adapter.input.rest.TariffRestMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

/**
 * Prueba de extremo a extremo (DispatcherServlet real, sin llamar a los
 * handlers directamente) para los escenarios senalados en la revision del
 * PR: JSON mal formado, valores de enum no reconocidos, parametros de
 * ruta/query con tipo incorrecto o ausentes. Antes de la correccion,
 * estos casos caian en el catch-all de Exception y devolvian 500 en vez
 * de 400 -confundiendo un error de cliente con un fallo de servidor-.
 *
 * Desde SEC-07 todas las llamadas llevan .with(jwt()): el SecurityFilterChain
 * actua antes que el DispatcherServlet, asi que sin token estos escenarios
 * darian 401 en vez del 400 que es lo que este test quiere comprobar.
 */
@SpringBootTest
@AutoConfigureMockMvc
class TariffClientErrorScenariosTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateTariffUseCase createTariffUseCase;
    @MockitoBean
    private GetTariffUseCase getTariffUseCase;
    @MockitoBean
    private GetAllTariffsUseCase getAllTariffsUseCase;
    @MockitoBean
    private GetActiveTariffUseCase getActiveTariffUseCase;
    @MockitoBean
    private UpdateTariffUseCase updateTariffUseCase;
    @MockitoBean
    private ActivateTariffUseCase activateTariffUseCase;
    @MockitoBean
    private DeactivateTariffUseCase deactivateTariffUseCase;
    @MockitoBean
    private TariffRestMapper mapper;
    @MockitoBean
    private PriceCalculateUseCase priceCalculator;

    @Test
    @DisplayName("POST con JSON sintacticamente invalido debe devolver 400 con el contrato ErrorResponse, no 500")
    void shouldReturnBadRequestOnMalformedJson() throws Exception {
        String malformedJson = "{ \"name\": \"Standard\", \"type\": \"CAR\", "; // JSON incompleto/roto

        mockMvc.perform(post("/v1/tariffs")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").exists());
    }

    @Test
    @DisplayName("POST con un valor de enum VehicleType no reconocido debe devolver 400 con el contrato ErrorResponse, no 500")
    void shouldReturnBadRequestOnUnrecognizedEnumValue() throws Exception {
        String jsonWithInvalidEnum = "{"
                + "\"name\": \"Standard\","
                + "\"type\": \"BUS\","
                + "\"pricePerMinute\": 0.05,"
                + "\"basePrice\": 2.0,"
                + "\"active\": true"
                + "}";

        mockMvc.perform(post("/v1/tariffs")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonWithInvalidEnum))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"));
    }

    @Test
    @DisplayName("GET con un UUID mal formado en la ruta debe devolver 400 con el contrato ErrorResponse, no 500")
    void shouldReturnBadRequestOnInvalidUuidPathVariable() throws Exception {
        mockMvc.perform(get("/v1/tariffs/{id}", "not-a-valid-uuid").with(jwt()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("GET con un query param obligatorio ausente debe devolver 400 con el contrato ErrorResponse, no 500")
    void shouldReturnBadRequestOnMissingRequiredQueryParam() throws Exception {
        mockMvc.perform(get("/v1/tariffs/active").with(jwt()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"));
    }

    @Test
    @DisplayName("GET con un query param con tipo de enum invalido debe devolver 400 con el contrato ErrorResponse, no 500")
    void shouldReturnBadRequestOnInvalidEnumQueryParam() throws Exception {
        mockMvc.perform(get("/v1/tariffs/active").with(jwt()).param("type", "BUS"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"));
    }
}
