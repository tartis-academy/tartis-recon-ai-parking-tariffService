package com.tartis_recon_ai_parking.application.tariff.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.tartis_recon_ai_parking.application.tariff.dto.TariffDTO;
import com.tartis_recon_ai_parking.application.tariff.dto.TariffUpdateDTO;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateTariffUseCaseTest {

    @Mock
    private TariffPersistence tariffPersistence;

    @InjectMocks
    private UpdateTariffUseCase updateTariffUseCase;

    @Test
    @DisplayName("Debe actualizar una tarifa exitosamente")
    void shouldUpdateTariff() {
        // QUE HACE:
        // Simula la existencia de una tarifa y su actualización con nuevos datos
        UUID id = UUID.randomUUID();
        Tariff existingTariff = Tariff.reconstruct(id, "Standard", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("2.0"), true);
        TariffUpdateDTO updateDto = new TariffUpdateDTO("Premium", new BigDecimal("0.08"), new BigDecimal("3.0"));
        
        when(tariffPersistence.findById(id)).thenReturn(Optional.of(existingTariff));
        when(tariffPersistence.save(any(Tariff.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TariffDTO result = updateTariffUseCase.execute(id, updateDto);

        // QUE DEBERIA HACER:
        // Verificar que la tarifa se actualiza correctamente y se persiste

        assertNotNull(result);
        assertEquals("Premium", result.getName());
        assertEquals(new BigDecimal("0.08"), result.getPricePerMinute());
        assertEquals(new BigDecimal("3.0"), result.getBasePrice());
        
        verify(tariffPersistence).save(any(Tariff.class));
    }

    @Test
    @DisplayName("Debe lanzar excepcion si la tarifa a actualizar no existe")
    void shouldThrowExceptionWhenTariffNotFound() {
        // QUE HACE:
        // Simula que la tarifa a actualizar no existe
        UUID id = UUID.randomUUID();
        TariffUpdateDTO updateDto = new TariffUpdateDTO("Premium", new BigDecimal("0.08"), new BigDecimal("3.0"));
        
        when(tariffPersistence.findById(id)).thenReturn(Optional.empty());

        // QUE DEBERIA HACER:
        // Verificar que se lanza TariffNotFoundException
        assertThrows(TariffNotFoundException.class, () -> updateTariffUseCase.execute(id, updateDto));
    }
}
