package com.tartis_recon_ai_parking.application.tariff.usecase;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tartis_recon_ai_parking.application.tariff.dto.TariffChangedEvent;
import com.tartis_recon_ai_parking.application.tariff.dto.TariffDTO;
import com.tartis_recon_ai_parking.application.tariff.port.output.TariffEventPublisher;
import com.tartis_recon_ai_parking.application.tariff.port.output.TariffPersistence;
import com.tartis_recon_ai_parking.domain.tariff.Tariff;
import com.tartis_recon_ai_parking.domain.tariff.VehicleType;
import com.tartis_recon_ai_parking.domain.tariff.exception.TariffNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class ActivateTariffUseCaseTest {

    @Mock
    private TariffPersistence tariffPersistence;

    @Mock
    private TariffEventPublisher eventPublisher;

    @InjectMocks
    private ActivateTariffUseCase activateTariffUseCase;

    @Test
    @DisplayName("Debe activar una tarifa existente y desactivar las activas del mismo tipo")
    void shouldActivateTariffAndSwap() {
        UUID id = UUID.randomUUID();
        Tariff existingTariff = Tariff.reconstruct(id, "Standard", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("2.0"), false);
        Tariff otherActiveTariff = Tariff.reconstruct(UUID.randomUUID(), "Other", VehicleType.CAR, new BigDecimal("0.10"), new BigDecimal("3.0"), true);
        when(tariffPersistence.findById(id)).thenReturn(Optional.of(existingTariff));
        when(tariffPersistence.findActiveByType(VehicleType.CAR)).thenReturn(List.of(otherActiveTariff));
        when(tariffPersistence.save(any(Tariff.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TariffDTO result = activateTariffUseCase.execute(id);

        assertNotNull(result);
        assertTrue(result.isActive());
        // Debe guardar la tarifa antigua desactivada y la nueva activada
        ArgumentCaptor<Tariff> captor = ArgumentCaptor.forClass(Tariff.class);
        verify(tariffPersistence, times(2)).save(captor.capture());
        
        List<Tariff> savedTariffs = captor.getAllValues();
        boolean hasDeactivated = savedTariffs.stream().anyMatch(t -> !t.isActive() && t.getUniqueId().equals(otherActiveTariff.getUniqueId()));
        assertTrue(hasDeactivated, "Deberia haberse guardado una version inactiva de la tarifa anterior");

        ArgumentCaptor<TariffChangedEvent> eventCaptor = ArgumentCaptor.forClass(TariffChangedEvent.class);
        verify(eventPublisher).publish(eventCaptor.capture());
        TariffChangedEvent published = eventCaptor.getValue();

        assertEquals("TariffChangedEvent", published.type());
        assertEquals(id, published.data().tariffId());
        assertEquals(VehicleType.CAR, published.data().vehicleType());
        assertTrue(published.data().active());
    }

    @Test
    @DisplayName("Debe lanzar excepcion si la tarifa no existe")
    void shouldThrowExceptionWhenTariffNotFound() {
        UUID id = UUID.randomUUID();
        when(tariffPersistence.findById(id)).thenReturn(Optional.empty());

        assertThrows(TariffNotFoundException.class, () -> activateTariffUseCase.execute(id));
    }

    @Test
    @DisplayName("Si publicar TariffChangedEvent falla, se registra pero no se propaga: la tarifa ya se activo")
    void shouldSwallowEventPublishFailure() {
        UUID id = UUID.randomUUID();
        Tariff existingTariff = Tariff.reconstruct(id, "Standard", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("2.0"), false);

        when(tariffPersistence.findById(id)).thenReturn(Optional.of(existingTariff));
        when(tariffPersistence.save(any(Tariff.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doThrow(new IllegalStateException("rabbitmq no disponible"))
                .when(eventPublisher).publish(any(TariffChangedEvent.class));

        TariffDTO result = assertDoesNotThrow(() -> activateTariffUseCase.execute(id));

        assertTrue(result.isActive());
        verify(eventPublisher).publish(any(TariffChangedEvent.class));
    }
}
