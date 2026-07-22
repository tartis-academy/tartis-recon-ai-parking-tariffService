package com.tartis_recon_ai_parking.application.tariff.usecase;

import com.tartis_recon_ai_parking.application.tariff.dto.TariffDTO;
import com.tartis_recon_ai_parking.application.tariff.factory.TariffDTOFactory;
import com.tartis_recon_ai_parking.application.tariff.port.output.TariffPersistence;
import com.tartis_recon_ai_parking.domain.tariff.VehicleType;

import java.util.List;

public class GetActiveTariffUseCase {

    private final TariffPersistence tariffPersistence;

    public GetActiveTariffUseCase(TariffPersistence tariffPersistence) {
        this.tariffPersistence = tariffPersistence;
    }

    public List<TariffDTO> execute(VehicleType type) {
        return tariffPersistence.findActiveByType(type).stream()
                .map(TariffDTOFactory::toDTO)
                .toList();
    }
}