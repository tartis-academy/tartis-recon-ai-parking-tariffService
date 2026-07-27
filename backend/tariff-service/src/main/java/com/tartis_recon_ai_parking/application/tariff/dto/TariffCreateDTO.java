package com.tartis_recon_ai_parking.application.tariff.dto;

import com.tartis_recon_ai_parking.domain.tariff.VehicleType;
import java.math.BigDecimal;

public class TariffCreateDTO {

    private final String name;
    private final VehicleType type;
    private final BigDecimal pricePerMinute;
    private final BigDecimal basePrice;
    private final boolean active;

    public TariffCreateDTO(String name, VehicleType type, BigDecimal pricePerMinute,
                            BigDecimal basePrice, boolean active) {
        this.name = name;
        this.type = type;
        this.pricePerMinute = pricePerMinute;
        this.basePrice = basePrice;
        this.active = active;
    }

    public String getName() { return name; }
    public VehicleType getType() { return type; }
    public BigDecimal getPricePerMinute() { return pricePerMinute; }
    public BigDecimal getBasePrice() { return basePrice; }
    public boolean isActive() { return active; }
}