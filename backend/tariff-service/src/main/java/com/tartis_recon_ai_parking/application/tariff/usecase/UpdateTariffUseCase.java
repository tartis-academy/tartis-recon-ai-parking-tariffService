package com.tartis_recon_ai_parking.application.tariff.usecase;

import com.tartis_recon_ai_parking.application.tariff.dto.TariffChangedEvent;
import com.tartis_recon_ai_parking.application.tariff.dto.TariffDTO;
import com.tartis_recon_ai_parking.application.tariff.dto.TariffUpdateDTO;
import com.tartis_recon_ai_parking.application.tariff.factory.TariffDTOFactory;
import com.tartis_recon_ai_parking.application.tariff.port.output.TariffEventPublisher;
import com.tartis_recon_ai_parking.application.tariff.port.output.TariffPersistence;
import com.tartis_recon_ai_parking.domain.tariff.Tariff;
import com.tartis_recon_ai_parking.domain.tariff.exception.TariffAlreadyExistsException;
import com.tartis_recon_ai_parking.domain.tariff.exception.TariffNotFoundException;

import java.time.Instant;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateTariffUseCase {

    private static final Logger log = LoggerFactory.getLogger(UpdateTariffUseCase.class);

    private final TariffPersistence tariffPersistence;
    private final TariffEventPublisher eventPublisher;

    public UpdateTariffUseCase(TariffPersistence tariffPersistence, TariffEventPublisher eventPublisher) {
        this.tariffPersistence = tariffPersistence;
        this.eventPublisher = eventPublisher;
    }

    public TariffDTO execute(UUID id, TariffUpdateDTO updateDTO) {
        Tariff existing = tariffPersistence.findById(id)
                .orElseThrow(() -> new TariffNotFoundException(id));

        // Solo comprobamos si el nombre cambia de verdad. Sin este
        // guard, reenviar el mismo nombre daria un 409 falso.
        if (!existing.getName().equals(updateDTO.getName())
                && tariffPersistence.existsByName(updateDTO.getName())) {
            throw new TariffAlreadyExistsException(
                    "A tariff with that name already exists.");
        }

        Tariff updated = existing.update(
                updateDTO.getName(),
                updateDTO.getPricePerMinute(),
                updateDTO.getBasePrice()
        );

        Tariff saved = tariffPersistence.save(updated);
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