package com.tartis_recon_ai_parking.application.tariff.usecase;

import com.tartis_recon_ai_parking.application.tariff.dto.TariffDTO;
import com.tartis_recon_ai_parking.application.tariff.dto.TariffUpdateDTO;
import com.tartis_recon_ai_parking.application.tariff.factory.TariffDTOFactory;
import com.tartis_recon_ai_parking.application.tariff.port.output.TariffPersistence;
import com.tartis_recon_ai_parking.domain.tariff.Tariff;
import com.tartis_recon_ai_parking.domain.tariff.exception.TariffNotFoundException;

import java.util.UUID;

public class UpdateTariffUseCase {

    private final TariffPersistence tariffPersistence;

    public UpdateTariffUseCase(TariffPersistence tariffPersistence) {
        this.tariffPersistence = tariffPersistence;
    }

    public TariffDTO execute(UUID id, TariffUpdateDTO updateDTO) {
        Tariff existing = tariffPersistence.findById(id)
                .orElseThrow(() -> new TariffNotFoundException(id));

        Tariff updated = existing.update(
                updateDTO.getName(),
                updateDTO.getPricePerMinute(),
                updateDTO.getBasePrice()
        );

        Tariff saved = tariffPersistence.save(updated);
        return TariffDTOFactory.toDTO(saved);
    }
}