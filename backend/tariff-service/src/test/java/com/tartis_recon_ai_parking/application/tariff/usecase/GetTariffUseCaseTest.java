package com.tartis_recon_ai_parking.application.tariff.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetTariffUseCaseTest {

    @Mock
    private TariffPersistence tariffPersistence;

    @InjectMocks
    private GetTariffUseCase getTariffUseCase;

    @Test
    @DisplayName("Debe retornar una tarifa por su ID")
    void shouldGetTariffById() {
        // QUE HACE:
        // Simula la obtención de una tarifa por su ID
        UUID id = UUID.randomUUID();
        Tariff tariff = Tariff.reconstruct(id, "Standard", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("2.0"), true);
        
        when(tariffPersistence.findById(id)).thenReturn(Optional.of(tariff));

        TariffDTO result = getTariffUseCase.execute(id);

        // QUE DEBERIA HACER:
        // Verificar que devuelve la tarifa correspondiente

        assertNotNull(result);
        assertEquals(id, result.getUniqueId());
        
        verify(tariffPersistence).findById(id);
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
        assertThrows(TariffNotFoundException.class, () -> getTariffUseCase.execute(id));
    }
}
