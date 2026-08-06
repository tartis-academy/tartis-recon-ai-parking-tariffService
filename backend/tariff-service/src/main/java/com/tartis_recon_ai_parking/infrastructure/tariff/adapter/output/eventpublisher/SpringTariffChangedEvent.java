package com.tartis_recon_ai_parking.infrastructure.tariff.adapter.output.eventpublisher;

import org.springframework.context.ApplicationEvent;

import com.tartis_recon_ai_parking.application.tariff.dto.TariffChangedEvent;

public class SpringTariffChangedEvent extends ApplicationEvent {

    private final TariffChangedEvent event;

    public SpringTariffChangedEvent(Object source, TariffChangedEvent event) {
        super(source);
        this.event = event;
    }

    public TariffChangedEvent getEvent() {
        return event;
    }
}
