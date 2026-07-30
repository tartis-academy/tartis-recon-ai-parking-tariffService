package com.tartis_recon_ai_parking.infrastructure.customizedexception.adapter.output;

import com.tartis_recon_ai_parking.application.tariff.usecase.ActivateTariffUseCase;
import com.tartis_recon_ai_parking.application.tariff.usecase.CreateTariffUseCase;
import com.tartis_recon_ai_parking.application.tariff.usecase.DeactivateTariffUseCase;
import com.tartis_recon_ai_parking.application.tariff.usecase.GetActiveTariffUseCase;
import com.tartis_recon_ai_parking.application.tariff.usecase.GetAllTariffsUseCase;
import com.tartis_recon_ai_parking.application.tariff.usecase.GetTariffUseCase;
import com.tartis_recon_ai_parking.application.tariff.usecase.PriceCalculateUseCase;
import com.tartis_recon_ai_parking.application.tariff.usecase.UpdateTariffUseCase;
import com.tartis_recon_ai_parking.infrastructure.tariff.adapter.input.rest.TariffRestMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.dao.DataAccessException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
    @DisplayName("Debe propagar AccessDeniedException (provocada por @PreAuthorize) y resultar en HTTP 403 Forbidden con ErrorResponse")
    void shouldReturn403WhenAccessDeniedOccurs() throws Exception {
        mockMvc.perform(get("/v1/tariffs")
                        .with(jwt().authorities(createAuthorityList("ROLE_USER"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("You do not have permission to perform this action."));

        verify(getAllTariffsUseCase, never()).execute();
    }
}
