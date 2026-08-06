package com.tartis_recon_ai_parking.application.tariff.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tartis_recon_ai_parking.application.tariff.dto.PriceTransferDTO;
import com.tartis_recon_ai_parking.application.tariff.port.output.TariffPersistence;
import com.tartis_recon_ai_parking.domain.tariff.Tariff;
import com.tartis_recon_ai_parking.domain.tariff.VehicleType;
import com.tartis_recon_ai_parking.domain.tariff.exception.TariffNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class PriceCalculateUseCaseTest {

    @Mock
    private TariffPersistence tariffPersistence;

    @InjectMocks
    private PriceCalculateUseCase priceCalculateUseCase;

    @Test
    @DisplayName("Debe calcular el precio total usando la tarifa por ID")
    void shouldCalculatePriceUsingTariffId() {
        java.util.UUID tariffId = java.util.UUID.randomUUID();
        Tariff tariff = Tariff.reconstruct(
                tariffId,
                "Standard",
                VehicleType.CAR,
                new BigDecimal("0.05"),
                new BigDecimal("2.0"),
                true
        );

        when(tariffPersistence.findById(tariffId)).thenReturn(java.util.Optional.of(tariff));

        PriceTransferDTO result = priceCalculateUseCase.execute(tariffId, 120);

        assertNotNull(result);
        assertEquals(new BigDecimal("8.00"), result.price());

        verify(tariffPersistence).findById(tariffId);
    }

    @Test
    @DisplayName("Debe lanzar excepcion cuando no existe la tarifa por ID")
    void shouldThrowExceptionWhenNoTariffExists() {
        java.util.UUID tariffId = java.util.UUID.randomUUID();
        when(tariffPersistence.findById(tariffId)).thenReturn(java.util.Optional.empty());

        assertThrows(TariffNotFoundException.class, () -> priceCalculateUseCase.execute(tariffId, 60));
    }
}
