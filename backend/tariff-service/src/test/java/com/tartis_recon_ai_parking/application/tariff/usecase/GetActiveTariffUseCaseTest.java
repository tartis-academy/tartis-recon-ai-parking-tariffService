package com.tartis_recon_ai_parking.application.tariff.usecase;

import com.tartis_recon_ai_parking.application.tariff.dto.TariffDTO;
import com.tartis_recon_ai_parking.application.tariff.port.output.TariffPersistence;
import com.tartis_recon_ai_parking.domain.tariff.Tariff;
import com.tartis_recon_ai_parking.domain.tariff.VehicleType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetActiveTariffUseCaseTest {

    @Mock
    private TariffPersistence tariffPersistence;

    @InjectMocks
    private GetActiveTariffUseCase getActiveTariffUseCase;

    @Test
    @DisplayName("Debe retornar las tarifas activas por tipo de vehiculo")
    void shouldGetActiveTariffs() {
        Tariff tariff = Tariff.reconstruct(UUID.randomUUID(), "Standard", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("2.0"), true);
        
        when(tariffPersistence.findActiveByType(VehicleType.CAR)).thenReturn(List.of(tariff));

        List<TariffDTO> result = getActiveTariffUseCase.execute(VehicleType.CAR);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(VehicleType.CAR, result.get(0).getType());
        
        verify(tariffPersistence).findActiveByType(VehicleType.CAR);
    }
}
