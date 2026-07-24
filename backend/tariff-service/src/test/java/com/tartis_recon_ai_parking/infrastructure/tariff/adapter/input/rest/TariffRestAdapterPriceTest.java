package com.tartis_recon_ai_parking.infrastructure.tariff.adapter.input.rest;

import com.tartis_recon_ai_parking.application.tariff.dto.PriceTransferDTO;
import com.tartis_recon_ai_parking.application.tariff.usecase.PriceCalculateUseCase;
import com.tartis_recon_ai_parking.domain.tariff.VehicleType;
import com.tartis_recon_ai_parking.infrastructure.tariff.adapter.input.rest.dto.request.TariffPriceRequest;
import com.tartis_recon_ai_parking.infrastructure.tariff.adapter.input.rest.dto.response.PriceResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TariffRestAdapterPriceTest {

    @Mock
    private PriceCalculateUseCase priceCalculateUseCase;

    @Mock
    private TariffRestMapper mapper;

    @InjectMocks
    private TariffRestAdapter tariffRestAdapter;

    @Test
    @DisplayName("Debe calcular el precio desde el endpoint del adapter")
    void shouldCalculatePriceThroughRestEndpoint() {
        TariffPriceRequest request = new TariffPriceRequest(VehicleType.CAR, 120);
        PriceTransferDTO priceDto = new PriceTransferDTO(new BigDecimal("8.00"));
        PriceResponse response = new PriceResponse(new BigDecimal("8.00"));

        when(priceCalculateUseCase.execute(VehicleType.CAR, 120)).thenReturn(priceDto);
        when(mapper.toResponse(priceDto)).thenReturn(response);

        var result = tariffRestAdapter.calculatePrice(request);

        assertNotNull(result);
        assertEquals(200, result.getStatusCode().value());
        assertEquals(new BigDecimal("8.00"), result.getBody().getPrice());

        verify(priceCalculateUseCase).execute(VehicleType.CAR, 120);
    }
}
