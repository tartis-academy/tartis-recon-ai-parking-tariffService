package com.tartis_recon_ai_parking.infrastructure.tariff.adapter.input.rest.dto.response;

import com.tartis_recon_ai_parking.domain.tariff.VehicleType;
import java.math.BigDecimal;
import java.util.UUID;

public class TariffResponse {

    private final UUID id;
    private final String name;
    private final VehicleType type;
    private final BigDecimal pricePerMinute;
    private final BigDecimal basePrice;
    private final boolean active;

    public TariffResponse(UUID id, String name, VehicleType type,
                           BigDecimal pricePerMinute, BigDecimal basePrice, boolean active) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.pricePerMinute = pricePerMinute;
        this.basePrice = basePrice;
        this.active = active;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public VehicleType getType() { return type; }
    public BigDecimal getPricePerMinute() { return pricePerMinute; }
    public BigDecimal getBasePrice() { return basePrice; }
    public boolean isActive() { return active; }
}