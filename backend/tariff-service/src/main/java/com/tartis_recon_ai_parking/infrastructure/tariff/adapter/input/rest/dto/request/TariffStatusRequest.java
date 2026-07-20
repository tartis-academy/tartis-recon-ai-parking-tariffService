// infrastructure/tariff/adapter/input/rest/dto/request/TariffStatusRequest.java
package com.tartis_recon_ai_parking.infrastructure.tariff.adapter.input.rest.dto.request;

import jakarta.validation.constraints.NotNull;

public class TariffStatusRequest {

    @NotNull
    private Boolean active;

    public TariffStatusRequest() {
    }

    public TariffStatusRequest(Boolean active) {
        this.active = active;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}