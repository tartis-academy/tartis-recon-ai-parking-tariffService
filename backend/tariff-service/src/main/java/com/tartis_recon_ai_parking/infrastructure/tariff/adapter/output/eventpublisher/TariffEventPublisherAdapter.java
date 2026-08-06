package com.tartis_recon_ai_parking.infrastructure.tariff.adapter.output.eventpublisher;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.tartis_recon_ai_parking.application.tariff.dto.TariffChangedEvent;
import com.tartis_recon_ai_parking.application.tariff.port.output.TariffEventPublisher;
import com.tartis_recon_ai_parking.infrastructure.config.RabbitMQConfig;

@Component
public class TariffEventPublisherAdapter implements TariffEventPublisher {
    private final ApplicationEventPublisher applicationEventPublisher;
    private final RabbitTemplate rabbitTemplate;

    public TariffEventPublisherAdapter(ApplicationEventPublisher applicationEventPublisher, RabbitTemplate rabbitTemplate) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publish(TariffChangedEvent event) {
        applicationEventPublisher.publishEvent(new SpringTariffChangedEvent(this, event));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSpringTariffChangedEvent(SpringTariffChangedEvent springEvent) {
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.EXCHANGE_NAME,
            RabbitMQConfig.ROUTING_KEY_TARIFF_CHANGED,
            springEvent.getEvent());
    }
}