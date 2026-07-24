package com.tartis_recon_ai_parking.infrastructure.tariff.adapter.input.rest.dto.request;


import com.tartis_recon_ai_parking.domain.tariff.VehicleType;

import jakarta.validation.constraints.NotNull;

public class TariffPriceRequest {

    @NotNull
    VehicleType type;

    @NotNull
    int minutes;

    public TariffPriceRequest() {
    }

    public TariffPriceRequest(VehicleType type, int minutes) {
        this.type = type;
        this.minutes = minutes;
    }

    public VehicleType getVehicleType() { return type; }
    public void setVehicleType(VehicleType type) { this.type = type; }

    public int getMinutes() { return minutes; }
    public void setMinutes(int minutes) { this.minutes = minutes; }

}
