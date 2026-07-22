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
class GetAllTariffsUseCaseTest {

    @Mock
    private TariffPersistence tariffPersistence;

    @InjectMocks
    private GetAllTariffsUseCase getAllTariffsUseCase;

    @Test
    @DisplayName("Debe retornar todas las tarifas")
    void shouldGetAllTariffs() {
        // QUE HACE:
        // Simula la obtención de todas las tarifas de la BD
        Tariff tariff = Tariff.reconstruct(UUID.randomUUID(), "Standard", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("2.0"), true);
        
        when(tariffPersistence.findAll()).thenReturn(List.of(tariff));

        List<TariffDTO> result = getAllTariffsUseCase.execute();

        // QUE DEBERIA HACER:
        // Verificar que devuelve la lista con todas las tarifas

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        
        verify(tariffPersistence).findAll();
    }
    @Test
    @DisplayName("Debe retornar lista vacía si no hay tarifas")
    void shouldReturnEmptyListWhenNoTariffsExist() {
        // QUE HACE:
        // Simula que la base de datos no tiene tarifas
        when(tariffPersistence.findAll()).thenReturn(List.of());

        List<TariffDTO> result = getAllTariffsUseCase.execute();

        // QUE DEBERIA HACER:
        // Verificar que devuelve una lista vacía
        assertEquals(0, result.size());
        verify(tariffPersistence).findAll();
    }
}
