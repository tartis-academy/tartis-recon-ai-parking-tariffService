package com.tartis_recon_ai_parking.application.tariff.usecase;

import com.tartis_recon_ai_parking.application.tariff.dto.TariffDTO;
import com.tartis_recon_ai_parking.application.tariff.factory.TariffDTOFactory;
import com.tartis_recon_ai_parking.application.tariff.port.output.TariffPersistence;

import java.util.List;

public class GetAllTariffsUseCase {

    private final TariffPersistence tariffPersistence;

    public GetAllTariffsUseCase(TariffPersistence tariffPersistence) {
        this.tariffPersistence = tariffPersistence;
    }

    public List<TariffDTO> execute() {
        return tariffPersistence.findAll().stream()
                .map(TariffDTOFactory::toDTO)
                .toList();
    }
}