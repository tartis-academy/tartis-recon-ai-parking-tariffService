package com.tartis_recon_ai_parking.infrastructure.tariff.adapter.input.rest.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class TariffUpdateRequest {

    @NotBlank
    private String name;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal pricePerMinute;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal basePrice;

    public TariffUpdateRequest() {
    }

    public TariffUpdateRequest(String name, BigDecimal pricePerMinute, BigDecimal basePrice) {
        this.name = name;
        this.pricePerMinute = pricePerMinute;
        this.basePrice = basePrice;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPricePerMinute() {
        return pricePerMinute;
    }

    public void setPricePerMinute(BigDecimal pricePerMinute) {
        this.pricePerMinute = pricePerMinute;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }
}