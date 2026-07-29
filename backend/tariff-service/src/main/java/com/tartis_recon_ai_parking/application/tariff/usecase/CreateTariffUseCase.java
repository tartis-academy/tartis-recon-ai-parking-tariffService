package com.tartis_recon_ai_parking.application.tariff.usecase;

import com.tartis_recon_ai_parking.application.tariff.dto.TariffCreateDTO;
import com.tartis_recon_ai_parking.application.tariff.dto.TariffDTO;
import com.tartis_recon_ai_parking.application.tariff.factory.TariffDTOFactory;
import com.tartis_recon_ai_parking.application.tariff.port.output.TariffPersistence;
import com.tartis_recon_ai_parking.domain.tariff.Tariff;
import com.tartis_recon_ai_parking.domain.tariff.exception.TariffAlreadyExistsException;

public class CreateTariffUseCase {

    private final TariffPersistence tariffPersistence;

    public CreateTariffUseCase(TariffPersistence tariffPersistence) {
        this.tariffPersistence = tariffPersistence;
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
        Tariff saved = tariffPersistence.save(tariff);
        return TariffDTOFactory.toDTO(saved);
    }
}