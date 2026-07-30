package com.tartis_recon_ai_parking.infrastructure.config;

import com.tartis_recon_ai_parking.application.tariff.usecase.*;
import com.tartis_recon_ai_parking.infrastructure.tariff.adapter.input.rest.TariffRestMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * SEC-11: verifica que el exceptionHandling de SecurityConfig enruta correctamente
 * los errores 401 (sin token) y 403 (rol insuficiente) a través del
 * HandlerExceptionResolver → CustomizedExceptionAdapter, produciendo un
 * ErrorResponse con estructura definida en lugar de la respuesta por defecto de Spring.
 */
@SpringBootTest
class SecurityConfigTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockitoBean private CreateTariffUseCase createTariffUseCase;
    @MockitoBean private GetTariffUseCase getTariffUseCase;
    @MockitoBean private GetAllTariffsUseCase getAllTariffsUseCase;
    @MockitoBean private GetActiveTariffUseCase getActiveTariffUseCase;
    @MockitoBean private UpdateTariffUseCase updateTariffUseCase;
    @MockitoBean private ActivateTariffUseCase activateTariffUseCase;
    @MockitoBean private DeactivateTariffUseCase deactivateTariffUseCase;
    @MockitoBean private TariffRestMapper mapper;
    @MockitoBean private PriceCalculateUseCase priceCalculator;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("SEC-11: Sin token, el authenticationEntryPoint enruta a CustomizedExceptionAdapter → 401 con ErrorResponse")
    void shouldReturn401WithErrorResponseWhenNoToken() throws Exception {
        mockMvc.perform(get("/v1/tariffs"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("SEC-11: Con rol insuficiente, el accessDeniedHandler enruta a CustomizedExceptionAdapter → 403 con ErrorResponse")
    void shouldReturn403WithErrorResponseWhenInsufficientRole() throws Exception {
        mockMvc.perform(get("/v1/tariffs")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));
    }
}
