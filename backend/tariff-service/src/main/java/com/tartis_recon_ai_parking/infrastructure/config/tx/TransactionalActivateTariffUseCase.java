package com.tartis_recon_ai_parking.infrastructure.config.tx;

import java.util.UUID;

import org.springframework.transaction.annotation.Transactional;

import com.tartis_recon_ai_parking.application.tariff.dto.TariffDTO;
import com.tartis_recon_ai_parking.application.tariff.port.output.TariffEventPublisher;
import com.tartis_recon_ai_parking.application.tariff.port.output.TariffPersistence;
import com.tartis_recon_ai_parking.application.tariff.usecase.ActivateTariffUseCase;

public class TransactionalActivateTariffUseCase extends ActivateTariffUseCase {

    public TransactionalActivateTariffUseCase(TariffPersistence tariffPersistence, TariffEventPublisher eventPublisher) {
        super(tariffPersistence, eventPublisher);
    }

    @Override
    @Transactional
    public TariffDTO execute(UUID id) {
        return super.execute(id);
    }
}
