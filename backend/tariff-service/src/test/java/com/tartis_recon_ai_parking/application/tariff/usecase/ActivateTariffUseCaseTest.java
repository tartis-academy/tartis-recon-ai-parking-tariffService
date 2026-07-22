package com.tartis_recon_ai_parking.application.tariff.usecase;

import com.tartis_recon_ai_parking.application.tariff.dto.TariffDTO;
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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivateTariffUseCaseTest {

    @Mock
    private TariffPersistence tariffPersistence;

    @InjectMocks
    private ActivateTariffUseCase activateTariffUseCase;

    @Test
    @DisplayName("Debe activar una tarifa existente")
    void shouldActivateTariff() {
        // QUE HACE:
        // Configura un UUID y simula la existencia de una tarifa inactiva
        UUID id = UUID.randomUUID();
        Tariff existingTariff = Tariff.reconstruct(id, "Standard", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("2.0"), false);
        
        when(tariffPersistence.findById(id)).thenReturn(Optional.of(existingTariff));
        when(tariffPersistence.save(any(Tariff.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TariffDTO result = activateTariffUseCase.execute(id);

        // QUE DEBERIA HACER:
        // Verificar que la tarifa devuelta está activada y se guardó en BD

        assertNotNull(result);
        assertTrue(result.isActive());
        verify(tariffPersistence).save(any(Tariff.class));
    }

    @Test
    @DisplayName("Debe lanzar excepcion si la tarifa no existe")
    void shouldThrowExceptionWhenTariffNotFound() {
        // QUE HACE:
        // Simula que la tarifa no existe
        UUID id = UUID.randomUUID();
        when(tariffPersistence.findById(id)).thenReturn(Optional.empty());

        // QUE DEBERIA HACER:
        // Verificar que se lanza TariffNotFoundException
        assertThrows(TariffNotFoundException.class, () -> activateTariffUseCase.execute(id));
    }
}
