package com.tartis_recon_ai_parking.infrastructure.tariff.adapter.input.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.tartis_recon_ai_parking.application.tariff.dto.TariffCreateDTO;
import com.tartis_recon_ai_parking.application.tariff.dto.TariffDTO;
import com.tartis_recon_ai_parking.application.tariff.dto.TariffUpdateDTO;
import com.tartis_recon_ai_parking.domain.tariff.VehicleType;
import com.tartis_recon_ai_parking.infrastructure.tariff.adapter.input.rest.dto.request.TariffCreateRequest;
import com.tartis_recon_ai_parking.infrastructure.tariff.adapter.input.rest.dto.request.TariffUpdateRequest;
import com.tartis_recon_ai_parking.infrastructure.tariff.adapter.input.rest.dto.response.TariffResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;


class TariffRestMapperTest {

    private final TariffRestMapper mapper;

    TariffRestMapperTest() throws Exception {
        Class<?> implClass = Class.forName(
            "com.tartis_recon_ai_parking.infrastructure.tariff.adapter.input.rest.TariffRestMapperImpl");
        Constructor<?> constructor = implClass.getDeclaredConstructor();
        mapper = (TariffRestMapper) constructor.newInstance();
    }

    @Test
    @DisplayName("Debe mapear de TariffCreateRequest a TariffCreateDTO")
    void shouldMapCreateRequestToDTO() {
        TariffCreateRequest request = new TariffCreateRequest("Standard", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("2.0"), true);
        
        TariffCreateDTO dto = mapper.toCreateDTO(request);
        
        assertNotNull(dto);
        assertEquals(request.getName(), dto.getName());
        assertEquals(request.getType(), dto.getType());
        assertEquals(request.getPricePerMinute(), dto.getPricePerMinute());
        assertEquals(request.getBasePrice(), dto.getBasePrice());
        assertEquals(request.getActive(), dto.isActive());
    }

    @Test
    @DisplayName("Debe mapear de TariffCreateRequest a TariffCreateDTO cuando active es nulo")
    void shouldMapCreateRequestToDTOWhenActiveIsNull() {
        TariffCreateRequest request = new TariffCreateRequest("Standard", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("2.0"), null);
        
        TariffCreateDTO dto = mapper.toCreateDTO(request);
        
        assertNotNull(dto);
        assertEquals(false, dto.isActive());
    }

    @Test
    @DisplayName("Debe mapear de TariffUpdateRequest a TariffUpdateDTO")
    void shouldMapUpdateRequestToDTO() {
        TariffUpdateRequest request = new TariffUpdateRequest("Premium", new BigDecimal("0.08"), new BigDecimal("3.0"));
        
        TariffUpdateDTO dto = mapper.toUpdateDTO(request);
        
        assertNotNull(dto);
        assertEquals(request.getName(), dto.getName());
        assertEquals(request.getPricePerMinute(), dto.getPricePerMinute());
        assertEquals(request.getBasePrice(), dto.getBasePrice());
    }

    @Test
    @DisplayName("Debe mapear de TariffDTO a TariffResponse")
    void shouldMapDTOToResponse() {
        UUID id = UUID.randomUUID();
        TariffDTO dto = new TariffDTO(id, "Standard", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("2.0"), true);
        
        TariffResponse response = mapper.toResponse(dto);
        
        assertNotNull(response);
        assertEquals(dto.getUniqueId(), response.getId());
        assertEquals(dto.getName(), response.getName());
        assertEquals(dto.getType(), response.getType());
        assertEquals(dto.getPricePerMinute(), response.getPricePerMinute());
        assertEquals(dto.getBasePrice(), response.getBasePrice());
        assertEquals(dto.isActive(), response.isActive());
    }

    @Test
    @DisplayName("Debe mapear de lista de TariffDTO a lista de TariffResponse")
    void shouldMapDTOListToResponseList() {
        UUID id = UUID.randomUUID();
        TariffDTO dto = new TariffDTO(id, "Standard", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("2.0"), true);
        
        List<TariffResponse> responses = mapper.toResponseList(List.of(dto));
        
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(dto.getUniqueId(), responses.get(0).getId());
    }

    @Test
    @DisplayName("Debe retornar null al mapear lista de TariffDTO nula")
    void shouldReturnNullWhenDTOListIsNull() {
        assertNull(mapper.toResponseList(null));
    }
}