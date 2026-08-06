package com.tartis_recon_ai_parking.application.tariff.usecase;

import com.tartis_recon_ai_parking.application.tariff.dto.TariffChangedEvent;
import com.tartis_recon_ai_parking.application.tariff.dto.TariffDTO;
import com.tartis_recon_ai_parking.application.tariff.factory.TariffDTOFactory;
import com.tartis_recon_ai_parking.application.tariff.port.output.TariffEventPublisher;
import com.tartis_recon_ai_parking.application.tariff.port.output.TariffPersistence;
import com.tartis_recon_ai_parking.domain.tariff.Tariff;
import com.tartis_recon_ai_parking.domain.tariff.exception.TariffNotFoundException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActivateTariffUseCase {

    private static final Logger log = LoggerFactory.getLogger(ActivateTariffUseCase.class);

    private final TariffPersistence tariffPersistence;
    private final TariffEventPublisher eventPublisher;

    public ActivateTariffUseCase(TariffPersistence tariffPersistence, TariffEventPublisher eventPublisher) {
        this.tariffPersistence = tariffPersistence;
        this.eventPublisher = eventPublisher;
    }

    public TariffDTO execute(UUID id) {
        Tariff existing = tariffPersistence.findById(id)
                .orElseThrow(() -> new TariffNotFoundException(id));

        // IN-17: el bloqueo pesimista cierra la ventana TOCTOU y de paso nos da
        // la lista para los eventos; el apagado va en un update masivo que se
        // escribe antes de activar la nueva (ver TariffRepository).
        List<Tariff> activeTariffs = tariffPersistence.findActiveByTypeForUpdate(existing.getType());
        tariffPersistence.deactivateActiveByType(existing.getType(), existing.getUniqueId());

        for (Tariff activeTariff : activeTariffs) {
            if (!activeTariff.getUniqueId().equals(existing.getUniqueId())) {
                publishTariffChangedEventQuietly(activeTariff.deactivate());
            }
        }

        Tariff activated = existing.activate();
        Tariff saved = tariffPersistence.save(activated);
        publishTariffChangedEventQuietly(saved);
        return TariffDTOFactory.toDTO(saved);
    }

    private void publishTariffChangedEventQuietly(Tariff tariff) {
        try {
            eventPublisher.publish(TariffChangedEvent.of(tariff, Instant.now()));
        } catch (RuntimeException e) {
            log.error("No se pudo publicar el evento de cambio de tarifa para el ID: {}", tariff.getUniqueId(), e);
        }
    }
}