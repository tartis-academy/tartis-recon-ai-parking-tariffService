package com.tartis_recon_ai_parking.infrastructure.tariff.adapter.input.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tartis_recon_ai_parking.application.tariff.dto.TariffCreateDTO;
import com.tartis_recon_ai_parking.application.tariff.dto.TariffDTO;
import com.tartis_recon_ai_parking.application.tariff.dto.TariffUpdateDTO;
import com.tartis_recon_ai_parking.application.tariff.usecase.*;
import com.tartis_recon_ai_parking.domain.tariff.VehicleType;
import com.tartis_recon_ai_parking.infrastructure.tariff.adapter.input.rest.dto.request.TariffCreateRequest;
import com.tartis_recon_ai_parking.infrastructure.tariff.adapter.input.rest.dto.request.TariffPriceRequest;
import com.tartis_recon_ai_parking.infrastructure.tariff.adapter.input.rest.dto.request.TariffStatusRequest;
import com.tartis_recon_ai_parking.infrastructure.tariff.adapter.input.rest.dto.request.TariffUpdateRequest;
import com.tartis_recon_ai_parking.infrastructure.tariff.adapter.input.rest.dto.response.TariffResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class TariffRestAdapterTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    @DisplayName("Debe retornar la lista de tarifas activas filtradas por tipo de vehiculo")
    void shouldGetActiveTariffs() throws Exception {
        UUID id = UUID.randomUUID();
        TariffDTO dto = new TariffDTO(id, "Standard", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("2.0"), true);
        TariffResponse response = new TariffResponse(id, "Standard", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("2.0"), true);

        when(getActiveTariffUseCase.execute(VehicleType.CAR)).thenReturn(List.of(dto));
        when(mapper.toResponseList(List.of(dto))).thenReturn(List.of(response));

        mockMvc.perform(get("/v1/tariffs/active")
                .param("type", "CAR")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id.toString()))
                .andExpect(jsonPath("$[0].name").value("Standard"));
        
        verify(getActiveTariffUseCase).execute(VehicleType.CAR);
    }

    @Test
    @DisplayName("Debe retornar la lista de todas las tarifas")
    void shouldGetAllTariffs() throws Exception {
        UUID id = UUID.randomUUID();
        TariffDTO dto = new TariffDTO(id, "Standard", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("2.0"), true);
        TariffResponse response = new TariffResponse(id, "Standard", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("2.0"), true);

        when(getAllTariffsUseCase.execute()).thenReturn(List.of(dto));
        when(mapper.toResponseList(List.of(dto))).thenReturn(List.of(response));

        mockMvc.perform(get("/v1/tariffs")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id.toString()))
                .andExpect(jsonPath("$[0].name").value("Standard"));
        
        verify(getAllTariffsUseCase).execute();
    }

    @Test
    @DisplayName("Debe retornar una tarifa especifica por su ID")
    void shouldGetTariffById() throws Exception {
        UUID id = UUID.randomUUID();
        TariffDTO dto = new TariffDTO(id, "Standard", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("2.0"), true);
        TariffResponse response = new TariffResponse(id, "Standard", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("2.0"), true);

        when(getTariffUseCase.execute(id)).thenReturn(dto);
        when(mapper.toResponse(dto)).thenReturn(response);

        mockMvc.perform(get("/v1/tariffs/{id}", id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Standard"));
        
        verify(getTariffUseCase).execute(id);
    }

    @Test
    @DisplayName("Debe crear una tarifa retornando el codigo 201 CREATED")
    void shouldCreateTariff() throws Exception {
        UUID id = UUID.randomUUID();
        TariffCreateRequest request = new TariffCreateRequest("Standard", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("2.0"), true);
        TariffCreateDTO createDto = new TariffCreateDTO("Standard", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("2.0"), true);
        TariffDTO createdDto = new TariffDTO(id, "Standard", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("2.0"), true);
        TariffResponse response = new TariffResponse(id, "Standard", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("2.0"), true);

        when(mapper.toCreateDTO(any(TariffCreateRequest.class))).thenReturn(createDto);
        when(createTariffUseCase.execute(createDto)).thenReturn(createdDto);
        when(mapper.toResponse(createdDto)).thenReturn(response);

        mockMvc.perform(post("/v1/tariffs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Standard"));
        
        verify(createTariffUseCase).execute(createDto);
    }

    @Test
    @DisplayName("Debe actualizar una tarifa correctamente devolviendo 200 OK")
    void shouldUpdateTariff() throws Exception {
        UUID id = UUID.randomUUID();
        TariffUpdateRequest request = new TariffUpdateRequest("Premium", new BigDecimal("0.08"), new BigDecimal("3.0"));
        TariffUpdateDTO updateDto = new TariffUpdateDTO("Premium", new BigDecimal("0.08"), new BigDecimal("3.0")); 
        TariffDTO updatedDto = new TariffDTO(id, "Premium", VehicleType.CAR, new BigDecimal("0.08"), new BigDecimal("3.0"), true);
        TariffResponse response = new TariffResponse(id, "Premium", VehicleType.CAR, new BigDecimal("0.08"), new BigDecimal("3.0"), true);

        when(mapper.toUpdateDTO(any(TariffUpdateRequest.class))).thenReturn(updateDto);
        when(updateTariffUseCase.execute(eq(id), eq(updateDto))).thenReturn(updatedDto);
        when(mapper.toResponse(updatedDto)).thenReturn(response);

        mockMvc.perform(put("/v1/tariffs/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Premium"));
                
        verify(updateTariffUseCase).execute(eq(id), eq(updateDto));
    }

    @Test
    @DisplayName("Debe activar una tarifa al recibir status request true")
    void shouldActivateTariffStatus() throws Exception {
        UUID id = UUID.randomUUID();
        TariffStatusRequest request = new TariffStatusRequest(true);
        TariffDTO activatedDto = new TariffDTO(id, "Standard", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("2.0"), true);
        TariffResponse response = new TariffResponse(id, "Standard", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("2.0"), true);

        when(activateTariffUseCase.execute(id)).thenReturn(activatedDto);
        when(mapper.toResponse(activatedDto)).thenReturn(response);

        mockMvc.perform(patch("/v1/tariffs/{id}/status", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
                
        verify(activateTariffUseCase).execute(id);
    }

    @Test
    @DisplayName("Debe desactivar una tarifa al recibir status request false")
    void shouldDeactivateTariffStatus() throws Exception {
        UUID id = UUID.randomUUID();
        TariffStatusRequest request = new TariffStatusRequest(false);
        TariffDTO deactivatedDto = new TariffDTO(id, "Standard", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("2.0"), false);
        TariffResponse response = new TariffResponse(id, "Standard", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("2.0"), false);

        when(deactivateTariffUseCase.execute(id)).thenReturn(deactivatedDto);
        when(mapper.toResponse(deactivatedDto)).thenReturn(response);

        mockMvc.perform(patch("/v1/tariffs/{id}/status", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
                
        verify(deactivateTariffUseCase).execute(id);
    }

    @Test
    @DisplayName("POST /v1/tariffs - Debe retornar 400 cuando el payload es invalido")
    void shouldReturn400OnCreateWithInvalidData() throws Exception {
        // Enviar request con precio negativo (violando @DecimalMin)
        TariffCreateRequest request = new TariffCreateRequest("Standard", VehicleType.CAR, new BigDecimal("-0.05"), new BigDecimal("2.0"), true);

        mockMvc.perform(post("/v1/tariffs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /v1/tariffs/{id} - Debe retornar 400 cuando el payload es invalido")
    void shouldReturn400OnUpdateWithInvalidData() throws Exception {
        UUID id = UUID.randomUUID();
        // Request con nombre vacio (violando @NotBlank)
        TariffUpdateRequest request = new TariffUpdateRequest("", new BigDecimal("0.05"), new BigDecimal("2.0"));

        mockMvc.perform(put("/v1/tariffs/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /v1/tariffs/{id}/status - Debe retornar 400 cuando el payload es invalido")
    void shouldReturn400OnStatusChangeWithInvalidData() throws Exception {
        UUID id = UUID.randomUUID();
        TariffStatusRequest request = new TariffStatusRequest(null);

        mockMvc.perform(patch("/v1/tariffs/{id}/status", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /v1/tariffs/{id} - Debe retornar 404 cuando la tarifa no existe")
    void shouldReturn404OnGetNonExistentTariff() throws Exception {
        UUID id = UUID.randomUUID();
        when(getTariffUseCase.execute(id)).thenThrow(new com.tartis_recon_ai_parking.domain.tariff.exception.TariffNotFoundException(id));

        mockMvc.perform(get("/v1/tariffs/{id}", id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /v1/tariffs/{id} - Debe retornar 404 cuando la tarifa no existe")
    void shouldReturn404OnUpdateNonExistentTariff() throws Exception {
        UUID id = UUID.randomUUID();
        TariffUpdateRequest request = new TariffUpdateRequest("Premium", new BigDecimal("0.08"), new BigDecimal("3.0"));
        TariffUpdateDTO updateDto = new TariffUpdateDTO("Premium", new BigDecimal("0.08"), new BigDecimal("3.0"));
        
        when(mapper.toUpdateDTO(any(TariffUpdateRequest.class))).thenReturn(updateDto);
        when(updateTariffUseCase.execute(eq(id), eq(updateDto))).thenThrow(new com.tartis_recon_ai_parking.domain.tariff.exception.TariffNotFoundException(id));

        mockMvc.perform(put("/v1/tariffs/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}
