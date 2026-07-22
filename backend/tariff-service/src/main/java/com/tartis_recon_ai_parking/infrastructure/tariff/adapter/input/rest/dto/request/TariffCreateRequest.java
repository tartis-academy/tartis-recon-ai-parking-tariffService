package com.tartis_recon_ai_parking.infrastructure.tariff.adapter.input.rest.dto.request;

import com.tartis_recon_ai_parking.domain.tariff.VehicleType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class TariffCreateRequest {

    @NotBlank
    private String name;

    @NotNull
    private VehicleType type;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal pricePerMinute;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal basePrice;

    @NotNull
    private Boolean active;
    // + getter/setter

    public TariffCreateRequest() {
    }

    public TariffCreateRequest(String name, VehicleType type, BigDecimal pricePerMinute, BigDecimal basePrice, Boolean active) {
        this.name = name;
        this.type = type;
        this.pricePerMinute = pricePerMinute;
        this.basePrice = basePrice;
        this.active = active;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public VehicleType getType() { return type; }
    public void setType(VehicleType type) { this.type = type; }

    public BigDecimal getPricePerMinute() { return pricePerMinute; }
    public void setPricePerMinute(BigDecimal pricePerMinute) { this.pricePerMinute = pricePerMinute; }

    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}