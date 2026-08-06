package com.tartis_recon_ai_parking.infrastructure.config.tx;

import org.springframework.transaction.annotation.Transactional;

import com.tartis_recon_ai_parking.application.tariff.dto.TariffCreateDTO;
import com.tartis_recon_ai_parking.application.tariff.dto.TariffDTO;
import com.tartis_recon_ai_parking.application.tariff.port.output.TariffEventPublisher;
import com.tartis_recon_ai_parking.application.tariff.port.output.TariffPersistence;
import com.tartis_recon_ai_parking.application.tariff.usecase.CreateTariffUseCase;

public class TransactionalCreateTariffUseCase extends CreateTariffUseCase {

    public TransactionalCreateTariffUseCase(TariffPersistence tariffPersistence, TariffEventPublisher eventPublisher) {
        super(tariffPersistence, eventPublisher);
    }

    @Override
    @Transactional
    public TariffDTO execute(TariffCreateDTO createDTO) {
        return super.execute(createDTO);
    }
}
