package com.tartis_recon_ai_parking.application.tariff.usecase;

import com.tartis_recon_ai_parking.application.tariff.dto.TariffChangedEvent;
import com.tartis_recon_ai_parking.application.tariff.dto.TariffDTO;
import com.tartis_recon_ai_parking.application.tariff.factory.TariffDTOFactory;
import com.tartis_recon_ai_parking.application.tariff.port.output.TariffEventPublisher;
import com.tartis_recon_ai_parking.application.tariff.port.output.TariffPersistence;
import com.tartis_recon_ai_parking.domain.tariff.Tariff;
import com.tartis_recon_ai_parking.domain.tariff.exception.TariffNotFoundException;

import java.time.Instant;
import com.tartis_recon_ai_parking.domain.tariff.exception.TariffConstraintException;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeactivateTariffUseCase {

    private static final Logger log = LoggerFactory.getLogger(DeactivateTariffUseCase.class);

    private final TariffPersistence tariffPersistence;
    private final TariffEventPublisher eventPublisher;

    public DeactivateTariffUseCase(TariffPersistence tariffPersistence, TariffEventPublisher eventPublisher) {
        this.tariffPersistence = tariffPersistence;
        this.eventPublisher = eventPublisher;
    }

    public TariffDTO execute(UUID id) {
        Tariff existing = tariffPersistence.findById(id)
                .orElseThrow(() -> new TariffNotFoundException(id));

        // IN-17 Lifecycle protection: Block deactivation if it's the last active tariff
        if (existing.isActive()) {
            List<Tariff> activeTariffs = tariffPersistence.findActiveByTypeForUpdate(existing.getType());
            
            // Check if this is the only active one
            if (activeTariffs.size() == 1 && activeTariffs.get(0).getUniqueId().equals(existing.getUniqueId())) {
                throw new TariffConstraintException("Cannot deactivate the only active tariff for this vehicle type");
            }
        }

        Tariff deactivated = existing.deactivate();
        Tariff saved = tariffPersistence.save(deactivated);
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