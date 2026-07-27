package com.tartis_recon_ai_parking.application.tariff.dto;

import java.math.BigDecimal;

public class TariffUpdateDTO {

    private final String name;
    private final BigDecimal pricePerMinute;
    private final BigDecimal basePrice;

    public TariffUpdateDTO(String name, BigDecimal pricePerMinute, BigDecimal basePrice) {
        this.name = name;
        this.pricePerMinute = pricePerMinute;
        this.basePrice = basePrice;
    }

    public String getName() { return name; }
    public BigDecimal getPricePerMinute() { return pricePerMinute; }
    public BigDecimal getBasePrice() { return basePrice; }
}