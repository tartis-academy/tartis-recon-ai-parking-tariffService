package com.tartis_recon_ai_parking.application.tariff.usecase;

import com.tartis_recon_ai_parking.application.tariff.dto.TariffChangedEvent;
import com.tartis_recon_ai_parking.application.tariff.dto.TariffCreateDTO;
import com.tartis_recon_ai_parking.application.tariff.dto.TariffDTO;
import com.tartis_recon_ai_parking.application.tariff.port.output.TariffEventPublisher;
import com.tartis_recon_ai_parking.application.tariff.port.output.TariffPersistence;
import com.tartis_recon_ai_parking.domain.tariff.Tariff;
import com.tartis_recon_ai_parking.domain.tariff.VehicleType;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import com.tartis_recon_ai_parking.domain.tariff.exception.InvalidTariffException;
import com.tartis_recon_ai_parking.domain.tariff.exception.TariffAlreadyExistsException;

@ExtendWith(MockitoExtension.class)
class CreateTariffUseCaseTest {

    @Mock
    private TariffPersistence tariffPersistence;

    @Mock
    private TariffEventPublisher eventPublisher;

    @InjectMocks
    private CreateTariffUseCase createTariffUseCase;

    @Test
    @DisplayName("Debe crear una tarifa inactiva exitosamente sin swap")
    void shouldCreateInactiveTariff() {
        TariffCreateDTO createDto = new TariffCreateDTO("Standard", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("2.0"), false);
        
        UUID expectedId = UUID.randomUUID();
        Tariff savedTariff = Tariff.reconstruct(expectedId, "Standard", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("2.0"), false);

        when(tariffPersistence.save(any(Tariff.class))).thenReturn(savedTariff);

        TariffDTO result = createTariffUseCase.execute(createDto);

        assertNotNull(result);
        assertEquals(expectedId, result.getUniqueId());
        
        verify(tariffPersistence).save(any(Tariff.class));
        verify(tariffPersistence, never()).findActiveByType(any());

        ArgumentCaptor<TariffChangedEvent> eventCaptor = ArgumentCaptor.forClass(TariffChangedEvent.class);
        verify(eventPublisher).publish(eventCaptor.capture());
        TariffChangedEvent published = eventCaptor.getValue();

        assertEquals("TariffChangedEvent", published.type());
        assertEquals(expectedId, published.data().tariffId());
        assertEquals("Standard", published.data().name());
        assertEquals(VehicleType.CAR, published.data().vehicleType());
    }

    @Test
    @DisplayName("Debe crear una tarifa activa y hacer swap desactivando la anterior")
    void shouldCreateActiveTariffAndSwap() {
        TariffCreateDTO createDto = new TariffCreateDTO("Standard", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("2.0"), true);
        
        UUID expectedId = UUID.randomUUID();
        Tariff savedTariff = Tariff.reconstruct(expectedId, "Standard", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("2.0"), true);
        Tariff otherActiveTariff = Tariff.reconstruct(UUID.randomUUID(), "Other", VehicleType.CAR, new BigDecimal("0.10"), new BigDecimal("3.0"), true);

        when(tariffPersistence.findActiveByType(VehicleType.CAR)).thenReturn(List.of(otherActiveTariff));
        when(tariffPersistence.save(any(Tariff.class))).thenReturn(savedTariff);

        TariffDTO result = createTariffUseCase.execute(createDto);

        assertNotNull(result);
        assertEquals(expectedId, result.getUniqueId());
        
        // Debe guardar la tarifa anterior desactivada y la nueva activada
        ArgumentCaptor<Tariff> captor = ArgumentCaptor.forClass(Tariff.class);
        verify(tariffPersistence, times(2)).save(captor.capture());
        
        List<Tariff> savedTariffs = captor.getAllValues();
        boolean hasDeactivated = savedTariffs.stream().anyMatch(t -> !t.isActive() && t.getUniqueId().equals(otherActiveTariff.getUniqueId()));
        org.junit.jupiter.api.Assertions.assertTrue(hasDeactivated, "Deberia haberse guardado una version inactiva de la tarifa anterior");

        ArgumentCaptor<TariffChangedEvent> eventCaptor = ArgumentCaptor.forClass(TariffChangedEvent.class);
        verify(eventPublisher).publish(eventCaptor.capture());
    }

    @Test
    @DisplayName("Si publicar TariffChangedEvent falla, se registra pero no se propaga: la tarifa ya se creo")
    void shouldSwallowEventPublishFailure() {
        TariffCreateDTO createDto = new TariffCreateDTO("Standard", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("2.0"), false);
        UUID expectedId = UUID.randomUUID();
        Tariff savedTariff = Tariff.reconstruct(expectedId, "Standard", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("2.0"), false);

        when(tariffPersistence.save(any(Tariff.class))).thenReturn(savedTariff);
        doThrow(new IllegalStateException("rabbitmq no disponible"))
                .when(eventPublisher).publish(any(TariffChangedEvent.class));

        TariffDTO result = assertDoesNotThrow(() -> createTariffUseCase.execute(createDto));

        assertEquals(expectedId, result.getUniqueId());
        verify(eventPublisher).publish(any(TariffChangedEvent.class));
    }

    @Test
    @DisplayName("Debe lanzar InvalidTariffException al crear una tarifa con datos invalidos")
    void shouldThrowExceptionWhenCreatingWithInvalidData() {
        TariffCreateDTO createDto = new TariffCreateDTO("Standard", VehicleType.CAR, new BigDecimal("-0.05"), new BigDecimal("2.0"), true);
        
        assertThrows(InvalidTariffException.class, () -> createTariffUseCase.execute(createDto));
        
        verify(tariffPersistence, never()).save(any(Tariff.class));
    }

    @Test
    @DisplayName("Debe lanzar TariffAlreadyExistsException si el nombre ya existe, sin llegar a guardar")
    void shouldThrowExceptionWhenNameAlreadyExists() {
        TariffCreateDTO createDto = new TariffCreateDTO("Standard", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("2.0"), true);

        when(tariffPersistence.existsByName("Standard")).thenReturn(true);

        assertThrows(TariffAlreadyExistsException.class, () -> createTariffUseCase.execute(createDto));

        verify(tariffPersistence, never()).save(any(Tariff.class));
    }
}
