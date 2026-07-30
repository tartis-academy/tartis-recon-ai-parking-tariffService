package com.tartis_recon_ai_parking.infrastructure.tariff.adapter.input.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tartis_recon_ai_parking.application.tariff.dto.TariffCreateDTO;
import com.tartis_recon_ai_parking.application.tariff.dto.TariffDTO;
import com.tartis_recon_ai_parking.application.tariff.dto.TariffUpdateDTO;
import com.tartis_recon_ai_parking.application.tariff.dto.PriceTransferDTO;
import com.tartis_recon_ai_parking.infrastructure.tariff.adapter.input.rest.dto.response.PriceResponse;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

// Desde SEC-07, @AutoConfigureMockMvc engancha el SecurityFilterChain solo (a
// diferencia de spot-service, donde el MockMvc se construye a mano y hace falta
// .apply(springSecurity()) explicito). Por eso aqui basta con anadir .with(jwt())
// a cada llamada para simular una peticion autenticada. El caso sin token se
// prueba aparte, al final de la clase.
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
    // =========================================================================
    // PRUEBAS PARA ROL: ADMIN
    // =========================================================================

    @Test
    @DisplayName("ADMIN: Debe crear una tarifa retornando el codigo 201 CREATED")
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
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Standard"));

        verify(createTariffUseCase).execute(createDto);
    }

    @Test
    @DisplayName("ADMIN: Debe actualizar una tarifa correctamente devolviendo 200 OK")
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
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Premium"));

        verify(updateTariffUseCase).execute(eq(id), eq(updateDto));
    }

    @Test
    @DisplayName("ADMIN: Debe activar una tarifa al recibir status request true (200)")
    void shouldActivateTariffStatus() throws Exception {
        UUID id = UUID.randomUUID();
        TariffStatusRequest request = new TariffStatusRequest(true);
        TariffDTO activatedDto = new TariffDTO(id, "Standard", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("2.0"), true);
        TariffResponse response = new TariffResponse(id, "Standard", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("2.0"), true);

        when(activateTariffUseCase.execute(id)).thenReturn(activatedDto);
        when(mapper.toResponse(activatedDto)).thenReturn(response);

        mockMvc.perform(patch("/v1/tariffs/{id}/status", id)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));

        verify(activateTariffUseCase).execute(id);
    }

    @Test
    @DisplayName("ADMIN: Debe desactivar una tarifa al recibir status request false (200)")
    void shouldDeactivateTariffStatus() throws Exception {
        UUID id = UUID.randomUUID();
        TariffStatusRequest request = new TariffStatusRequest(false);
        TariffDTO deactivatedDto = new TariffDTO(id, "Standard", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("2.0"), false);
        TariffResponse response = new TariffResponse(id, "Standard", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("2.0"), false);

        when(deactivateTariffUseCase.execute(id)).thenReturn(deactivatedDto);
        when(mapper.toResponse(deactivatedDto)).thenReturn(response);

        mockMvc.perform(patch("/v1/tariffs/{id}/status", id)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        verify(deactivateTariffUseCase).execute(id);
    }

    @Test
    @DisplayName("ADMIN: Debe permitir consultar tarifas activas (200)")
    void shouldGetActiveTariffsForAdmin() throws Exception {
        UUID id = UUID.randomUUID();
        TariffDTO dto = new TariffDTO(id, "Standard", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("2.0"), true);
        TariffResponse response = new TariffResponse(id, "Standard", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("2.0"), true);

        when(getActiveTariffUseCase.execute(VehicleType.CAR)).thenReturn(List.of(dto));
        when(mapper.toResponseList(List.of(dto))).thenReturn(List.of(response));

        mockMvc.perform(get("/v1/tariffs/active")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .param("type", "CAR")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id.toString()));
    }

    @Test
    @DisplayName("ADMIN: Debe permitir consultar todas las tarifas (200)")
    void shouldGetAllTariffsForAdmin() throws Exception {
        UUID id = UUID.randomUUID();
        TariffDTO dto = new TariffDTO(id, "Standard", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("2.0"), true);
        TariffResponse response = new TariffResponse(id, "Standard", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("2.0"), true);

        when(getAllTariffsUseCase.execute()).thenReturn(List.of(dto));
        when(mapper.toResponseList(List.of(dto))).thenReturn(List.of(response));

        mockMvc.perform(get("/v1/tariffs")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id.toString()));
    }

    @Test
    @DisplayName("ADMIN: Debe permitir consultar una tarifa por ID (200)")
    void shouldGetTariffByIdForAdmin() throws Exception {
        UUID id = UUID.randomUUID();
        TariffDTO dto = new TariffDTO(id, "Standard", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("2.0"), true);
        TariffResponse response = new TariffResponse(id, "Standard", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("2.0"), true);

        when(getTariffUseCase.execute(id)).thenReturn(dto);
        when(mapper.toResponse(dto)).thenReturn(response);

        mockMvc.perform(get("/v1/tariffs/{id}", id)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    @DisplayName("ADMIN: Debe permitir calcular el precio (200)")
    void shouldAllowCalculatePriceForAdmin() throws Exception {
        TariffPriceRequest priceRequest =
                new TariffPriceRequest(VehicleType.CAR, 120);
        PriceTransferDTO priceDto = new PriceTransferDTO(new BigDecimal("8.00"));
        PriceResponse response = new PriceResponse(new BigDecimal("8.00"));

        when(priceCalculator.execute(VehicleType.CAR, 120)).thenReturn(priceDto);
        when(mapper.toResponse(priceDto)).thenReturn(response);

        mockMvc.perform(post("/v1/tariffs/calculate")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(priceRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(8.00));

        verify(priceCalculator).execute(VehicleType.CAR, 120);
    }

    // =========================================================================
    // PRUEBAS PARA ROL: OPERARIO
    // =========================================================================

    @Test
    @DisplayName("OPERARIO: Debe retornar la lista de tarifas activas filtradas por tipo de vehiculo (200)")
    void shouldGetActiveTariffs() throws Exception {
        UUID id = UUID.randomUUID();
        TariffDTO dto = new TariffDTO(id, "Standard", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("2.0"), true);
        TariffResponse response = new TariffResponse(id, "Standard", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("2.0"), true);

        when(getActiveTariffUseCase.execute(VehicleType.CAR)).thenReturn(List.of(dto));
        when(mapper.toResponseList(List.of(dto))).thenReturn(List.of(response));

        mockMvc.perform(get("/v1/tariffs/active")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_OPERARIO")))
                .param("type", "CAR")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id.toString()))
                .andExpect(jsonPath("$[0].name").value("Standard"));

        verify(getActiveTariffUseCase).execute(VehicleType.CAR);
    }

    @Test
    @DisplayName("OPERARIO: Debe retornar la lista de todas las tarifas (200)")
    void shouldGetAllTariffs() throws Exception {
        UUID id = UUID.randomUUID();
        TariffDTO dto = new TariffDTO(id, "Standard", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("2.0"), true);
        TariffResponse response = new TariffResponse(id, "Standard", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("2.0"), true);

        when(getAllTariffsUseCase.execute()).thenReturn(List.of(dto));
        when(mapper.toResponseList(List.of(dto))).thenReturn(List.of(response));

        mockMvc.perform(get("/v1/tariffs")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_OPERARIO")))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id.toString()))
                .andExpect(jsonPath("$[0].name").value("Standard"));

        verify(getAllTariffsUseCase).execute();
    }

    @Test
    @DisplayName("OPERARIO: Debe retornar una tarifa especifica por su ID (200)")
    void shouldGetTariffById() throws Exception {
        UUID id = UUID.randomUUID();
        TariffDTO dto = new TariffDTO(id, "Standard", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("2.0"), true);
        TariffResponse response = new TariffResponse(id, "Standard", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("2.0"), true);

        when(getTariffUseCase.execute(id)).thenReturn(dto);
        when(mapper.toResponse(dto)).thenReturn(response);

        mockMvc.perform(get("/v1/tariffs/{id}", id)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_OPERARIO")))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Standard"));

        verify(getTariffUseCase).execute(id);
    }

    @Test
    @DisplayName("OPERARIO: Debe denegar la creacion de tarifas (403)")
    void shouldDenyCreateTariffForOperario() throws Exception {
        TariffCreateRequest request = new TariffCreateRequest("Standard", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("2.0"), true);

        mockMvc.perform(post("/v1/tariffs")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_OPERARIO")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(createTariffUseCase, never()).execute(any());
    }

    @Test
    @DisplayName("OPERARIO: Debe denegar la actualizacion de tarifas (403)")
    void shouldDenyUpdateTariffForOperario() throws Exception {
        UUID id = UUID.randomUUID();
        TariffUpdateRequest request = new TariffUpdateRequest("Premium", new BigDecimal("0.08"), new BigDecimal("3.0"));

        mockMvc.perform(put("/v1/tariffs/{id}", id)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_OPERARIO")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(updateTariffUseCase, never()).execute(any(), any());
    }

    @Test
    @DisplayName("OPERARIO: Debe denegar la activacion de tarifas (403)")
    void shouldDenyActivateTariffForOperario() throws Exception {
        UUID id = UUID.randomUUID();
        TariffStatusRequest request = new TariffStatusRequest(true);

        mockMvc.perform(patch("/v1/tariffs/{id}/status", id)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_OPERARIO")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(activateTariffUseCase, never()).execute(any());
    }

    @Test
    @DisplayName("OPERARIO: Debe denegar la desactivacion de tarifas (403)")
    void shouldDenyDeactivateTariffForOperario() throws Exception {
        UUID id = UUID.randomUUID();
        TariffStatusRequest request = new TariffStatusRequest(false);

        mockMvc.perform(patch("/v1/tariffs/{id}/status", id)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_OPERARIO")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(deactivateTariffUseCase, never()).execute(any());
    }

    @Test
    @DisplayName("OPERARIO: Debe permitir el calculo de precio (200)")
    void shouldAllowCalculatePriceForOperario() throws Exception {
        TariffPriceRequest priceRequest =
                new TariffPriceRequest(VehicleType.CAR, 120);
        PriceTransferDTO priceDto = new PriceTransferDTO(new BigDecimal("8.00"));
        PriceResponse response = new PriceResponse(new BigDecimal("8.00"));

        when(priceCalculator.execute(VehicleType.CAR, 120)).thenReturn(priceDto);
        when(mapper.toResponse(priceDto)).thenReturn(response);

        mockMvc.perform(post("/v1/tariffs/calculate")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_OPERARIO")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(priceRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(8.00));

        verify(priceCalculator).execute(VehicleType.CAR, 120);
    }

    // =========================================================================
    // PRUEBAS PARA ROL: SERVICE (maquina-a-maquina, usado por stay-service)
    // =========================================================================

    @Test
    @DisplayName("SERVICE: Debe permitir consultar tarifas activas (200)")
    void shouldGetActiveTariffsForService() throws Exception {
        UUID id = UUID.randomUUID();
        TariffDTO dto = new TariffDTO(id, "Standard", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("2.0"), true);
        TariffResponse tariffResponse = new TariffResponse(id, "Standard", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("2.0"), true);

        when(getActiveTariffUseCase.execute(VehicleType.CAR)).thenReturn(List.of(dto));
        when(mapper.toResponseList(List.of(dto))).thenReturn(List.of(tariffResponse));

        mockMvc.perform(get("/v1/tariffs/active")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SERVICE")))
                .param("type", "CAR")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id.toString()));

        verify(getActiveTariffUseCase).execute(VehicleType.CAR);
    }

    @Test
    @DisplayName("SERVICE: Debe permitir calcular el precio (200)")
    void shouldAllowCalculatePriceForService() throws Exception {
        TariffPriceRequest priceRequest =
                new TariffPriceRequest(VehicleType.CAR, 120);
        PriceTransferDTO priceDto = new PriceTransferDTO(new BigDecimal("8.00"));
        PriceResponse response = new PriceResponse(new BigDecimal("8.00"));

        when(priceCalculator.execute(VehicleType.CAR, 120)).thenReturn(priceDto);
        when(mapper.toResponse(priceDto)).thenReturn(response);

        mockMvc.perform(post("/v1/tariffs/calculate")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SERVICE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(priceRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(8.00));

        verify(priceCalculator).execute(VehicleType.CAR, 120);
    }

    @Test
    @DisplayName("SERVICE: Debe denegar la consulta de todas las tarifas (403)")
    void shouldDenyGetAllTariffsForService() throws Exception {
        mockMvc.perform(get("/v1/tariffs")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SERVICE"))))
                .andExpect(status().isForbidden());

        verify(getAllTariffsUseCase, never()).execute();
    }

    @Test
    @DisplayName("SERVICE: Debe denegar la creacion de tarifas (403)")
    void shouldDenyCreateTariffForService() throws Exception {
        TariffCreateRequest request = new TariffCreateRequest("Standard", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("2.0"), true);

        mockMvc.perform(post("/v1/tariffs")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SERVICE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(createTariffUseCase, never()).execute(any());
    }

    @Test
    @DisplayName("SERVICE: Debe denegar la actualizacion de tarifas (403)")
    void shouldDenyUpdateTariffForService() throws Exception {
        UUID id = UUID.randomUUID();
        TariffUpdateRequest request = new TariffUpdateRequest("Premium", new BigDecimal("0.08"), new BigDecimal("3.0"));

        mockMvc.perform(put("/v1/tariffs/{id}", id)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SERVICE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(updateTariffUseCase, never()).execute(any(), any());
    }

    @Test
    @DisplayName("SERVICE: Debe denegar el cambio de estado de tarifas (403)")
    void shouldDenyChangeStatusForService() throws Exception {
        UUID id = UUID.randomUUID();
        TariffStatusRequest request = new TariffStatusRequest(true);

        mockMvc.perform(patch("/v1/tariffs/{id}/status", id)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SERVICE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(activateTariffUseCase, never()).execute(any());
    }

    // =========================================================================
    // PRUEBAS PARA ROL: USER
    // =========================================================================

    @Test
    @DisplayName("USER: Debe denegar la consulta de todas las tarifas (403)")
    void shouldDenyGetAllTariffsForUser() throws Exception {
        mockMvc.perform(get("/v1/tariffs")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isForbidden());

        verify(getAllTariffsUseCase, never()).execute();
    }

    @Test
    @DisplayName("USER: Debe denegar la consulta de tarifas activas (403)")
    void shouldDenyGetActiveTariffsForUser() throws Exception {
        mockMvc.perform(get("/v1/tariffs/active")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .param("type", "CAR"))
                .andExpect(status().isForbidden());

        verify(getActiveTariffUseCase, never()).execute(any());
    }

    @Test
    @DisplayName("USER: Debe denegar la consulta de una tarifa por ID (403)")
    void shouldDenyGetTariffByIdForUser() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(get("/v1/tariffs/{id}", id)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isForbidden());

        verify(getTariffUseCase, never()).execute(any());
    }

    @Test
    @DisplayName("USER: Debe denegar la creacion de tarifas (403)")
    void shouldDenyCreateTariffForUser() throws Exception {
        TariffCreateRequest request = new TariffCreateRequest("Standard", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("2.0"), true);

        mockMvc.perform(post("/v1/tariffs")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(createTariffUseCase, never()).execute(any());
    }

    @Test
    @DisplayName("USER: Debe denegar la actualizacion de tarifas (403)")
    void shouldDenyUpdateTariffForUser() throws Exception {
        UUID id = UUID.randomUUID();
        TariffUpdateRequest request = new TariffUpdateRequest("Premium", new BigDecimal("0.08"), new BigDecimal("3.0"));

        mockMvc.perform(put("/v1/tariffs/{id}", id)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(updateTariffUseCase, never()).execute(any(), any());
    }

    @Test
    @DisplayName("USER: Debe denegar la activacion de tarifas (403)")
    void shouldDenyActivateTariffForUser() throws Exception {
        UUID id = UUID.randomUUID();
        TariffStatusRequest request = new TariffStatusRequest(true);

        mockMvc.perform(patch("/v1/tariffs/{id}/status", id)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(activateTariffUseCase, never()).execute(any());
    }

    @Test
    @DisplayName("USER: Debe denegar la desactivacion de tarifas (403)")
    void shouldDenyDeactivateTariffForUser() throws Exception {
        UUID id = UUID.randomUUID();
        TariffStatusRequest request = new TariffStatusRequest(false);

        mockMvc.perform(patch("/v1/tariffs/{id}/status", id)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(deactivateTariffUseCase, never()).execute(any());
    }

    @Test
    @DisplayName("USER: Debe denegar el calculo de precio (403)")
    void shouldDenyCalculatePriceForUser() throws Exception {
        TariffPriceRequest priceRequest =
                new TariffPriceRequest(VehicleType.CAR, 120);

        mockMvc.perform(post("/v1/tariffs/calculate")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(priceRequest)))
                .andExpect(status().isForbidden());

        verify(priceCalculator, never()).execute(any(), anyInt());
    }

    // =========================================================================
    // PRUEBAS DE INFRAESTRUCTURA GENERAL Y NEGOCIO (400, 404, 401)
    // =========================================================================

    @Test
    @DisplayName("POST /v1/tariffs - Debe retornar 400 cuando el payload es invalido")
    void shouldReturn400OnCreateWithInvalidData() throws Exception {
        // Enviar request con precio negativo (violando @DecimalMin)
        TariffCreateRequest request = new TariffCreateRequest("Standard", VehicleType.CAR, new BigDecimal("-0.05"), new BigDecimal("2.0"), true);

        mockMvc.perform(post("/v1/tariffs")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
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
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
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
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
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
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_OPERARIO")))
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
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Debe rechazar con 401 una peticion sin token")
    void shouldReturn401WhenNoTokenProvided() throws Exception {
        mockMvc.perform(get("/v1/tariffs"))
                .andExpect(status().isUnauthorized());

        verify(getAllTariffsUseCase, never()).execute();
    }
}
