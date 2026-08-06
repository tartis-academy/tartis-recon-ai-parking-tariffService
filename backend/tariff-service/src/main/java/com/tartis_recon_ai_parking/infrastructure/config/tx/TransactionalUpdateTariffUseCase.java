package com.tartis_recon_ai_parking.infrastructure.config.tx;

import com.tartis_recon_ai_parking.application.tariff.dto.TariffDTO;
import com.tartis_recon_ai_parking.application.tariff.dto.TariffUpdateDTO;
import com.tartis_recon_ai_parking.application.tariff.port.output.TariffEventPublisher;
import com.tartis_recon_ai_parking.application.tariff.port.output.TariffPersistence;
import com.tartis_recon_ai_parking.application.tariff.usecase.UpdateTariffUseCase;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class TransactionalUpdateTariffUseCase extends UpdateTariffUseCase {

    public TransactionalUpdateTariffUseCase(TariffPersistence tariffPersistence, TariffEventPublisher eventPublisher) {
        super(tariffPersistence, eventPublisher);
    }

    @Override
    @Transactional
    public TariffDTO execute(UUID id, TariffUpdateDTO updateDTO) {
        return super.execute(id, updateDTO);
    }
}
