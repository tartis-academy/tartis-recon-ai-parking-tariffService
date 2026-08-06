package com.tartis_recon_ai_parking.infrastructure.tariff.adapter.input.rest;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tartis_recon_ai_parking.application.tariff.dto.PriceTransferDTO;
import com.tartis_recon_ai_parking.application.tariff.usecase.PriceCalculateUseCase;
import com.tartis_recon_ai_parking.domain.tariff.VehicleType;
import com.tartis_recon_ai_parking.domain.tariff.exception.TariffNotFoundException;
import com.tartis_recon_ai_parking.infrastructure.tariff.adapter.input.rest.dto.request.TariffPriceRequest;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@SpringBootTest
@AutoConfigureMockMvc
class TariffRestAdapterPriceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PriceCalculateUseCase priceCalculateUseCase;

    @Test
    @DisplayName("Debe devolver el precio calculado exitosamente")
    void shouldReturnCalculatedPriceSuccessfully() throws Exception {
        UUID tariffId = UUID.randomUUID();
        TariffPriceRequest request = new TariffPriceRequest(tariffId, VehicleType.CAR, 120);
        PriceTransferDTO priceTransferDTO = new PriceTransferDTO(new BigDecimal("8.00"));

        when(priceCalculateUseCase.execute(tariffId, VehicleType.CAR, 120)).thenReturn(priceTransferDTO);

        mockMvc.perform(post("/v1/tariffs/calculate")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value("8.0"));
    }

    @Test
    @DisplayName("Debe devolver 404 cuando no se encuentra tarifa activa al calcular")
    void shouldReturnNotFoundWhenActiveTariffDoesNotExist() throws Exception {
        UUID tariffId = UUID.randomUUID();
        TariffPriceRequest request = new TariffPriceRequest(tariffId, VehicleType.CAR, 60);

        when(priceCalculateUseCase.execute(tariffId, VehicleType.CAR, 60))
                .thenThrow(new TariffNotFoundException(tariffId));

        mockMvc.perform(post("/v1/tariffs/calculate")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}
