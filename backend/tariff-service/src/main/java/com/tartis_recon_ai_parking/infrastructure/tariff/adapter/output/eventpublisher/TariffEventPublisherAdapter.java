package com.tartis_recon_ai_parking.infrastructure.tariff.adapter.output.eventpublisher;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.tartis_recon_ai_parking.application.tariff.dto.TariffChangedEvent;
import com.tartis_recon_ai_parking.application.tariff.port.output.TariffEventPublisher;
import com.tartis_recon_ai_parking.infrastructure.config.RabbitMQConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class TariffEventPublisherAdapter implements TariffEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(TariffEventPublisherAdapter.class);
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
        TariffChangedEvent event = springEvent.getEvent();
        try {
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY_TARIFF_CHANGED,
                event);
        } catch (RuntimeException e) {
            log.error("No se pudo publicar TariffChangedEvent para {}", event.data().tariffId(), e);
        }
    }
}
