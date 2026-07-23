package com.tartis_recon_ai_parking.infrastructure.tariff.adapter.input.rest.dto.response;

import java.math.BigDecimal;

public class PriceResponse {
    
    private final BigDecimal price;

    public PriceResponse(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getPrice() { return price; }

}
