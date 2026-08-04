package com.tartis_recon_ai_parking.application.tariff.port.output;

import com.tartis_recon_ai_parking.application.tariff.dto.TariffChangedEvent;

public interface TariffEventPublisher {
    void publish(TariffChangedEvent event);
}