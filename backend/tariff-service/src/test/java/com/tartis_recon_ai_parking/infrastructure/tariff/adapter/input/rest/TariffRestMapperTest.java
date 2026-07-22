package com.tartis_recon_ai_parking.infrastructure.tariff.adapter.input.rest;

import com.tartis_recon_ai_parking.application.tariff.dto.TariffCreateDTO;
import com.tartis_recon_ai_parking.application.tariff.dto.TariffDTO;
import com.tartis_recon_ai_parking.application.tariff.dto.TariffUpdateDTO;
import com.tartis_recon_ai_parking.domain.tariff.VehicleType;
import com.tartis_recon_ai_parking.infrastructure.tariff.adapter.input.rest.dto.request.TariffCreateRequest;
import com.tartis_recon_ai_parking.infrastructure.tariff.adapter.input.rest.dto.request.TariffUpdateRequest;
import com.tartis_recon_ai_parking.infrastructure.tariff.adapter.input.rest.dto.response.TariffResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TariffRestMapperTest {

    private final TariffRestMapper mapper = Mappers.getMapper(TariffRestMapper.class);

    @Test
    @DisplayName("Debe mapear de TariffCreateRequest a TariffCreateDTO")
    void shouldMapCreateRequestToDTO() {
        // QUE HACE:
        // Configura un request para crear tarifa
        TariffCreateRequest request = new TariffCreateRequest("Standard", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("2.0"), true);
        
        TariffCreateDTO dto = mapper.toCreateDTO(request);
        
        // QUE DEBERIA HACER:
        // Verificar que el mapper mapea todos los campos correctamente
        
        assertNotNull(dto);
        assertEquals(request.getName(), dto.getName());
        assertEquals(request.getType(), dto.getType());
        assertEquals(request.getPricePerMinute(), dto.getPricePerMinute());
        assertEquals(request.getBasePrice(), dto.getBasePrice());
        assertEquals(request.getActive(), dto.isActive());
    }

    @Test
    @DisplayName("Debe retornar null al mapear TariffCreateRequest nulo")
    void shouldReturnNullWhenCreateRequestIsNull() {
        // QUE HACE:
        // Intenta mapear un objeto null
        // QUE DEBERIA HACER:
        // Verificar que devuelve null
        assertNull(mapper.toCreateDTO(null));
    }

    @Test
    @DisplayName("Debe mapear de TariffUpdateRequest a TariffUpdateDTO")
    void shouldMapUpdateRequestToDTO() {
        // QUE HACE:
        // Configura un request para actualizar tarifa
        TariffUpdateRequest request = new TariffUpdateRequest("Premium", new BigDecimal("0.08"), new BigDecimal("3.0"));
        
        TariffUpdateDTO dto = mapper.toUpdateDTO(request);
        
        // QUE DEBERIA HACER:
        // Verificar que el mapper transfiere los datos correspondientes
        
        assertNotNull(dto);
        assertEquals(request.getName(), dto.getName());
        assertEquals(request.getPricePerMinute(), dto.getPricePerMinute());
        assertEquals(request.getBasePrice(), dto.getBasePrice());
    }

    @Test
    @DisplayName("Debe retornar null al mapear TariffUpdateRequest nulo")
    void shouldReturnNullWhenUpdateRequestIsNull() {
        // QUE HACE:
        // Intenta mapear un objeto null
        // QUE DEBERIA HACER:
        // Verificar que devuelve null
        assertNull(mapper.toUpdateDTO(null));
    }

    @Test
    @DisplayName("Debe mapear de TariffDTO a TariffResponse")
    void shouldMapDTOToResponse() {
        // QUE HACE:
        // Configura un DTO de tarifa
        UUID id = UUID.randomUUID();
        TariffDTO dto = new TariffDTO(id, "Standard", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("2.0"), true);
        
        TariffResponse response = mapper.toResponse(dto);
        
        // QUE DEBERIA HACER:
        // Verificar que el mapeo genera el response con los datos correctos
        
        assertNotNull(response);
        assertEquals(dto.getUniqueId(), response.getId());
        assertEquals(dto.getName(), response.getName());
        assertEquals(dto.getType(), response.getType());
        assertEquals(dto.getPricePerMinute(), response.getPricePerMinute());
        assertEquals(dto.getBasePrice(), response.getBasePrice());
        assertEquals(dto.isActive(), response.isActive());
    }

    @Test
    @DisplayName("Debe retornar null al mapear TariffDTO nulo")
    void shouldReturnNullWhenDTOIsNull() {
        // QUE HACE:
        // Intenta mapear un dto nulo
        // QUE DEBERIA HACER:
        // Verificar que devuelve null
        assertNull(mapper.toResponse(null));
    }

    @Test
    @DisplayName("Debe mapear de lista de TariffDTO a lista de TariffResponse")
    void shouldMapDTOListToResponseList() {
        // QUE HACE:
        // Pasa una lista con un DTO
        UUID id = UUID.randomUUID();
        TariffDTO dto = new TariffDTO(id, "Standard", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("2.0"), true);
        
        List<TariffResponse> responses = mapper.toResponseList(List.of(dto));
        
        // QUE DEBERIA HACER:
        // Verificar que la lista devuelta tiene el mismo tamaño y elementos mapeados
        
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(dto.getUniqueId(), responses.get(0).getId());
    }

    @Test
    @DisplayName("Debe retornar null al mapear lista de TariffDTO nula")
    void shouldReturnNullWhenDTOListIsNull() {
        // QUE HACE:
        // Pasa una lista nula
        // QUE DEBERIA HACER:
        // Verificar que devuelve null
        assertNull(mapper.toResponseList(null));
    }
}
