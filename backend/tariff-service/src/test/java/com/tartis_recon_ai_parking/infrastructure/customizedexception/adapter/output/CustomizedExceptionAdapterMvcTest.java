package com.tartis_recon_ai_parking.infrastructure.customizedexception.adapter.output;

import com.tartis_recon_ai_parking.application.tariff.usecase.GetAllTariffsUseCase;
import com.tartis_recon_ai_parking.infrastructure.config.SecurityConfig;
import com.tartis_recon_ai_parking.infrastructure.tariff.adapter.input.rest.TariffRestAdapter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.dao.DataAccessException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.core.authority.AuthorityUtils.createAuthorityList;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CustomizedExceptionAdapterMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private com.tartis_recon_ai_parking.application.tariff.usecase.CreateTariffUseCase createTariffUseCase;
    @MockitoBean
    private com.tartis_recon_ai_parking.application.tariff.usecase.GetTariffUseCase getTariffUseCase;
    @MockitoBean
    private GetAllTariffsUseCase getAllTariffsUseCase;
    @MockitoBean
    private com.tartis_recon_ai_parking.application.tariff.usecase.GetActiveTariffUseCase getActiveTariffUseCase;
    @MockitoBean
    private com.tartis_recon_ai_parking.application.tariff.usecase.UpdateTariffUseCase updateTariffUseCase;
    @MockitoBean
    private com.tartis_recon_ai_parking.application.tariff.usecase.ActivateTariffUseCase activateTariffUseCase;
    @MockitoBean
    private com.tartis_recon_ai_parking.application.tariff.usecase.DeactivateTariffUseCase deactivateTariffUseCase;
    @MockitoBean
    private com.tartis_recon_ai_parking.infrastructure.tariff.adapter.input.rest.TariffRestMapper mapper;
    @MockitoBean
    private com.tartis_recon_ai_parking.application.tariff.usecase.PriceCalculateUseCase priceCalculator;

    @Test
    @DisplayName("Debe capturar DataAccessException genérica via @RestControllerAdvice y responder HTTP 503 Service Unavailable")
    void shouldReturn503WhenGenericDatabaseErrorOccurs() throws Exception {
        when(getAllTariffsUseCase.execute()).thenThrow(new DataAccessException("Connection failed") {});

        mockMvc.perform(get("/v1/tariffs")
                        .with(jwt().authorities(createAuthorityList("ROLE_ADMIN"))))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.error").value("SERVICE_UNAVAILABLE"))
                .andExpect(jsonPath("$.message").value("The service is temporarily unavailable. Please try again shortly."));
    }

    @Test
    @DisplayName("Debe propagar AccessDeniedException (provocada por @PreAuthorize) y resultar en HTTP 403 Forbidden")
    void shouldReturn403WhenAccessDeniedOccurs() throws Exception {
        mockMvc.perform(get("/v1/tariffs")
                        .with(jwt().authorities(createAuthorityList("ROLE_USER"))))
                .andExpect(status().isForbidden());
    }
}
