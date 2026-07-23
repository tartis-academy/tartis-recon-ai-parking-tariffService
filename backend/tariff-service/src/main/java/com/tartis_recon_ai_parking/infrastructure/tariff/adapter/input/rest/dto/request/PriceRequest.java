package com.tartis_recon_ai_parking.infrastructure.tariff.adapter.input.rest.dto.request;


import com.tartis_recon_ai_parking.domain.tariff.VehicleType;

import jakarta.validation.constraints.NotNull;

public class PriceRequest {
    
    @NotNull
    VehicleType type;

    @NotNull
    int minutes;

    public PriceRequest() {
    }

    public PriceRequest(VehicleType type, int minutes) {
        this.type = type;
        this.minutes = minutes;
    }

    public VehicleType getVehicleType() { return type; }
    public void setName(VehicleType type) { this.type = type; }

    public int getMinutes() { return minutes; }
    public void setType(int minutes) { this.minutes = minutes; }

}
