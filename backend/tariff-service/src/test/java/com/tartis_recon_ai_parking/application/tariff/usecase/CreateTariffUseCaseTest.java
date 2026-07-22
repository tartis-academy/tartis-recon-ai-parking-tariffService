package com.tartis_recon_ai_parking.application.tariff.usecase;

import com.tartis_recon_ai_parking.application.tariff.dto.TariffCreateDTO;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateTariffUseCaseTest {

    @Mock
    private TariffPersistence tariffPersistence;

    @InjectMocks
    private CreateTariffUseCase createTariffUseCase;

    @Test
    @DisplayName("Debe crear una tarifa exitosamente")
    void shouldCreateTariff() {
        // QUE HACE:
        // Crea un DTO con datos de nueva tarifa y simula el guardado exitoso
        TariffCreateDTO createDto = new TariffCreateDTO("Standard", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("2.0"), true);
        
        UUID expectedId = UUID.randomUUID();
        Tariff savedTariff = Tariff.reconstruct(expectedId, "Standard", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("2.0"), true);

        when(tariffPersistence.save(any(Tariff.class))).thenReturn(savedTariff);

        TariffDTO result = createTariffUseCase.execute(createDto);

        // QUE DEBERIA HACER:
        // Verificar que la tarifa devuelta contiene el UUID y datos persistidos

        assertNotNull(result);
        assertEquals(expectedId, result.getUniqueId());
        assertEquals("Standard", result.getName());
        assertEquals(VehicleType.CAR, result.getType());
        
        verify(tariffPersistence).save(any(Tariff.class));
    }
}
