package com.tartis_recon_ai_parking.application.tariff.usecase;

import com.tartis_recon_ai_parking.application.tariff.dto.TariffChangedEvent;
import com.tartis_recon_ai_parking.application.tariff.dto.TariffCreateDTO;
import com.tartis_recon_ai_parking.application.tariff.dto.TariffDTO;
import com.tartis_recon_ai_parking.application.tariff.factory.TariffDTOFactory;
import com.tartis_recon_ai_parking.application.tariff.port.output.TariffEventPublisher;
import com.tartis_recon_ai_parking.application.tariff.port.output.TariffPersistence;
import com.tartis_recon_ai_parking.domain.tariff.Tariff;
import com.tartis_recon_ai_parking.domain.tariff.exception.TariffAlreadyExistsException;

import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateTariffUseCase {

    private static final Logger log = LoggerFactory.getLogger(CreateTariffUseCase.class);

    private final TariffPersistence tariffPersistence;
    private final TariffEventPublisher eventPublisher;

    public CreateTariffUseCase(TariffPersistence tariffPersistence, TariffEventPublisher eventPublisher) {
        this.tariffPersistence = tariffPersistence;
        this.eventPublisher = eventPublisher;
    }

    public TariffDTO execute(TariffCreateDTO createDTO) {
        // Chequeo previo: falla rapido y con un mensaje claro, sin
        // castigar a la BD con un insert condenado. NO sustituye a la
        // constraint UNIQUE: entre este if y el save cabe una carrera,
        // y ahi la BD es el arbitro final (la traduce el adapter).
        if (tariffPersistence.existsByName(createDTO.getName())) {
            throw new TariffAlreadyExistsException(
                    "A tariff with that name already exists.");
        }

        Tariff tariff = TariffDTOFactory.toDomain(createDTO);

        // IN-17: un alta activa releva a la tarifa vigente del tipo. Mismo
        // motivo que en ActivateTariffUseCase para usar el update masivo en vez
        // de un bucle de save(). La tarifa aun no existe, asi que no hay nada
        // que excluir.
        if (tariff.isActive()) {
            List<Tariff> activeTariffs = tariffPersistence.findActiveByTypeForUpdate(tariff.getType());
            tariffPersistence.deactivateActiveByType(tariff.getType(), null);

            for (Tariff activeTariff : activeTariffs) {
                publishTariffChangedEventQuietly(activeTariff.deactivate());
            }
        }

        Tariff saved = tariffPersistence.save(tariff);
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