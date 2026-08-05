package com.tartis_recon_ai_parking.infrastructure.tariff.adapter.output.eventpublisher;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.tartis_recon_ai_parking.application.tariff.dto.TariffChangedEvent;
import com.tartis_recon_ai_parking.application.tariff.port.output.TariffEventPublisher;
import com.tartis_recon_ai_parking.infrastructure.config.RabbitMQConfig;

@Component
public class TariffEventPublisherAdapter implements TariffEventPublisher {
    private final RabbitTemplate rabbitTemplate;

    public TariffEventPublisherAdapter(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publish(TariffChangedEvent event) {
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.EXCHANGE_NAME,
            RabbitMQConfig.ROUTING_KEY_TARIFF_CHANGED,
            event);
    }
}