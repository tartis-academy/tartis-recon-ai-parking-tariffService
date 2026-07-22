package com.tartis_recon_ai_parking.application.tariff.usecase;

import com.tartis_recon_ai_parking.application.tariff.dto.TariffCreateDTO;
import com.tartis_recon_ai_parking.application.tariff.dto.TariffDTO;
import com.tartis_recon_ai_parking.application.tariff.factory.TariffDTOFactory;
import com.tartis_recon_ai_parking.application.tariff.port.output.TariffPersistence;
import com.tartis_recon_ai_parking.domain.tariff.Tariff;

public class CreateTariffUseCase {

    private final TariffPersistence tariffPersistence;

    public CreateTariffUseCase(TariffPersistence tariffPersistence) {
        this.tariffPersistence = tariffPersistence;
    }

    public TariffDTO execute(TariffCreateDTO createDTO) {
        Tariff tariff = TariffDTOFactory.toDomain(createDTO);
        Tariff saved = tariffPersistence.save(tariff);
        return TariffDTOFactory.toDTO(saved);
    }
}