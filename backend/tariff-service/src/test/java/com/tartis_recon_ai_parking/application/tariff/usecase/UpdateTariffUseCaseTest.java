package com.tartis_recon_ai_parking.application.tariff.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.tartis_recon_ai_parking.application.tariff.dto.TariffDTO;
import com.tartis_recon_ai_parking.application.tariff.dto.TariffUpdateDTO;
import com.tartis_recon_ai_parking.application.tariff.port.output.TariffPersistence;
import com.tartis_recon_ai_parking.domain.tariff.Tariff;
import com.tartis_recon_ai_parking.domain.tariff.VehicleType;
import com.tartis_recon_ai_parking.domain.tariff.exception.TariffAlreadyExistsException;
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
import static org.mockito.Mockito.never;
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
        UUID id = UUID.randomUUID();
        Tariff existingTariff = Tariff.reconstruct(id, "Standard", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("2.0"), true);
        TariffUpdateDTO updateDto = new TariffUpdateDTO("Premium", new BigDecimal("0.08"), new BigDecimal("3.0"));
        
        when(tariffPersistence.findById(id)).thenReturn(Optional.of(existingTariff));
        when(tariffPersistence.save(any(Tariff.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TariffDTO result = updateTariffUseCase.execute(id, updateDto);

        assertNotNull(result);
        assertEquals("Premium", result.getName());
        assertEquals(new BigDecimal("0.08"), result.getPricePerMinute());
        assertEquals(new BigDecimal("3.0"), result.getBasePrice());
        
        verify(tariffPersistence).save(any(Tariff.class));
    }

    @Test
    @DisplayName("Debe lanzar excepcion si la tarifa a actualizar no existe")
    void shouldThrowExceptionWhenTariffNotFound() {
        UUID id = UUID.randomUUID();
        TariffUpdateDTO updateDto = new TariffUpdateDTO("Premium", new BigDecimal("0.08"), new BigDecimal("3.0"));
        
        when(tariffPersistence.findById(id)).thenReturn(Optional.empty());

        assertThrows(TariffNotFoundException.class, () -> updateTariffUseCase.execute(id, updateDto));
    }

    @Test
    @DisplayName("Si el nombre no cambia, no debe comprobar duplicados aunque exista otro con ese nombre")
    void shouldNotCheckDuplicateWhenNameUnchanged() {
        UUID id = UUID.randomUUID();
        Tariff existingTariff = Tariff.reconstruct(id, "Standard", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("2.0"), true);
        TariffUpdateDTO updateDto = new TariffUpdateDTO("Standard", new BigDecimal("0.08"), new BigDecimal("3.0"));

        when(tariffPersistence.findById(id)).thenReturn(Optional.of(existingTariff));
        when(tariffPersistence.save(any(Tariff.class))).thenAnswer(invocation -> invocation.getArgument(0));

        updateTariffUseCase.execute(id, updateDto);

        verify(tariffPersistence, never()).existsByName(any());
    }

    @Test
    @DisplayName("Si el nombre cambia a uno ya existente, debe lanzar TariffAlreadyExistsException")
    void shouldThrowExceptionWhenRenamingToExistingName() {
        UUID id = UUID.randomUUID();
        Tariff existingTariff = Tariff.reconstruct(id, "Standard", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("2.0"), true);
        TariffUpdateDTO updateDto = new TariffUpdateDTO("Premium", new BigDecimal("0.08"), new BigDecimal("3.0"));

        when(tariffPersistence.findById(id)).thenReturn(Optional.of(existingTariff));
        when(tariffPersistence.existsByName("Premium")).thenReturn(true);

        assertThrows(TariffAlreadyExistsException.class, () -> updateTariffUseCase.execute(id, updateDto));

        verify(tariffPersistence, never()).save(any(Tariff.class));
    }
}
