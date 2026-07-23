package com.tartis_recon_ai_parking.application.tariff.usecase;

import com.tartis_recon_ai_parking.application.tariff.dto.TariffDTO;
import com.tartis_recon_ai_parking.application.tariff.factory.TariffDTOFactory;
import com.tartis_recon_ai_parking.application.tariff.port.output.TariffPersistence;
import com.tartis_recon_ai_parking.domain.tariff.Tariff;
import com.tartis_recon_ai_parking.domain.tariff.exception.TariffNotFoundException;

import java.util.UUID;

public class DeactivateTariffUseCase {

    private final TariffPersistence tariffPersistence;

    public DeactivateTariffUseCase(TariffPersistence tariffPersistence) {
        this.tariffPersistence = tariffPersistence;
    }

    public TariffDTO execute(UUID id) {
        Tariff existing = tariffPersistence.findById(id)
                .orElseThrow(() -> new TariffNotFoundException(id));

        Tariff deactivated = existing.deactivate();
        Tariff saved = tariffPersistence.save(deactivated);
        return TariffDTOFactory.toDTO(saved);
    }
}