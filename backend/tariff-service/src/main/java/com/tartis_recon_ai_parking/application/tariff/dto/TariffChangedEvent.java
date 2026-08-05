package com.tartis_recon_ai_parking.application.tariff.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.tartis_recon_ai_parking.domain.tariff.Tariff;
import com.tartis_recon_ai_parking.domain.tariff.VehicleType;

public record TariffChangedEvent(
    UUID eventId,
    String type,
    String version,
    Instant occurredAt,
    TariffChangedData data
) {
    public record TariffChangedData(
        UUID tariffId,
        String name,
        VehicleType vehicleType,
        BigDecimal pricePerMinute,
        BigDecimal basePrice,
        boolean active
    ) {}

    public static TariffChangedEvent of(Tariff tariff, Instant occurredAt) {
        return new TariffChangedEvent(
            UUID.randomUUID(), "TariffChangedEvent", "v1", occurredAt,
            new TariffChangedData(tariff.getUniqueId(), tariff.getName(), tariff.getType(),
                tariff.getPricePerMinute(), tariff.getBasePrice(), tariff.isActive())
        );
    }
}