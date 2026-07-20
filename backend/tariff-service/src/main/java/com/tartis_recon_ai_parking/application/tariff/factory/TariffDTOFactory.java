package com.tartis_recon_ai_parking.application.tariff.factory;

import com.tartis_recon_ai_parking.application.tariff.dto.TariffCreateDTO;
import com.tartis_recon_ai_parking.application.tariff.dto.TariffDTO;
import com.tartis_recon_ai_parking.domain.tariff.Tariff;

public final class TariffDTOFactory {
    private TariffDTOFactory() {
    }

    public static TariffDTO toDTO(Tariff tariff) {
        return new TariffDTO(
                tariff.getUniqueId(),
                tariff.getName(),
                tariff.getType(),
                tariff.getPricePerMinute(),
                tariff.getBasePrice(),
                tariff.isActive()
        );
    }

    public static Tariff toDomain(TariffCreateDTO tariffDTO) {
        return Tariff.create(
                tariffDTO.getName(),
                tariffDTO.getType(),
                tariffDTO.getPricePerMinute(),
                tariffDTO.getBasePrice(),
                tariffDTO.isActive()
        );
    }
}